/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.widget;

import android.app.PendingIntent;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.tct.weather.LocateActivity;
import com.tct.weather.MainActivity;
import com.tct.weather.R;
import com.tct.weather.bean.City;
import com.tct.weather.bean.WeatherForShow;
import com.tct.weather.provider.DBHelper;
import com.tct.weather.util.ColorCutQuantizer;
import com.tct.weather.util.CommonUtils;
import com.tct.weather.util.IconBackgroundUtil;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * User : user
 * Date : 2016-04-01
 * Time : 18:54
 */
public class WeatherWidget extends AppWidgetProvider {

    public static final String TAG = "WeatherWidget";

    private final String TEMP_KEY = "settings_temp";

    private ArrayList<City> mCityList = new ArrayList<City>();
    private int mIndex = 0;
    private String locationKey = null;
    private String cityName = null;
    private WeatherForShow weather = null;

    private boolean isDarkWallpaper = true;
    private boolean isLiveWallpaper = false;

    private static final int CALCULATE_BITMAP_MIN_DIMENSION = 500;

    private static final int NO_CITY = 0;
    private static final int NORMAL = 1;
    private static int widgetState = NO_CITY;

    private UpdateAsyncTask updateAsyncTask = null;
    private DBHelper mDBHelper = null; //MODIFIED by qian-li, 2016-04-19,BUG-1944797

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onReceive(context, intent);
        Log.i(TAG, "onReceive : intent : " + intent.toString());

        final String action = intent.getAction();
        if ("android.appwidget.action.APPWIDGET_UPDATE".equals(action)
                || "android.intent.action.WEATHER_BROADCAST".equals(action)
                || "android.intent.action.DELETE_CITY".equals(action)
                || "android.intent.action.WALLPAPER_CHANGED".equals(action)
                || "android.intent.action.UNIT_BROADCAST".equals(action)) {
            if (updateAsyncTask != null) {
                updateAsyncTask.cancel(true);
            }
            updateAsyncTask = new UpdateAsyncTask(context);
            updateAsyncTask.execute((Void[]) null);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.i(TAG, "onUpdate");
        for (int appWidgetId : appWidgetIds) {
            Log.i(TAG, "appWidgetId : " + appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.i(TAG, "onEnabled");
        /*MODIFIED-BEGIN by qian-li, 2016-04-19,BUG-1944797*/
        mDBHelper = new DBHelper(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        mDBHelper = null;
    }

    private class UpdateAsyncTask extends AsyncTask<Void, Void, Void> {

        private Context mContext;

        private UpdateAsyncTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected Void doInBackground(Void... params) {
            queryCurrentWeather(mContext);
            viewsUpdate(mContext);
            return null;
        }
    }

    private void queryCurrentWeather(Context mContext) {
        Log.d(TAG, "queryCurrentWeather");
        mIndex = 0;
        if (mDBHelper == null) {
            mDBHelper = new DBHelper(mContext);
        }
        /*MODIFIED-END by qian-li,BUG-1944797*/
        mCityList = mDBHelper.getCityListFromDB();
        if (mCityList != null && mCityList.size() > 0) {
            for (int i = 0; i < mCityList.size(); i++) {
                if (mCityList.get(i).isAutoLocate()) {
                    mIndex = i;
                    break;
                }
            }
            City city = mCityList.get(mIndex);
            cityName = city.getCityName();
            locationKey = city.getLocationKey();
            weather = mDBHelper.getWeatherForShow(locationKey);
            if (null == weather) {
                Log.e(TAG, "weather is null");
            }
        }
    }

    private void viewsUpdate(Context context) {
        Log.i(TAG, "viewsUpdate");
        AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = mAppWidgetManager.getAppWidgetIds(new ComponentName(context, WeatherWidget.class));
        for (int appWidgetId : appWidgetIds) {
            Log.i(TAG, "viewsUpdate : appWidgetId : " + appWidgetId);
            viewUpdate(context, appWidgetId);
        }
    }

    private void viewUpdate(Context context, final int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather_little_layout);

        boolean isUnitC;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String tempUnit = settings.getString(TEMP_KEY, "1");
        if (TextUtils.equals(tempUnit, "1")) {
            isUnitC = true;
        } else {
            isUnitC = false;
        }

        isDarkWallpaper = getBackgroundRgb(context);

        int size = mCityList.size();
        if (size == 0) {
            widgetState = NO_CITY;
            setViewsState(views, widgetState);
            setCurrentColor(views, widgetState);
            views.setImageViewResource(R.id.little_widget_iv_icon, getCurrentWeatherIcon(null));

            Intent selectLocationIntent = new Intent(context, LocateActivity.class);
            PendingIntent pSelectLocationIntent = PendingIntent.getActivity(context, 0, selectLocationIntent, 0);
            views.setOnClickPendingIntent(R.id.little_widget_iv_add, pSelectLocationIntent);

            Intent selectWeatherIconIntent = new Intent(context, LocateActivity.class);
            PendingIntent pSelectWeatherIconIntent = PendingIntent.getActivity(context, 0, selectWeatherIconIntent, 0);
            views.setOnClickPendingIntent(R.id.little_widget_iv_icon, pSelectWeatherIconIntent);

        } else {
            if (null == weather) {
                return;
            }

            widgetState = NORMAL;
            setViewsState(views, widgetState);
            setCurrentColor(views, widgetState);
            views.setImageViewResource(R.id.little_widget_iv_icon, getCurrentWeatherIcon(weather.getIcon()));

            views.setTextViewText(R.id.little_widget_tv_city, cityName);
            views.setTextViewText(R.id.little_widget_tv_desc, weather.getText());
            if (isUnitC) {
                views.setTextViewText(R.id.little_widget_tv_temp, CommonUtils.deletaDec(weather.getTemp()) + "°");
            } else {
                views.setTextViewText(R.id.little_widget_tv_temp, CommonUtils.c2f(weather.getTemp()) + "°");
            }

            Intent mainIntent = new Intent(context, MainActivity.class);
            mainIntent.putExtra("newCityKey", locationKey);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pMainIntent = PendingIntent.getActivity(context, 2, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.little_widget_ll_weather_info, pMainIntent);
            views.setOnClickPendingIntent(R.id.little_widget_ll_weather_detail, pMainIntent);
        }

        String langcode = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry().toLowerCase();
        Log.i(TAG, "langcode:" + langcode);
        CharSequence dateFormat = null;
        if (langcode.contains("ru")) {
            dateFormat = DateFormat.format("EEE, dd.MM.yyyy", Calendar.getInstance());
        } else {
            dateFormat = DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEE, d MMM");
        }
        views.setCharSequence(R.id.little_widget_tc_date, "setFormat12Hour", dateFormat);
        views.setCharSequence(R.id.little_widget_tc_date, "setFormat24Hour", dateFormat);
        if (langcode.contains("id")) {
            views.setCharSequence(R.id.little_widget_tc_time, "setFormat12Hour", "hh.mm");
            views.setCharSequence(R.id.little_widget_tc_time, "setFormat24Hour", "kk.mm");
        }

        Intent clockIntent = new Intent();
        clockIntent.setAction(getAlarmAction());
        PendingIntent pClockIntent = PendingIntent.getActivity(context, 0, clockIntent, 0);
        views.setOnClickPendingIntent(R.id.little_widget_tc_time, pClockIntent);
        views.setOnClickPendingIntent(R.id.little_widget_tc_date, pClockIntent);

        AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(context);
        mAppWidgetManager.updateAppWidget(appWidgetId, views);

    }

    //get the lanch wallpaper  RGB
    private boolean getBackgroundRgb(Context context) {
        WallpaperManager mWallpaperManager = WallpaperManager.getInstance(context);
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

    private static Bitmap drawableToBitmap(Drawable drawable) {
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

    public void setCurrentColor(RemoteViews views, int widgetState) {
        switch (widgetState) {
            case NO_CITY:
                if (isDarkWallpaper || isLiveWallpaper) {
                    views.setInt(R.id.little_widget_tc_time, "setTextColor", Color.WHITE);
                    views.setInt(R.id.little_widget_tc_date, "setTextColor", Color.WHITE);
                    /*MODIFIED-BEGIN by qian-li, 2016-04-13,BUG-1940875*/
                    views.setImageViewResource(R.id.little_widget_iv_unknown, R.drawable.ic_thin_white_unknown);
                    views.setImageViewResource(R.id.little_widget_iv_add, R.drawable.ic_thin_white_add);
                } else {
                    views.setInt(R.id.little_widget_tc_time, "setTextColor", 0xDD000000);
                    views.setInt(R.id.little_widget_tc_date, "setTextColor", 0xDD000000);
                    views.setImageViewResource(R.id.little_widget_iv_unknown, R.drawable.ic_thin_black_unknown);
                    views.setImageViewResource(R.id.little_widget_iv_add, R.drawable.ic_thin_black_add);
                    views.setInt(R.id.little_widget_iv_add, "setImageAlpha", 0xDD);
                    views.setInt(R.id.little_widget_iv_unknown, "setImageAlpha", 0xDD);
                    /*MODIFIED-END by qian-li,BUG-1940875*/
                }
                break;
            case NORMAL:
                if (isDarkWallpaper || isLiveWallpaper) {
                    views.setInt(R.id.little_widget_tv_desc, "setTextColor", Color.WHITE);
                    views.setInt(R.id.little_widget_tc_time, "setTextColor", Color.WHITE);
                    views.setInt(R.id.little_widget_tc_date, "setTextColor", Color.WHITE);
                    views.setInt(R.id.little_widget_tv_temp, "setTextColor", Color.WHITE);
                    views.setInt(R.id.little_widget_tv_city, "setTextColor", Color.WHITE);
                } else {
                    views.setInt(R.id.little_widget_tv_desc, "setTextColor", 0xDD000000);
                    views.setInt(R.id.little_widget_tc_time, "setTextColor", 0xDD000000);
                    views.setInt(R.id.little_widget_tc_date, "setTextColor", 0xDD000000);
                    views.setInt(R.id.little_widget_tv_temp, "setTextColor", 0xDD000000);
                    views.setInt(R.id.little_widget_tv_city, "setTextColor", 0xDD000000);
                    views.setInt(R.id.little_widget_iv_icon, "setImageAlpha", 0xDD);
                }
                break;
            default:
                break;
        }
    }

    private void setViewsState(RemoteViews views, int widgetState) {
        switch (widgetState) {
            case NO_CITY:
                views.setInt(R.id.little_widget_tv_desc, "setVisibility", View.GONE);
                /*MODIFIED-BEGIN by qian-li, 2016-04-13,BUG-1940875*/
                views.setInt(R.id.little_widget_fl_icon, "setVisibility", View.GONE);
                views.setInt(R.id.little_widget_fl_temp, "setVisibility", View.GONE);
                views.setInt(R.id.little_widget_tv_city, "setVisibility", View.GONE);
                views.setInt(R.id.little_widget_iv_unknown, "setVisibility", View.VISIBLE);
                views.setInt(R.id.little_widget_iv_add, "setVisibility", View.VISIBLE);
                break;
            case NORMAL:
                views.setInt(R.id.little_widget_tv_desc, "setVisibility", View.VISIBLE);
                views.setInt(R.id.little_widget_fl_icon, "setVisibility", View.VISIBLE);
                views.setInt(R.id.little_widget_fl_temp, "setVisibility", View.VISIBLE);
                views.setInt(R.id.little_widget_tv_city, "setVisibility", View.VISIBLE);
                views.setInt(R.id.little_widget_iv_unknown, "setVisibility", View.GONE);
                /*MODIFIED-END by qian-li,BUG-1940875*/
                views.setInt(R.id.little_widget_iv_add, "setVisibility", View.GONE);
                break;
            default:
                break;
        }
    }

    private int getCurrentWeatherIcon(String icon) {
        if (icon != null) {
            int resId = 0;
            if (isDarkWallpaper || isLiveWallpaper) {
                /*MODIFIED-BEGIN by qian-li, 2016-04-13,BUG-1940875*/
                resId = IconBackgroundUtil.getLittleWidgetWhiteIcon(icon);
            } else {
                resId = IconBackgroundUtil.getLittleWidgetBlackIcon(icon);
            }
            return resId;
        } else {
            if (isDarkWallpaper || isLiveWallpaper) {
                return R.drawable.ic_thin_white_unknown;
            } else {
                return R.drawable.ic_thin_black_unknown;
                /*MODIFIED-END by qian-li,BUG-1940875*/
            }
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
