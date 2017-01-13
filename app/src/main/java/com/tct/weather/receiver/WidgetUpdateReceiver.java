/**************************************************************************************************/
/*                                                                     Date : 08/2014 */
/*                            PRESENTATION                                            */
/*              Copyright (c) 2014 JRD Communications, Inc.                           */
/**************************************************************************************************/
/*                                                                                                */
/*    This material is company confidential, cannot be reproduced in any              */
/*    form without the written permission of JRD Communications, Inc.                 */
/*                                                                                                */
/*================================================================================================*/
/*   Author :  jielong.xing                                                            */
/*   Role :   JrdWeather                                                              */
/*================================================================================================*/
/* Comments :                                                                         */
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/receiver/WidgetUpdateReceiver.java     */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.tct.weather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tct.weather.service.MiniWeatherWidgetService;

public class WidgetUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		//modified for PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 
		if ("android.intent.action.NEXT_CITY_MINIWIDGET_UPDATE".equals(action)) {
			Intent serviceIntent = new Intent(context, MiniWeatherWidgetService.class);
			//modified for PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 
			//serviceIntent.setAction("android.intent.action.NEXT_CITY_WIDGET_UPDATE");
			serviceIntent.setAction("android.intent.action.NEXT_CITY_MINIWIDGET_UPDATE");
			context.startService(serviceIntent);
			
		}
		else if ("android.intent.action.REFRESH_WIDGET_VIEW".equals(action)) {
			Intent serviceIntent = new Intent(context, MiniWeatherWidgetService.class);
			serviceIntent.setAction("android.intent.action.REFRESH_WIDGET_VIEW");
			context.startService(serviceIntent);
		} 
		// added by jielong.xing at 2015-3-23 begin
		else if ("android.intent.action.WEATHER_BROADCAST".equals(action)) {
			Boolean isDataReady = intent.getBooleanExtra("weather", false);
			Intent miniAppIntent = new Intent(context, MiniWeatherWidgetService.class);
			miniAppIntent.setAction("android.intent.action.WEATHER_BROADCAST");
			miniAppIntent.putExtra("weather", isDataReady);
			context.startService(miniAppIntent);
		}
		// added by jielong.xing at 2015-3-23 end
	}

}
