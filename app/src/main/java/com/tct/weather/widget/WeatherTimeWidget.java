package com.tct.weather.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tct.weather.service.WeatherTimeWidgetService;

public class WeatherTimeWidget extends AppWidgetProvider {

//	public static final String UPDATE_VIEW = "com.tct.widget.update";
    public final String TAG = "WeatherTimeWidget";
    public String WEATHER_UPDATE = "android.intent.action.WEATHER_BROADCAST";
    public String DELETE_UPDATE = "android.intent.action.DELETE_CITY";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onReceive(context, intent);
        Log.e(TAG, "onReceive  " + intent.toString());
        if((intent!= null)&&(intent.getAction().equals(WEATHER_UPDATE)
        /*MODIFIED-BEGIN by qian-li, 2016-04-06,BUG-1914252*/
        || intent.getAction().equals(DELETE_UPDATE))
                /*MODIFIED-BEGIN by xiangnan.zhou, 2016-04-08,BUG-1923725*/
                || intent.getAction().equals("android.intent.action.WALLPAPER_CHANGED")
                || intent.getAction().equals("android.intent.action.UNIT_BROADCAST")){
                /*MODIFIED-END by xiangnan.zhou,BUG-1923725*/
                /*MODIFIED-END by qian-li,BUG-1914252*/
            Intent mintent = new Intent(context, WeatherTimeWidgetService.class);
            context.startService(mintent);
        }

//        Intent selectLocationIntent = new Intent(context, MainActivity.class);
//        PendingIntent pSelectLocationIntent = PendingIntent.getActivity(context, 0, selectLocationIntent, 0);
//        views.setOnClickPendingIntent(R.id.widget_weather_layout, pSelectLocationIntent);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Intent intent = new Intent(context, WeatherTimeWidgetService.class);
        intent.putExtra("appWidgetIds", appWidgetIds);
        context.startService(intent);
    }


    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        /*MODIFIED-BEGIN by qian-li, 2016-04-13,BUG-1940875*/
        context.startService(new Intent(context, WeatherTimeWidgetService.class));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        context.stopService(new Intent(context, WeatherTimeWidgetService.class));
        /*MODIFIED-END by qian-li,BUG-1940875*/
    }

}
