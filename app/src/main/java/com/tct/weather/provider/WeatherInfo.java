/**************************************************************************************************/
/*                                                                     Date : 10/2012 */
/*                            PRESENTATION                                            */
/*              Copyright (c) 2012 JRD Communications, Inc.                           */
/**************************************************************************************************/
/*                                                                                                */
/*    This material is company confidential, cannot be reproduced in any              */
/*    form without the written permission of JRD Communications, Inc.                 */
/*                                                                                                */
/*================================================================================================*/
/*   Author :  Feng zhuang                                                            */
/*   Role :   JrdWeather                                                              */
/*================================================================================================*/
/* Comments :                                                                         */
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/provider/WeatherInfo.java     */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.tct.weather.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class WeatherInfo {
    
    public static final String AUTHORITY = "com.tct.provider.weatherinfo";

    public static final class UnitInfo implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/unitinfo");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tct.provider.unitinfo";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.tct.provider.unitinfo";

        public static final String ISUNITC = "isUnitC";
    }
    
    public static final class Current implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/current");
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tct.provider.weather";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.tct.provider.weather";
        
        public static final String LOCATION_KEY = "locationKey";
        public static final String ICON = "icon";
        public static final String WEATHER_DESCRIPTION = "text";
        public static final String CURRENT_TEMPERATURE = "temp";
        public static final String REALFEEL = "realfeel";
        public static final String LOW_TEMPERATURE = "tempLow";
        public static final String HIGH_TEMPERATURE="tempHigh";
        public static final String PRECIPTATION="preciptation";
        public static final String UV_INDEX="uv_index";
        public static final String WIND_SPEED="wind";
        public static final String PRESSURE="pressure";
        public static final String VISIBILITY="visibility";
        public static final String HUMIDITY="humidity";
        public static final String WINDDIR="winddir";//[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398,
        //[FEATURE]-Add-BEGIN by TSCD.qian-li,01/22/2016,1490227,[Launcher][Ergo v5.2.6]Boom Key
        public static final String ISDAYTIME="isdaytime";
        //[FEATURE]-Add-END by TSCD.qian-li
    }
    
    public static final class CityInfo implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/city");
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tct.provider.city";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.tct.provider.city";
        
        public static final String LOCATION_KEY = "locationKey";
        public static final String CITY_NAME = "cityName";
        public static final String UPDATE_TIME = "updateTime";
        public static final String STATE_NAME = "state";
        public static final String ISAUTOLOCATE = "isautolocate";//[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398,
        
    }

    public static final class DailyInfo implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/daily");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tct.provider.daily";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.tct.provider.daily";

        public static final String LOCATION_KEY = "locationKey";
        public static final String ICON = "icon";
        public static final String WEEK = "week";
        public static final String DATE = "date";
        public static final String WEATHER_DESCRIPTION = "phrase";
        public static final String LOW_TEMPERATURE = "low";
        public static final String HIGH_TEMPERATURE="high";
        //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398,
        public static final String WINDDIR = "winddir";
        public static final String WIND = "wind";
        //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398,
    }
       
}
