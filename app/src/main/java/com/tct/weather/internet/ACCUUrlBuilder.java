package com.tct.weather.internet;

import android.util.Log;

/**
 * Created by jiajun.shen on 11/12/15.
 */
public class ACCUUrlBuilder implements UrlBuilder{
    private static String TAG="weather UrlBuilder";
    private static final String APIKEY = "af7408e9f4d34fa6a411dd92028d4630";

     public String currentWeatherUrl(String locationKey,String lantitude, String longitude, String lang) {
        StringBuffer sb = new StringBuffer();
        sb.append("http://api.accuweather.com/currentconditions/v1/");
        sb.append(locationKey).append(".json?");
        sb.append("apikey=").append(APIKEY);
        sb.append("&details=true");
        sb.append("&language=").append(lang);

        Log.d(TAG, "getCurrentWeather url: " + sb.toString());
        return sb.toString();
    }

     public String forecastDailyWeatherUrl(String locationKey, String lantitude, String longitude,String lang)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("http://api.accuweather.com/forecasts/v1/daily/5day/");
        sb.append(locationKey).append(".json?");
        sb.append("apikey=").append(APIKEY);
        sb.append("&language=").append(lang);
        sb.append("&metric=true");
        Log.d(TAG, "getDailyForecastWeather url: " + sb.toString());
        return sb.toString();
    }

    public String findCityByLocationKey(String locationKey,String lang, boolean withLang) {
        StringBuffer sb = new StringBuffer();
        sb.append("http://api.accuweather.com/locations/v1/");
        sb.append(locationKey);
        sb.append(".json?apikey=");
        sb.append(APIKEY);
        if (withLang) {
            sb.append("&language=").append(lang);
        }
        Log.d("jielong", "findCityByLocationKey url: " + sb.toString());
        return sb.toString();
    }

    public  String findCityByGeoLocation(String geolocation, String lang, boolean withLang) {
        StringBuffer sb = new StringBuffer();
        sb.append("http://api.accuweather.com/locations/v1/cities/geoposition/search.json?");
        sb.append("q=").append(geolocation);
        if (withLang) {
            sb.append("&language=").append(lang);
        }
        sb.append("&apikey=").append(APIKEY);
        Log.d(TAG, "findCityByGeoLocation url: " + sb.toString());
        return sb.toString();
    }

    public  String findCityByName(String name, String lang, boolean withLang) {
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
        sb.append("http://api.accuweather.com/locations/v1/cities/autocomplete.json?");
        sb.append("q=").append(name);
        sb.append("&apikey=").append(APIKEY);
        if (withLang) {
            sb.append("&language=").append(lang);
        }
        Log.d("jielong", "findCityNameByKeywords url: " + sb.toString());
        return sb.toString();
    }

    public  String findCityByPostal(String postal, String lang, boolean withLang) {
        StringBuffer sb = new StringBuffer();
        sb.append("http://api.accuweather.com/locations/v1/postalcodes/search.json?");
        sb.append("q=").append(postal);
        sb.append("&apikey=").append(APIKEY);
        if (withLang) {
            sb.append("&language=").append(lang);
        }

        Log.d("jielong", "findCityByPostal url: " + sb.toString());
        return sb.toString();
    }

    @Override
    public String forcastHourlyWeatherUrl(String locationKey, String lantitude, String longitude,String lang) {
        StringBuffer sb = new StringBuffer();
        sb.append("http://api.accuweather.com/forecasts/v1/hourly/24hour/");
        sb.append(locationKey).append(".json?");
        sb.append("apikey=").append(APIKEY);
        sb.append("&details=true");
        sb.append("&metric=true");
        sb.append("&language=").append(lang);

        Log.d(TAG, "getCurrentWeather url: " + sb.toString());
        return sb.toString();
    }

}
