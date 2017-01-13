package com.tct.weather.internet;

import android.util.Log;

/**
 * Created by jiajun.shen on 8/28/15.
 */
/*
 ==========================================================================
 *HISTORY
 *
 *Tag            Date              Author        Description
 *============== ============ =============== ==============================
 *BUGFIX-1175818 2015/12/31       xing.zhao     [Weather]Weather APK  can't  auto match city by zip code
 *===========================================================================
 */
public class TWCUrlBuilder implements UrlBuilder {
    private static String TAG = "TWC weather UrlBuilder";
    private static final String APIKEY = "0efd9b4f14275d37789a2f57e5101852";

    public String currentWeatherUrl(String locationKey,String lantitude, String longitude, String lang) {
        lang = reformatLang(lang);
        StringBuffer sb = new StringBuffer();
        sb.append("http://api.weather.com/v1/");
        sb.append("geocode/").append(lantitude + "/").append(longitude + "/");
        sb.append("observations/current.json?apiKey=").append(APIKEY);
        sb.append("&language=").append(lang).append("&units=m");

        Log.d(TAG, "getCurrentWeather url: " + sb.toString());
        return sb.toString();
    }

    public String forcastHourlyWeatherUrl(String locationKey,String lantitude, String longitude, String lang) {
        lang = reformatLang(lang);
        StringBuffer sb = new StringBuffer();
        sb.append("http://api.weather.com/v1/");
        sb.append("geocode/").append(lantitude + "/").append(longitude + "/");
        sb.append("forecast/hourly/24hour.json?apiKey=").append(APIKEY);
        sb.append("&language=").append(lang).append("&units=m");

        Log.d(TAG, "getCurrentWeather url: " + sb.toString());
        return sb.toString();
    }

    public String forecastDailyWeatherUrl(String locationKey,String lantitude, String longitude, String lang) {
        lang = reformatLang(lang);
        StringBuffer sb = new StringBuffer();
        sb.append("http://api.weather.com/v1/");
        sb.append("geocode/").append(lantitude + "/").append(longitude + "/");
        sb.append("forecast/daily/5day.json?apiKey=").append(APIKEY);
        sb.append("&language=").append(lang).append("&units=m");
        Log.d(TAG, "getDailyForecastWeather url: " + sb.toString());
        return sb.toString();
    }

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/12/2016,1313172,[Weather]weather not translate to russian
    public String findCityByLocationKey(String locationKey, String lang, boolean withLang) {
        StringBuffer sb = new StringBuffer();
        sb.append("http://api.accuweather.com/locations/v1/");
        sb.append(locationKey);
        sb.append(".json?apikey=");
        sb.append(APIKEY);
        if (withLang) {
            sb.append("&language=").append(lang);
        }
        Log.d(TAG, "findCityByLocationKey url: " + sb.toString());
        return sb.toString();
    }
    //[BUGFIX]-Add-END by TSCD.qian-li

    public String findCityByGeoLocation(String geolocation, String lang, boolean withLang) {
        lang = reformatLang(lang);
        StringBuffer sb = new StringBuffer();
        sb.append("http://api.weather.com/v2/location?");
        sb.append("geocode=").append(geolocation);
        sb.append("&language=").append(lang).append("&format=").append("json")
                .append("&apiKey=").append(APIKEY);

        Log.d(TAG, "findCityNameByKeywords url: " + sb.toString());
        return sb.toString();
    }

    public String findCityByName(String name, String lang, boolean withLang) {
        lang = reformatLang(lang);
        /*StringBuffer sb = new StringBuffer();
        sb.append("http://api.accuweather.com/locations/v1/search?");
		sb.append("q=").append(name);
		if (withLang) {
			sb.append("&language=").append(lang);
		}
		sb.append("&apikey=").append(APIKEY);
		Log.d("jielong", "findCityByName url: " + sb.toString());
		return sendFindCityRequest(sb.toString(), ARRAY);*/
        StringBuffer sb = new StringBuffer();
        sb.append("http://api.weather.com/v2/location?");
        sb.append("address=").append(name);
        sb.append("&language=").append(lang).append("&format=").append("json")
                .append("&apiKey=").append(APIKEY);

        Log.d(TAG, "findCityNameByKeywords url: " + sb.toString());
        return sb.toString();
    }

    //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,12/31/2015,1175818,[Weather]Weather APK  can't  auto match city by zip code
    public String findCityByPostal(String postal, String lang, boolean withLang) {
        StringBuffer sb = new StringBuffer();
        sb.append("http://api.weather.com/v2/location?");
        sb.append("address=").append(postal);
        sb.append("&format=").append("json").append("&apiKey=").append(APIKEY);
        if (withLang) {
            sb.append("&language=").append(lang);
        }

        Log.d("jielong", "findCityByPostal url: " + sb.toString());
        return sb.toString();
    }
    //[BUGFIX]-Add-END by TSCD.xing.zhao

    public String reformatLang(String lang) {
        Log.e(TAG, "lang length=" + lang.length());
        if (lang.length() > 2) {
            String head = lang.substring(0, 2);
            String tail = lang.substring(3);
            Log.e(TAG, "head=" + head + " tail=" + tail);
            return head + "-" + tail.toUpperCase();
        } else {
            return lang;
        }
    }

}
