package com.tct.weather.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.tct.weather.PermissionActivity;
import com.tct.weather.bean.City;
import com.tct.weather.bean.DayForShow;
import com.tct.weather.bean.WeatherForShow;
import com.tct.weather.provider.DBHelper;
import com.tct.weather.util.CommonUtils;
import com.tct.weather.util.CustomizeUtils;
import com.tct.weather.util.IconBackgroundUtil;
import com.tct.weather.LocateActivity;
import com.tct.weather.MainActivity;
import com.tct.weather.R;
import com.tct.weather.widget.MiniWeatherWidget;
/*BUGFIX-1173717 2015/12/25       xing.zhao   [Weather] Some content in setting screen does not match ergo*/
/*BUGFIX-1239545 2015/12/28       xing.zhao   [Weather]Can't enter the weather via widget if no weather data*/
/*BUGFIX-1470934 2016/1/20       xing.zhao     [Weather]MiddleMan Runtime permission Phone group*/
public class MiniWeatherWidgetService extends Service {
    private static final String TAG = "MiniWeatherWgtService";

    private static final String PACKAGENAME = "com.tct.weather";

    private final String TEMP_KEY = "settings_temp";

    private static final int NO_CITY = 0;
    private static final int NORMAL = 1;
    private static final int OFF_LINE = -1;
    public static int widgetState = NO_CITY;

    private final String INVALIDATA_LOCATION = "no_city_key";
    private String locationKey = INVALIDATA_LOCATION;

    private AppWidgetManager mWidgetManager = null;

    private Context mContext = null;

    private IntentFilter mFilter = null;

    private DBHelper mDBHelper = null;

    private String cityName = null;
//    private String updateTime = null;
    private boolean isAutoLocate = false;
    //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 begin
    private boolean mFirstAddCity = false;
    private Timer mTimer = null;
    //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 end
    private boolean isUnitC = false;
    private int mIndex = 0;
    private ArrayList<City> mCityList = new ArrayList<City>();
    private long weatherOfflineTime = 0;
    private boolean is24Hour = false;
    private int animationTime = 0;
    private Timer animationTimer = null;

    //	private double[] hourItemValues = new double[7];
//	private String[] hourItemTime = new String[7];
    private WeatherForShow mWeatherInfo = null;
    private List<DayForShow> mDayList = null;

    private boolean isTwcWeather = false;

    private boolean isMiddleManAvavible = false;

    private RemoteViews parentViews = null;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.UNIT_BROADCAST".equals(action)) {
                if (null != refreshHandler) {
                    refreshHandler.removeCallbacks(queryRunable);
                    refreshHandler.post(queryRunable);
                }
            }else if ("android.intent.action.START_ANIMATION".equals(action)) {
               //[BUGFIX]-Add-BEGIN by TSCD.peng.du,03/09/2016,1758364,[Weather][Launcher]
              //There is no prompt when click update without the internet
                if (isNetWorkConnected()) {
                    Intent updateIntent = new Intent(mContext, UpdateService.class);
                    updateIntent.setAction("android.intent.action.REFRESH_WIDGET_VIEW");
                    startService(updateIntent);
                    setRefreshAnimation(true);
                }else{
                    Log.d(TAG,"there is not network");
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.locate_connect_error),
                          Toast.LENGTH_LONG).show();
                }
              //[BUGFIX]-Add-END by TSCD.peng.du
            } else if (action.equals(Intent.ACTION_TIME_TICK)) {
                setCurrentRefreshTime();
                //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,12/28/2015,1239545,[Weather]Can't enter the weather via widget if no weather data
            } else if ("android.intent.action.UPDATE_WIDGET".equals(action) || "android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                //[BUGFIX]-Add-END by TSCD.xing.zhao
                viewsUpdate(MiniWeatherWidgetService.this);
            }
        }
    };

    private Handler refreshHandler = new Handler() {
    };

    private Runnable queryRunable = new Runnable() {
        @Override
        public void run() {
            queryCurrentWeather();
            viewsUpdate(mContext);
        }
    };

    private Handler nextCityUpdateHandler = new Handler() {
    };
    private Runnable nextCityQueryRunalbe = new Runnable() {

        @Override
        public void run() {
            queryNextWeather();
            viewsUpdate(mContext);

        }
    };

    private Handler updateDeleteCityHandler = new Handler() {
    };
    private Runnable updateDeleteCityRunnable = new Runnable() {

        @Override
        public void run() {
            updateDeleteCityWeather();
            viewsUpdate(mContext);

        }
    };

    private void viewsUpdate(Context context) {
        int[] appWidgetIds = mWidgetManager.getAppWidgetIds(new ComponentName(mContext, MiniWeatherWidget.class));
        for (int miniAppId : appWidgetIds) {
            viewUpdate(mContext, miniAppId);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        if (null == mFilter) {
            mFilter = new IntentFilter();
            mFilter.addAction("android.intent.action.UNIT_BROADCAST");
            //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 begin
            //mFilter.addAction("com.tct.weather.synclocation");
            //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 end
            mFilter.addAction("android.intent.action.START_ANIMATION");
            mFilter.addAction(Intent.ACTION_TIME_TICK);
            mFilter.addAction("android.intent.action.UPDATE_WIDGET");
            //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,12/28/2015,1239545,[Weather]Can't enter the weather via widget if no weather data
            mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            //[BUGFIX]-Add-END by TSCD.xing.zhao
        }
        registerReceiver(mReceiver, mFilter);

        mDBHelper = new DBHelper(mContext);

        mWidgetManager = AppWidgetManager.getInstance(mContext);

        // add by jielong.xing at 2015-5-5 begin
        startService(new Intent(MiniWeatherWidgetService.this, WeatherTimeWidgetService.class)); //MODIFIED by qian-li, 2016-04-13,BUG-1940875
        startService(new Intent(MiniWeatherWidgetService.this, UpdateService.class));
        // add by jielong.xing at 2015-5-5 end

        //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 begin
        Timer timer = new Timer();
        //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 end
    }

    //added for PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 begin
    public void creatTimerTask() {
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (null != mTimer) {
                    mTimer.cancel();
                    mTimer = null;
                }
                refreshHandler.removeCallbacks(queryRunable);
                refreshHandler.post(queryRunable);
            }

        }, 10000);
    }
    //added for PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 end

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isTwcWeather = CustomizeUtils.getBoolean(MiniWeatherWidgetService.this, "use_twc_weather");
        isMiddleManAvavible = CustomizeUtils.isMiddleManAvavible(this);
        flags = START_STICKY;
        String action = null;
        if (intent != null) {
            action = intent.getAction();
        }
        if (action != null && "android.action.deletecity".equals(action)) {
            // Fixed PR958435 by jielong.xing at 2015-3-24 begin
            if (null != refreshHandler) {
                refreshHandler.removeCallbacks(queryRunable);
                refreshHandler.post(queryRunable);
            }
            // Fixed PR958435 by jielong.xing at 2015-3-24 end
        } else if (action != null && "android.intent.action.NEXT_CITY_MINIWIDGET_UPDATE".equals(action)) {
            if (null != nextCityUpdateHandler) {
                nextCityUpdateHandler.post(nextCityQueryRunalbe);
            }
            //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 begin
            creatTimerTask();
            //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 end
        } else if (action != null && "android.intent.action.REFRESH_WIDGET_VIEW".equals(action)) {
            Intent serviceIntent = new Intent(this, UpdateService.class);
            serviceIntent.setAction("android.intent.action.REFRESH_WIDGET_VIEW");
            startService(serviceIntent);
        }
        // added by jielong.xing at 2015-3-23 begin
        else if (null != action && "android.intent.action.WEATHER_BROADCAST".equals(action)) {
            Boolean isDataReady = intent.getBooleanExtra("weather", false);
            if (isDataReady) {
                if (null != refreshHandler) {
                    refreshHandler.removeCallbacks(queryRunable);
                    refreshHandler.post(queryRunable);
                }
            }
        }
        // added by jielong.xing at 2015-3-23 end
        else {
            if (null != refreshHandler) {
                refreshHandler.removeCallbacks(queryRunable);
                refreshHandler.post(queryRunable);
            }
        }
        setRefreshAnimation(false);
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void queryCurrentWeather() {
        Log.d(TAG, "MiniWeatherWidgetService->queryCurrentWeather()");
        mCityList = mDBHelper.getCityListFromDB();
        if (mCityList.size() == 0) {
            return;
        }
        if (INVALIDATA_LOCATION.equals(locationKey) || null == locationKey) {
            mIndex = 0;
        } else {
            mIndex = 0;
            int size = mCityList.size();
            //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 begin
            mFirstAddCity = true;
            for (int i = 0; i < size; i++) {
                if (mCityList.get(i).isAutoLocate()) {
                    isAutoLocate = true;
                    mIndex = i;
                    break;
                }
            }
            //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 end

            //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 begin
            /*for (int i = 0; i < size; i ++) {
				if (mCityList.get(i).getLocationKey().equals(locationKey)) {
					mIndex = i;
					break;
				}
			}*/
            //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 end
        }

        City city = mCityList.get(mIndex);
        setWeatherValue(city);
    }

    private void queryNextWeather() {
        Log.d(TAG, "MiniWeatherWidgetService->queryNextWeather()");
        // add by jielong.xing for PR929207 begin
        mCityList = mDBHelper.getCityListFromDB();
        if (mCityList.size() == 0) {
            return;
        }
        // add by jielong.xing for PR929207 end
        mIndex++;
        if (mIndex > mCityList.size() - 1) {
            mIndex = 0;
        }

        City city = mCityList.get(mIndex);
        setWeatherValue(city);
    }

    private void updateDeleteCityWeather() {
        Log.d(TAG, "MiniWeatherWidgetService->updateDeleteCityWeather()");
        mCityList = mDBHelper.getCityListFromDB();
        if (mCityList.size() > 0) {
            if (mIndex > mCityList.size() - 1) {
                mIndex = 0;
            }
            City city = mCityList.get(mIndex);
            setWeatherValue(city);
        } else {
            locationKey = INVALIDATA_LOCATION;
        }
    }

    private void setWeatherValue(City city) {
        //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07
        //isAutoLocate = city.isAutoLocate();
        cityName = city.getCityName();
        locationKey = city.getLocationKey();
        mWeatherInfo = mDBHelper.getWeatherForShow(locationKey);
        if (mWeatherInfo == null) {
            Log.e(TAG, "MiniWeatherWidgetService->setWeatherValue()->weatherInfo is null");
            return;
        }
        mDayList = mDBHelper.getDayForShow(locationKey);
//        updateTime = mWeatherInfo.getTime();
    }

	/*private String getCurrentTime() {
		StringBuilder result = new StringBuilder();
		int hour;

		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		String timeFormat = android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.System.TIME_12_24);
		calendar.setTimeInMillis(System.currentTimeMillis());

		if ("12".equals(timeFormat)) {
			is24Hour = false;
			hour = calendar.get(Calendar.HOUR);
			result.append(hour);
			if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
				result.append("AM");
			} else {
				result.append("PM");
			}
		} else {
			is24Hour = true;
			hour = calendar.get(Calendar.HOUR_OF_DAY);
			result.append(hour).append(":00");
		}

		return result.toString();
	}*/

    private void viewUpdate(Context context, int appWidgetId) {
//        RemoteViews parentViews = null;
        if (isTwcWeather){
            parentViews = new RemoteViews(context.getPackageName(), R.layout.miniapp_widget_layout);
        } else {
            parentViews = new RemoteViews(context.getPackageName(), R.layout.miniapp_widget_acc_layout);
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,12/25/2015,1173717,[Weather] Some content in setting screen does not match ergo
        String tempUnit = settings.getString(TEMP_KEY, "1");
        if (TextUtils.equals(tempUnit, "1")) {
            isUnitC = true;
        } else {
            isUnitC = false;
        }
        //[BUGFIX]-Add-END by TSCD.xing.zhao
        int size = mCityList.size();
//        if (size != 0) {
//            try {
//                weatherOfflineTime = System.currentTimeMillis() - Long.parseLong(updateTime);
//            } catch (Exception e) {
//                weatherOfflineTime = 0;
//            }
//        }

        if(isTwcWeather){
            Intent mainIntent = new Intent(context, MainActivity.class);
            mainIntent.putExtra("newCityKey", locationKey);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent gotowebIntent = PendingIntent.getActivity(context, 1, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Fixed PR958181 by jielong.xing at 2015-3-24 end
            parentViews.setOnClickPendingIntent(R.id.miniapp_iv_goto, gotowebIntent);
            // Fixed PR958181 by jielong.xing at 2015-3-24 begin

            //[BUGFIX]-Add-BEGIN by TSCD.qian-li,12/21/2015,1192921,[Android6.0][Weather_v5.2.8.1.0305.0]The "AccuWeather" icon display too small
//        } else {
//            Intent gotoIntent = null;
//            gotoIntent = new Intent();
//            gotoIntent.setAction("android.intent.action.VIEW");
//            Uri content_url = Uri.parse("http://www.accuweather.com");
//            gotoIntent.setData(content_url);
//            gotoIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            PendingIntent gotowebIntent = PendingIntent.getActivity(context, 1, gotoIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//            // Fixed PR958181 by jielong.xing at 2015-3-24 end
//            parentViews.setOnClickPendingIntent(R.id.miniapp_iv_goto, gotowebIntent);
            //[BUGFIX]-Add-END by TSCD.qian-li
        }


        if (size == 0) {
            widgetState = NO_CITY;
            setViewsState(parentViews, widgetState);
            //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,12/28/2015,1239545,[Weather]Can't enter the weather via widget if no weather data
            //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,01/20/2016,1470934,[Weather]MiddleMan Runtime permission Phone group.
            boolean isPermissionAccess = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && (isMiddleManAvavible || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
            //[BUGFIX]-Add-END by TSCD.xing.zhao
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
            boolean isNetWorkConnected = activeInfo != null && activeInfo.isConnected();
            if (isNetWorkConnected) {
                if (isPermissionAccess) {
                    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/06/2016,1192339,[Android6.0][Weather_v5.2.8.1.0304.0]The weather widget prompt should be replace
                    parentViews.setTextViewText(R.id.miniapp_tv_no_data, getString(R.string.miniapp_widget_no_location));
                    parentViews.setTextViewText(R.id.miniapp_tv_no_data_desc, getString(R.string.miniapp_widget_no_location_desc));
                    String setString = getResources().getString(R.string.miniapp_widget_add_location);
                    parentViews.setTextViewText(R.id.miniapp_tv_setting, setString.toUpperCase());
                    Intent accmainIntent = new Intent(context, MainActivity.class);
                    accmainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent wightpendingIntent = PendingIntent.getActivity(context, 0, accmainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    parentViews.setOnClickPendingIntent(R.id.miniapp_tv_setting, wightpendingIntent);
                    //[BUGFIX]-Add-END by TSCD.qian-li
                } else {
                    parentViews.setTextViewText(R.id.miniapp_tv_no_data, getString(R.string.miniapp_widget_no_permission));
                    parentViews.setTextViewText(R.id.miniapp_tv_no_data_desc, getString(R.string.miniapp_widget_no_permission_desc));
                    String setString = getResources().getString(R.string.miniapp_widget_setting);
                    parentViews.setTextViewText(R.id.miniapp_tv_setting, setString.toUpperCase());
                    Intent clickSettingIntent = new Intent(context, PermissionActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickSettingIntent, 0);
                    parentViews.setOnClickPendingIntent(R.id.miniapp_tv_setting, pendingIntent);
                }
            } else {
                parentViews.setTextViewText(R.id.miniapp_tv_no_data, getString(R.string.miniapp_widget_no_data));
                parentViews.setTextViewText(R.id.miniapp_tv_no_data_desc, getString(R.string.miniapp_widget_no_data_desc));
                String setString = getResources().getString(R.string.miniapp_widget_setting);
                parentViews.setTextViewText(R.id.miniapp_tv_setting, setString.toUpperCase());
                Intent clickSettingIntent = new Intent(Settings.ACTION_SETTINGS);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickSettingIntent, 0);
                parentViews.setOnClickPendingIntent(R.id.miniapp_tv_setting, pendingIntent);
            }
            //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/06/2016,1192339,[Android6.0][Weather_v5.2.8.1.0304.0]The weather widget prompt should be replace
//            String setString = getResources().getString(R.string.miniapp_widget_setting);
//            parentViews.setTextViewText(R.id.miniapp_tv_setting, setString.toUpperCase());
//            Intent accmainIntent = new Intent();
//            accmainIntent = new Intent(context, MainActivity.class);
//            accmainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            PendingIntent wightpendingIntent = PendingIntent.getActivity(context, 0, accmainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//            parentViews.setOnClickPendingIntent(R.id.miniapp_addlocation_layout, wightpendingIntent);
            //[BUGFIX]-Add-END by TSCD.xing.zhao
//            if(isTwcWeather) {
//                Intent selectLocationIntent = new Intent(context, LocateActivity.class);
//                PendingIntent pSelectLocationIntent = PendingIntent.getActivity(context, 0, selectLocationIntent, 0);
//                parentViews.setOnClickPendingIntent(R.id.miniapp_addlocation_layout, pSelectLocationIntent);
//            }
            //[BUGFIX]-Add-END by TSCD.qian-li
        }
        // Fixed PR967376,967153 by jielong.xing at 2015-4-3 begin
		/*else if (weatherOfflineTime > 172800000) {
			widgetState = OFF_LINE;
//			setViewsState(parentViews, widgetState);

//			parentViews.setOnClickPendingIntent(R.id.widget_tv_offline, pMainIntent);
		} */
        // Fixed PR967376,967153 by jielong.xing at 2015-4-3 end
        else {
            widgetState = NORMAL;
            setViewsState(parentViews, widgetState);
            if (mWeatherInfo == null) {
                return;
            }
			/*int backgroundResId = getCurrentBackgroundWeatherIcon(mWeatherInfo.getIcon());
			parentViews.setInt(R.id.mini_app_main_layout, "setBackgroundResource", backgroundResId);*/

            int iconID = Integer.parseInt(mWeatherInfo.getIcon()) - 1;
            String[] weatherDescriptions = this.getResources().getStringArray(R.array.weather_icon_desc);
            parentViews.setTextViewText(R.id.miniapp_weather_desc, mWeatherInfo.getText());
            // update by jielong.xing for PR923763 at 2015-2-4 end
            if (isUnitC) {
//                parentViews.setTextViewText(R.id.miniapp_weather_temp, (int) Math.round(Double.parseDouble(mWeatherInfo.getTemp())) + "°");
                parentViews.setTextViewText(R.id.miniapp_weather_temp, CommonUtils.deletaDec(mWeatherInfo.getTemp()) + "°");
//				parentViews.setTextViewText(R.id.widget_weather_unit, "C");
            } else {
                parentViews.setTextViewText(R.id.miniapp_weather_temp, CommonUtils.c2f(mWeatherInfo.getTemp()) + "°");
//				parentViews.setTextViewText(R.id.widget_weather_unit, "F");
            }

//			if (isAutoLocate) {
//				parentViews.setInt(R.id.widget_iv_autoloaction, "setVisibility", View.VISIBLE);
//			} else {
//				parentViews.setInt(R.id.widget_iv_autoloaction, "setVisibility", View.GONE);
//			}

            //Begin added by jiajun.shen for1106434
            Intent animationIntent = new Intent("android.intent.action.START_ANIMATION");
            PendingIntent pAnimationIntent = PendingIntent.getBroadcast(context, 0, animationIntent, 0);
            parentViews.setOnClickPendingIntent(R.id.miniapp_iv_refresh, pAnimationIntent);

            //[BUGFIX]-Add-BEGIN by TSCD.qian-li,12/31/2015,1192929,[Android6.0][Weather_v5.2.8.1.0305.0][monitor]The update time in widget is not same as weather app
//            long updateTime = Long.parseLong(mWeatherInfo.getTime());
            long updateTime = Long.parseLong(mCityList.get(mIndex).getUpdateTime());
            //[BUGFIX]-Add-END by TSCD.qian-li
            parentViews.setTextViewText(R.id.miniapp_tv_refresh_time, getRefreshTime(updateTime));
            //End added by jiajun.shen for1106434

            parentViews.setTextViewText(R.id.miniapp_weather_city, cityName);

            // [BUGFIX]-Add-BEGIN by TSCD.qian-li,12/21/2015,1192920,[Android6.0][Weather_v5.2.8.1.0305.0]Should not display "today" in Weather widget
            //add by jiajun.shen for 1016688 at 2015.6.5
            if (isTwcWeather) {
                String language = getResources().getConfiguration().locale.getCountry();
                boolean isJapanese = ("jp".equals(language.toLowerCase()));//add by jiajun.shen for 1016688 at 2015.6.5
                if (isJapanese) {
                    parentViews.setTextViewText(R.id.miniapp_tv_today, getResources().getString(R.string.label_today) + "  ");
                }//add by jiajun.shen for 1016688 at 2015.6.5
                else {
                    parentViews.setTextViewText(R.id.miniapp_tv_today, getResources().getString(R.string.label_today) + ", ");
                }
            }
            //[BUGFIX]-Add-END by TSCD.qian-li
//			parentViews.setTextViewText(R.id.widget_weather_time, newUpdateTime);

            //[BUGFIX]-Add-BEGIN by TSCD.tianjing.su,12/16/2015,1134660,[Android6.0][Weather_v5.2.8.1.0302.0]The weather widget lack one line
            parentViews.removeAllViews(R.id.miniapp_week_layout);
            //[BUGFIX]-Add-END by TSCD.tianjing.su

            parentViews.removeAllViews(R.id.miniapp_forecast_layout);
            int len = mDayList.size();
            if(isTwcWeather){
                for (int i = 0; i < len - 1; i++) {
                    DayForShow day = mDayList.get(i);

                    //[BUGFIX]-Add-BEGIN by TSCD.tianjing.su,12/16/2015,1134660,[Android6.0][Weather_v5.2.8.1.0302.0]The weather widget lack one line
                    RemoteViews weekView = new RemoteViews(context.getPackageName(), R.layout.miniapp_forecast_week_view);
                    weekView.setTextViewText(R.id.widget_tv_weekly, getWeekly(day.getWeek()));
                    parentViews.addView(R.id.miniapp_week_layout, weekView);
                    //[BUGFIX]-Add-END by TSCD.tianjing.su

                    RemoteViews dayView = new RemoteViews(context.getPackageName(), R.layout.miniapp_forecast_day_view);
                    dayView.setTextViewText(R.id.widget_tv_weekly, getWeekly(day.getWeek()));
                    if (isUnitC) {
//                        dayView.setTextViewText(R.id.widget_tv_high, (int) Math.round(Double.parseDouble(day.getTemph())) + "°");
                        dayView.setTextViewText(R.id.widget_tv_high, CommonUtils.deletaDec(day.getTemph()) + "°");
//                        dayView.setTextViewText(R.id.widget_tv_low, (int) Math.round(Double.parseDouble(day.getTempl())) + "°");
                        dayView.setTextViewText(R.id.widget_tv_low, CommonUtils.deletaDec(day.getTempl()) + "°");
                    } else {
                        dayView.setTextViewText(R.id.widget_tv_high, CommonUtils.c2f(day.getTemph()) + "°");
                        dayView.setTextViewText(R.id.widget_tv_low, CommonUtils.c2f(day.getTempl()) + "°");
                    }
                    String icon = day.getIcon();
/*MODIFIED-BEGIN by qian-li, 2016-04-13,BUG-1940875*/
//                    int resId = IconBackgroundUtil.getTWCWidgetIcon(icon);
//                    dayView.setImageViewResource(R.id.widget_iv_icon, resId);
/*MODIFIED-END by qian-li,BUG-1940875*/
                    parentViews.addView(R.id.miniapp_forecast_layout, dayView);
                }
                //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 begin
                //Intent cityIntent = new Intent("android.intent.action.NEXT_CITY_WIDGET_UPDATE");
                Intent cityIntent = new Intent("android.intent.action.NEXT_CITY_MINIWIDGET_UPDATE");
                //PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 end
                PendingIntent pCityIntent = PendingIntent.getBroadcast(context, 0, cityIntent, 0);
                parentViews.setOnClickPendingIntent(R.id.miniapp_info_layout, pCityIntent);
                parentViews.setOnClickPendingIntent(R.id.miniapp_forecast_layout, pCityIntent);
            }else {
                for (int i = 1; i < len; i++) {
                    DayForShow day = mDayList.get(i);

                    //[BUGFIX]-Add-BEGIN by TSCD.tianjing.su,12/16/2015,1134660,[Android6.0][Weather_v5.2.8.1.0302.0]The weather widget lack one line
                    RemoteViews weekView = new RemoteViews(context.getPackageName(), R.layout.miniapp_forecast_week_view);
                    weekView.setTextViewText(R.id.widget_tv_weekly, getWeekly(day.getWeek()));
                    parentViews.addView(R.id.miniapp_week_layout, weekView);
                    //[BUGFIX]-Add-END by TSCD.tianjing.su

                    RemoteViews dayView = new RemoteViews(context.getPackageName(), R.layout.miniapp_forecast_day_view);
                    dayView.setTextViewText(R.id.widget_tv_weekly, getWeekly(day.getWeek()));
                    if (isUnitC) {
                        dayView.setTextViewText(R.id.widget_tv_high, CommonUtils.deletaDec(day.getTemph()) + "°");
                        dayView.setTextViewText(R.id.widget_tv_low, CommonUtils.deletaDec(day.getTempl()) + "°");
                    } else {
                        dayView.setTextViewText(R.id.widget_tv_high, CommonUtils.c2f(day.getTemph()) + "°");
                        dayView.setTextViewText(R.id.widget_tv_low, CommonUtils.c2f(day.getTempl()) + "°");
                    }
                    String icon = day.getIcon();
                    int resId = IconBackgroundUtil.getACCWidgetIcon(icon);
                    dayView.setImageViewResource(R.id.widget_iv_icon, resId);
                    parentViews.addView(R.id.miniapp_forecast_layout, dayView);
                }

                Intent accmainIntent = new Intent();
                accmainIntent = new Intent(context, MainActivity.class);
                accmainIntent.putExtra("newCityKey", locationKey);
                accmainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pMainIntent = PendingIntent.getActivity(context, 1, accmainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                parentViews.setOnClickPendingIntent(R.id.miniapp_info_layout, pMainIntent);
                parentViews.setOnClickPendingIntent(R.id.miniapp_forecast_layout, pMainIntent);

                //[BUGFIX]-Add-BEGIN by TSCD.tianjing.su,12/16/2015,1134660,[Android6.0][Weather_v5.2.8.1.0302.0]The weather widget lack one line
                parentViews.setOnClickPendingIntent(R.id.miniapp_week_layout, pMainIntent);
                //[BUGFIX]-Add-END by TSCD.tianjing.su
            }

        }

        mWidgetManager.updateAppWidget(appWidgetId, parentViews);
    }

    private void setViewsState(RemoteViews views, int widgetState) {
        switch (widgetState) {
            case NO_CITY:
                views.setInt(R.id.miniapp_addlocation_layout, "setVisibility", View.VISIBLE);
                views.setInt(R.id.miniapp_info_layout, "setVisibility", View.GONE);
                break;
		/*case OFF_LINE:
			views.setInt(R.id.widget_addlocation_layout, "setVisibility", View.VISIBLE);
			views.setInt(R.id.widget_info_layout, "setVisibility", View.GONE);
			break;*/
            case NORMAL:
                views.setInt(R.id.miniapp_addlocation_layout, "setVisibility", View.GONE);
                views.setInt(R.id.miniapp_info_layout, "setVisibility", View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private String getWeekly(String sWeek) {
        String[] enWeekly = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri",
                "Sat", "Sun"};
        String result = null;
        String[] weekly = this.getResources().getStringArray(R.array.weather_weekly);
        for (int i = 0; i < 7; i++) {
            if (sWeek.contains(enWeekly[i])) {
                result = weekly[i];
            }
        }
        if (result == null) {
            result = sWeek;
        }
        return result;
    }

	/*private int getCurrentBackgroundWeatherIcon(String iconId) {
		Log.e("jielong", "MiniWeatherWidgetService getCurrentBackgroundWeatherIcon iconId == " + iconId);
		int icon = Integer.parseInt(iconId);
        if (icon > 0) {
            if (Config.SUNNY_LIST.contains(icon)) {
                if(Config.SUNNY_NIGHT_LIST.contains(icon))
                {
                	return R.drawable.mini_bg_clear;
                }
                return R.drawable.mini_bg_sunny;
            } else if (Config.CLOUDY_LIST.contains(icon)) {
                return R.drawable.mini_bg_cloudy;
            } else if (Config.RAIN_LIST.contains(icon)) {
                return R.drawable.mini_bg_rainy;
            } else if (Config.SNOW_LIST.contains(icon)) {
                return R.drawable.mini_bg_snow;
            } else if (Config.FOG_LIST.contains(icon)) {
                return R.drawable.mini_bg_fog;
            } else if (Config.FROST_LIST.contains(icon)) {
                return R.drawable.mini_bg_frost;
            } else if (Config.LIGHTNING_LIST.contains(icon)) {
                return R.drawable.mini_bg_storm;
            } else {
                return R.drawable.mini_bg_sunny;
            }
        }
        return R.drawable.mini_bg_sunny;
    }*/

    private boolean getCurrentTime() {
        boolean result = false;
        int hour;

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        ContentResolver cv = getContentResolver();
        String timeFormat = android.provider.Settings.System.getString(cv,
                android.provider.Settings.System.TIME_12_24);
        calendar.setTimeInMillis(System.currentTimeMillis());
        if ("12".equals(timeFormat)) {

            hour = calendar.get(Calendar.HOUR);
            if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
                if (hour < 6) {
                    result = true;
                }
            } else {
                if (hour >= 6) {
                    result = true;
                }
            }
        } else {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour >= 18 || hour < 6) {
                result = true;
            }
        }

        return result;
    }

    //Begin add by jiajun.shen for 1106434
    private String getRefreshTime(long time) {
        long SECOND = 1000L;
        long MINUTE = 60 * 1000L;
        long HOUR = 60 * 60 * 1000L;
        long DAY = 24 * 60 * 60 * 1000L;
        long WEEK = 7 * 24 * 60 * 60 * 1000L;
        long MONTH = 4 * 7 * 24 * 60 * 60 * 1000L;
        long tempTime = System.currentTimeMillis() - time;
        if (tempTime > MONTH) {
            int months = (int) (tempTime / MONTH);
            if (months > 1) {
                return getResources().getQuantityString(R.plurals.months_ago, months, months);
            } else {
                return getResources().getQuantityString(R.plurals.months_ago, 1, 1);
            }
        }else if (tempTime > WEEK) {
//            return formatTime(time);
            int weeks = (int) (tempTime / WEEK);
            if (weeks > 1) {
                return getResources().getQuantityString(R.plurals.weeks_ago, weeks, weeks);
            } else {
                return getResources().getQuantityString(R.plurals.weeks_ago, 1, 1);
            }
        } else if (tempTime > DAY) {
            int days = (int) (tempTime / DAY);
            if (days > 1) {
                return getResources().getQuantityString(R.plurals.days_ago, days, days);
            } else {
                return getResources().getQuantityString(R.plurals.days_ago, 1, 1);
            }
        } else if (tempTime > HOUR) {
            int hours = (int) (tempTime / HOUR);
            if (hours > 1) {
                return getResources().getQuantityString(R.plurals.hours_ago, hours, hours);
            } else {
                return getResources().getQuantityString(R.plurals.hours_ago, 1, 1);
            }
        } else if (tempTime > MINUTE) {
            int minutes = (int) (tempTime / MINUTE);
            if (minutes > 1) {
                return getResources().getQuantityString(R.plurals.minutes_ago, minutes, minutes);
            } else {
                return getResources().getQuantityString(R.plurals.minutes_ago, 1, 1);
            }
        } else {
            return getResources().getString(R.string.just_updated);
        }
    }


    private String formatTime(long time) {
        String language = getResources().getConfiguration().locale.getCountry();
        String newUpdateTime = "";

        java.text.DateFormat format = null;
        boolean isGetLongDateFormat = isGetLongDateFormat();
        if (isGetLongDateFormat) {
            format = android.text.format.DateFormat.getLongDateFormat(this);
        } else {
            format = DateFormat.getDateFormat(this);
        }

        java.util.Calendar currentTime = java.util.Calendar.getInstance();
        currentTime.setTimeInMillis(time);
        newUpdateTime = format.format(currentTime.getTime());
        return newUpdateTime;
    }

    private boolean isGetLongDateFormat() {
        return CustomizeUtils.getBoolean(MiniWeatherWidgetService.this, "def_weather_dateformat_long");
    }

    public void updateRefreshImage(Context context, int appWidgetIds) {
        //[BUGFIX]-Add-BEGIN by TSCD.peng.du,03/15/2016,1718461,[GAPP][Android6.0][Weather]The weather mini app display white
//        RemoteViews parentViews = null;
        //[BUGFIX]-Add-END by TSCD.peng.du
        if (isTwcWeather){
            if (parentViews == null) {
                parentViews = new RemoteViews(context.getPackageName(), R.layout.miniapp_widget_layout);
            }
            RemoteViews subViews = new RemoteViews(context.getPackageName(), R.layout.widget_refresh_layout);
            parentViews.removeAllViews(R.id.miniapp_layout_refresh);
            parentViews.addView(R.id.miniapp_layout_refresh, subViews);
        }else {
            //[BUGFIX]-Add-BEGIN by TSCD.peng.du,03/15/2016,1718461,[GAPP][Android6.0][Weather]The weather mini app display white
            if (parentViews == null) {
                parentViews = new RemoteViews(context.getPackageName(), R.layout.miniapp_widget_acc_layout);
            }
            //[BUGFIX]-Add-END by TSCD.peng.du
            RemoteViews subViews = new RemoteViews(context.getPackageName(), R.layout.widget_refresh_acc_layout);
            parentViews.removeAllViews(R.id.miniapp_layout_refresh);
            parentViews.addView(R.id.miniapp_layout_refresh, subViews);
        }

        //[BUGFIX]-Add-BEGIN by TSCD.peng.du,03/15/2016,1718461,[GAPP][Android6.0][Weather]The weather mini app display white
//        Intent animationIntent = new Intent("android.intent.action.START_ANIMATION");
//        PendingIntent pAnimationIntent = PendingIntent.getBroadcast(context, 0, animationIntent, 0);
//        parentViews.setOnClickPendingIntent(R.id.miniapp_iv_refresh, pAnimationIntent);
        //[BUGFIX]-Add-END by TSCD.peng.du
        mWidgetManager.updateAppWidget(appWidgetIds, parentViews);
    }

    public void setRefreshAnimation(boolean toStart) {
        if (toStart) {
            animationTime = 0;
            if (null != animationTimer) {
                animationTimer.cancel();
                animationTimer = null;
            }
            animationTimer = new Timer();
            animationTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    animationTime++;
                    if (animationTime < 100) {
                        animationHandler.removeCallbacks(animationRunable);
                        animationHandler.post(animationRunable);
                    } else {
                        if (null != animationTimer) {
                            animationTimer.cancel();
                            animationTimer = null;
                        }
                    }
                }

            }, 0, 600);
        } else {
            if (null != animationTimer) {
                animationTimer.cancel();
                animationTimer = null;
            }
        }

    }

    private void setCurrentRefreshTime() {
        if (null != parentViews && null != mCityList && mCityList.size() > 0) {
            int[] appWidgetIds = mWidgetManager.getAppWidgetIds(new ComponentName(mContext, MiniWeatherWidget.class));
            long updateTime = Long.parseLong(mCityList.get(mIndex).getUpdateTime());
            parentViews.setTextViewText(R.id.miniapp_tv_refresh_time, getRefreshTime(updateTime));
            for (int miniAppId : appWidgetIds) {
                mWidgetManager.updateAppWidget(miniAppId, parentViews);
            }

        }
    }

    private Handler animationHandler = new Handler() {
    };

    private Runnable animationRunable = new Runnable() {
        @Override
        public void run() {
            int[] appWidgetIds = mWidgetManager.getAppWidgetIds(new ComponentName(mContext, MiniWeatherWidget.class));
            for (int miniAppId : appWidgetIds) {
                //isRefreshing = true;
                updateRefreshImage(mContext, miniAppId);
            }

        }
    };

    private boolean isNetWorkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
    //End add by jiajun.shen for 1106434

}
