package com.tct.weather.internet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;// MODIFIED by peng.du, 2016-03-22,BUG-1842312
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.util.Log;

import com.tct.weather.bean.City;

public class CityFindRequest {
    //private static final String APIKEY = "af7408e9f4d34fa6a411dd92028d4630";
    private static String TAG = "weather CityFindRequest";

    private static final int SINGLE = 0x01;
    private static final int ARRAY = 0x02;

//	public static List<City> findCityByLocationKey(String locationKey, String lang, boolean withLang) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("http://api.accuweather.com/locations/v1/");
//		sb.append(locationKey);
//		sb.append(".json?apikey=");
//		sb.append(APIKEY);
//		if (withLang) {
//			sb.append("&language=").append(lang);
//		}
//		Log.d("jielong", "findCityByLocationKey url: " + sb.toString());
//		return sendFindCityRequest(sb.toString(), SINGLE);
//	}

    public static List<City> findCityByGeoLocation(String geolocation, String lang, boolean withLang, boolean isTwcWeather) {
        UrlBuilder urlBuilder;
        int retryTime = 0;
        if (isTwcWeather) {
            urlBuilder=new TWCUrlBuilder();
            String string = urlBuilder.findCityByGeoLocation(geolocation, lang, withLang);
            Log.d(TAG, "findCityByGeoLocation url: " + string.toString());
            return sendFindCityRequest(string.toString(), SINGLE, true, retryTime);
        } else {
            urlBuilder=new ACCUUrlBuilder();
            String string = urlBuilder.findCityByGeoLocation(geolocation, lang, withLang);
            Log.d(TAG, "findCityByGeoLocation url: " + string.toString());
            return sendFindCityRequest(string.toString(), SINGLE, false, retryTime);
        }

    }

//	public static List<City> findCityByName(String name, String lang, boolean withLang) {
//		/*StringBuffer sb = new StringBuffer();
//		sb.append("http://api.accuweather.com/locations/v1/search?");
//		sb.append("q=").append(name);
//		if (withLang) {
//			sb.append("&language=").append(lang);
//		}
//		sb.append("&apikey=").append(APIKEY);
//		Log.d("jielong", "findCityByName url: " + sb.toString());
//		return sendFindCityRequest(sb.toString(), ARRAY);*/
//		StringBuffer sb = new StringBuffer();
//		sb.append("http://api.accuweather.com/locations/v1/cities/autocomplete.json?");
//		sb.append("q=").append(name);
//		sb.append("&apikey=").append(APIKEY);
//		if (withLang) {
//			sb.append("&language=").append(lang);
//		}
//		Log.d("jielong", "findCityNameByKeywords url: " + sb.toString());
//		return sendFindCityRequest(sb.toString(), ARRAY);
//	}

//	public static List<City> findCityByPostal(String postal, String lang, boolean withLang) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("http://api.accuweather.com/locations/v1/postalcodes/search.json?");
//		sb.append("q=").append(postal);
//		sb.append("&apikey=").append(APIKEY);
//		if (withLang) {
//			sb.append("&language=").append(lang);
//		}
//
//		Log.d("jielong", "findCityByPostal url: " + sb.toString());
//		return sendFindCityRequest(sb.toString(), ARRAY);
//	}

    private static List<City> sendFindCityRequest(String reqUrl, int type, boolean isTwcWeather, int retryTime) {
        if (retryTime >= 2) {
            return null;
        }
        retryTime++;
        List<City> cityList = new ArrayList<City>();
        ResponseUtil responseUtil;
        if (isTwcWeather) {
            responseUtil = new TWCResponseUtil();
        } else {
            responseUtil = new ACCUResponseUtil();
        }

//        try {
            BasicHttpParams httpParameters = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
            HttpConnectionParams.setSoTimeout(httpParameters, 20000);
            HttpClient httpClient = new DefaultHttpClient(httpParameters);

            HttpGet httpRequest = new HttpGet(reqUrl);
            httpRequest.addHeader("Accept-Encoding", "gzip");
        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,04/05/2016,1876718,[FT][France][Weather]The Weather APK can't locate current location automatically and can't sync weather by manual
//            httpRequest.addHeader("Content-Length", String.valueOf(580));
        //[BUGFIX]-Add-END by TSCD.qian-li

        try {
            HttpResponse response = httpClient.execute(httpRequest);
            int ret = response.getStatusLine().getStatusCode();
            Log.d(TAG, "sendFindCityRequest ret: " + ret);
            if (ret == 200) {
                //[BUGFIX]-Add-BEGIN by TSCD.peng.du,03/22/2016,1842312,[REG][Weather][V5.2.8.1.0328.0_0316]The weather can't get weather data during auto fixed position
                Header[] headers = response.getHeaders("Content-Encoding");
                String strEntity = null;
                if (headers != null && headers.length > 0 && headers[0].getValue().toLowerCase().equals("gzip")) {
                    InputStream is = new GZIPInputStream(response.getEntity().getContent());
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line = null;
                    StringBuffer sb = new StringBuffer();
                    while((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    strEntity = sb.toString();
                } else {
                    strEntity = EntityUtils.toString(response.getEntity());
                }
                //[BUGFIX]-Add-END by TSCD.peng.du

//                String strEntity = EntityUtils.toString(response.getEntity());
                Log.d(TAG, "sendFindCityRequest strEntity: " + strEntity);
                JSONObject obj = new JSONObject(strEntity);
                cityList = responseUtil.getCityList(obj);
            } else {
                return sendFindCityRequest(reqUrl, SINGLE, false, retryTime);
            }
        } catch (Exception e) {
            Log.e(TAG, "sendFindCityRequest() Exception :" + e.toString());
            return sendFindCityRequest(reqUrl, SINGLE, false, retryTime);
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().closeExpiredConnections();
                httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.SECONDS);
                httpClient.getConnectionManager().shutdown();
            }
        }
        return cityList;
    }

//	private static void parseJsonObject(JSONObject obj, List<City> cityList) throws Exception {
//		JSONObject countryObj = obj.getJSONObject("Country");
//		JSONObject adminObj = obj.getJSONObject("AdministrativeArea");
//		City city = new City();
//		city.setLocationKey(obj.getString("Key"));
//		city.setCityName(obj.getString("LocalizedName"));
//		city.setCountry(countryObj.getString("LocalizedName"));
//		city.setState(adminObj.getString("LocalizedName"));
//		city.setUpdateTime("");
//		cityList.add(city);
//	}
}
