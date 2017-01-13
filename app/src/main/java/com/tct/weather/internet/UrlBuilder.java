package com.tct.weather.internet;

/**
 * Created by jiajun.shen on 11/12/15.
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
public interface UrlBuilder {
    abstract public String currentWeatherUrl(String locationKey, String lantitude, String longitude, String lang);

    abstract public String forcastHourlyWeatherUrl(String locationKey, String lantitude, String longitude, String lang);

    abstract public String forecastDailyWeatherUrl(String locationKey, String lantitude, String longitude, String lang);

    abstract public String findCityByGeoLocation(String geolocation, String lang, boolean withLang);

    abstract public String findCityByName(String name, String lang, boolean withLang);

    //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,12/31/2015,1175818,[Weather]Weather APK  can't  auto match city by zip code
    abstract public String findCityByPostal(String name, String lang, boolean withLang);
    //[BUGFIX]-Add-END by TSCD.xing.zhao

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/12/2016,1313172,[Weather]weather not translate to russian
    abstract public String findCityByLocationKey(String locationKey, String lang, boolean withLang);
    //[BUGFIX]-Add-END by TSCD.qian-li
}
