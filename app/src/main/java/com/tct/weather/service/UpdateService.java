package com.tct.weather.service;

import android.Manifest;
/* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
/* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
/* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
/* MODIFIED-END by xiangnan.zhou,BUG-1983334*/

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.tct.weather.R; // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
import com.tct.weather.bean.City;
import com.tct.weather.bean.Currentconditions;
import com.tct.weather.bean.Day;
import com.tct.weather.bean.Forecast;
import com.tct.weather.bean.Hour;
import com.tct.weather.bean.Local;
import com.tct.weather.bean.Weather;
import com.tct.weather.bean.WeatherForShow;
import com.tct.weather.bean.WeatherInfo;
import com.tct.weather.internet.ACCUResponseUtil;
import com.tct.weather.internet.ACCUUrlBuilder;
import com.tct.weather.internet.CityFindRequest;
import com.tct.weather.internet.CurrentWeatherRequest;
import com.tct.weather.internet.ForecastWeatherRequest;
import com.tct.weather.internet.ResponseUtil;
import com.tct.weather.internet.TWCResponseUtil;
import com.tct.weather.internet.TWCUrlBuilder;
import com.tct.weather.internet.UrlBuilder;
import com.tct.weather.provider.DBHelper;
import com.tct.weather.util.CommonUtils;
import com.tct.weather.util.CustomizeUtils;
import com.tct.weather.util.SharePreferenceUtils;

import org.apache.http.Header; // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/*
 ==========================================================================
 *HISTORY
 *
 *Tag            Date              Author        Description
 *============== ============ =============== ==============================
 *BUGFIX-1175818 2015/12/31       xing.zhao     [Weather]Weather APK  can't  auto match city by zip code
 *BUGFIX-1470934 2016/1/20       xing.zhao     [Weather]MiddleMan Runtime permission Phone group
 *===========================================================================
 */
public class UpdateService extends Service implements BDLocationListener {
    private static final String TAG = "weather UpdateService";

    //	private static final String URL_CITY_FIND = "http://tclandroidicsapp.accu-weather.com/widget/tclandroidicsapp/city-find.asp?";
//    private static final String URL_WEATHER_DATA = "http://tclandroidicsapp.accu-weather.com/widget/tclandroidicsapp/weather-data.asp?location=";
//
    private static final String TCLREGISTERKEY = "C704c66781c37b94ca24a7fcefb44303";
    private static final String URL_BAIDU_GEOCODER = "http://api.map.baidu.com/geocoder?";
    private static final String KEY_LAST_REFRESHTIME = "key_last_record_time";
    private static final String KEY_LAST_AUTOLOCATETIME = "key_last_autolocate_time";
    private static final String CITY_TOKYO_LOCATIONKEY = "226396";// add by jiajun.shen for 1016685 at 2015.6.4
    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/20/2016,1313172,[Weather]weather not translate to russian
    private static final String KEY_LAST_LANGUAGE = "lang";
    //[BUGFIX]-Add-END by TSCD.qian-li

    private final String AUTO_UPDATE_KEY = "settings_auto_update";

    private static final String UPDATE_ALARM = "update_alarm";
    public static final String CITYID = "cityId:";

//	private static final String LOCATION_KEY = "location_key";
//    private static final String PREFERENCES_NAME = "autolocate_city";

    public static final String POSTALCODE = "postalCode";

//	private static final int LANGUAGE_ID_ZH_TW = 14;

    //	private static final long HALFHOUR = 1800000; // update auto locate

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,11/03/2015,702164,[Android5.1][Weather_v5.1.3.4.0303.0]The weather update time is overlap
    private static final long ONEHOUR = 2600000; // update autolocate weather
    private static final long TWOHOUR = 6000000; // update all weather
    private static final long THREEHOUR = 9600000; // update all weather
    private static final long FOURHOUR = 13000000; //update all weather and forecast
    private static final long LOCATIONCHANGETIME = 3000; // wait the location change time
    //[BUGFIX]-Add-END by TSCD.qian-li

    // add by jielong.xing for power consumption high issue at 2015-3-18 begin
    private static final int INTERVALTIME = 5; // if auto locate failed, wait 5 minute
    private static final int HOURCNT = 60;
    private static final int CONNECTRETRYMAXTIME = 3; // auto locate request on one hour can only 3 times
    private boolean isAutoLocateFailed = false; // is auto locate failed
    private int mIntervalCnt = 0;
    private int mAutoLocateFailedCnt = 0;
    private int mHourIntervalCnt = 0;
    private static final boolean DEBUG = true;
    // add by jielong.xing for power consumption high issue at 2015-3-18 end

    //	private static final int MSGUPDATEWIFISTATE = 0x10002;
    private static final int MSGOPENLOCATIONREQUEST = 0x1003;
    private static final int MSGREMOVELOCATIONREQUEST = 0x1004;
    private static final int MSGCHECKUPDATE = 0x1005;

    private static final int RETRYTIME = 10; // wait 30s for each autolocate

    private DBHelper mDBHelper = null;
    private List<City> searchCityList = null;

    private LocationManager mLocationManager;
    private boolean mRequestUpdateSuccess = false;
    private boolean mLocationUpdated = false;

    private boolean isWifiConnected = false;
    private boolean isMobileConnected = false;
    private boolean isOtherConnected = false;
    private boolean isMiddleManAvavible = false;

    private LocationListener mGpsListener = null;
    private LocationListener mNetworkListener = null;
    private Location mGpsLocation = null;
    private Location mNetworkLocation = null;

    private boolean isNetworkProvideEnable = true;
    private boolean isGPSProvideEnable = true;

    private boolean isNetworkRequestOpen = false;
    private boolean isGPSRequestOpen = false;

//	private SharedPreferences mPreferences = null;

    private UpdateBinder updateBinder = new UpdateBinder();

    private boolean isLocationOpen = false;
    private boolean isScreenOn = false;
    //[BUGFIX]-MOD-BEGIN by TSCD.xing.zhao,12/29/2015,1192931,[Android6.0][Weather_v5.2.8.1.0305.0]Click the refresh button on widget continually,weather will crash
    private boolean isWidgetUpdating = false;
    //[BUGFIX]-Add-END by TSCD.xing.zhao

    private boolean mAutoUpdate = false;

    private City tempCity = null;

    private boolean isNetworkConnected = true;

    private boolean isUpdateWeatherThreadRunning = false;
    private boolean isUpdateWeatherThreadStillAlive = false;
    //Defect 212555 Outdoor auto location is failed by bing.wang.hz
    private boolean mIsFirstLocationTry = true;

    private boolean isFirstLaunch = true; // add by qian.li for 928742 at 2015.11.25

    private boolean isFirstUse = true; // add by jiajun.shen for 1016685 at 2015.6.4

    private boolean isUseBaiduAsDefault = false;

    private boolean autoUpdate = true;

    private RequestQueue requestQueue;

    private LocationClient mLocationClient = null;
    private static final int START_BAIDU_LOCATE = 0;
    private static final int STOP_BAIDU_LOCATE = 1;

    private ArrayList<Currentconditions> currentconditionsArrayList;
    private int updateCurrnetNum = 0;
    private int updateForecastNum = 0;
    private int updateHourlyNum = 0;
    private int updateCityNum = 0;
    private ArrayList<City> mCityList;
    private ArrayList<Weather> mWeatherList;
    //private static final int INSERT_ALL_WEATHER_INTO_DB=101;
    private static final int INSERT_CURRENT_WEATHER_INTO_DB = 102;
    private static final int INSERT_HOURLY_WEATHER_INTO_DB = 103;
    private static final int INSERT_DAILY_WEATHER_INTO_DB = 104;
    private static final int INSERT_CITY_INTO_DB = 105;
    private int mcitySize = 0;
    //private boolean updateCurrentOnly = true;//true:update current weather false:update current and forecast weather
    private static final int UPDATE_AUTO_LOCATION_WEATHER = 1;
    private static final int UPDATE_ALL_CURRENT_WEATHER = 2;
    private static final int UPDATE_ALL_WEATHER = 3;
    private static final int UPDATE_ALL_WEATHER_AUTO_LOCATION = 4;
    private int updateGrade = UPDATE_AUTO_LOCATION_WEATHER;//1:update autolocate weather   2:update all current weather 3:update all current and forecast weather

    private ResponseUtil responseUtil;
    private UrlBuilder urlBuilder;
    private boolean isTwcWeather = false;

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/12/2016,1313172,[Weather]weather not translate to russian
    private boolean isLanguageChange = false;
    private boolean isUpdating = false;
    private boolean updateCityNameWrong = false;
    //[BUGFIX]-Add-END by TSCD.qian-li

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg != null) {
                Log.d(TAG, " location msg = " + msg.toString()); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
            }
            switch (msg.what) {
                case MSGOPENLOCATIONREQUEST: {
                    mHandler.removeMessages(MSGOPENLOCATIONREQUEST);
                    requestLocationUpdate();
                    break;
                }
                case MSGREMOVELOCATIONREQUEST: {
                    mHandler.removeMessages(MSGREMOVELOCATIONREQUEST);
                    removeLocationUpdate();
                    break;
                }
                case MSGCHECKUPDATE: {
                    if (isUpdateWeatherThreadRunning) {
                        mHandler.removeMessages(MSGCHECKUPDATE);
                        return;
                    }
                    //checkUpdate();
                }
                break;
                case START_BAIDU_LOCATE:
                    mHandler.removeMessages(START_BAIDU_LOCATE);
                    if (mLocationClient == null) {
                        mLocationClient = new LocationClient(UpdateService.this);
                    }

                    mLocationClient.registerLocationListener(UpdateService.this);

                    LocationClientOption option = new LocationClientOption();
                    option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
                    option.setCoorType("bd09ll");
                    option.setScanSpan(10 * 1000);
                    option.setIsNeedAddress(false);
                    option.setOpenGps(false);
                    mLocationClient.setLocOption(option);

                    mLocationClient.start();
                    break;
                case STOP_BAIDU_LOCATE:
                    mHandler.removeMessages(STOP_BAIDU_LOCATE);
                    if (mLocationClient != null) {
                        mLocationClient.stop();
                        mLocationClient.unRegisterLocationListener(UpdateService.this);
                        mLocationClient = null;
                    }
                    break;
                case INSERT_CURRENT_WEATHER_INTO_DB:
//                    if (updateCurrnetNum >= mcitySize || updateGrade == 1) {
//                    if (updateCurrnetNum >= mcitySize) {
                    /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "insert current weather into db size=" + mWeatherList.size());
                            for (int i = 0; i < mWeatherList.size(); i++) {
                                Weather weather = mWeatherList.get(i);
                                if (weather != null) {
                                    Local local = weather.getLocal();
                                    //[BUGFIX]-MOD-BEGIN by TSCD.xing.zhao,12/29/2015,1192931,[Android6.0][Weather_v5.2.8.1.0305.0]Click the refresh button on widget continually,weather will crash
                                    if (local != null) {
                                        String locationKey = local.getCityId();
                                        mDBHelper.updateCurrentWeather(locationKey, weather);
                                        mDBHelper.updateCityTimeByLocationKey(locationKey);
                                    }
                                    //[BUGFIX]-Add-END by TSCD.xing.zhao
                                }
                            }
                            //mWeatherList=null;
                            checkUpdateFinished();
                        }
                    }).start();
//                    }
                    break;
                case INSERT_HOURLY_WEATHER_INTO_DB:
                    //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/01/2015,983895,[ergo5.2.8]delete the weather_24forecast_card
                    if (isTwcWeather && (updateHourlyNum >= mcitySize || updateGrade == UPDATE_AUTO_LOCATION_WEATHER)) {
                        //[FEATURE]-Add-END by TSCD.peng.du
                        /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "insert hourly weather into db size=" + mWeatherList.size());
                                for (int i = 0; i < mWeatherList.size(); i++) {
                                    Weather weather = mWeatherList.get(i);
                                    if (weather != null) {
                                        String locationKey = weather.getHourlyLocationKey();
                                        mDBHelper.updateHourly(locationKey, weather);
                                        //mDBHelper.updateCityTimeByLocationKey(locationKey);
                                    }
                                }
                                //mWeatherList=null;
                                checkUpdateFinished();
                            }
                        }).start();
                    }
                    break;
                case INSERT_DAILY_WEATHER_INTO_DB:
                    if (updateForecastNum >= mcitySize) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "insert daily weather into db size=" + mWeatherList.size());
                                for (int i = 0; i < mWeatherList.size(); i++) {
                                    Weather weather = mWeatherList.get(i);
                                    if (weather != null) {
                                        String locationKey = weather.getDailyLocationKey();
                                        mDBHelper.updateForecast(locationKey, weather);
                                        //mDBHelper.updateCityTimeByLocationKey(locationKey);
                                    }
                                }
                                //mWeatherList=null;
                                checkUpdateFinished();
                            }
                        }).start();
                    }
                    break;
                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/12/2016,1313172,[Weather]weather not translate to russian
                case INSERT_CITY_INTO_DB: // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "insert city into db size=" + mCityList.size());
                            for (int i = 0; i < mCityList.size(); i++) {
                                if (updateGrade == UPDATE_ALL_WEATHER_AUTO_LOCATION && mCityList.get(i).isAutoLocate()) {
                                    continue;
                                }
                                mDBHelper.insertCity(mCityList.get(i));
                            }
                            checkUpdateFinished();
                        }
                    }).start();
                    break;
                //[BUGFIX]-Add-END by TSCD.qian-li
                default:
                    break;
            }
        }
    };

    public UpdateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDBHelper = new DBHelper(this);
        searchCityList = new ArrayList<City>();
        mCityList = new ArrayList<City>();
        //Defect 212555 Outdoor auto location is failed by bing.wang.hz
        mIsFirstLocationTry = SharePreferenceUtils.getInstance().getBoolean(this, CommonUtils.FIRST_LOCATION_TRY, true);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mGpsListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e(TAG, "GPS____onLocationChanged Latitude = "
                        + location.getLatitude() + "Longitude = "
                        + location.getLongitude());
                if (location != null) {
                    mGpsLocation = location;
                    mLocationUpdated = true;
                }
            }

            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        mNetworkListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e(TAG, "Network____onLocationChanged Latitude = "
                        + location.getLatitude() + "Longitude = "
                        + location.getLongitude());
                if (location != null) {
                    mNetworkLocation = location;
                    mLocationUpdated = true;
                }
            }

            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        IntentFilter myIntentFilter = new IntentFilter();
//        myIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        myIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
//        myIntentFilter.addAction(Intent.ACTION_TIME_TICK);
//        myIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        myIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//        myIntentFilter.addAction(UPDATE_ALARM);
        myIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mBroadcastReceiver, myIntentFilter);

        // add by jiajun.shen for 1016685 at 2015.6.4
        SharedPreferences pref = getApplicationContext().getSharedPreferences("isFirstUse", Context.MODE_PRIVATE);
        isFirstLaunch = pref.getBoolean("isFirstLaunch", true);
        isMiddleManAvavible = CustomizeUtils.isMiddleManAvavible(this);

        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,11/24/2015,928742,[Weather] In Weather app the temperature unit should be Celsius.
        if (isFirstLaunch) {
            //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/20/2016,1313172,[Weather]weather not translate to russian
            SharePreferenceUtils.getInstance().saveString(this, KEY_LAST_LANGUAGE, getLanguage());
            //[BUGFIX]-Add-END by TSCD.qian-li
            mDBHelper.updateIsUnitC(true);
            isFirstLaunch = false;
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isFirstLaunch", isFirstLaunch);
            editor.commit();
        }
        //[BUGFIX]-Add-END by TSCD.qian-li

        isFirstUse = pref.getBoolean("isFirstUse", true);
        boolean isForceSetTokyo = isForceSetTokyo();
        if (isFirstUse && isForceSetTokyo) {
            myIntentFilter = new IntentFilter();
            myIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(mNetworkBroadcastReceiver, myIntentFilter);
        } else {
            isFirstUse = false;
        }
        // add by jiajun.shen for 1016685 at 2015.6.4

        // add by jielong.xing at 2015-3-25 begin
        startService(new Intent(UpdateService.this, WeatherTimeWidgetService.class)); //MODIFIED by qian-li, 2016-04-13,BUG-1940875
        startService(new Intent(UpdateService.this, MiniWeatherWidgetService.class));
        // add by jielong.xing at 2015-3-25 end
        //add by qian-li for track new weather developing in 99  at 2015-11-16 begin
        //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,01/20/2016,1470934,[Weather]MiddleMan Runtime permission Phone group.

        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/22/2016,1313172,[Android6.0][Weather_v5.2.8.1.0305.0][FT test][Monitor]Weather locate auto failed
//        String mimsi = null;
//        if (isMiddleManAvavible) {
//            mimsi = CustomizeUtils.getSubscriberId(this);
//        } else {
//            TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
//            if (tm != null) {
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
//                        == PackageManager.PERMISSION_DENIED) {
//                    mimsi = null;
//                } else {
//                    mimsi = tm.getSubscriberId();
//                }
//            }
//        }
//        Log.e(TAG, " imsi = " + mimsi);
//        if((mimsi!= null)&&mimsi.startsWith("460")){
//            isUseBaiduAsDefault = true;
//        }
        //[BUGFIX]-Add-END by TSCD.qian-li

        //[BUGFIX]-Add-END by TSCD.xing.zhao
//        isUseBaiduAsDefault = CustomizeUtils.getBoolean(UpdateService.this, "use_baidu_location");
        //end by qian-li
        requestQueue = Volley.newRequestQueue(this);
        if (isUseTwcWeather()) {
            responseUtil = new TWCResponseUtil();
            isTwcWeather = true;
            urlBuilder = new TWCUrlBuilder();
        } else {
            Log.e(TAG, "the isTwcWeather = " + isTwcWeather); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
            responseUtil = new ACCUResponseUtil();
            isTwcWeather = false;
            urlBuilder = new ACCUUrlBuilder();
        }

        //[FEATURE]-Add-BEGIN by TSCD.qian-li,12/23/2015,984832,[Weather]Local city doesn't auto refresh after half an hour ago
        Context mContext = getApplicationContext();
        cancelTimerTask(mContext);
        invokeTimerTask(mContext);
        //[FEATURE]-Add-END by TSCD.qian-li

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (null != intent && "android.intent.action.REFRESH_WIDGET_VIEW".equals(intent.getAction())) {
            //[BUGFIX]-MOD-BEGIN by TSCD.xing.zhao,12/29/2015,1192931,[Android6.0][Weather_v5.2.8.1.0305.0]Click the refresh button on widget continually,weather will crash
            if (!isWidgetUpdating) {
                isWidgetUpdating = true;
                setUpdateManue();
                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/20/2016,1313172,[Weather]weather not translate to russian
                String currentLang = getLanguage();
                String preLang = SharePreferenceUtils.getInstance().getString(this, KEY_LAST_LANGUAGE, currentLang);
                if (!preLang.equals(currentLang)) {
                    isLanguageChange = true;
                }
                //[BUGFIX]-Add-END by TSCD.qian-li
                updateAllWeatherWithAutoLocation();
            }
            //[BUGFIX]-Add-END by TSCD.xing.zhao
        } else if (null != intent && "com.tct.checkUpdate".equals(intent.getAction())) {
            Log.w(TAG, "------StartService, com.tct.checkUpdate");
//            long lastRefreshTime = SharePreferenceUtils.getLong(UpdateService.this, KEY_LAST_REFRESHTIME, -1);
//            long time = System.currentTimeMillis();
//            if (lastRefreshTime == -1 || time - lastRefreshTime > TWOHOUR) {
//                Log.w(CommonUtils.TAG_BING, "------StartService, com.tct.checkUpdate ininin");
////				checkUpdate();
//                //mHandler.sendEmptyMessage(MSGCHECKUPDATE);
//                autoUpdate();
//            }
            /* MODIFIED-BEGIN by peng.du, 2016-03-22,BUG-1842312 */
            if (isScreenOn()) {
                autoUpdate();
            }
            /* MODIFIED-END by peng.du,BUG-1842312 */

            /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
            //[FEATURE]-Add-BEGIN by TSCD.qian-li,01/27/2016,1532388,[Weather]MIE call weather interface to locate automaticly
        } else if (null != intent && "com.tct.start_location".equals(intent.getAction())) {
            startAutoLocation();
            //[FEATURE]-Add-END by TSCD.qian-li
            /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/

        } else {
//			checkUpdate();
            //mHandler.sendEmptyMessage(MSGCHECKUPDATE);
            autoUpdate();
        }

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(UpdateService.this);
        }
        //invokeTimerTask(UpdateService.this);
        return START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        if (mDBHelper != null) {
            mDBHelper.close();
            mDBHelper = null;
        }
        mHandler.sendEmptyMessage(MSGREMOVELOCATIONREQUEST);
        // add by jielong.xing at 2015-3-23 begin
        Intent intent = new Intent("com.tct.weather.SERVICE_DESTROY");
        sendBroadcast(intent);
        // add by jielong.xing at 2015-3-23 end
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return updateBinder;
    }

    public class UpdateBinder extends Binder {
        public UpdateService getService() {
            return UpdateService.this;
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
//				mHandler.sendEmptyMessageDelayed(MSGUPDATEWIFISTATE, 3000);
//			} else
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Log.i(TAG, "screen off checkupdate");
                if (isUseBaiduAsDefault) {
                    mHandler.sendEmptyMessage(STOP_BAIDU_LOCATE);
                } else {
                    mHandler.sendEmptyMessage(MSGREMOVELOCATIONREQUEST);
                }
            }
//            else if (Intent.ACTION_TIME_TICK.equals(action)
//                    || Intent.ACTION_TIME_CHANGED.equals(action)) {
////				checkUpdate();
//                if (Intent.ACTION_TIME_TICK.equals(action)) {
//                    mHourIntervalCnt++;
//                    if (mHourIntervalCnt >= HOURCNT) {
//                        resetRetryData();
//                    }
//                    if (isUpdateWeatherThreadRunning) {
//                        if (isUpdateWeatherThreadStillAlive) {
//                            isUpdateWeatherThreadStillAlive = false;
//                            isUpdateWeatherThreadRunning = false;
//                        } else {
//                            isUpdateWeatherThreadStillAlive = true;
//                        }
//                    }
//                }
//                mHandler.sendEmptyMessage(MSGCHECKUPDATE);
//            }
            else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    isNetworkConnected = true;
                } else {
                    isNetworkConnected = false;
                }
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                Log.i(TAG, "screen on checkupdate");
                autoUpdate();
            }
        }
    };

    //[FEATURE]-Add-BEGIN by TSCD.qian-li,01/27/2016,1532388,[Weather]MIE call weather interface to locate automaticly
    public void startAutoLocation() {
        Log.i(TAG, "startAutoLocation");
        Log.i(TAG, "isUpdating : " + isUpdating);
        if (isUpdating) {
            return;
        }
        isUpdating = true;
        mCityList = getCityListFromDB();
        if (mCityList != null && !mCityList.isEmpty()) {
            Log.i(TAG, "mCityList is not empty");
            isUpdating = false;
            return;
        }
        mcitySize = 1;
        isUseBaiduAsDefault = CustomizeUtils.isUseBaiDuLocation(UpdateService.this, isMiddleManAvavible);
        updateCurrnetNum = 0;
        updateForecastNum = 0;
        updateHourlyNum = 0;
        updateGrade = UPDATE_ALL_WEATHER_AUTO_LOCATION;
        Weather weather = new Weather();
        if (mWeatherList == null) {
            mWeatherList = new ArrayList<Weather>(); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
        } else {
            mWeatherList.clear();
        }
        mWeatherList.add(0, weather);
        AutoLocationThread autoLocationThread = new AutoLocationThread(null, 0);
        autoLocationThread.start();
    }
    //[FEATURE]-Add-END by TSCD.qian-li

    public void updateAutoLocateWeather() {
        Log.i(TAG, "updateAutoLocateWeather");
        Log.i(TAG, "isUpdating : " + isUpdating);
        if (isUpdating) {
            Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
            it.putExtra("stopUpdating", true);
            sendBroadcast(it);
            return;
        }
        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/22/2016,1313172,[Android6.0][Weather_v5.2.8.1.0305.0][FT test][Monitor]Weather locate auto failed
        isUseBaiduAsDefault = CustomizeUtils.isUseBaiDuLocation(UpdateService.this, isMiddleManAvavible);
        //[BUGFIX]-Add-END by TSCD.qian-li
        isUpdating = true;
        updateCurrnetNum = 0;
        updateHourlyNum = 0;
        //updateCurrentOnly = true;
        updateGrade = UPDATE_AUTO_LOCATION_WEATHER;
        mCityList = getCityListFromDB();
        if (null == mCityList || 0 == mCityList.size()) {
            Log.i(TAG, "mCityList is useless");
            isUpdating = false;
            return;
        }
        Weather weather = new Weather();
        if (mWeatherList == null) {
            mWeatherList = new ArrayList<Weather>();
        } else {
            mWeatherList.clear();
        }
        mWeatherList.add(0, weather);
        UpdateLocationThread updateLocationThread = new UpdateLocationThread();
        updateLocationThread.start();

    }

    public void updateAllCurrentWeather() {
        Log.i(TAG, "updateAllCurrentWeather");
        Log.i(TAG, "isUpdating : " + isUpdating);
        if (isUpdating) {
            Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
            it.putExtra("stopUpdating", true);
            sendBroadcast(it);
            return;
        }
        isUpdating = true;
        updateCityNameWrong = false;
        //updateCurrentOnly = true;
        updateCurrnetNum = 0;
        updateHourlyNum = 0;
        updateCityNum = 0;
        updateGrade = UPDATE_ALL_CURRENT_WEATHER;
        mCityList = getCityListFromDB();
        if (null == mCityList || 0 == mCityList.size()) {
            Log.i(TAG, "mCityList is useless");
            isUpdating = false;
            return;
        }
        if (mWeatherList == null) {
            mWeatherList = new ArrayList<Weather>();
        } else {
            mWeatherList.clear();
        }
        String lang = getLanguage();
        mcitySize = mCityList.size();
        for (int i = 0; i < mcitySize; i++) {
            City var = mCityList.get(i);
            boolean isAutoLocate = var.isAutoLocate();
            String locationKey = var.getLocationKey();
            String latitude = var.getLatitude();
            String longitude = var.getLongitude();

            Weather weather = new Weather();
            mWeatherList.add(i, weather);
            //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/01/2015,983895,[ergo5.2.8]delete the weather_24forecast_card
            if (isTwcWeather) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                updateHourlyWeather(i, locationKey, latitude, longitude, lang);
            }
            //[FEATURE]-Add-END by TSCD.peng.du
            updateCurrentWeather(i, locationKey, latitude, longitude, lang, false);
            //updateForcastWeather(locationKey,getLanguage());

            updateWeatherCityName(var, lang);
        }

    }

    public void updateAllWeather() {
        Log.i(TAG, "updateAllWeather");
        Log.i(TAG, "isUpdating : " + isUpdating);
        if (isUpdating) {
            Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
            it.putExtra("stopUpdating", true);
            sendBroadcast(it);
            return;
        }
        isUpdating = true;
        updateCityNameWrong = false;
        //updateCurrentOnly = false;
        updateGrade = UPDATE_ALL_WEATHER;
        updateCurrnetNum = 0;
        updateHourlyNum = 0;
        updateForecastNum = 0;
        updateCityNum = 0;
        mCityList = getCityListFromDB();
        if (null == mCityList || 0 == mCityList.size()) {
            Log.i(TAG, "mCityList is useless");
            isUpdating = false;
            return;
        }
        if (mWeatherList == null) {
            mWeatherList = new ArrayList<Weather>();
        } else {
            mWeatherList.clear();
        }

        String lang = getLanguage();
        mcitySize = mCityList.size();
        //updateCurrnetNum=cityArrayList.size();
        for (int i = 0; i < mcitySize; i++) {
            City var = mCityList.get(i);
            boolean isAutoLocate = var.isAutoLocate();
            String locationKey = var.getLocationKey();
            String latitude = var.getLatitude();
            String longitude = var.getLongitude();

            Weather weather = new Weather();
            mWeatherList.add(i, weather);

            updateCurrentWeather(i, locationKey, latitude, longitude, lang, false);
            //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/01/2015,983895,[ergo5.2.8]delete the weather_24forecast_card
            if (isTwcWeather) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                updateHourlyWeather(i, locationKey, latitude, longitude, lang);
            }
            //[FEATURE]-Add-END by TSCD.peng.du
            updateForcastWeather(i, locationKey, latitude, longitude, lang, false);

            updateWeatherCityName(var, lang);
        }

    }

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/23/2016,1443957,[Weather]It can't automatically recognize new site when move to new site
    public void updateAllWeatherWithAutoLocation() {
        Log.i(TAG, "updateAllWeatherWithAutoLocation");
        Log.i(TAG, "isUpdating : " + isUpdating);
        if (isUpdating) {
            Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
            it.putExtra("stopUpdating", true);
            sendBroadcast(it);
            return;
        }
        isUpdating = true;
        updateCityNameWrong = false;
        //updateCurrentOnly = false;
        updateGrade = UPDATE_ALL_WEATHER_AUTO_LOCATION;
        updateCurrnetNum = 0;
        updateHourlyNum = 0;
        updateForecastNum = 0;
        updateCityNum = 0;
        mCityList = getCityListFromDB();
        if (null == mCityList || 0 == mCityList.size()) {
            Log.i(TAG, "mCityList is useless");
            isUpdating = false;
            return;
        }
        if (mWeatherList == null) {
            mWeatherList = new ArrayList<Weather>();
        } else {
            mWeatherList.clear();
        }

        String lang = getLanguage();
        mcitySize = mCityList.size();
        for (int i = 0; i < mcitySize; i++) {
            Weather weather = new Weather();
            mWeatherList.add(i, weather);

            City var = mCityList.get(i);
            boolean isAutoLocate = var.isAutoLocate();

            if (isAutoLocate) {
                isUseBaiduAsDefault = CustomizeUtils.isUseBaiDuLocation(UpdateService.this, isMiddleManAvavible);
                AutoLocationThread autoLocationThread = new AutoLocationThread(var, i);
                autoLocationThread.start();
            } else {
                String locationKey = var.getLocationKey();
                String latitude = var.getLatitude();
                String longitude = var.getLongitude();
                updateCurrentWeather(i, locationKey, latitude, longitude, lang, false);
                //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/01/2015,983895,[ergo5.2.8]delete the weather_24forecast_card
                if (isTwcWeather) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                    updateHourlyWeather(i, locationKey, latitude, longitude, lang);
                }
                //[FEATURE]-Add-END by TSCD.peng.du
                updateForcastWeather(i, locationKey, latitude, longitude, lang, false);

                updateWeatherCityName(var, lang);
            }
        }
    }
    //[BUGFIX]-Add-END by TSCD.qian-li

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/20/2016,1313172,[Weather]weather not translate to russian
    public void updateAllWeatherWithLanguageChanged() {
        String currentLang = getLanguage();
        String preLang = SharePreferenceUtils.getInstance().getString(this, KEY_LAST_LANGUAGE, currentLang);
        Log.i(TAG, "preLang : " + preLang + ", currentLang : " + currentLang);
        if (!preLang.equals(currentLang)) {
            isLanguageChange = true;
            updateAllWeatherWithAutoLocation();
        }
    }
    //[BUGFIX]-Add-END by TSCD.qian-li

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/23/2016,1443957,[Weather]It can't automatically recognize new site when move to new site
    public class AutoLocationThread extends Thread {

        private City autolocateCity = null;
        private double latitude;
        private double longitude;
        private int index;

        public AutoLocationThread(City city, int index) {
            this.autolocateCity = city;
            this.index = index;
        }

        @Override
        public void run() {
            Log.i(TAG, "AutoLocationThread");
            if (autolocateCity != null) {
                boolean isUpdateSuccess = isUpdateSuccess();
                if (isUpdateSuccess) {
                    Location location = getBetterLocation();
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.e(TAG, "UpdateLocationThread:::latitude = " + latitude + ", longitude = " + longitude);
                    updateLocation(index, latitude, longitude, autolocateCity);
                } else {
                    String lang = getLanguage();
                    updateCurrentWeather(index, autolocateCity.getLocationKey(), autolocateCity.getLatitude(), autolocateCity.getLongitude(), lang, false);
                    updateForcastWeather(index, autolocateCity.getLocationKey(), autolocateCity.getLatitude(), autolocateCity.getLongitude(), lang, false);
                    if (isTwcWeather) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                        updateHourlyWeather(index, autolocateCity.getLocationKey(), autolocateCity.getLatitude(), autolocateCity.getLongitude(), lang);
                    }
                    updateWeatherCityName(autolocateCity, lang);
                }
            } else {
                //[FEATURE]-Add-BEGIN by TSCD.qian-li,01/27/2016,1532388,[Weather]MIE call weather interface to locate automaticly
                Log.i(TAG, "autolocateCity is null");
                boolean isUpdateSuccess = isUpdateSuccess();
                if (isUpdateSuccess) {
                    Location location = getBetterLocation();
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.e(TAG, "UpdateLocationThread:::latitude = " + latitude + ", longitude = " + longitude);
                    City city = findCity(latitude, longitude);
                    String currentLanguage = getLanguage();
                    if (city != null) {
                        city.setAutoLocate(true);
                        mDBHelper.insertCity(city);

                        Log.i(TAG, "autoLocationCity : " + city.toString());
                        mCityList.add(index, city);
                        if (isLanguageChange) {
                            updateCityNum++;
                            Log.i(TAG, "UpdateWeatherCityName, updateCityNum : " + updateCityNum);
                            if (updateCityNum >= mcitySize) {
                                mHandler.sendEmptyMessage(INSERT_CITY_INTO_DB);
                            }
                        }
                        updateCurrentWeather(index, city.getLocationKey(), city.getLatitude(), city.getLongitude(), currentLanguage, true);
                        updateForcastWeather(index, city.getLocationKey(), city.getLatitude(), city.getLongitude(), currentLanguage, true);
                        if (isTwcWeather) {
                            updateHourlyWeather(index, city.getLocationKey(), city.getLatitude(), city.getLongitude(), currentLanguage);
                        }
                    } else {
                        Log.e(TAG, "find city error");
                        isUpdating = false;
                        if (!mAutoUpdate) {
                            Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                            it.putExtra("connect_timeout", true);
                            sendBroadcast(it);
                        }
                    }
                } else {
                    Log.e(TAG, "isUpdateSuccess is false");
                    isUpdating = false;
                    if (!mAutoUpdate) {
                        Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                        it.putExtra("connect_timeout", true);
                        sendBroadcast(it);
                    }
                }
                //[FEATURE]-Add-END by TSCD.qian-li
            }
        }
    }
    //[BUGFIX]-Add-END by TSCD.qian-li


    private void updateCurrentWeather(final int index, final String locationKey, final String latitude, final String longitude, String lang, final boolean isAutoLocate) {
        if (isTwcWeather) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(urlBuilder.currentWeatherUrl(locationKey, latitude, longitude, lang), null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            Log.e(TAG, "current weather onResponse " + jsonObject.toString());
                            Currentconditions currentconditions = responseUtil.getCurrentWeather(jsonObject);

                            try {
                                Weather weather = mWeatherList.get(index);
                                weather.setCurrentconditions(currentconditions);
                                Local local = new Local();
                                local.setCityId(latitude + longitude);
                                local.setTime(System.currentTimeMillis() + "");
                                weather.setLocal(local);
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                            updateCurrnetNum++;
                            if (updateGrade == UPDATE_AUTO_LOCATION_WEATHER) {
                                mHandler.sendEmptyMessage(INSERT_CURRENT_WEATHER_INTO_DB);
                            } else if (updateCurrnetNum >= mWeatherList.size()) {
                                mHandler.sendEmptyMessage(INSERT_CURRENT_WEATHER_INTO_DB);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
                            //[BUGFIX]-Add-BEGIN by TSCD.peng.du,12/22/2015,1059954,
                            // [Weather] The refresh process have lasted >3 minutes
                            String error = volleyError.toString();
                            Log.e(TAG, "updateCurrentWeather" + error);
                            Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                            if ((null != error) && error.contains("TimeoutError")) {
                                it.putExtra("connect_timeout", true);
                            } else {
                                it.putExtra("connect_faild", true);
                            }
                            sendBroadcast(it);
                            //[BUGFIX]-Add-END by TSCD.peng.du
                            /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
                        }
                    });
            jsonObjectRequest.setShouldCache(false);
            requestQueue.add(jsonObjectRequest);
        } else {
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(urlBuilder.currentWeatherUrl(locationKey, latitude, longitude, lang),
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray jsonArray) {
                            Log.i(TAG, "current weather onResponse " + jsonArray.toString());
                            Currentconditions currentconditions = responseUtil.getCurrentWeather(jsonArray);

                            try {
                                Weather weather = mWeatherList.get(index);
                                weather.setCurrentconditions(currentconditions);
                                Local local = new Local();
                                local.setCityId(locationKey);
                                local.setTime(System.currentTimeMillis() + "");
                                weather.setLocal(local);
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                            updateCurrnetNum++;
                            Log.i(TAG, "UpdateCurrentWeather, updateCurrnetNum : " + updateCurrnetNum);
                            if (updateGrade == UPDATE_AUTO_LOCATION_WEATHER) {
                                mHandler.sendEmptyMessage(INSERT_CURRENT_WEATHER_INTO_DB);
                            } else if (updateCurrnetNum >= mcitySize) {
                                mHandler.sendEmptyMessage(INSERT_CURRENT_WEATHER_INTO_DB);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    //[BUGFIX]-Add-BEGIN by TSCD.peng.du,12/22/2015,1059954,
                    // [Weather] The refresh process have lasted >3 minutes
                    Log.e(TAG, "updateCurrnetWeather, onErrorResponse");
                    if (isAutoLocate) {
                        Log.i(TAG, "UpdateCurrentWeather, deleteCity : " + locationKey);
                        mDBHelper.deleteCity(locationKey);
                    }
                    updateCurrnetNum++;
                    Log.i(TAG, "UpdateCurrentWeather, updateCurrnetNum : " + updateCurrnetNum);
                    if (updateGrade == UPDATE_AUTO_LOCATION_WEATHER) {
                        isUpdating = false;
                    } else if (updateCurrnetNum >= mcitySize) {
                        mHandler.sendEmptyMessage(INSERT_CURRENT_WEATHER_INTO_DB);
                    }
                    /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
                    String error = volleyError.toString();
                    Log.e(TAG, volleyError.toString());
                    Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                    if ((null != error) && error.contains("TimeoutError")) {
                        it.putExtra("connect_timeout", true);
                    } else {
                    /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
                        it.putExtra("connect_faild", true);
                    }
                    sendBroadcast(it);
                    //[BUGFIX]-Add-END by TSCD.peng.du
                }
            });
            jsonArrayRequest.setShouldCache(false);
            requestQueue.add(jsonArrayRequest);
        }

    }

    private void updateHourlyWeather(final int index, final String locationKey, final String latitude, final String longitude, String lang) {
        if (isTwcWeather) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(urlBuilder.forcastHourlyWeatherUrl(locationKey, latitude, longitude, lang), null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            Log.i(TAG, "hourly weather onResponse " + jsonObject.toString());
                            List<Hour> hourList = responseUtil.getHourlyForecastWeather(jsonObject);
                            //Log.e(TAG, "hourly list= " + hourList.toString());

                            try {
                                Weather weather = mWeatherList.get(index);
                                weather.setHourList(hourList);
//                            Local local = new Local();
//                            local.setCityId(latitude + longitude);
//                            local.setTime(System.currentTimeMillis() + "");
//                            weather.setLocal(local);
                                weather.setHourlyLocationKey(latitude + longitude);
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                            updateHourlyNum++;
                            if (updateGrade == UPDATE_AUTO_LOCATION_WEATHER) {
                                mHandler.sendEmptyMessage(INSERT_HOURLY_WEATHER_INTO_DB);
                            } else if (updateHourlyNum >= mcitySize) {
                                mHandler.sendEmptyMessage(INSERT_HOURLY_WEATHER_INTO_DB);

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.e(TAG, volleyError.toString());
                        }
                    });
            jsonObjectRequest.setShouldCache(false);
            requestQueue.add(jsonObjectRequest);
        }
//[FEATURE]-Add-BEGIN by TSCD.peng.du,11/27/2015,983895,delete weather_24forecast_card
//        else {
//            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(urlBuilder.forcastHourlyWeatherUrl(locationKey, latitude, longitude, lang),
//                    new Response.Listener<JSONArray>() {
//                        @Override
//                        public void onResponse(JSONArray jsonArray) {
//                            Log.i(TAG, "hourly weather onResponse " + jsonArray.toString());
//                            List<Hour> hourList = responseUtil.getHourlyForecastWeather(jsonArray);
//                            //Log.e(TAG, "hourly list= " + hourList.toString());
//                            try {
//                                Weather weather = mWeatherList.get(updateHourlyNum);
//                                weather.setHourList(hourList);
////                            Local local = new Local();
////                            local.setCityId(latitude + longitude);
////                            local.setTime(System.currentTimeMillis() + "");
////                            weather.setLocal(local);
//                                weather.setHourlyLocationKey(locationKey);
//                            } catch (Exception e) {
//                                Log.e(TAG, e.toString());
//                            }
//                            updateHourlyNum++;
//                            if (updateGrade == 1) {
//                                mHandler.sendEmptyMessage(INSERT_HOURLY_WEATHER_INTO_DB);
//                            } else if (updateHourlyNum >= mcitySize) {
//                                mHandler.sendEmptyMessage(INSERT_HOURLY_WEATHER_INTO_DB);
//
//                            }
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError volleyError) {
//                    Log.e(TAG, volleyError.toString());
//                }
//            });
//            requestQueue.add(jsonArrayRequest);
//        }
//[FEATURE]-Add-END by TSCD.peng.du
    }

    private void updateForcastWeather(final int index, final String locationKey, final String latitude, final String longitude, String lang, final boolean isAutoLocate) {
        //Forecast forecast = new Forecast();
        //List<Day> dayList = ForecastWeatherRequest.getDailyForecastWeather(locationKey, lang);
        //final List<Day>[] dayList = new List[]{null};

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(urlBuilder.forecastDailyWeatherUrl(locationKey, latitude, longitude, lang), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.i(TAG, "forecast weather onResponse " + jsonObject.toString());
                        try {
                            List<Day> dayList = responseUtil.getDailyForecastWeather(jsonObject);
//                            Forecast forecast = new Forecast();
//                            forecast.setDays(dayList);

                            Weather weather = mWeatherList.get(index);
                            weather.setDayList(dayList);
//                            Local local = new Local();
//                            local.setCityId(latitude + longitude);
//                            local.setTime(System.currentTimeMillis() + "");
//                            weather.setLocal(local);
                            if (isTwcWeather) {
                                weather.setDailyLocationKey(latitude + longitude);
                            } else {
                                weather.setDailyLocationKey(locationKey);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                        updateForecastNum++;
                        Log.i(TAG, "UpdateForecasttWeather, updateForecastNum : " + updateForecastNum);
                        if (updateForecastNum >= mcitySize) {
                            mHandler.sendEmptyMessage(INSERT_DAILY_WEATHER_INTO_DB);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //[BUGFIX]-Add-BEGIN by TSCD.peng.du,12/22/2015,1059954,
                        // [Weather] The refresh process have lasted >3 minutes
                        Log.e(TAG, "updateForecastWeather, onErrorResponse");
                        if (isAutoLocate) {
                            Log.i(TAG, "UpdateForecasttWeather, deleteCity : " + locationKey);
                            mDBHelper.deleteCity(locationKey);
                        }
                        updateForecastNum++;
                        Log.i(TAG, "UpdateForecasttWeather, updateForecastNum : " + updateForecastNum);
                        if (updateForecastNum >= mcitySize) {
                            mHandler.sendEmptyMessage(INSERT_DAILY_WEATHER_INTO_DB);
                        }
                        /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
                        String error = volleyError.toString();
                        Log.e(TAG, volleyError.toString());
                        Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                        if ((null != error) && error.contains("TimeoutError")) {
                            it.putExtra("connect_timeout", true);
                        } else {
                        /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
                            it.putExtra("connect_faild", true);
                        }
                        sendBroadcast(it);
                        //[BUGFIX]-Add-END by TSCD.peng.du
                    }
                });
        jsonObjectRequest.setShouldCache(false);
        requestQueue.add(jsonObjectRequest);
    }

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/12/2016,1313172,[Weather]weather not translate to russian
    private void updateWeatherCityName(final City mCity, final String lang) {
        if (isLanguageChange) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(urlBuilder.findCityByLocationKey(mCity.getLocationKey(), lang, true), null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            Log.i(TAG, "weather city name onResponse " + jsonObject.toString());
                            responseUtil.getCityNameFromObject(mCity, jsonObject);
                            if (TextUtils.isEmpty(mCity.getCityName())) {
                                mCity.setCityName(mCity.getEnglishName());
                            }
                            updateCityNum++;
                            Log.i(TAG, "UpdateWeatherCityName, updateCityNum : " + updateCityNum);
                            if (updateCityNum >= mcitySize) {
                                mHandler.sendEmptyMessage(INSERT_CITY_INTO_DB);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.e(TAG, "updateWeatherCityName, onErrorResponse");
                            updateCityNum++;
                            updateCityNameWrong = true;
                            Log.i(TAG, "UpdateWeatherCityName, updateCityNum : " + updateCityNum);
                            if (updateCityNum >= mcitySize) {
                                mHandler.sendEmptyMessage(INSERT_CITY_INTO_DB);
                            }
                            /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
                            String error = volleyError.toString();
                            Log.e(TAG, volleyError.toString());
                            Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                            if ((null != error) && error.contains("TimeoutError")) {
                                it.putExtra("connect_timeout", true);
                            } else {
                            /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
                                it.putExtra("connect_faild", true);
                            }
                            sendBroadcast(it);
                        }
                    });
            jsonObjectRequest.setShouldCache(false);
            requestQueue.add(jsonObjectRequest);
        }
    }
    //[BUGFIX]-Add-END by TSCD.qian-li

    public ArrayList<City> getCityListFromDB() {
        if (mDBHelper == null) {
            mDBHelper = new DBHelper(this);
        }
        return mDBHelper.getCityListFromDB();
    }

    public List<City> getCityList() {
        return searchCityList;
    }

    public String getTempkeyByItem(int item) {
        searchCityList = getCityListFromDB();
        if (searchCityList == null || searchCityList.size() <= item) {
            return null;
        } else {
            return searchCityList.get(item).getLocationKey();
        }
    }

    public int getCurrentPosition(ArrayList<City> cityList, String tempKey) {
        int j = 0;
        for (int i = 0; i < cityList.size(); i++) {
            if (cityList.get(i).getLocationKey().equals(tempKey)) {
                j = i;
            }
        }
        return j;
    }

    public WeatherInfo getWeatherFromDB(String locationKey) {
        WeatherInfo weather = new WeatherInfo();
        weather.setWeatherForShow(mDBHelper.getWeatherForShow(locationKey));
        weather.setDayForShow(mDBHelper.getDayForShow(locationKey));
        weather.setHourList(mDBHelper.getHourList(locationKey));
        // delete by jielong.xing at 2015-3-19 begin
//		weather.setHours(mDBHelper.getHourForShow(locationKey));
        // delete by jielong.xing at 2015-3-19 end

        return weather;
    }

    public boolean requestLocationUpdate() {
        boolean noError = true;
        Log.e(TAG, "jielong_requestLocationUpdate "+mRequestUpdateSuccess +" mLocationManager="+mLocationManager); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
        if (!mRequestUpdateSuccess && isScreenOn() && mLocationManager != null
                && isLocationOpen() && !isAirModeOn()) {
            Log.e(TAG, "jielong_requestLocationUpdate");
            try {
                if (isNetworkProvideEnable && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, 10000, 1,
                            mNetworkListener);
                    isNetworkRequestOpen = true;
                }
                if (isGPSProvideEnable && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 10000, 1,
                            mGpsListener);
                    isGPSRequestOpen = true;
                }
            } catch (Exception e) {
                noError = false;
                Log.e(TAG, "requestLocationUpdates failed");
            } finally {
                if (isNetworkRequestOpen || isGPSRequestOpen) {
                    mRequestUpdateSuccess = true;
                    mLocationUpdated = false;
                }
            }
        } else if (isAirModeOn()) {
            mHandler.sendEmptyMessage(MSGREMOVELOCATIONREQUEST);
        }
        return noError;
    }

    private int mRemoveLocationUpdateRetryTimeCnt = 0; // if removelocateupdate caught exception, we can retry at most 3 time to try to removelocateupdate

    public void removeLocationUpdate() {
        if (mRequestUpdateSuccess) {
            Log.e(TAG, "jielong_removeLocationUpdate");
            try {
                if (isNetworkRequestOpen) {
                    if (mNetworkListener != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mLocationManager.removeUpdates(mNetworkListener);
                    }
                    isNetworkRequestOpen = false;
                }
                if (isGPSRequestOpen) {
                    if (mGpsListener != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mLocationManager.removeUpdates(mGpsListener);
                    }
                    isGPSRequestOpen = false;
                }
            } catch (Exception e) {
                Log.e(TAG, "removeLocationUpdate failed");
            } finally {
                if (isNetworkRequestOpen || isGPSRequestOpen) {
                    if (mRemoveLocationUpdateRetryTimeCnt > 2) {
                        mRequestUpdateSuccess = false;
                        mLocationUpdated = false;
                        mRemoveLocationUpdateRetryTimeCnt = 0;
                    } else {
                        mRemoveLocationUpdateRetryTimeCnt++;
                        mHandler.sendEmptyMessage(MSGREMOVELOCATIONREQUEST);
                    }
                } else {
                    mRequestUpdateSuccess = false;
                    mLocationUpdated = false;
                    mRemoveLocationUpdateRetryTimeCnt = 0;
                }
            }
        }
    }

    private boolean getConnectedStatus() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();

//        Log.i(TAG, "activeInfo.getExtraInfo()" + activeInfo.getExtraInfo() + "activeInfo.getReason()" +
//                activeInfo.getReason() + "activeInfo.getDetailedState()" + activeInfo.getDetailedState() +
//                "activeInfo.getTypeName()" + activeInfo.getTypeName() + "activeInfo.getState()" +
//                activeInfo.getState() + "activeInfo.getSubtype()" + activeInfo.getSubtype() + "activeInfo.getSubtypeName()" + activeInfo.getSubtypeName());
        boolean isConnected = false;

        if (activeInfo != null && activeInfo.isConnected()) {
            isWifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            isMobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if (!isWifiConnected && !isMobileConnected) {
                isOtherConnected = true;
            }
            isConnected = true;
        } else {
            isWifiConnected = false;
            isMobileConnected = false;
            isOtherConnected = false;
            isConnected = false;
        }

        Log.e(TAG, "jielong_isWifiConnected == " + isWifiConnected + ", isMobileConnected == " + isMobileConnected + ", isOtherConnected == " + isOtherConnected);
        return isConnected;
    }

    private boolean isLocationOpen() {
        isLocationOpen = (Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF);

        PackageManager pm = getApplicationContext().getPackageManager();
        isNetworkProvideEnable = (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) &&
                pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK));
        isGPSProvideEnable = (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS));

        Log.d(TAG, "jielong_isLocationOpen == " + isLocationOpen + ", isNetworkProvideEnable == " + isNetworkProvideEnable + ", isGPSProvideEnable == " + isGPSProvideEnable);
        return isLocationOpen;
    }

    private boolean isScreenOn() {
        PowerManager pw = (PowerManager) getSystemService(Context.POWER_SERVICE);
        isScreenOn = pw.isScreenOn();

        Log.d(TAG, "jielong_isScreenOn == " + isScreenOn);
        return isScreenOn;
    }

    //Begin add by jiajun.shen for 436489
    private boolean isAirModeOn() {
        int status = 0;
        try {
            status = Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (status == 1) {
            return true;
        } else {
            return false;
        }
    }
    //End add by jiajun.shen for 436489

    private boolean isAutoLocateInChinaArea() {
        return CustomizeUtils.getBoolean(UpdateService.this, "def_weather_use_baidumap");
    }

    private boolean isUseProvinceNameAsLocationName() {
        return CustomizeUtils.getBoolean(UpdateService.this, "def_weather_use_province_as_locationname");
    }

	/*private int getLanguageId() {
        int languageId = 0;
        String langcode = Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry();
        String[] langids = this.getResources().getStringArray(R.array.language);

        for (int i = 0; i < langids.length; i++) {
            if (langcode.contains(langids[i])) {
                languageId = i;
            }
        }
        languageId ++;

        return languageId;
    }*/

    private String getLanguage() {
        String langcode = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry().toLowerCase();
        String[] langids = this.getResources().getStringArray(R.array.language);
        for (int i = 0; i < langids.length; i++) {
            if (langcode.contains(langids[i])) {
                //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,04/08/2016,1872819,[Weather]
                if ("en".equals(langids[i])) {
                    return "en";
                } else {
                    return langcode;
                }
                //[BUGFIX]-Add-END by TSCD.xing.zhao
            }
        }
        //[BUGFIX]-Add-BEGIN by TSCD.xiangnan.zhou,02/26/2016,1692505,[Language][G01][Galician][Basque][Weather][ V5.2.8.1.0313.0]The interface of forecast with weather was no translate.
        //[BUGFIX]-Add-BEGIN by TSCD.peng.du,02/02/2016,1537921,[Weather]Can't add location in serbian
//        String lang = Locale.getDefault().getLanguage();
//        if (null == lang){
//            lang = "en-us";
//        }
        //[BUGFIX]-Add-END by TSCD.peng.du
        //[BUGFIX]-Add-END by TSCD.xiangnan.zhou
        return "en";
    }

    private String getDistributeName(double latitude, double longitude) {
        String distribute = null;
        String baidulocation = "location=" + latitude + "," + longitude;
        String outputformat = "output=json";
        String tclkey = "key=" + TCLREGISTERKEY;
        String baiduUrl = URL_BAIDU_GEOCODER + baidulocation + "&" + outputformat + "&" + tclkey;
        Log.i(TAG, "getDistributeName url : " + baiduUrl);// MODIFIED by peng.du, 2016-03-22,BUG-1842312

//        try {
        /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
        BasicHttpParams httpParameters = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
        HttpConnectionParams.setSoTimeout(httpParameters, 20000);
        HttpClient httpClient = new DefaultHttpClient(httpParameters);

        HttpGet httpRequest = new HttpGet(baiduUrl);
        httpRequest.addHeader("Accept-Encoding", "gzip");
        /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
/* MODIFIED-BEGIN by peng.du, 2016-03-22,BUG-1842312 */
//            httpRequest.addHeader("Content-Length", String.valueOf(580));

        try {
            HttpResponse response = httpClient.execute(httpRequest);
            int ret = response.getStatusLine().getStatusCode();
            if (ret == 200) {
                //[BUGFIX]-Add-BEGIN by TSCD.peng.du,03/22/2016,1842312,[REG][Weather][V5.2.8.1.0328.0_0316]The weather can't get weather data during auto fixed position
                Header[] headers = response.getHeaders("Content-Encoding");
                String strEntity = null;
                if (headers != null && headers.length > 0 && headers[0].getValue().toLowerCase().equals("gzip")) {
                    InputStream is = new GZIPInputStream(response.getEntity().getContent());
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line = null;
                    StringBuffer sb = new StringBuffer();
                    while ((line = br.readLine()) != null) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                        sb.append(line);
                    }
                    strEntity = sb.toString();
                } else {
                    strEntity = EntityUtils.toString(response.getEntity());
                }
                //[BUGFIX]-Add-END by TSCD.peng.du
                /* MODIFIED-END by peng.du,BUG-1842312 */

//                String strEntity = EntityUtils.toString(response.getEntity());
                Log.d(TAG, "sendFindCityRequest strEntity: " + strEntity);
                JSONObject json = new JSONObject(strEntity);
                if (json != null) {
                    JSONObject resultJson = json.getJSONObject("result");

                    if (resultJson == null) {
                        Log.e(TAG, "result json=null");
                        return null;
                    }
                    JSONObject conponentJson = resultJson.getJSONObject("addressComponent");
                    if (conponentJson != null) {
                        distribute = conponentJson.getString("district");
                    } else {
                        Log.e(TAG, "conponentJson==null");
                        return null;
                    }
                }
            } else {
                Log.i(TAG, "response status code is " + ret);// MODIFIED by peng.du, 2016-03-22,BUG-1842312
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().closeExpiredConnections();
                httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.SECONDS);
                httpClient.getConnectionManager().shutdown();
            }
        }

        return distribute;
    }

    private City findCity(double latitude, double longitude) {
        String distributeName = null;
//		int languageId = getLanguageId();
        String lang = getLanguage();
        if (isAutoLocateInChinaArea() || "zh-tw".equals(lang)) {
            distributeName = getDistributeName(latitude, longitude);
            Log.i(TAG, "distributeName : " + distributeName);

            //[BUGFIX]-Add-BEGIN by TSCD.peng.du,03/22/2016,1842312,[REG][Weather][V5.2.8.1.0328.0_0316]The weather can't get weather data during auto fixed position
//            if (distributeName == null) {
//                return null;
//            }
            //[BUGFIX]-Add-END by TSCD.peng.du
        }

//		String location = "latitude=" + latitude + "&longitude=" + longitude;
//		String searchURL = (URL_CITY_FIND + location + "&langid=" + languageId).trim();
//		String searchURLNoId = (URL_CITY_FIND + location).trim();

        try {

            String location = latitude + "," + longitude;
            List<City> citys;
            if (isTwcWeather) {
                citys = CityFindRequest.findCityByGeoLocation(location, lang, true, true);
            } else {
                citys = CityFindRequest.findCityByGeoLocation(location, lang, true, false);
            }

            /*InputStream mInputStream = downloadUrl(searchURL).getInputStream();
            List<City> citys = CityFindParser.parse(mInputStream);
			mInputStream.close();*/

            if (null != citys && citys.size() != 0) {
                City city = citys.get(0);
                if (TextUtils.isEmpty(city.getCityName())) {
//					InputStream mNoIdInputStream = downloadUrl(searchURLNoId).getInputStream();
//					List<City> mCitys = CityFindParser.parse(mNoIdInputStream);
//					mNoIdInputStream.close();
                    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/29/2016,1537191,[3rd APK][Weather v5.2.8.1.0313.0_video]Weather condition and location can't sync change unless click "Refresh"  when change system language
                    if (TextUtils.isEmpty(city.getEnglishName())) {
                        List<City> mCitys;
                        if (isTwcWeather) {
                            mCitys = CityFindRequest.findCityByGeoLocation(location, lang, false, true);
                        } else {
                            mCitys = CityFindRequest.findCityByGeoLocation(location, lang, false, false);
                        }

                        if (mCitys == null) {
                            return null;
                        }
                        if (mCitys.size() != 0) {
                            city = mCitys.get(0);
                        }
                        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/29/2016,1537191,[3rd APK][Weather v5.2.8.1.0313.0_video]Weather condition and location can't sync change unless click "Refresh"  when change system language // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                    } else {
                        city.setCityName(city.getEnglishName());
                    }
                    //[BUGFIX]-Add-END by TSCD.qian-li
                }

                if (city != null && distributeName != null) {
                    if (!"".equals(distributeName.trim())) {
                        city.setCityName(distributeName);
                    }
                }

                if (city != null && isUseProvinceNameAsLocationName()) {
                    city.setCityName(city.getState());
                }
                return city;
            }

        } catch (Exception e) {
            Log.e(TAG, "CityFindGPSThread" + e.getMessage());
        }
        return null;
    }

    public void setUpdateManue() {
        mAutoUpdate = false;
    }

    public void autoUpdate() {
        Log.i(TAG, "autoUpdate");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String updateVal = settings.getString(AUTO_UPDATE_KEY, "0");
        if (TextUtils.equals(updateVal, "0")) {
            autoUpdate = true;
        } else {
            autoUpdate = false;
        }

        long time = System.currentTimeMillis();
        long lastRefreshTime = SharePreferenceUtils.getInstance().getLong(UpdateService.this, KEY_LAST_REFRESHTIME, -1);
        if (lastRefreshTime == -1 || isFirstUse) {
            Log.i(TAG, "lastRefreshTime is -1");
            return;
        }
        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/01/2016,1539804,[Weather][REG]The refresh time is error when Cellular Data or wifi is disconnect
        getConnectedStatus();
        Log.i(TAG, "autoUpdate : " + autoUpdate + ", isWifiConnected : " + isWifiConnected + ", isMobileConnected : " + isMobileConnected);
//        if (autoUpdate || isWifiConnected) {
        if ((autoUpdate && (isWifiConnected || isMobileConnected)) || (!autoUpdate && isWifiConnected)) {
            //[BUGFIX]-Add-END by TSCD.qian-li
            //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/23/2016,1443957,[Weather]It can't automatically recognize new site when move to new site
            Log.i(TAG, "autoUpdate time - lastRefreshTime=" + (time - lastRefreshTime));
            if (lastRefreshTime == -1 || time - lastRefreshTime >= FOURHOUR) {
                updateAllWeatherWithAutoLocation();
//                updateAllWeather();
            } else if (lastRefreshTime == -1 || time - lastRefreshTime >= THREEHOUR) {
                updateAllCurrentWeather();
            } else if (lastRefreshTime == -1 || time - lastRefreshTime >= TWOHOUR) {
                updateAllWeather();
            } else if (lastRefreshTime == -1 || time - lastRefreshTime >= ONEHOUR) {
                updateAllCurrentWeather();
//                updateAutoLocateWeather();
            }
            //[BUGFIX]-Add-END by TSCD.qian-li
        }
    }


//    public void checkUpdate() {
//        // add by jielong.xing for power consumption high issue at 2015-3-18
//        if (DEBUG) {
//            Log.e("isCanRunUpdateThread", "isAutoLocateFailed == " + isAutoLocateFailed + ", mAutoLocateFailedCnt == " + mAutoLocateFailedCnt + ", mIntervalCnt == " + mIntervalCnt + ", mHourIntervalCnt == " + mHourIntervalCnt);
//            Log.e(TAG, "jielong_mRequestUpdateSuccess == " + mRequestUpdateSuccess);
//        }
//        boolean isScreenOn = isScreenOn();
//        if (!isScreenOn) {
//            if (isAutoLocateFailed) {
//                if (mAutoLocateFailedCnt < CONNECTRETRYMAXTIME) {
//                    if (mIntervalCnt > 0 && mIntervalCnt < 5) {
//                        mIntervalCnt++;
//                    }
//                }
//            }
//        }
//
//        // add by jielong.xing for power consumption high issue at 2015-3-18
//        if (getConnectedStatus() && isScreenOn) {
//            long time = System.currentTimeMillis();
//
//            List<City> citys = getCityListFromDB();
//            boolean isUpdate = false;
//            boolean updateAutoLocationCityOnly = false;
//            if (citys != null && citys.size() != 0) {
//                long lastRefreshTime = SharePreferenceUtils.getLong(UpdateService.this, KEY_LAST_REFRESHTIME, -1);
//                if (lastRefreshTime == -1 || time - lastRefreshTime > TWOHOUR) {
//                    // add by jielong.xing for power consumption high issue at 2015-3-18 begin
//                    if (DEBUG) {
//                        Log.e("isCanRunUpdateThread", "more than two hours");
//                    }
//                    resetRetryData();
//                    // add by jielong.xing for power consumption high issue at 2015-3-18 end
//                    isUpdate = true;
//                    updateAutoLocationCityOnly = false;
//                } else if (isLocationOpen()) {
//                    long autoLocateTime = -1;
//                    autoLocateTime = SharePreferenceUtils.getLong(UpdateService.this, KEY_LAST_AUTOLOCATETIME, -1);
//
//                    if (autoLocateTime == -1 || time - autoLocateTime > ONEHOUR) {
//                        // add by jielong.xing for power consumption high issue at 2015-3-18 begin
//                        if (DEBUG) {
//                            Log.e("isCanRunUpdateThread", "more than one hour");
//                        }
//                        boolean flag = isCanRunUpdateThread();
//                        if (!flag) {
//                            return;
//                        }
//                        // add by jielong.xing for power consumption high issue at 2015-3-18 end
//
//                        isUpdate = true;
//                        updateAutoLocationCityOnly = true;
//                    }
//                }
//            } else {
//                // add by jielong.xing for power consumption high issue at 2015-3-18 begin
//                if (DEBUG) {
//                    Log.e("isCanRunUpdateThread", "no city");
//                }
//                boolean flag = isCanRunUpdateThread();
//                if (!flag) {
//                    return;
//                }
//                // add by jielong.xing for power consumption high issue at 2015-3-18 end
//                isUpdate = true;
//                updateAutoLocationCityOnly = false;
//            }
//
//            if (isUpdate) {
//                // if location update is ongoing, don't duplicate do the update action.
//                if (mRequestUpdateSuccess) {
//                    return;
//                }
//                mAutoUpdate = true;
//                updateAllWeather();
////                UpdateWeatherThread thread = new UpdateWeatherThread();
////                thread.setUpdateAutoLocationCityOnly(updateAutoLocationCityOnly);
////                thread.start();
//            }
//        }
//    }

    public void insertCity(City city, boolean isAutoLocate) {
        city.setAutoLocate(isAutoLocate);
        mDBHelper.insertCity(city);
        if (!city.getLocationKey().startsWith(POSTALCODE)) {
            mDBHelper.insertCity(city);
        } else {
            tempCity = city;
        }

        sendWeatherDataRequest(city.getLocationKey(), city.getLatitude(), city.getLongitude());
    }

    // Send the weather data request
    public void sendWeatherDataRequest(String locationKey, String latitude, String longitude) {
        WeatherDataThread weatherDataThread = new WeatherDataThread();
        weatherDataThread.locationKey = locationKey;
        weatherDataThread.latitude = latitude;
        weatherDataThread.longitude = longitude;
        if (locationKey.startsWith(POSTALCODE)) {
            weatherDataThread.isCityId = false;
        } else {
            weatherDataThread.isCityId = true;
        }

        weatherDataThread.start();
    }

    // Parse the xml returned from weather-data, and insert it into database.
    private class WeatherDataThread extends Thread {
        public boolean isCityId = true;

        public String locationKey = null;
        public String latitude = null;
        public String longitude = null;

        @Override
        public void run() {
            try {
                String lang = getLanguage();
                Weather weather = getWeather(locationKey, latitude, longitude, lang);
                if (weather == null) {
                    deleteCity(locationKey);
                    Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                    it.putExtra("connect_timeout", true);
                    sendBroadcast(it);
                    return;
                }

                if (!isCityId) {
                    //parse the location key from weather
                    tempCity.setLocationKey(locationKey);
                    mDBHelper.insertCity(tempCity);
                    tempCity = null;
                }
                insertWeatherIntoDB(locationKey, weather, true);

            } catch (Exception e) {
                Log.e(TAG, "WeatherDataThread Exception error = " + e.toString()); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                it.putExtra("connect_timeout", true);
                sendBroadcast(it);
            }
        }
    }

    // Send the request of city-find.
    // Fixed PR1022056 by jielong.xing at 2015-6-18, add param reqId
    public void sendCityFindRequest(final String cityName, final String reqId, final boolean isPostal) {
        String lang = getLanguage();
        String url = null;
        //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,12/31/2015,1175818,[Weather]Weather APK  can't  auto match city by zip code
        try {
            if (isPostal) {
                url = urlBuilder.findCityByPostal(URLEncoder.encode(cityName, "utf-8"), lang, true);
            } else {
                url = urlBuilder.findCityByName(URLEncoder.encode(cityName, "utf-8"), lang, true);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //[BUGFIX]-Add-END by TSCD.xing.zhao
        if (isTwcWeather) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            Log.i(TAG, "city find request respose=" + jsonObject.toString());
                            List<City> tempCityList;
                            tempCityList = responseUtil.getCityList(jsonObject);
                            Log.i(TAG, "citylist=" + tempCityList.toString());
                            if (tempCityList == null) {
                                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                                it.putExtra("connect_timeout", true);
                                sendBroadcast(it);
                                return;
                            }

                            if (tempCityList.size() == 0) {
                                Intent i = new Intent("android.intent.action.CITY_BROADCAST");
                                i.putExtra("city", false);
                                sendBroadcast(i);
                            } else {
                                searchCityList = tempCityList;
                                Intent i = new Intent("android.intent.action.CITY_BROADCAST");
                                i.putExtra("city", true);
                                // Fixed PR1022056 by jielong.xing at 2015-6-18 begin
                                i.putExtra("reqId", reqId);
                                // Fixed PR1022056 by jielong.xing at 2015-6-18 end
                                sendBroadcast(i);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.e(TAG, "city find request error::" + volleyError);
                            Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                            it.putExtra("connect_timeout", true);
                            sendBroadcast(it);
                        }
                    });
            jsonObjectRequest.setShouldCache(false);
            requestQueue.add(jsonObjectRequest);
        } else {
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray jsonArray) {
                            Log.i(TAG, "city find request respose=" + jsonArray.toString());
                            List<City> tempCityList;
                            tempCityList = responseUtil.getCityListFromArray(jsonArray);
                            Log.i(TAG, "citylist=" + tempCityList.toString());
                            try {
                                if (jsonArray != null && jsonArray.length() > 0) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                                    //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,01/22/2016,1487598,[Weather]When insert uninvaild number,There haven't any prompt
                                    if (isPostal) {
                                        String postalCode = cityName;
                                        postalCode = jsonObject.getString("PrimaryPostalCode");
                                        if (isPostal && !cityName.equals(postalCode)) {
                                            tempCityList.clear();
                                        }
                                    }
                                    //[BUGFIX]-Add-END by TSCD.xing.zhao
                                }
                            } catch (JSONException e) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                                Log.e(TAG, e.getMessage());
                            }
                            //[BUGFIX]-Add-END by TSCD.xing.zhao
                            if (tempCityList == null) {
                                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                                it.putExtra("connect_timeout", true);
                                sendBroadcast(it);
                                return;
                            }

                            if (tempCityList.size() == 0) {
                                Intent i = new Intent("android.intent.action.CITY_BROADCAST");
                                i.putExtra("city", false);
                                sendBroadcast(i);
                            } else {
                                searchCityList = tempCityList;
                                Intent i = new Intent("android.intent.action.CITY_BROADCAST");
                                i.putExtra("city", true);
                                // Fixed PR1022056 by jielong.xing at 2015-6-18 begin
                                i.putExtra("reqId", reqId);
                                // Fixed PR1022056 by jielong.xing at 2015-6-18 end
                                sendBroadcast(i);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.e(TAG, "city find request error::" + volleyError);
                            Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                            it.putExtra("connect_timeout", true);
                            sendBroadcast(it);
                        }
                    });
            jsonArrayRequest.setShouldCache(false);
            requestQueue.add(jsonArrayRequest);
        }
    }

    // Parse the city information from AccuWeather.
//    private class CityFindThread extends Thread {
//        public String cityName = null;
//        // Fixed PR1022056 by jielong.xing at 2015-6-18 begin
//        public String reqId = null;
//        // Fixed PR1022056 by jielong.xing at 2015-6-18 end
//        public boolean isPostal = false;
//
//        @Override
//        public void run() {
//            try {
//                String lang = getLanguage();
//                List<City> tempCityList;
//                //Begin modified by jiajun.shen for 1045406
//                if (isPostal) {
//                    tempCityList = CityFindRequest.findCityByPostal(URLEncoder.encode(cityName, "utf-8"), lang, true);
//                } else {
//                    tempCityList = CityFindRequest.findCityByName(URLEncoder.encode(cityName, "utf-8"), lang, true);
//                }
//                //End modified by jiajun.shen for 1045406
//
//				/*int languageId = getLanguageId();
//                String searchUrl = new StringBuilder(URL_CITY_FIND).append("location=" + URLEncoder.encode(cityName, "utf-8")
//						+ "&langid=" + languageId).toString().trim();
//
//				InputStream mInputStream = downloadUrl(searchUrl).getInputStream();
//				List<City> tempCityList = CityFindParser.parse(mInputStream);
//				mInputStream.close();*/
//                if (tempCityList == null) {
//                    Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
//                    it.putExtra("connect_timeout", true);
//                    sendBroadcast(it);
//                    return;
//                }
//
//                if (tempCityList.size() == 0) {
//                    Intent i = new Intent("android.intent.action.CITY_BROADCAST");
//                    i.putExtra("city", false);
//                    sendBroadcast(i);
//                } else {
//                    searchCityList = tempCityList;
//                    Intent i = new Intent("android.intent.action.CITY_BROADCAST");
//                    i.putExtra("city", true);
//                    // Fixed PR1022056 by jielong.xing at 2015-6-18 begin
//                    i.putExtra("reqId", reqId);
//                    // Fixed PR1022056 by jielong.xing at 2015-6-18 end
//                    sendBroadcast(i);
//                }
//            } catch (UnsupportedEncodingException e) {
//                Log.e(TAG, "CityFindThread__UnsupportedEncodingException::" + e.getMessage());
//                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
//                it.putExtra("connect_timeout", true);
//                sendBroadcast(it);
//                return;
//            } catch (Exception e) {
//                Log.e(TAG, "CityFindThread:" + e.getMessage());
//                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
//                it.putExtra("connect_timeout", true);
//                sendBroadcast(it);
//            }
//        }
//    }

    private boolean isUpdateSuccess() {
        boolean updateSuccess = false;
        /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
        Log.i("isUpdateSuccess", "isLocationOpen()=" + isLocationOpen() + " isUseBaiduAsDefault=" + isUseBaiduAsDefault+" isScreenOn()=="+isScreenOn());
//        if (isLocationOpen()) {
            try {
                if (isUseBaiduAsDefault) {
                    mHandler.sendEmptyMessage(START_BAIDU_LOCATE);
                } else {
                    if (isLocationOpen()) {
                        mHandler.sendEmptyMessage(MSGOPENLOCATIONREQUEST);
                        // wait 500ms for handler message handle
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                        }
                        if (!mRequestUpdateSuccess) {
                            return false;
                        }
                    } else {
                        if (isScreenOn()) {
                            Intent it = new Intent("com.tct.weather.shownolocationsnackbar");
                            sendBroadcast(it);
                            return updateSuccess;
                        }
                        /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
                    }
                }

                for (int i = 0; i < RETRYTIME; i++) {
                    if (!mLocationUpdated) {
                        if (!isNetworkConnected) {
                            break;
                        }
                        try {
                            Thread.sleep(3000);
                        } catch (Exception e) {
                        }
                    } else {
                        updateSuccess = true;
                        mLocationUpdated = false;
                        break;
                    }
                }

            } finally {
                if (isUseBaiduAsDefault) {
                    mHandler.sendEmptyMessage(STOP_BAIDU_LOCATE);
                } else {
                    mHandler.sendEmptyMessage(MSGREMOVELOCATIONREQUEST);
                }
            }
        return updateSuccess;
    }

    private boolean insertCityFunc(City city, boolean needDelete) {
        boolean autolocateSuccess = false;
        mDBHelper.insertCity(city);
        boolean ret = insertCityBlock(city.getLocationKey(), city.getLatitude(), city.getLongitude());
        if (ret) {
            autolocateSuccess = true;
        } else {
            if (needDelete) {
                deleteCity(city.getLocationKey());
            }
        }
        return autolocateSuccess;
    }

    //if the autolocate city needs to update,return true,else return false
    private boolean needReplaceCity(City newCity, City oldCity) {
        if (newCity == null || newCity.getLocationKey() == null) {
            return false;
        }
        if (oldCity == null) {
            return true;
        }

        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/01/2016,1539263,[jrdlogger]com.tct.weather ANR
        String newCityLocationKey = newCity.getLocationKey();
        boolean ret = false;

        if (!newCityLocationKey.equals(oldCity.getLocationKey())) {
            ret = true;
        }
        //[BUGFIX]-Add-END by TSCD.qian-li
        return ret;
    }

    public void deleteCity(String locationKey) {
        mDBHelper.deleteCity(locationKey);

        // added for launcher dynamical icon at 2015-5-20 begin
        List<City> cityList = getCityListFromDB();
        if (cityList == null || cityList.size() == 0) {
            SharePreferenceUtils.getInstance().clearCityWeatherInfo(UpdateService.this);
        } else {
            String firstCityKey = cityList.get(0).getLocationKey();
            WeatherInfo weather = getWeatherFromDB(firstCityKey);
            WeatherForShow weatherForShow = weather.getWeatherForShow();
            if (null != weatherForShow) {
                SharePreferenceUtils.getInstance().saveCityWeatherInfo(UpdateService.this, firstCityKey, CommonUtils.c2f(weatherForShow.getTemp()), weatherForShow.getIcon());
            } else {
                Log.e(TAG, "deleteCity weather info is null");
            }
        }
        // added for launcher dynamical icon at 2015-5-20 end

        Intent it = new Intent("android.intent.action.DELETE_CITY");
        sendBroadcast(it);

        Intent i = new Intent(this, MiniWeatherWidgetService.class);
        i.setAction("android.action.deletecity");
        startService(i);
        // added by tingma for start WeatherWidgetService begin
        Intent intent = new Intent(this, WeatherTimeWidgetService.class); //MODIFIED by qian-li, 2016-04-13,BUG-1940875
        intent.setAction("android.action.deletecity");
        startService(intent);
        // added by tingma for start WeatherWidgetService end
    }

    public boolean insertCityBlock(String locationkey, String latitude, String longitude) {
        return insertCityBlock(locationkey, latitude, longitude, false);
    }

    /**
     * @param locationkey
     * @author hongjun.tang
     */
    public boolean insertCityBlock(String locationkey, String latitude, String longitude, boolean manu) {
        boolean result = false;
        try {
            String locationKey2 = locationkey;
            if (locationKey2 != null && locationKey2.startsWith(CITYID)) {
                locationKey2 = locationKey2.substring(CITYID.length());
            }
            Weather weather = getWeather(locationKey2, latitude, longitude, getLanguage());
            if (weather == null) {
                return false;
            }

            insertWeatherIntoDB(locationkey, weather, manu);
            result = true;
        } catch (SocketException e) {
            /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
            Log.e(TAG, "WeatherDataThread SocketException error = " + e.toString());
            if (!mAutoUpdate) {
                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                it.putExtra("connect_timeout", true);
                sendBroadcast(it);
            }
        } catch (Exception e) {
            Log.e(TAG, "WeatherDataThread Exception error = " + e.toString());
            /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
            if (!mAutoUpdate) {
                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                it.putExtra("connect_timeout", true);
                sendBroadcast(it);
            }
        }

        return result;
    }

    private void insertWeatherIntoDB(String locationKey, Weather weather, boolean manu) {
        mDBHelper.updateWeather(locationKey, weather);

        if (locationKey != null && locationKey.startsWith(CITYID)) {
            locationKey = locationKey.substring(CITYID.length());
        }

        ArrayList<City> cityList = getCityListFromDB();

        Intent i = new Intent("android.intent.action.WEATHER_BROADCAST");
        i.putExtra("weather", true);
        i.putExtra("manu", manu);
        i.putExtra("city_count", cityList.size());
        i.putExtra("location_key", locationKey);
        sendBroadcast(i);

        // added for launcher dynamical icon at 2015-5-20 begin
        if (cityList != null && cityList.size() > 0) {
            String firstCityKey = cityList.get(0).getLocationKey();
            if (firstCityKey != null && firstCityKey.equals(locationKey)) {
                WeatherForShow weatherForShow = weather.getWeatherForShow();
                Log.d(TAG, "insertWeatherIntoDB() weather locationKey = " + locationKey + ", temperature = " + weatherForShow.getTemp() + ", icon = " + weatherForShow.getIcon());
                SharePreferenceUtils.getInstance().saveCityWeatherInfo(UpdateService.this, locationKey, CommonUtils.c2f(weatherForShow.getTemp()), weatherForShow.getIcon());
                Intent mieIntent = new Intent("com.tct.weather.MIE_SYNC");
                mieIntent.putExtra("locationKey", locationKey);
                sendBroadcast(mieIntent);
            }
        }
        // added for launcher dynamical icon at 2015-5-20 end

        long lastRefreshTime = SharePreferenceUtils.getInstance().getLong(UpdateService.this, KEY_LAST_REFRESHTIME, -1);
        if (lastRefreshTime == -1) {
            SharePreferenceUtils.getInstance().saveLong(UpdateService.this, KEY_LAST_REFRESHTIME, System.currentTimeMillis());
        }
    }

//    public void updateWeather2() {
//        UpdateWeatherThread thread = new UpdateWeatherThread();
//        thread.setUpdateAutoLocationCityOnly(false);
//        thread.start();
//    }
//
//    private class UpdateWeatherThread extends Thread {
//        private boolean updateAutoLocationCityOnly = false;
//
//        public void setUpdateAutoLocationCityOnly(boolean updateAutoLocationCityOnly) {
//            this.updateAutoLocationCityOnly = updateAutoLocationCityOnly;
//        }
//
//        @Override
//        public void run() {
//            // added for launcher dynamical icon at 2015-5-20 begin
//            boolean isSendSyncBroadcast = false;
//            String firstCityKey = "";
//            // added for launcher dynamical icon at 2015-5-20 end
//
//            try {
//                isUpdateWeatherThreadRunning = true;
//                boolean autolocateSuccess = false;
//                double latitude = -1;
//                double longitude = -1;
//                boolean updateSuccess = isUpdateSuccess();
//                if (!isNetworkConnected) {
//                    throw new Exception();
//                }
//                if (!updateSuccess) {
//                    // add by jielong.xing for power consumption high issue at 2015-3-18 begin
//                    if (mAutoUpdate) {
//                        isAutoLocateFailed = true;
//                        mAutoLocateFailedCnt++;
//                    }
//                    // add by jielong.xing for power consumption high issue at 2015-3-18 end
//                    Log.e(TAG, "jielong_UpdateWeatherThread::fail to get location");
//                    /*if (!mAutoUpdate) {
//                        Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
//						it.putExtra("connect_timeout", true);
//						sendBroadcast(it);
//					}*/
//                } else {
//                    Location location = getBetterLocation();
//
//                    latitude = location.getLatitude();
//                    longitude = location.getLongitude();
//                    Log.e(TAG, "jielong_UpdateWeatherThread:::latitude = " + latitude + ", longitude = " + longitude);
//
//                    //Defect 212555 Outdoor auto location is failed by bing.wang.hz begin
//                    if (mIsFirstLocationTry) {
//                        SharePreferenceUtils.saveBoolean(UpdateService.this, CommonUtils.FIRST_LOCATION_TRY, false);
//                        mIsFirstLocationTry = false;
//                    }
//                    //Defect 212555 Outdoor auto location is failed by bing.wang.hz end
//                }
//
//                boolean updateWeatherOnly = !isLocationOpen;
//                if (!mAutoUpdate && !updateSuccess) {
//                    updateWeatherOnly = true;
//                }
//
//                City autolocateCity = null;
//                long lastAutoLocateTime = -1;
//                int citySize = 0;
//                ArrayList<City> cityList = getCityListFromDB();
//                if (cityList != null) {
//                    citySize = cityList.size();
//                }
//                // added for launcher dynamical icon at 2015-5-20 begin
//                if (citySize > 0) {
//                    firstCityKey = cityList.get(0).getLocationKey();
//                }
//                // added for launcher dynamical icon at 2015-5-20 end
//
//
//                for (int i = 0; i < citySize; i++) {
//                    City var = cityList.get(i);
//                    boolean isAutoLocate = var.isAutoLocate();
//                    String locationKey = var.getLocationKey();
//                    String locationKey2 = locationKey;
//                    Log.w(CommonUtils.TAG_BING, "---!updateAutoLocationCityOnly, isAutoLocation " + isAutoLocate + " updateSuccess " + updateSuccess);
//                    if (!updateAutoLocationCityOnly && (updateWeatherOnly || !isAutoLocate || (isAutoLocate && !updateSuccess))) {
//                        Log.w(CommonUtils.TAG_BING, "---!updateAutoLocationCityOnly, isAutoLocation " + isAutoLocate);
//                        if (locationKey2 != null && locationKey2.startsWith(CITYID)) {
//                            locationKey2 = locationKey2.substring(CITYID.length());
//                            mDBHelper.updateCity(locationKey, locationKey2);
//                        }
//                        Weather weather = getWeather(locationKey2, getLanguage());
//                        if (weather == null) {
//                            continue;
//                        }
//                        mDBHelper.updateWeather(locationKey2, weather);
//                        mDBHelper.updateCityTimeByLocationKey(locationKey2);
//                    } else if (isAutoLocate) {
//                        autolocateCity = var;
//                        lastAutoLocateTime = Long.parseLong(var.getUpdateTime());
//                        Log.e("xingjl", "updateAutoLocationCityOnly == " + updateAutoLocationCityOnly + ", mAutoLocateFailedCnt == " + mAutoLocateFailedCnt + ", locationKey == " + locationKey);
//                        if (!updateSuccess && mAutoLocateFailedCnt >= CONNECTRETRYMAXTIME) {
//                            Weather weather = getWeather(locationKey, getLanguage());
//                            if (weather == null) {
//                                continue;
//                            }
//                            Log.e("xingjl", "maybe one hour, update autolocation weather data");
//                            mDBHelper.updateWeather(locationKey, weather);
//                            mDBHelper.updateCityTimeByLocationKey(locationKey);
//
//                            if (updateAutoLocationCityOnly) {
//                                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
//                                it.putExtra("weather", true);
//                                it.putExtra("locationerror", true);
//
//                                sendBroadcast(it);
//                            }
//                            // added for launcher dynamical icon at 2015-5-20 begin
//                            isSendSyncBroadcast = true;
//                            // added for launcher dynamical icon at 2015-5-20 end
//                        }
//                    }
//                }
//
//                if (updateSuccess) {
//                    City city = findCity(latitude, longitude);
//
//                    if (city != null) {
//                        city.setAutoLocate(true);
//                        if (autolocateCity != null) {
//                            // new autolocate city replace the old autolocte city
//                            if (needReplaceCity(city, autolocateCity)) {
//
//                                //PR941173 auto-location entry is somehow moved to end of city list by ting.ma at 2015-03-06 begin
//                                //deleteCity(autolocateCity.getLocationKey());
//                                mDBHelper.updateCity(autolocateCity.getLocationKey(), city.getLocationKey());
//                                //PR941173 auto-location entry is somehow moved to end of city list by ting.ma at 2015-03-06 end
//
//                                city.setAutoLocate(true);
//                                autolocateSuccess = insertCityFunc(city, true);
//                                Log.w(CommonUtils.TAG_BING, "---!updateAutoLocationCityOnly, needReplaceCity ");
//                                firstCityKey = city.getLocationKey();
//                            }
//                            // new autolocate city and old autolocate city is in the same region,use the same locationkey
//                            else {
//                                boolean isInsert = true;
//                                if (updateAutoLocationCityOnly) {
//                                    long currentTime = System.currentTimeMillis();
//                                    isInsert = (lastAutoLocateTime != -1 && currentTime - lastAutoLocateTime >= ONEHOUR);
//                                }
//                                if (isInsert) {
//                                    city.setLocationKey(autolocateCity.getLocationKey());
//                                    autolocateSuccess = insertCityFunc(city, false);
//                                    Log.w(CommonUtils.TAG_BING, "---!updateAutoLocationCityOnly, isInsert ");
//                                }
//                                firstCityKey = autolocateCity.getLocationKey();
//                            }
//                        }
//                        // there is no autolocation city before
//                        else {
//                            autolocateSuccess = insertCityFunc(city, true);
//                            firstCityKey = city.getLocationKey();
//                        }
//                    } else {
//                        if (!mAutoUpdate) {
//                            Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
//                            it.putExtra("connect_timeout", true);
//                            sendBroadcast(it);
//                        }
//                    }
//                }
//
//                if (autolocateSuccess) {
//                    // add by jielong.xing for power consumption high issue at 2015-3-18 begin
//                    resetRetryData();
//                    // add by jielong.xing for power consumption high issue at 2015-3-18 end
//                    // added for launcher dynamical icon at 2015-5-20 begin
//                    isSendSyncBroadcast = true;
//                    // added for launcher dynamical icon at 2015-5-20 end
//                    SharePreferenceUtils.saveLong(UpdateService.this, KEY_LAST_AUTOLOCATETIME, System.currentTimeMillis());
//                }
//
//                if (!updateAutoLocationCityOnly) {
//                    if (cityList != null && cityList.size() > 0) {
//                        /*if (autolocateSuccess || updateWeatherOnly) {
//                            mDBHelper.updateCityTime();
//						} else {
//							mDBHelper.updateNotAutoLocateCityTime();
//						}*/
//                        Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
//                        it.putExtra("weather", true);
//                        // fix PR957547 by jielong.xing at 2015-3-24 begin
//                        if (isLocationOpen && !autolocateSuccess) {
//                            it.putExtra("locationerror", true);
//                        }
//                        // fix PR957547 by jielong.xing at 2015-3-24 end
//                        sendBroadcast(it);
//                        // added for launcher dynamical icon at 2015-5-20 begin
//                        isSendSyncBroadcast = true;
//                        // added for launcher dynamical icon at 2015-5-20 end
//                    }
//
//                    SharePreferenceUtils.saveLong(UpdateService.this, KEY_LAST_REFRESHTIME, System.currentTimeMillis());
//                }
//            } catch (SocketException e) {
//                Log.e(TAG, "UpdateWeatherThread SocketException : " + e.toString());
//                if (!mAutoUpdate) {
//                    Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
//                    it.putExtra("connect_timeout", true);
//                    sendBroadcast(it);
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "UpdateWeatherThread Exception : " + e.toString());
//                if (!mAutoUpdate) {
//                    Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
//                    it.putExtra("connect_timeout", true);
//                    sendBroadcast(it);
//                }
//            } finally {
//                isUpdateWeatherThreadRunning = false;
//                // added for launcher dynamical icon at 2015-5-20 begin
//                if (isSendSyncBroadcast) {
//                    WeatherInfo weather = getWeatherFromDB(firstCityKey);
//                    WeatherForShow weatherForShow = weather.getWeatherForShow();
//                    //Begin add by jiajun.shen  for 1050342
//                    if (null == weatherForShow) {
//                        return;
//                    }
//                    //End add by jiajun.shen for 1050342
//                    Log.d("xjl_weather", "UpdateWeatherThread() weather locationKey = " + firstCityKey + ", temperature = " + weatherForShow.getTemp() + ", icon = " + weatherForShow.getIcon());
//                    SharePreferenceUtils.saveCityWeatherInfo(UpdateService.this, firstCityKey, weatherForShow.getTemp(), weatherForShow.getIcon());
//                    Intent mieIntent = new Intent("com.tct.weather.MIE_SYNC");
//                    mieIntent.putExtra("locationKey", firstCityKey);
//                    sendBroadcast(mieIntent);
//                }
//                // added for launcher dynamical icon at 2015-5-20 end
//            }
//        }
//    }

    // Auto locate,get the city by location,insert the city to database,and show the weather information directly
    public void autoLocate(double latitude, double longitude) {
        CityFindGPSThread thread = new CityFindGPSThread();
        thread.mLatitude = latitude;
        thread.mLongitude = longitude;
        thread.start();
    }

    private class CityFindGPSThread extends Thread {
        public double mLatitude = 0;
        public double mLongitude = 0;

        public void run() {
            try {
                City city = findCity(mLatitude, mLongitude);
                if (city == null) {
                    Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                    it.putExtra("connect_timeout", true);
                    sendBroadcast(it);
                    return;
                }

                List<City> cityList = getCityListFromDB();

                String oriName = null;
                String oriLocationKey = null;
                int citySize = 0;
                if (cityList != null) {
                    citySize = cityList.size();
                }
                for (int i = 0; i < citySize; i++) {
                    City var = cityList.get(i);
                    if (var.isAutoLocate()) {
                        oriName = var.getCityName();
                        oriLocationKey = var.getLocationKey();
                        break;
                    }
                }

                if (oriName != null && oriName.equals(city.getCityName())) {
                    city.setLocationKey(oriLocationKey);
                } else if (oriName != null) {
                    if (oriLocationKey != null) {
                        deleteCity(oriLocationKey);
                    }
                }

                SharePreferenceUtils.getInstance().saveLong(UpdateService.this, KEY_LAST_AUTOLOCATETIME, System.currentTimeMillis());

                insertCity(city, true);
                SharePreferenceUtils.getInstance().checkCommonCity(UpdateService.this, city.getLocationKey());
            } catch (Exception e) {
                Log.e(TAG, "CityFindGPSThread" + e.getMessage());
                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                it.putExtra("connect_timeout", true);
                sendBroadcast(it);
                e.printStackTrace();
            }
        }
    }

    private boolean isBetterLocation(Location locationA, Location locationB) {
        if (locationA == null) {
            return false;
        }
        if (locationB == null) {
            return true;
        }
        // A provider is better if the reading is sufficiently newer. Heading
        // underground can cause GPS to stop reporting fixes. In this case it's
        // appropriate to revert to cell, even when its accuracy is less.
        if (locationA.getElapsedRealtimeNanos() > (locationB.getElapsedRealtimeNanos() + 11 * 1000000000)) {
            return true;
        } else if (locationB.getElapsedRealtimeNanos() > (locationA.getElapsedRealtimeNanos() + 11 * 1000000000)) {
            return false;
        }

        // A provider is better if it has better accuracy. Assuming both readings are fresh (and by that accurate), choose the one with the smaller accuracy circle.
        if (!locationA.hasAccuracy()) {
            return false;
        }
        if (!locationB.hasAccuracy()) {
            return true;
        }
        return locationA.getAccuracy() < locationB.getAccuracy();
    }

    private Location getBetterLocation() {
        Location location = null;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (isBetterLocation(mGpsLocation, mNetworkLocation)) {
            Log.i(TAG, "jielong_BetterLocation()::GpsLocation");
            location = mGpsLocation;
        } else {
            Log.i(TAG, "jielong_BetterLocation()::NetworkLocation");
            location = mNetworkLocation;
        }
        return location;
    }

    private Weather getWeather(String locationKey, String latitude, String longitude, String lang) throws ClientProtocolException, IOException, JSONException, ParseException {
        Weather weather = new Weather();
        Currentconditions current;
        if (isTwcWeather) {
            current = CurrentWeatherRequest.getCurrentWeather(locationKey, latitude, longitude, lang, true);
        } else {
            current = CurrentWeatherRequest.getCurrentWeather(locationKey, latitude, longitude, lang, false);
        }

        if (current == null) {
            return null;
        }
        weather.setCurrentconditions(current);
        Log.e(TAG, "getgetWeather isTwcWeather = " + isTwcWeather); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
        List<Hour> hourList = null;
        if (isTwcWeather) {
            hourList = ForecastWeatherRequest.getHourlyForecastWeather(locationKey, latitude, longitude, lang, true);
        }
//[FEATURE]-Add-BEGIN by TSCD.peng.du,11/27/2015,983895,delete weather_24forecast_card
//        else {
//            hourList = ForecastWeatherRequest.getHourlyForecastWeather(locationKey, latitude, longitude, lang, false);
//        }

        if (hourList != null) {
//            return null;
            weather.setHourList(hourList);
        }
//[FEATURE]-Add-END by TSCD.peng.du
        Forecast forecast = new Forecast();
        List<Day> dayList;
        if (isTwcWeather) {
            dayList = ForecastWeatherRequest.getDailyForecastWeather(locationKey, latitude, longitude, lang, true);
        } else {
            dayList = ForecastWeatherRequest.getDailyForecastWeather(locationKey, latitude, longitude, lang, false);
        }

        // Fixed PR965957 by jielong.xing at 2015-4-2 begin
        if (dayList == null) {
            return null;
        }
        // Fixed PR965957 by jielong.xing at 2015-4-2 end
//    	List<Hour> hourList = ForecastWeatherRequest.get24HourForecastWeather(locationKey, lang);
//        forecast.setDays(dayList);
////    	forecast.setHours(hourList);
//        weather.setForecast(forecast);
        weather.setDayList(dayList);
        Local local = new Local();
        local.setCityId(locationKey);
        local.setTime(System.currentTimeMillis() + "");
        weather.setLocal(local);
        return weather;
    }

    // add by jielong.xing for power consumption high issue at 2015-3-18 begin
    private void resetRetryData() {
        isAutoLocateFailed = false;
        mAutoLocateFailedCnt = 0;
        mIntervalCnt = 0;
        mHourIntervalCnt = 1;
    }

    private boolean isCanRunUpdateThread() {
        if (isAutoLocateFailed) {
            if (mAutoLocateFailedCnt >= CONNECTRETRYMAXTIME) {
                return false;
            }
            if (mIntervalCnt < INTERVALTIME) {
                mIntervalCnt++;
                return false;
            }
            if (mIntervalCnt == INTERVALTIME) {
                mIntervalCnt = 0;
            }
        }

        return true;
    }

    // add by jielong.xing for power consumption high issue at 2015-3-18 end
    private boolean isDefUseBaiduApi() {
        boolean isDefUseBaiduApi = CustomizeUtils.getBoolean(UpdateService.this, "def_weather_use_baiduapi");
        Log.d(TAG, "isDefUseBaiduApi = " + isDefUseBaiduApi);
        return isDefUseBaiduApi;
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        try {
            if (bdLocation != null) {
                String locType = null;
                if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                    locType = " network (" + bdLocation.getNetworkLocationType() + ")";
                } else if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                    locType = " gps (" + bdLocation.getSatelliteNumber() + " satellites)";
                } else {
                    locType = " #" + bdLocation.getLocType();
                }
                String provider = "baidu: " + locType;
                Location location = new Location(provider);
                location.setLongitude(bdLocation.getLongitude());
                location.setLatitude(bdLocation.getLatitude());
                location.setAccuracy(bdLocation.getRadius());
                location.setTime(System.currentTimeMillis());
                location.setElapsedRealtimeNanos(System.nanoTime());
                mNetworkLocation = location;

                mLocationUpdated = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "BaiduLocationClient.stop() Exception : " + e.toString());
        } finally {
            mHandler.sendEmptyMessage(STOP_BAIDU_LOCATE);
        }
    }

    //Begin add by jiajun.shen for 1016685 at 2015.6.4
    private synchronized void sendTokyoFindRequest(String cityName) {
        /*int languageId = getLanguageId();
        try {
			mSearchURL = new StringBuilder(URL_CITY_FIND)
				.append("location=" + URLEncoder.encode(cityName, "utf-8")
					+ "&langid=" + languageId).toString().trim();
			mWeatherURL = new StringBuilder(URL_WEATHER_DATA).append(CITY_TOKYO_LOCATIONKEY).toString().trim();
		} catch (Exception e) {
			Log.e(TAG, "findTokyoData exception :: " + e.getMessage());
		}*/
        String lang = getLanguage();
        String url = "";
        try {
            url = urlBuilder.findCityByName(URLEncoder.encode(cityName, "utf-8"), lang, true);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.i(TAG, "city find request respose=" + jsonObject.toString());
                        List<City> tempCityList;
                        tempCityList = responseUtil.getCityList(jsonObject);
                        City tokyoCity = null;
                        if (null != tempCityList && tempCityList.size() > 0) {
                            for (City city : tempCityList) {
                                String locationKey = city.getLocationKey();
                                if (locationKey.contains(CITY_TOKYO_LOCATIONKEY)) {
                                    tokyoCity = city;
                                    break;
                                }
                            }
                        }
                        if (tokyoCity != null) {
                            setUpdateManue();
                            insertCity(tokyoCity, false);
                            SharePreferenceUtils.getInstance().checkCommonCity(getApplicationContext(), tokyoCity.getLocationKey());
                            SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("isFirstUse", Context.MODE_PRIVATE).edit();
                            editor.putBoolean("isFirstUse", false);
                            editor.commit();
                            isFirstUse = false;
                            unregisterReceiver(mNetworkBroadcastReceiver);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e(TAG, "city find request error::" + volleyError);
                        Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                        it.putExtra("connect_timeout", true);
                        sendBroadcast(it);
                    }
                });
        jsonObjectRequest.setShouldCache(false);
        requestQueue.add(jsonObjectRequest);
//
//        TokyoCityAndWeatherFindThread thread = new TokyoCityAndWeatherFindThread();
//        thread.cityName = cityName;
//
//        thread.start();
    }

//    private class TokyoCityAndWeatherFindThread extends Thread {
//        public String cityName = "";
//
//        public void run() {
//            try {
//				/*InputStream mInputStream = downloadUrl(mSearchURL).getInputStream();
//	            List<City> tempCityList = CityFindParser.parse(mInputStream);
//	            mInputStream.close();*/
//                List<City> tempCityList = CityFindRequest.findCityByName(URLEncoder.encode(cityName, "utf-8"), getLanguage(), true);
//
//                City tokyoCity = null;
//                if (null != tempCityList && tempCityList.size() > 0) {
//                    for (City city : tempCityList) {
//                        String locationKey = city.getLocationKey();
//                        if (locationKey.contains(CITY_TOKYO_LOCATIONKEY)) {
//                            tokyoCity = city;
//                            break;
//                        }
//                    }
//                }
//
//                if (tokyoCity != null) {
//                    setUpdateManue();
//                    insertCity(tokyoCity, false);
//
//                    SharePreferenceUtils.checkCommonCity(getApplicationContext(), tokyoCity.getLocationKey());
//
//	            	/*mInputStream = downloadUrl(mWeatherURL).getInputStream();
//	                Weather weather = new WeatherDataParser().parse(mInputStream);
//	                mInputStream.close();*/
//	            	/*Weather weather = getWeather(CITY_TOKYO_LOCATIONKEY, getLanguage());
//	                insertWeatherIntoDB(weather, true);*/
//
//                    SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("isFirstUse", Context.MODE_PRIVATE).edit();
//                    editor.putBoolean("isFirstUse", false);
//                    editor.commit();
//
//                    isFirstUse = false;
//
//                    unregisterReceiver(mNetworkBroadcastReceiver);
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "TokyoCityAndWeatherFindThread run exception :: " + e.getMessage());
//            }
//        }
//    }

    private boolean isForceSetTokyo() {
        return CustomizeUtils.getBoolean(UpdateService.this, "def_weather_forceSetTokyoAsDefaultCity_on");
    }

    private BroadcastReceiver mNetworkBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                sendTokyoFindRequest(getString(R.string.tokyo_name));
            }
        }

    };

    //End add by jiajun.shen for 1016685 at 2015.6.4
    private class UpdateLocationThread extends Thread {
        City autolocateCity = null;
        double latitude = -1;
        double longitude = -1;

        @Override
        public void run() {
            super.run();
            if (mCityList != null && mCityList.size() >= 0) {
                mcitySize = mCityList.size();
                for (int i = 0; i < mcitySize; i++) {
                    City var = mCityList.get(i);
                    boolean isAutoLocate = var.isAutoLocate();
                    if (isAutoLocate) {
                        autolocateCity = var;
                    }
                }
                if (autolocateCity != null) {
                    boolean isUpdateSuccess = isUpdateSuccess();
                    if (isUpdateSuccess) {
                        Location location = getBetterLocation();
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Log.e(TAG, "UpdateLocationThread:::latitude = " + latitude + ", longitude = " + longitude);
                        updateLocation(0, latitude, longitude, autolocateCity);
                    } else {
                        updateCurrentWeather(0, autolocateCity.getLocationKey(), autolocateCity.getLatitude(), autolocateCity.getLongitude(), getLanguage(), false);
                        //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/01/2015,983895,[ergo5.2.8]delete the weather_24forecast_card
                        /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
                        if (isTwcWeather) {
                            updateHourlyWeather(0, autolocateCity.getLocationKey(), autolocateCity.getLatitude(), autolocateCity.getLongitude(), getLanguage());
                        }
                        //[FEATURE]-Add-END by TSCD.peng.du
                    }
                    //[BUGFIX]-Add-BEGIN by TSCD.peng.du,12/28/2015,1192934,
                    //[Android6.0][Weather_v5.2.8.1.0305.0][Monitor]Weather pop crash all the time
                } else if (mcitySize > 0) {
                    //[BUGFIX]-Add-END by TSCD.peng.du
                    City firstCity = mCityList.get(0);
                    updateCurrentWeather(0, firstCity.getLocationKey(), firstCity.getLatitude(), firstCity.getLongitude(), getLanguage(), false);
                    //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/01/2015,983895,[ergo5.2.8]delete the weather_24forecast_card
                    if (isTwcWeather) {
                    /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
                        updateHourlyWeather(0, firstCity.getLocationKey(), firstCity.getLatitude(), firstCity.getLongitude(), getLanguage());
                    }
                    //[FEATURE]-Add-END by TSCD.peng.du
                }
            }
        }
    }

    private void updateLocation(int index, double latitude, double longitude, City autoCity) {
        String firstCityKey = "";
//        City autolocateCity = autoCity;
        boolean autolocateSuccess = false;
//        ArrayList<City> cityList = getCityListFromDB();
//        if (cityList != null) {
//            mcitySize = cityList.size();
//        }
        // added for launcher dynamical icon at 2015-5-20 begin
        if (mcitySize > 0) {
            firstCityKey = mCityList.get(0).getLocationKey();
        }
//        for (int i = 0; i < mcitySize; i++) {
//            City var = mCityList.get(i);
//            boolean isAutoLocate = var.isAutoLocate();
//            if (isAutoLocate) {
//                autolocateCity = var;
//            }
//        }
        City city = findCity(latitude, longitude);
        String currentLanguage = getLanguage();
        if (city != null) {
            city.setAutoLocate(true);
            firstCityKey = city.getLocationKey();
            if (needReplaceCity(city, autoCity)) {
                mDBHelper.updateCity(autoCity.getLocationKey(), city.getLocationKey());
                city.setAutoLocate(true);
                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/26/2016,1487609,[Weather]The widget weather show moon in daytime and the local weather disappear
                mDBHelper.insertCity(city);
                mDBHelper.updateCurrentLocationKey(autoCity.getLocationKey(), city.getLocationKey());
                mDBHelper.updateForecastLocationKey(autoCity.getLocationKey(), city.getLocationKey());
//                autolocateSuccess = insertCityFunc(city, true);
                //[BUGFIX]-Add-END by TSCD.qian-li
                Log.w(TAG, "---!updateAutoLocationCityOnly, needReplaceCity ");
            }
            // new autolocate city and old autolocate city is in the same region,use the same locationkey
//                else {
//                    boolean isInsert = true;
//                    if (updateAutoLocationCityOnly) {
//                        long currentTime = System.currentTimeMillis();
//                        isInsert = (lastAutoLocateTime!=-1 && currentTime - lastAutoLocateTime >= ONEHOUR);
//                    }
//                    if (isInsert) {
//                        city.setLocationKey(autolocateCity.getLocationKey());
//                        autolocateSuccess = insertCityFunc(city, false);
//                        Log.w(CommonUtils.TAG_BING, "---!updateAutoLocationCityOnly, isInsert ");
//                    }
//                    firstCityKey = autolocateCity.getLocationKey();
//                }

            // there is no autolocation city before
            else {
                if (isLanguageChange) {
                    city.setAutoLocate(true);
                    mDBHelper.insertCity(city);
                }
                //autolocateSuccess = insertCityFunc(city, true);
                Log.w(TAG, "---!updateAutoLocationCityOnly, don't needReplaceCity ");
            }
//            updateCurrentWeather(index, autoCity.getLocationKey(), autoCity.getLatitude(), autoCity.getLongitude(), currentLanguage);
//            updateForcastWeather(index, autoCity.getLocationKey(), autoCity.getLatitude(), autoCity.getLongitude(), currentLanguage);
            //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/01/2015,983895,[ergo5.2.8]delete the weather_24forecast_card
//            if(isTwcWeather) {
//                updateHourlyWeather(index, autoCity.getLocationKey(), autoCity.getLatitude(), autoCity.getLongitude(), currentLanguage);
//            }
            //[FEATURE]-Add-END by TSCD.peng.du

            //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/23/2016,1443957,[Weather]It can't automatically recognize new site when move to new site
            Log.i(TAG, "autoCity before : " + autoCity.toString());
            autoCity = city;
            Log.i(TAG, "autoCity after : " + city.toString());
            mCityList.set(index, city);
            //[BUGFIX]-Add-END by TSCD.qian-li

            if (isLanguageChange) {
                updateCityNum++;
                Log.i(TAG, "UpdateWeatherCityName, updateCityNum : " + updateCityNum);
                if (updateCityNum >= mcitySize) {
                    mHandler.sendEmptyMessage(INSERT_CITY_INTO_DB);
                }
            }
        } else {
            Log.e(TAG, "find city error");
            updateWeatherCityName(autoCity, currentLanguage);
            if (!mAutoUpdate) {
                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                it.putExtra("connect_timeout", true);
                sendBroadcast(it);
            }
//            isUpdating = false;
        }

        updateCurrentWeather(index, autoCity.getLocationKey(), autoCity.getLatitude(), autoCity.getLongitude(), currentLanguage, false);
        updateForcastWeather(index, autoCity.getLocationKey(), autoCity.getLatitude(), autoCity.getLongitude(), currentLanguage, false);
        //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/01/2015,983895,[ergo5.2.8]delete the weather_24forecast_card
        if (isTwcWeather) {
            updateHourlyWeather(index, autoCity.getLocationKey(), autoCity.getLatitude(), autoCity.getLongitude(), currentLanguage);
        }
        //[FEATURE]-Add-END by TSCD.peng.du
//        WeatherInfo weather = getWeatherFromDB(firstCityKey);
//        WeatherForShow weatherForShow = weather.getWeatherForShow();
//        //Begin add by jiajun.shen  for 1050342
//        if (null == weatherForShow) {
//            return;
//        }
//        //End add by jiajun.shen for 1050342
//        Log.d(TAG, "UpdateWeatherThread() weather locationKey = " + firstCityKey + ", temperature = " + weatherForShow.getTemp() + ", icon = " + weatherForShow.getIcon());
//        SharePreferenceUtils.getInstance().saveCityWeatherInfo(UpdateService.this, firstCityKey, CommonUtils.c2f(weatherForShow.getTemp()), weatherForShow.getIcon());
//        Intent mieIntent = new Intent("com.tct.weather.MIE_SYNC");
//        mieIntent.putExtra("locationKey", firstCityKey);
//        sendBroadcast(mieIntent);

    }

    private void checkUpdateFinished() {
        //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/01/2015,983895,[ergo5.2.8]delete the weather_24forecast_card
        boolean update = false;
        /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
        if (isTwcWeather) {
            if (isLanguageChange) {
                update = (updateCurrnetNum >= mcitySize) && (updateForecastNum >= mcitySize)
                        && (updateHourlyNum >= mcitySize) && (updateCityNum >= mcitySize);
                isLanguageChange = false;
            } else {
                update = (updateCurrnetNum >= mcitySize) && (updateForecastNum >= mcitySize)
                        && (updateHourlyNum >= mcitySize);
            }
        } else {
            switch (updateGrade) {
                case UPDATE_AUTO_LOCATION_WEATHER:
                    update = updateCurrnetNum >= 1;
                    break;
                case UPDATE_ALL_CURRENT_WEATHER:
                    if (isLanguageChange) {
                        update = (updateCurrnetNum >= mcitySize) && (updateCityNum >= mcitySize);
                    } else {
                        update = updateCurrnetNum >= mcitySize;
                    }
                    break;
                case UPDATE_ALL_WEATHER:
                    if (isLanguageChange) {
                        update = (updateCurrnetNum >= mcitySize) && (updateForecastNum >= mcitySize) && (updateCityNum >= mcitySize);
                    } else {
                        update = (updateCurrnetNum >= mcitySize) && (updateForecastNum >= mcitySize);
                    }
                    break;
                case UPDATE_ALL_WEATHER_AUTO_LOCATION:
                /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
                    if (isLanguageChange) {
                        update = (updateCurrnetNum >= mcitySize) && (updateForecastNum >= mcitySize) && (updateCityNum >= mcitySize);
                    } else {
                        update = (updateCurrnetNum >= mcitySize) && (updateForecastNum >= mcitySize);
                    }
                    break;
            }
            Log.i(TAG, "update : " + update + ", updateCurrnetNum : " + updateCurrnetNum + ", updateForecastNum : " + updateForecastNum + ", updateCityNum : " + updateCityNum + ", mcitySize : " + mcitySize);
//            if (isLanguageChange) {
//                update = (updateCurrnetNum >= mcitySize) && (updateForecastNum >= mcitySize) && (updateCityNum >= mcitySize);
//                isLanguageChange = false;
//            } else {
//                update = (updateCurrnetNum >= mcitySize) && (updateForecastNum >= mcitySize);
//            }
        }

//        if ((updateGrade == 1 && updateCurrnetNum >= 1)
//                || (updateGrade == 2 && updateCurrnetNum >= mcitySize && updateCityNum >= mcitySize)
//        if ((updateGrade == 1 && updateCurrnetNum >= 1 && updateHourlyNum >= 1)
//                || (updateHourlyNum >= mcitySize && updateCurrnetNum >= mcitySize && updateForecastNum >= mcitySize)) {
//                || update) {
        if (update) {
            //[FEATURE]-Add-END by TSCD.peng.du // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
            //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/01/2016,1539804,[Weather][REG]The refresh time is error when Cellular Data or wifi is disconnect
//            if (updateGrade != UPDATE_AUTO_LOCATION_WEATHER) {
//                mDBHelper.updateCityTime();
//            }
            //[BUGFIX]-Add-END by TSCD.qian-li
            // when there are no error when update city name
            //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/20/2016,1313172,[Weather]weather not translate to russian
            if (isLanguageChange) {
                if (updateCityNameWrong) {
                    isLanguageChange = true;
                    updateCityNameWrong = false;
                } else {
                    isLanguageChange = false;
                    SharePreferenceUtils.getInstance().saveString(this, KEY_LAST_LANGUAGE, getLanguage());
                }
            }
            //[BUGFIX]-Add-END by TSCD.qian-li
            isUpdating = false;
            updateForecastNum = 0;
            updateCurrnetNum = 0;
            updateHourlyNum = 0;
            updateCityNum = 0;
//            mcitySize = 0;
            //updateCurrentOnly = true;
            if (updateGrade == UPDATE_ALL_WEATHER_AUTO_LOCATION) {
                Log.i(TAG, "save KEY_LAST_REFRESHTIME=" + KEY_LAST_REFRESHTIME);
                SharePreferenceUtils.getInstance().saveLong(UpdateService.this, KEY_LAST_REFRESHTIME, System.currentTimeMillis());
            }
            updateGrade = UPDATE_AUTO_LOCATION_WEATHER;
            Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
            it.putExtra("weather", true);
            sendBroadcast(it);


            String firstCityKey = "";
            ArrayList<City> cityList = getCityListFromDB();
            if (cityList != null) {
                mcitySize = cityList.size();
            }
            // added for launcher dynamical icon at 2015-5-20 begin
            if (mcitySize > 0) {
                firstCityKey = cityList.get(0).getLocationKey();
            }
            WeatherInfo weather = getWeatherFromDB(firstCityKey);
            WeatherForShow weatherForShow = weather.getWeatherForShow();
            if (null == weatherForShow) {
                return;
            }
            Log.d(TAG, "UpdateWeatherThread() weather locationKey = " + firstCityKey + ", temperature = " + weatherForShow.getTemp() + ", icon = " + weatherForShow.getIcon());
            SharePreferenceUtils.getInstance().saveCityWeatherInfo(UpdateService.this, firstCityKey, CommonUtils.c2f(weatherForShow.getTemp()), weatherForShow.getIcon());
            Intent mieIntent = new Intent("com.tct.weather.MIE_SYNC");
            mieIntent.putExtra("locationKey", firstCityKey);
            sendBroadcast(mieIntent);
            //[BUGFIX]-MOD-BEGIN by TSCD.xing.zhao,12/29/2015,1192931,[Android6.0][Weather_v5.2.8.1.0305.0]Click the refresh button on widget continually,weather will crash
            isWidgetUpdating = false;
            //[BUGFIX]-Add-END by TSCD.xing.zhao

        }
    }

    private boolean isUseTwcWeather() {
        return CustomizeUtils.getBoolean(UpdateService.this, "use_twc_weather");
    }

    //[FEATURE]-Add-BEGIN by TSCD.qian-li,12/23/2015,984832,[Weather]Local city doesn't auto refresh after half an hour ago
    private void invokeTimerTask(Context aContext) {
        Log.w(TAG, "-----------invokeTimerTask");
        PendingIntent alarmSender = null;
        Intent intent = new Intent(CommonUtils.LOCATION_TIMER_TASK_ACTION);
        alarmSender = PendingIntent.getBroadcast(aContext, 0, intent, 0);

        AlarmManager alarms = (AlarmManager) aContext.getSystemService(Context.ALARM_SERVICE);
        alarms.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, alarmSender);
    }

    private void cancelTimerTask(Context aContext) {
        PendingIntent alarmSender = null;
        Intent intent = new Intent(CommonUtils.LOCATION_TIMER_TASK_ACTION);
        alarmSender = PendingIntent.getBroadcast(aContext, 0, intent, 0);
        AlarmManager alarms = (AlarmManager) aContext.getSystemService(Context.ALARM_SERVICE);
        alarms.cancel(alarmSender);
    }
    //[FEATURE]-Add-END by TSCD.qian-li
}
