package com.tct.weather.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.tct.weather.provider.WeatherInfo.CityInfo;
import com.tct.weather.provider.WeatherInfo.Current;
import com.tct.weather.provider.WeatherInfo.DailyInfo;

public class WeatherContentProvider extends ContentProvider {
    private DBHelper mDbHelper = null;
    private static final UriMatcher sUriMatcher;
    private static final int Weather = 1;
    private static final int Weather_Id = 2;
    private static final int City = 3;
    private static final int City_Id = 4;
    private static final int Daily = 5;
    private static final int Daily_Id = 6;
    private static final int Unit = 7;
    private static final int Unit_Id = 8;

    private static HashMap<String, String> WeatherProjection;
    private static HashMap<String, String> cityProjection;
    private static HashMap<String, String> dailyProjection;
    private static HashMap<String, String> hourlyProjection;
    private static HashMap<String, String> unitProjection;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(WeatherInfo.AUTHORITY, "current", Weather);
        sUriMatcher.addURI(WeatherInfo.AUTHORITY, "current/#", Weather_Id);
        sUriMatcher.addURI(WeatherInfo.AUTHORITY, "city", City);
        sUriMatcher.addURI(WeatherInfo.AUTHORITY, "city/#", City_Id);
        sUriMatcher.addURI(WeatherInfo.AUTHORITY, "daily", Daily);
        sUriMatcher.addURI(WeatherInfo.AUTHORITY, "daily/#", Daily_Id);
        sUriMatcher.addURI(WeatherInfo.AUTHORITY, "unitinfo", Unit);
        sUriMatcher.addURI(WeatherInfo.AUTHORITY, "unitinfo/#", Unit_Id);

        WeatherProjection = new HashMap<String, String>();
        WeatherProjection.put(Current.LOCATION_KEY, Current.LOCATION_KEY);
        WeatherProjection.put(Current.ICON, Current.ICON);
        WeatherProjection.put(Current.CURRENT_TEMPERATURE, Current.CURRENT_TEMPERATURE);
        WeatherProjection.put(Current.LOW_TEMPERATURE, Current.LOW_TEMPERATURE);
        WeatherProjection.put(Current.HIGH_TEMPERATURE, Current.HIGH_TEMPERATURE);
        WeatherProjection.put(Current.WEATHER_DESCRIPTION, Current.WEATHER_DESCRIPTION);
        WeatherProjection.put(Current.PRECIPTATION, Current.PRECIPTATION);
        WeatherProjection.put(Current.UV_INDEX, Current.UV_INDEX);
        WeatherProjection.put(Current.WIND_SPEED, Current.WIND_SPEED);
        WeatherProjection.put(Current.PRESSURE, Current.PRESSURE);
        WeatherProjection.put(Current.VISIBILITY, Current.VISIBILITY);
        WeatherProjection.put(Current.HUMIDITY, Current.HUMIDITY);
        WeatherProjection.put(Current.REALFEEL, Current.REALFEEL);
        WeatherProjection.put(Current.WINDDIR, Current.WINDDIR);//[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398,
        //[FEATURE]-Add-BEGIN by TSCD.qian-li,01/22/2016,1490227,[Launcher][Ergo v5.2.6]Boom Key
        WeatherProjection.put(Current.ISDAYTIME, Current.ISDAYTIME);
        //[FEATURE]-Add-END by TSCD.qian-li

        cityProjection = new HashMap<String, String>();
        cityProjection.put(CityInfo.LOCATION_KEY, CityInfo.LOCATION_KEY);
        cityProjection.put(CityInfo.CITY_NAME, CityInfo.CITY_NAME);
        cityProjection.put(CityInfo.UPDATE_TIME, CityInfo.UPDATE_TIME);
        cityProjection.put(CityInfo.STATE_NAME, CityInfo.STATE_NAME);
        cityProjection.put(CityInfo.ISAUTOLOCATE, CityInfo.ISAUTOLOCATE);//[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398,

        dailyProjection = new HashMap<String, String>();
        dailyProjection.put(DailyInfo.LOCATION_KEY, DailyInfo.LOCATION_KEY);
        dailyProjection.put(DailyInfo.ICON, DailyInfo.ICON);
        dailyProjection.put(DailyInfo.WEATHER_DESCRIPTION, DailyInfo.WEATHER_DESCRIPTION);
        dailyProjection.put(DailyInfo.LOW_TEMPERATURE, DailyInfo.LOW_TEMPERATURE);
        dailyProjection.put(DailyInfo.HIGH_TEMPERATURE, DailyInfo.HIGH_TEMPERATURE);
        dailyProjection.put(DailyInfo.WEEK, DailyInfo.WEEK);
        dailyProjection.put(DailyInfo.DATE, DailyInfo.DATE);

        unitProjection = new HashMap<String, String>();
        unitProjection.put(WeatherInfo.UnitInfo.ISUNITC, WeatherInfo.UnitInfo.ISUNITC);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DBHelper(this.getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case Weather:
                qBuilder.setTables("current");
                qBuilder.setProjectionMap(WeatherProjection);
                break;
            case Weather_Id:
                qBuilder.setTables("current");
                qBuilder.setProjectionMap(WeatherProjection);
                qBuilder.appendWhere(Current.LOCATION_KEY + "=" + uri.getPathSegments().get(1));
                break;
            case City:
                qBuilder.setTables("city");
                qBuilder.setProjectionMap(cityProjection);
                break;
            case City_Id:
                qBuilder.setTables("city");
                qBuilder.setProjectionMap(cityProjection);
                qBuilder.appendWhere(CityInfo.LOCATION_KEY + "=" + uri.getPathSegments().get(1));
                break;
            case Daily:
                qBuilder.setTables("forecast");
                qBuilder.setProjectionMap(dailyProjection);
                break;
            case Daily_Id:
                qBuilder.setTables("forecast");
                qBuilder.setProjectionMap(dailyProjection);
                qBuilder.appendWhere(CityInfo.LOCATION_KEY + "=" + uri.getPathSegments().get(1));
                break;
            case Unit:
                qBuilder.setTables("unitinfo");
                qBuilder.setProjectionMap(unitProjection);
                break;
            case Unit_Id:
                qBuilder.setTables("unitinfo");
                qBuilder.setProjectionMap(unitProjection);
                qBuilder.appendWhere(WeatherInfo.UnitInfo.ISUNITC + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Uri error！ " + uri);
        }
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = qBuilder.query(db, projection, selection, selectionArgs, null, null, null);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case Weather:
                return Current.CONTENT_TYPE;
            case Weather_Id:
                return Current.CONTENT_ITEM_TYPE;
            case City:
                return CityInfo.CONTENT_TYPE;
            case City_Id:
                return CityInfo.CONTENT_ITEM_TYPE;
            case Daily:
                return DailyInfo.CONTENT_TYPE;
            case Daily_Id:
                return DailyInfo.CONTENT_ITEM_TYPE;
            case Unit:
                return WeatherInfo.UnitInfo.CONTENT_TYPE;
            case Unit_Id:
                return WeatherInfo.UnitInfo.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknow URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case Weather:
                count = db.update("Weather", values, selection, selectionArgs);
                break;
            case Weather_Id:
                String WeatherId = uri.getPathSegments().get(1);
                count = db.update("Weather", values, Current.LOCATION_KEY + "=" + WeatherId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI erro :" + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
