package com.tct.weather.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.tct.weather.service.MiniWeatherWidgetService;

public class MiniWeatherWidget extends AppWidgetProvider {
	
	private static final String TAG = "MiniWeatherWidget";
	
//	public static final String UPDATE_VIEW = "com.tct.weather.miniapp.update";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Intent intent = new Intent(context, MiniWeatherWidgetService.class);
//		intent.setAction(UPDATE_VIEW);
        intent.putExtra("appWidgetIds", appWidgetIds);
		context.startService(intent);
	}

	@Override
	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		super.onDisabled(context);
		context.stopService(new Intent(context, MiniWeatherWidgetService.class));
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		context.startService(new Intent(context, MiniWeatherWidgetService.class));
	}

}
