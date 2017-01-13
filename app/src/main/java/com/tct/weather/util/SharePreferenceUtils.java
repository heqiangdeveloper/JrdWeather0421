package com.tct.weather.util;
/*
 ==========================================================================
 *HISTORY
 *
 *Tag          Date            Author      Description
 *============== ============ =============== ==============================
 *BUGFIX-1355924 2016/1/07       xing.zhao       [Stability][CRASH][Navigation]com.tct.weather:java.lang.NullPointerException
 *===========================================================================
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

public class SharePreferenceUtils {
    private Editor editor1 = null;
    private Editor editor2 = null;
    private Editor editor3 = null;
    private Editor editor4 = null;
    //    private Editor editor5 = null; //MODIFIED by xiangnan.zhou, 2016-04-07,BUG-1915763
    private static SharePreferenceUtils mInstance = null;

    private static final String SP_COMMON_DATA = "Common_data";
    private static final String SP_COMMON_CITY = "Common_city";
    private static final String SP_CITY_KEY = "City_key";
    private static final String SP_WEATHER_INFO = "weather_info";

    private SharePreferenceUtils() {
    }

    public static SharePreferenceUtils getInstance() {
        /*MODIFIED-BEGIN by xiangnan.zhou, 2016-04-07,BUG-1915763*/
        if (mInstance != null) {
            return mInstance;
        } else {
            mInstance = new SharePreferenceUtils();
            return mInstance;
        }
    }

    public void checkCommonCity(Context context, String locationKey) {
        SharedPreferences preferences = context.getSharedPreferences(SP_COMMON_CITY, Context.MODE_PRIVATE);
        String commonCityLocationKey = preferences.getString("common_city", "");
        if (TextUtils.isEmpty(commonCityLocationKey)) {
            editor1 = preferences.edit();
            //[BUGFIX]-Mod-BEGIN by xinlei.sheng,2015/12/23,1125508
            if (editor1 != null) {
                editor1.putString("common_city", locationKey);
                editor1.commit();
//              editor1 = null;
            }
            //[BUGFIX]-Mod-END by xinlei.sheng,2015/12/23,1125508
        }
    }

    public void saveLong(Context context, String key, long value) {
        if (null == editor1) {
            SharedPreferences preferences = context.getSharedPreferences(SP_COMMON_CITY, Context.MODE_PRIVATE);
            editor1 = preferences.edit();
        }
        //[BUGFIX]-Mod-BEGIN by xinlei.sheng,2015/12/23,1125508
        editor1.putLong(key, value);
        editor1.commit();
        //[BUGFIX]-Mod-END by xinlei.sheng,2015/12/23,1125508
    }

    public int getInt(Context context, String key, int defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(SP_COMMON_DATA, Context.MODE_PRIVATE);
        return preferences.getInt(key, defaultValue);
    }

    public void saveInt(Context context, String key, int value) {
        if (null == editor2) {
            SharedPreferences preferences = context.getSharedPreferences(SP_COMMON_DATA, Context.MODE_PRIVATE);
            editor2 = preferences.edit();
        }
        //[BUGFIX]-Mod-BEGIN by xinlei.sheng,2015/12/23,1125508
        editor2.putInt(key, value);
        editor2.commit();
        //[BUGFIX]-Mod-END by xinlei.sheng,2015/12/23,1125508
    }

    public long getLong(Context context, String key, long defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(SP_COMMON_CITY, Context.MODE_PRIVATE);
        return preferences.getLong(key, defaultValue);
    }

    //Defect 212555 Outdoor auto location is failed by bing.wang.hz begin
    public void saveBoolean(Context context, String key, boolean value) {
        if (null == editor2) {
            SharedPreferences preferences = context.getSharedPreferences(SP_COMMON_DATA, Context.MODE_PRIVATE);
            editor2 = preferences.edit();
        }
        //[BUGFIX]-Mod-BEGIN by xinlei.sheng,2015/12/23,1125508
        editor2.putBoolean(key, value);
        editor2.commit();
        //[BUGFIX]-Mod-END by xinlei.sheng,2015/12/23,1125508
    }

    public boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(SP_COMMON_DATA, Context.MODE_PRIVATE);
        return preferences.getBoolean(key, defaultValue);
    }
    //Defect 212555 Outdoor auto location is failed by bing.wang.hz end

    public void saveCurrentCityKey(Context context, String cityKey) {
        if (null == editor3) {
            SharedPreferences preferences = context.getSharedPreferences(SP_CITY_KEY, Context.MODE_PRIVATE);
            editor3 = preferences.edit();
        }
        //[BUGFIX]-Mod-BEGIN by xinlei.sheng,2015/12/23,1125508
        editor3.putString("current_city_key", cityKey);
        editor3.commit();
        //[BUGFIX]-Mod-END by xinlei.sheng,2015/12/23,1125508
    }

    public String getCurrentCityKey(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SP_CITY_KEY, Context.MODE_PRIVATE);
        return preferences.getString("current_city_key", null);
    }

    // added for launcher dynamical icon at 2015-5-20 begin
    public void saveCityWeatherInfo(Context context, String locationKey, String temperature, String icon) {
        if (null == editor4) {
            SharedPreferences pref = context.getSharedPreferences(SP_WEATHER_INFO, Context.MODE_WORLD_READABLE);
            editor4 = pref.edit();// modified by jiajun for 1032855
        }
        //[BUGFIX]-Mod-BEGIN by xinlei.sheng,2015/12/23,1125508
        editor4.putString("locationKey", locationKey);
        editor4.putString("temperature", temperature);
        editor4.putString("icon", icon);
        editor4.commit();
        //[BUGFIX]-Add-BEGIN by TSCD.xiangnan.zhou,04/07/2016,1915763,[jrdlogger]com.tct.weather Java (JE)
//            editor4 = null;
        //[BUGFIX]-Add-END by TSCD.xiangnan.zhou
        //[BUGFIX]-Mod-END by xinlei.sheng,2015/12/23,1125508
    }

    public void clearCityWeatherInfo(Context context) {
        if (null == editor4) {
            SharedPreferences pref = context.getSharedPreferences(SP_WEATHER_INFO, Context.MODE_WORLD_READABLE);
            editor4 = pref.edit();//modified by jiajun for 1032855
        }
        //[BUGFIX]-Mod-BEGIN by xinlei.sheng,2015/12/23,1125508
        editor4.clear();
        editor4.commit();
        //[BUGFIX]-Add-BEGIN by TSCD.xiangnan.zhou,04/07/2016,1915763,[jrdlogger]com.tct.weather Java (JE)
//            editor4 = null;
        //[BUGFIX]-Add-END by TSCD.xiangnan.zhou
        /*MODIFIED-END by xiangnan.zhou,BUG-1915763*/
        //[BUGFIX]-Mod-END by xinlei.sheng,2015/12/23,1125508
    }
    // added for launcher dynamical icon at 2015-5-20 end


    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/20/2016,1313172,[Weather]weather not translate to russian
    public void saveString(Context context, String key, String val) {
        /*MODIFIED-BEGIN by xiangnan.zhou, 2016-04-07,BUG-1915763*/
        if (null == editor2) {
            SharedPreferences preferences = context.getSharedPreferences(SP_COMMON_DATA, Context.MODE_PRIVATE);
            editor2 = preferences.edit();
        }
        editor2.putString(key, val);
        editor2.commit();
        /*MODIFIED-END by xiangnan.zhou,BUG-1915763*/
    }

    public String getString(Context context, String key, String defaultVal) {
        SharedPreferences preferences = context.getSharedPreferences(SP_COMMON_DATA, Context.MODE_PRIVATE);
        return preferences.getString(key, defaultVal);
    }
    //[BUGFIX]-Add-END by TSCD.qian-li
}
