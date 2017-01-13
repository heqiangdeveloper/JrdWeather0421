package com.tct.weather.internet;

import org.apache.http.Header;// MODIFIED by peng.du, 2016-03-22,BUG-1842312
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.tct.weather.bean.Currentconditions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class CurrentWeatherRequest {
    private static final String APIKEY = "af7408e9f4d34fa6a411dd92028d4630";
    private static String TAG = "weather CurrentWeatherRequest";

    public static Currentconditions getCurrentWeather(String locationKey, String latitude, String longitude, String lang, boolean isTwcWeather) {
//        ResponseUtil responseUtil;
        UrlBuilder urlBuilder;
        if (isTwcWeather) {
//            responseUtil = new TWCResponseUtil();
            urlBuilder = new TWCUrlBuilder();
        } else {
//            responseUtil = new ACCUResponseUtil();
            urlBuilder = new ACCUUrlBuilder();
        }
        String sb = urlBuilder.currentWeatherUrl(locationKey, latitude, longitude, lang);
        Log.i(TAG, "getCurrentWeather url: " + sb.toString());

        return sendCurrentconditionsRequest(sb, isTwcWeather, 0);

//        Currentconditions current = new Currentconditions();
////        try {
//            BasicHttpParams httpParameters = new BasicHttpParams();
//
//            HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
//            HttpConnectionParams.setSoTimeout(httpParameters, 20000);
//            HttpClient httpClient = new DefaultHttpClient(httpParameters);
//
//            HttpGet httpRequest = new HttpGet(sb.toString());
//            httpRequest.addHeader("Accept-Encoding", "gzip");
//            httpRequest.addHeader("Content-Length", String.valueOf(580));
//
//        try {
//            HttpResponse response = httpClient.execute(httpRequest);
//            int ret = response.getStatusLine().getStatusCode();
//            Log.d(TAG, "getCurrentWeather ret: " + ret);
//            if (ret == 200) {
//                InputStream is = new GZIPInputStream(response.getEntity().getContent());;
//                BufferedReader br = new BufferedReader(new InputStreamReader(is));
//                String line = null;
//                StringBuffer sb1 = new StringBuffer();
//                while((line = br.readLine()) != null) {
//                    sb1.append(line);
//                }
//                String strEntity = sb1.toString();
////                String strEntity = EntityUtils.toString(response.getEntity());
//                Log.d(TAG, "getCurrentWeather strEntity: " + strEntity);
//                if (!TextUtils.isEmpty(strEntity)) {
//                    if (isTwcWeather) {
//                        JSONObject jsonObject = new JSONObject(strEntity);
//                        current = responseUtil.getCurrentWeather(jsonObject);
//                    } else {
//                        JSONArray jsonArray = new JSONArray(strEntity);
//                        current = responseUtil.getCurrentWeather(jsonArray);
//                    }
//
//                    Log.i(TAG, "currentCondition=" + current.toString());
//                } else {
//                    return null;
//                }
//            }
//            // Fixed PR965957 by jielong.xing at 2015-4-2 begin
//            else {
//                return null;
//            }
//            // Fixed PR965957 by jielong.xing at 2015-4-2 end
//        } catch (Exception e) {
//            Log.e(TAG, "getCurrentWeather Exception == " + e.toString());
//            return null;
//        } finally {
//            if (httpClient != null) {
//                httpClient.getConnectionManager().closeExpiredConnections();
//                httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.SECONDS);
//                httpClient.getConnectionManager().shutdown();
//            }
//        }
//
//        return current;
    }

    private static Currentconditions sendCurrentconditionsRequest(String reqUrl, boolean isTwcWeather, int retryTime) {
        if (retryTime >= 2) {
            return null;
        }
        retryTime++;
        ResponseUtil responseUtil;
        if (isTwcWeather) {
            responseUtil = new TWCResponseUtil();
        } else {
            responseUtil = new ACCUResponseUtil();
        }
        Currentconditions current = new Currentconditions();
//        try {
        BasicHttpParams httpParameters = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
        HttpConnectionParams.setSoTimeout(httpParameters, 20000);
        HttpClient httpClient = new DefaultHttpClient(httpParameters);

        HttpGet httpRequest = new HttpGet(reqUrl.toString());
        httpRequest.addHeader("Accept-Encoding", "gzip");
        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,04/05/2016,1876718,[FT][France][Weather]The Weather APK can't locate current location automatically and can't sync weather by manual
//        httpRequest.addHeader("Content-Length", String.valueOf(580));
        //[BUGFIX]-Add-END by TSCD.qian-li

        try {
            HttpResponse response = httpClient.execute(httpRequest);
            int ret = response.getStatusLine().getStatusCode();
            Log.d(TAG, "getCurrentWeather ret: " + ret);
            if (ret == 200) {
                //[BUGFIX]-Add-BEGIN by TSCD.peng.du,03/22/2016,1842312,[REG][Weather][V5.2.8.1.0328.0_0316]The weather can't get weather data during auto fixed position
                Header[] headers = response.getHeaders("Content-Encoding");
                String strEntity = null;
                if (headers != null && headers.length > 0 && headers[0].getValue().toLowerCase().equals("gzip")) {
                    InputStream is = new GZIPInputStream(response.getEntity().getContent());;
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line = null;
                    StringBuffer sb1 = new StringBuffer();
                    while((line = br.readLine()) != null) {
                        sb1.append(line);
                    }
                    strEntity = sb1.toString();
                } else {
                    strEntity = EntityUtils.toString(response.getEntity());
                }
                //[BUGFIX]-Add-END by TSCD.peng.du

//                String strEntity = EntityUtils.toString(response.getEntity());
                Log.d(TAG, "getCurrentWeather strEntity: " + strEntity);
                if (!TextUtils.isEmpty(strEntity)) {
                    if (isTwcWeather) {
                        JSONObject jsonObject = new JSONObject(strEntity);
                        current = responseUtil.getCurrentWeather(jsonObject);
                    } else {
                        JSONArray jsonArray = new JSONArray(strEntity);
                        current = responseUtil.getCurrentWeather(jsonArray);
                    }

                    Log.i(TAG, "currentCondition=" + current.toString());
                } else {
                    return sendCurrentconditionsRequest(reqUrl, isTwcWeather, retryTime);
                }
            }
            // Fixed PR965957 by jielong.xing at 2015-4-2 begin
            else {
                return sendCurrentconditionsRequest(reqUrl, isTwcWeather, retryTime);
            }
            // Fixed PR965957 by jielong.xing at 2015-4-2 end
        } catch (Exception e) {
            Log.e(TAG, "getCurrentWeather Exception == " + e.toString());
            return sendCurrentconditionsRequest(reqUrl, isTwcWeather, retryTime);
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().closeExpiredConnections();
                httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.SECONDS);
                httpClient.getConnectionManager().shutdown();
            }
        }

        return current;
    }

}
