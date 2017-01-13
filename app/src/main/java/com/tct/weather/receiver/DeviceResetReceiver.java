/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.weather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tct.weather.util.CustomizeUtils;

/**
 * [BUGFIX]-Add by TSCD.qian-li,14/4/2015 1937618
 * [Device reset][Weather]Some options can't reset to default value after device reset.
 */
public class DeviceResetReceiver extends BroadcastReceiver{

    /*MODIFIED-BEGIN by xiangnan.zhou, 2016-04-18,BUG-1937618*/
    private static final String isUnitC = "unit";
    private static final String isUnitKm = "unitKm";
    private static final String KEY_TEMP = "settings_temp";
    private static final String KEY_DISTANCE = "settings_distance";
    private static final String unitC = "1";
    private static final String unitF = "0";
    private static final String unitKm = "1";
    private static final String unitMi = "0";
    /*MODIFIED-END by xiangnan.zhou,BUG-1937618*/


   @Override
    public void onReceive(Context context, Intent intent) {
       Log.i("DeviceResetReceiver","onreceive");
       String action = null;
       if (null != intent) {
           action = intent.getAction();
           Log.i("DeviceResetReceiver","onreceive action="+action);
       }
       if (null != action && "android.intent.action.LAUNCH_DEVICE_RESET".equals(action)) {
           Intent it = new Intent("android.intent.action.UNIT_BROADCAST");
           SharedPreferences.Editor sharedata = context.getSharedPreferences("weather", Context.MODE_WORLD_READABLE).edit();
           String unit = CustomizeUtils.getString(context, "def_weather_unit_name");
           unit = CustomizeUtils.splitQuotationMarks(unit);

           SharedPreferences.Editor defaultEditor=PreferenceManager.getDefaultSharedPreferences(context).edit();
           defaultEditor.putString("settings_auto_update","0");

           if (unit != null && "isUnitC".equals(unit)) {
               /*MODIFIED-BEGIN by xiangnan.zhou, 2016-04-18,BUG-1937618*/
               sharedata.putBoolean(isUnitC, true);
               it.putExtra("isUnitC", true);

               /*MODIFIED-BEGIN by xiangnan.zhou, 2016-04-18,BUG-1937618*/
               defaultEditor.putString(KEY_TEMP, unitC);
           } else {
               sharedata.putBoolean(isUnitC, false);
               it.putExtra("isUnitC", false);

               defaultEditor.putString(KEY_TEMP, unitF);
           }
           String unitDistance = CustomizeUtils.getString(context, "def_weather_wind_visibility_unit_name");
           unitDistance = CustomizeUtils.splitQuotationMarks(unitDistance);
           if (unitDistance != null && "km".equals(unitDistance.toLowerCase())) {
               sharedata.putBoolean(isUnitKm, true);

               defaultEditor.putString(KEY_DISTANCE, unitKm);
           } else {
               sharedata.putBoolean(isUnitKm, false);

               defaultEditor.putString(KEY_DISTANCE, unitMi);
               /*MODIFIED-END by xiangnan.zhou,BUG-1937618*/
               /*MODIFIED-END by xiangnan.zhou,BUG-1937618*/
           }
           sharedata.commit();
           defaultEditor.commit();
           context.sendBroadcast(it);
        }
    }
}
