/**************************************************************************************************/
/*                                                                     Date : 04/2013 */
/*                            PRESENTATION                                            */
/*              Copyright (c) 2012 JRD Communications, Inc.                           */
/**************************************************************************************************/
/*                                                                                                */
/*    This material is company confidential, cannot be reproduced in any              */
/*    form without the written permission of JRD Communications, Inc.                 */
/*                                                                                                */
/*================================================================================================*/
/*   Author :  Chen Ting                                                            */
/*   Role :   JrdWeather                                                              */
/*================================================================================================*/
/* Comments :                                                                         */
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/autolocate/AutoLocateBroadCastReceiver.java */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.tct.weather.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tct.weather.service.UpdateService;
import com.tct.weather.service.WeatherTimeWidgetService; //MODIFIED by qian-li, 2016-04-13,BUG-1940875
import com.tct.weather.util.CommonUtils;
import com.tct.weather.service.MiniWeatherWidgetService;

public class AutoLocateBroadCastReceiver extends BroadcastReceiver{

	private Context mContext = null;
	String TAG="weather AutoLocateBroadCastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		String action = null;
		if (intent != null) {
			action = intent.getAction();
		}
		if (null != action && "android.intent.action.BOOT_COMPLETED".equals(action)) {
//			cancelTimerTask(context);
//			invokeTimerTask(context);
			Intent  updateServiceIntent =  new Intent(context,UpdateService.class);
			updateServiceIntent.putExtra("boot_complete", true);
			context.startService(updateServiceIntent);
		}
		// added by jielong.xing at 2015-3-23 begin
		else if (null != action && "com.tct.weather.SERVICE_DESTROY".equals(action)) {
			context.startService(new Intent(context,UpdateService.class));
			context.startService(new Intent(context,WeatherTimeWidgetService.class)); //MODIFIED by qian-li, 2016-04-13,BUG-1940875
			context.startService(new Intent(context,MiniWeatherWidgetService.class));
		}
		// added by jielong.xing at 2015-3-23 end
		else if (null != action && CommonUtils.LOCATION_TIMER_TASK_ACTION.equals(action)) {
			Log.i(TAG, "CommonUtils.LOCATION_TIMER_TASK_ACTION");
			context.startService(new Intent(context, UpdateService.class).setAction("com.tct.checkUpdate"));
		}

		//[FEATURE]-Add-BEGIN by TSCD.qian-li,01/27/2016,1532388,[Weather]MIE call weather interface to locate automaticly
		else if (null != action && CommonUtils.AUTO_LOCATION_TASK_ACTION.equals(action)) {
			Log.i(TAG, "CommonUtils.AUTO_LOCATION_TASK_ACTION");
			context.startService(new Intent(context, UpdateService.class).setAction("com.tct.start_location"));
		}
		//[FEATURE]-Add-END by TSCD.qian-li

		else {
			context.startService(new Intent(context,UpdateService.class));
		}
	}
//
//	private void invokeTimerTask(Context aContext) {
//		Log.w(TAG, "-----------invokeTimerTask");
//		PendingIntent alarmSender = null;
//		Intent intent = new Intent(CommonUtils.LOCATION_TIMER_TASK_ACTION);
//		alarmSender = PendingIntent.getBroadcast(aContext, 0, intent, 0);
//
//		AlarmManager alarms = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
//		alarms.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, alarmSender);
//	}
//
//	private void cancelTimerTask(Context aContext) {
//		PendingIntent alarmSender = null;
//		Intent intent = new Intent(CommonUtils.LOCATION_TIMER_TASK_ACTION);
//		alarmSender = PendingIntent.getBroadcast(aContext, 0, intent, 0);
//
//		AlarmManager alarms = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
//
//		alarms.cancel(alarmSender);
//	}

}
