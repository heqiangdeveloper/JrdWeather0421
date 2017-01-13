package com.tct.weather.service;

import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextClock;
import android.widget.TextView;

import com.tct.weather.LocateActivity;
import com.tct.weather.MainActivity;
import com.tct.weather.R;
import com.tct.weather.bean.City;
import com.tct.weather.bean.WeatherForShow;
import com.tct.weather.provider.DBHelper;
import com.tct.weather.util.ColorCutQuantizer;
import com.tct.weather.util.CommonUtils;
import com.tct.weather.util.IconBackgroundUtil;
import com.tct.weather.widget.WeatherTimeWidget;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by user on 15-12-5.
 */
public class WeatherTimeWidgetService extends Service{
//    public WeatherTimeWidgetService() {
//    }
    private Context mContext = null;
    private AppWidgetManager mAppWidgetManager = null;
    private DBHelper mDBHelper = null;
    private ArrayList<City> mCityList = null;
/*MODIFIED-BEGIN by xiangnan.zhou, 2016-04-08,BUG-1923725*/
//    private WeatherForShow mShowWeather =null;
    private String mIcon = null;
    private ImageView mImage = null;
    private final String TAG = "WeatherTimeService";
    private final String INVALIDATA_LOCATION = "no_city_key";
    private String mLocationKey = INVALIDATA_LOCATION;
    private String mCityName;
    private String mCityTemp;
    private boolean hasWeatherData = false;
    /*MODIFIED-END by xiangnan.zhou,BUG-1923725*/

    private boolean isDarkWallpaper = true;
    private boolean isLiveWallpaper = false;

    private static final int CALCULATE_BITMAP_MIN_DIMENSION = 500;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mAppWidgetManager = AppWidgetManager.getInstance(mContext);
        mDBHelper = new DBHelper(mContext);

        isDarkWallpaper = getBackgroundRgb();
        Log.i(TAG, "viewUpdate : isDarkWallpaper : " + isDarkWallpaper);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_NOT_STICKY;
        String action = null;
        if (intent != null) {
            action = intent.getAction();
        }
        viewsUpdate(this);

        super.onStartCommand(intent, flags, startId);
        return flags;
    }

    private void viewsUpdate(Context context) {
        int[] appWidgetIds = mAppWidgetManager.getAppWidgetIds(
                new ComponentName(mContext, WeatherTimeWidget.class));
        for (int appWidgetId : appWidgetIds) {
            viewUpdate(context, appWidgetId);
        }
    }


    private void viewUpdate(Context context, final int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.widget_weather_time_layout);
        //[BUGFIX]-Add-BEGIN by TSCD.peng.du,12/24/2015,1192932,
        // [Android6.0][Weather_v5.2.8.1.0305.0]Enter weather from Weather&Time widget display other city
        mLocationKey = INVALIDATA_LOCATION;
        queryCurrentWeather(mContext); //MODIFIED by xiangnan.zhou, 2016-04-08,BUG-1923725
        Intent selectLocationIntent = new Intent(context, MainActivity.class);
        selectLocationIntent.putExtra("newCityKey", mLocationKey);
        selectLocationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //[BUGFIX]-Add-END by TSCD.peng.du
        PendingIntent pSelectLocationIntent = PendingIntent.getActivity(context, 1, selectLocationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_weather_layout, pSelectLocationIntent);

        Intent clockIntent = new Intent();
        clockIntent.setAction(getAlarmAction());
        PendingIntent pClockIntent = PendingIntent.getActivity(context, 0, clockIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_time_data_layout, pClockIntent);
        views.setOnClickPendingIntent(R.id.widget_date, pClockIntent);

        isDarkWallpaper = getBackgroundRgb();
        Log.i(TAG, "viewUpdate : isDarkWallpaper : " + isDarkWallpaper);
        int resId = getCurrentWeatherIcon(); //MODIFIED by xiangnan.zhou, 2016-04-08,BUG-1923725
        setCurrentColor(views);


//        mIcon = queryCurrentWeather(mContext);
//        int resId =  R.drawable.ic_unknown;
//        if(null != mIcon){
//            resId = IconBackgroundUtil.getTimeWidgetIcon(mIcon);
//        }
        Log.d(TAG, "viewUpdate resId = " + resId + " icon = " + mIcon);

        //[BUGFIX]-Add-BEGIN by peng.du,2015/12/24,1192926
        //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,02/20/2016,1541377,[Pre-CTS][ALRU]Date format should be DD.MM.YYYY everywhere for Russian and CIS sw
        String langcode = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry().toLowerCase();
        Log.i(TAG, "langcode:" + langcode);
        CharSequence dateFormat = null;
        if (langcode.contains("ru")) {
            dateFormat = DateFormat.format("dd.MM.yyyy", Calendar.getInstance());
        } else {
            dateFormat = DateFormat.getBestDateTimePattern(Locale.getDefault(), "d MMMM");
        }
        //[BUGFIX]-Add-END by TSCD.xing.zhao
        views.setCharSequence(R.id.widget_date, "setFormat12Hour", dateFormat);
        views.setCharSequence(R.id.widget_date, "setFormat24Hour", dateFormat);
        //[BUGFIX]-Add-END by peng.du,2015/12/24,1192926

        if (langcode.contains("id")) {
            views.setCharSequence(R.id.widget_time, "setFormat12Hour", "hh.mm");
            views.setCharSequence(R.id.widget_time, "setFormat24Hour", "kk.mm");
        }

        /*MODIFIED-BEGIN by xiangnan.zhou, 2016-04-08,BUG-1923725*/
        if (hasWeatherData) {
            views.setInt(R.id.widget_ll_info, "setVisibility", View.VISIBLE);
            views.setTextViewText(R.id.widget_tv_city, mCityName); //MODIFIED by qian-li, 2016-04-13,BUG-1940875
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            String tempUnit = settings.getString("settings_temp", "1");
            if (TextUtils.equals(tempUnit, "1")) {
                views.setTextViewText(R.id.widget_tv_temp, CommonUtils.deletaDec(mCityTemp) + "°");
            } else {
                views.setTextViewText(R.id.widget_tv_temp, CommonUtils.c2f(mCityTemp) + "°");
            }
        } else {
            views.setInt(R.id.widget_ll_info, "setVisibility", View.GONE);
        }
        /*MODIFIED-END by xiangnan.zhou,BUG-1923725*/

        views.setImageViewResource(R.id.widget_weather_icon, resId);
        AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(context);
        mAppWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /*MODIFIED-BEGIN by xiangnan.zhou, 2016-04-08,BUG-1923725*/
    private void queryCurrentWeather(Context context) {
//        String icon = null;
//
//        mLocationKey = mDBHelper.getAutoLocateCity();
//        mShowWeather = mDBHelper.getWeatherForShow(mLocationKey);
//        if (null != mShowWeather) {
//            icon = mShowWeather.getIcon();
//        }
//        Log.d(TAG, " queryCurrentWeather locationKey = " + mLocationKey + " icon = " + icon);
//        return icon;


        Log.d(TAG, "queryCurrentWeather");
        int mIndex = 0;
        mCityList = mDBHelper.getCityListFromDB();
        if (mCityList != null && mCityList.size() > 0) {
            for (int i = 0; i < mCityList.size(); i++) {
                if (mCityList.get(i).isAutoLocate()) {
                    mIndex = i;
                    break;
                }
            }
            City city = mCityList.get(mIndex);
            mCityName = city.getCityName();
            mLocationKey = city.getLocationKey();
            WeatherForShow mWeatherForShow = mDBHelper.getWeatherForShow(mLocationKey);
            if (null != mWeatherForShow) {
                mIcon = mWeatherForShow.getIcon();
                mCityTemp = mWeatherForShow.getTemp();
                hasWeatherData = true;
            } else {
                hasWeatherData = false;
            }
        } else {
            hasWeatherData = false;
            /*MODIFIED-END by xiangnan.zhou,BUG-1923725*/
        }
    }

    //get the lanch wallpaper  RGB
    private boolean getBackgroundRgb() {
        WallpaperManager mWallpaperManager = WallpaperManager.getInstance(mContext);
        WallpaperInfo wpaperInfo = mWallpaperManager.getWallpaperInfo();
        if (wpaperInfo != null) {
            isLiveWallpaper = true;
            Log.i(TAG, "is live Wallpaper----------");
        } else {
            isLiveWallpaper = false;
        }
        Bitmap bitmap = drawableToBitmap(mWallpaperManager.getDrawable());

        WeakReference<Bitmap> mWallpaperBitmapRef = new WeakReference<Bitmap>(bitmap);
        Bitmap scaledBitmap = scaleBitmapDown(mWallpaperBitmapRef.get());

        ColorCutQuantizer colorCutQuantizer = ColorCutQuantizer.fromBitmap(scaledBitmap);
        return colorCutQuantizer.getBrightColor();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ?
                        Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Scale the bitmap down so that it's smallest dimension is
     * {@value #CALCULATE_BITMAP_MIN_DIMENSION}px. If {@code bitmap} is smaller than this, than it
     * is returned.
     */
    private Bitmap scaleBitmapDown(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }

        final int minDimension = Math.min(bitmap.getWidth(), bitmap.getHeight());

        if (minDimension <= CALCULATE_BITMAP_MIN_DIMENSION) {
            return bitmap;
        }

        final float scaleRatio = CALCULATE_BITMAP_MIN_DIMENSION / (float) minDimension;
        return Bitmap.createScaledBitmap(bitmap,
                Math.round(bitmap.getWidth() * scaleRatio),
                Math.round(bitmap.getHeight() * scaleRatio),
                false);
    }

    /*MODIFIED-BEGIN by xiangnan.zhou, 2016-04-08,BUG-1923725*/
    private int getCurrentWeatherIcon() {
        if (hasWeatherData) {
            int resId = 0;
            if (isDarkWallpaper || isLiveWallpaper) {
                resId = IconBackgroundUtil.getTimeWidgetWhiteIcon(mIcon);
            } else {
                resId = IconBackgroundUtil.getTimeWidgetBlackIcon(mIcon);
                /*MODIFIED-END by xiangnan.zhou,BUG-1923725*/
            }
            return resId;
        } else {
            if (isDarkWallpaper || isLiveWallpaper) {
                return R.drawable.ic_unknown;
            } else {
                return R.drawable.ic_black_unknown;
            }
        }
    }

    public void setCurrentColor(RemoteViews views) {
        if (isDarkWallpaper || isLiveWallpaper) {
            views.setInt(R.id.widget_time, "setTextColor", 0xFFFFFFFF);
            views.setInt(R.id.widget_week, "setTextColor", 0xFFFFFFFF);
            views.setInt(R.id.widget_date, "setTextColor", 0xFFFFFFFF);
            /*MODIFIED-BEGIN by xiangnan.zhou, 2016-04-08,BUG-1923725*/
            /*MODIFIED-BEGIN by qian-li, 2016-04-13,BUG-1940875*/
            views.setInt(R.id.widget_tv_city, "setTextColor", 0xFFFFFFFF);
            views.setInt(R.id.widget_tv_dot, "setTextColor", 0xFFFFFFFF); //MODIFIED by qian-li, 2016-04-13,BUG-1940875
            views.setInt(R.id.widget_tv_temp, "setTextColor", 0xFFFFFFFF);
        } else {
            views.setInt(R.id.widget_time, "setTextColor", 0xDD000000);
            views.setInt(R.id.widget_week, "setTextColor", 0xDD000000);
            views.setInt(R.id.widget_date, "setTextColor", 0xDD000000);
            views.setInt(R.id.widget_weather_icon, "setImageAlpha", 0xDD);
            views.setInt(R.id.widget_tv_city, "setTextColor", 0xDD000000);
            views.setInt(R.id.widget_tv_dot, "setTextColor", 0xDD000000); //MODIFIED by qian-li, 2016-04-13,BUG-1940875
            views.setInt(R.id.widget_tv_temp, "setTextColor", 0xDD000000);
            /*MODIFIED-END by qian-li,BUG-1940875*/
            /*MODIFIED-END by xiangnan.zhou,BUG-1923725*/
        }
    }

    private String getAlarmAction() {
        try {
            Class alarmClass = Class.forName("android.provider.AlarmClock");
            try {
                Field field = alarmClass.getDeclaredField("ACTION_SHOW_ALARMS");
                if (field != null) {
                    return "android.intent.action.SHOW_ALARMS";
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        return "android.intent.action.SET_TIMER";
    }
}
