package com.tct.weather.provider;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.tct.weather.bean.City;
import com.tct.weather.bean.Currentconditions;
import com.tct.weather.bean.DayForShow;
import com.tct.weather.bean.Hour;
import com.tct.weather.bean.Local;
import com.tct.weather.bean.Weather;
import com.tct.weather.bean.WeatherForShow;
import com.tct.weather.util.CommonUtils;
import com.tct.weather.util.CustomizeUtils;
/*
 ==========================================================================
 *HISTORY
 *
 *Tag            Date              Author        Description
 *============== ============ =============== ==============================
 *BUGFIX-1284454  2016/1/13      xing.zhao       [Weather][Force Close]It pop up FC ,When refresh weather comming phone
 *===========================================================================
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "weather DBHelper";

    private static final String CREATECITY = "CREATE TABLE IF NOT EXISTS city (_id INTEGER PRIMARY KEY,locationKey VARCHAR NOT NULL , latitude VARCHAR,longitude VARCHAR,cityName VARCHAR,country VARCHAR,state VARCHAR,updateTime VARCHAR,isautolocate INTEGER)";//CR 447398 - ting.chen@tct-nj.com - 001 added
    //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398,
    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/01/2016,1539263,[jrdlogger]com.tct.weather ANR
    private static final String CREATECURRENT = "CREATE TABLE IF NOT EXISTS current (id INTEGER PRIMARY KEY AUTOINCREMENT, locationKey VARCHAR , icon VARCHAR,text VARCHAR,temp VARCHAR,high VARCHAR,low VARCHAR,realfeel VARCHAR,time VARCHAR,humidity VARCHAR,wind VARCHAR,visibility VARCHAR, url VARCHAR,tempHigh VARCHAR,tempLow VARCHAR,pressure VARCHAR,preciptation VARCHAR,uv_index VARCHAR,uv_desc VARCHAR,winddir VARCHAR,isdaytime INTEGER)";
    //[BUGFIX]-Add-END by TSCD.qian-li
    private static final String CREATEFORECAST = "CREATE TABLE IF NOT EXISTS forecast (id INTEGER PRIMARY KEY AUTOINCREMENT, locationKey VARCHAR ,dayNum VARCHAR,icon VARCHAR,high VARCHAR,low VARCHAR,week VARCHAR,date VARCHAR,url VARCHAR,phrase VARCHAR,precipitation VARCHAR)";//modify by shenxin for PR460544
    private static final String CREATEHOURLY = "CREATE TABLE IF NOT EXISTS hourly (id INTEGER PRIMARY KEY AUTOINCREMENT, locationKey VARCHAR ,week VARCHAR,timeId VARCHAR,time VARCHAR,icon VARCHAR,temp VARCHAR,text VARCHAR)";
    private static final String CREATEUNITINFO = "CREATE TABLE IF NOT EXISTS unitinfo (id VARCHAR PRIMARY KEY, isUnitC INTEGER)";

    private static final String ADDHUMIDITYTOCURRENT = "alter table current add column humidity VARCHAR;";
    private static final String ADDWINDTOCURRENT = "alter table current add column wind VARCHAR;";
    private static final String ADDVISIBILITYTOCURRENT = "alter table current add column visibility VARCHAR;";
    private static final String ADDURLTOCURRENT = "alter table current add column url VARCHAR;";
    //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398,
    private static final String ADDWINDDIR = "alter table current add column winddir VARCHAR;";
    private static final String ADDISDAYTIME = "alter table current add column isdaytime INTEGER;";


    private static final String ADD_CITY_LATITUDE = "alter table city add column latitude VARCHAR;";
    private static final String ADD_CITY_LONGITUDE = "alter table city add column longitude VARCHAR;";
    private static final String ADD_CURRENT_TEMP_HIGH = "alter table current add column tempHigh VARCHAR;";
    private static final String ADD_CURRENT_TEMP_LOW = "alter table current add column tempLow VARCHAR;";
    private static final String ADD_CURRENT_PRECITION = "alter table current add column preciptation VARCHAR;";
    private static final String ADD_CURRENT_PRESSURE = "alter table current add column pressure VARCHAR;";
    private static final String ADD_CURRENT_UV = "alter table current add column uv_index VARCHAR;";
    private static final String ADD_CURRENT_UV_DESC = "alter table current add column uv_desc VARCHAR;";
    private static final String ADD_DAILY_PHRASH = "alter table forecast add column phrase VARCHAR;";
    private static final String ADD_DAILY_PRECIITATION = "alter table forecast add column precipitation VARCHAR;";

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/01/2016,1539263,[jrdlogger]com.tct.weather ANR
    private static final String CREATE_BACKUP_TABLE = "CREATE TEMPORARY TABLE backup (locationKey VARCHAR , icon VARCHAR,text VARCHAR,temp VARCHAR,high VARCHAR,low VARCHAR,realfeel VARCHAR,time VARCHAR,humidity VARCHAR,wind VARCHAR,visibility VARCHAR, url VARCHAR,tempHigh VARCHAR,tempLow VARCHAR,pressure VARCHAR,preciptation VARCHAR,uv_index VARCHAR,uv_desc VARCHAR,winddir VARCHAR,isdaytime INTEGER)";
    private static final String COPY_CURRENT_2_BACKUP = "INSERT INTO backup SELECT locationKey VARCHAR , icon VARCHAR,text VARCHAR,temp VARCHAR,high VARCHAR,low VARCHAR,realfeel VARCHAR,time VARCHAR,humidity VARCHAR,wind VARCHAR,visibility VARCHAR, url VARCHAR,tempHigh VARCHAR,tempLow VARCHAR,pressure VARCHAR,preciptation VARCHAR,uv_index VARCHAR,uv_desc VARCHAR,winddir VARCHAR,isdaytime INTEGER FROM current";
    private static final String COPY_BACKUP_2_CURRENT = "INSERT INTO current SELECT null, locationKey VARCHAR , icon VARCHAR,text VARCHAR,temp VARCHAR,high VARCHAR,low VARCHAR,realfeel VARCHAR,time VARCHAR,humidity VARCHAR,wind VARCHAR,visibility VARCHAR, url VARCHAR,tempHigh VARCHAR,tempLow VARCHAR,pressure VARCHAR,preciptation VARCHAR,uv_index VARCHAR,uv_desc VARCHAR,winddir VARCHAR,isdaytime INTEGER FROM backup";
    private static final String DELETE_CURRENT_TABLE = "DROP TABLE current";
    private static final String DELETE_BACKUP_TABLE = "DROP TABLE backup";
    //[BUGFIX]-Add-END by TSCD.qian-li

    private static final String DELETE_CITY_TABLE = "DROP TABLE IF EXISTS city";
    private static final String DELETE_FORECAST_TABLE = "DROP TABLE IF EXISTS forecast";

    public static final String CITYID = "cityId:";

    private SQLiteDatabase mDb;

    // [BUGFIX]-Add-BEGIN by TSCD.qian-li,12/19/2015,1192671,[Android6.0][Weather_v5.2.8.1.0304.0]Weather crash and can't locate after install it
//    private static final int VERSION = 4;
    // [BUGFIX]-Add-END by TSCD.qian-li

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/01/2016,1539263,[jrdlogger]com.tct.weather ANR
//    private static final int VERSION = 5;
    //[BUGFIX]-Add-END by TSCD.qian-li

    private static final int VERSION = 6;

    private static final String DATABASE_NAME = "weather";
    //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/27/2015,983895,deleta weather_24forecast_card
    private Context mContext = null;
    //[FEATURE]-Add-END by TSCD.peng.du
    public DBHelper(Context context, String name) {
        super(context, name, null, VERSION);
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/27/2015,983895,deleta weather_24forecast_card
        mContext = context;
        //[FEATURE]-Add-END by TSCD.peng.du
        mDb = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATECITY);
        db.execSQL(CREATECURRENT);
        db.execSQL(CREATEFORECAST);
        db.execSQL(CREATEHOURLY);
        db.execSQL(CREATEUNITINFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "on upgrade oldversion=" + oldVersion + " newversion=" + newVersion);
        if (oldVersion != newVersion) {
            db.beginTransaction();
            try {
                if (oldVersion <= 1) {
                    db.execSQL(ADDHUMIDITYTOCURRENT);
                    db.execSQL(ADDWINDTOCURRENT);
                    db.execSQL(ADDVISIBILITYTOCURRENT);
                    db.execSQL(ADDURLTOCURRENT);
                }
                if (oldVersion <= 2) {
                    db.execSQL(ADD_CITY_LATITUDE);
                    db.execSQL(ADD_CITY_LONGITUDE);
                    db.execSQL(ADD_CURRENT_TEMP_HIGH);
                    db.execSQL(ADD_CURRENT_TEMP_LOW);
                    db.execSQL(ADD_CURRENT_PRECITION);
                    db.execSQL(ADD_CURRENT_PRESSURE);
                    db.execSQL(ADD_CURRENT_UV);
                    db.execSQL(ADD_CURRENT_UV_DESC);
                    db.execSQL(ADD_DAILY_PHRASH);
                    db.execSQL(ADD_DAILY_PRECIITATION);

                    // When the version from branch 02 to branch Rel3_03,this method will to be called
                    // Becase in branch 02, the database of temperature is fahrenheit, we must convert the temperature to cel.
                    mDb = db;
                    convertData();

                    // [BUGFIX]-Add-BEGIN by TSCD.qian-li,12/19/2015,1192671,[Android6.0][Weather_v5.2.8.1.0304.0]Weather crash and can't locate after install it
//                    db.execSQL(ADDWINDDIR);
                }
                if (oldVersion <= 3) {
                    db.execSQL(CREATEUNITINFO);
                    db.execSQL(ADDWINDDIR);
                }
                if (oldVersion <= 4) {
                    db.execSQL(ADDISDAYTIME);
                }
                if (oldVersion <= 5) {
                    db.execSQL(CREATE_BACKUP_TABLE);
                    db.execSQL(COPY_CURRENT_2_BACKUP);
                    db.execSQL(DELETE_CURRENT_TABLE);
                    db.execSQL(CREATECURRENT);
                    db.execSQL(COPY_BACKUP_2_CURRENT);
                    db.execSQL(DELETE_BACKUP_TABLE);
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLiteException : " + e.toString());
            }
            db.setTransactionSuccessful();
            db.endTransaction();
//            switch (newVersion) {
//                case 2:
//                    db.beginTransaction();
//                    db.execSQL(ADDHUMIDITYTOCURRENT);
//                    db.execSQL(ADDWINDTOCURRENT);
//                    db.execSQL(ADDVISIBILITYTOCURRENT);
//                    db.execSQL(ADDURLTOCURRENT);
//                    db.setTransactionSuccessful();
//                    db.endTransaction();
//                case 3:
//                    db.beginTransaction();
//                    db.execSQL(ADD_CITY_LATITUDE);
//                    db.execSQL(ADD_CITY_LONGITUDE);
//                    db.execSQL(ADD_CURRENT_TEMP_HIGH);
//                    db.execSQL(ADD_CURRENT_TEMP_LOW);
//                    db.execSQL(ADD_CURRENT_PRECITION);
//                    db.execSQL(ADD_CURRENT_PRESSURE);
//                    db.execSQL(ADD_CURRENT_UV);
//                    db.execSQL(ADD_CURRENT_UV_DESC);
//                    db.execSQL(ADD_DAILY_PHRASH);
//                    db.execSQL(ADD_DAILY_PRECIITATION);
//                    // [BUGFIX]-Add-BEGIN by TSCD.qian-li,12/19/2015,1192671,[Android6.0][Weather_v5.2.8.1.0304.0]Weather crash and can't locate after install it
////                    db.execSQL(ADDWINDDIR);
//                    db.setTransactionSuccessful();
//                    db.endTransaction();
//                    break;
//                // [BUGFIX]-Add-BEGIN by TSCD.qian-li,12/19/2015,1192671,[Android6.0][Weather_v5.2.8.1.0304.0]Weather crash and can't locate after install it
//                case 4:
//                    db.beginTransaction();
//                    db.execSQL(ADDWINDDIR);
//                    db.setTransactionSuccessful();
//                    db.endTransaction();
//                    break;
//                // [BUGFIX]-Add-END by TSCD.qian-li
//                case 5:
//                    db.beginTransaction();
//                    db.execSQL(ADDISDAYTIME);
//                    db.setTransactionSuccessful();
//                    db.endTransaction();
//                    break;
//                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/01/2016,1539263,[jrdlogger]com.tct.weather ANR
//                case 6:
//                    db.beginTransaction();
//                    db.execSQL(CREATE_BACKUP_TABLE);
//                    db.execSQL(COPY_CURRENT_2_BACKUP);
//                    db.execSQL(DELETE_CURRENT_TABLE);
//                    db.execSQL(CREATECURRENT);
//                    db.execSQL(COPY_BACKUP_2_CURRENT);
//                    db.execSQL(DELETE_BACKUP_TABLE);
//                    db.setTransactionSuccessful();
//                    db.endTransaction();
//                    break;
//                //[BUGFIX]-Add-END by TSCD.qian-li
//            }
        }
    }

    private void convertData() {
        Log.i(TAG, "convertData");

        ArrayList<String> locationKeyList = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = mDb.query("city", new String[] {"locationKey"}, null, null, null, null, null, null);
            if (cursor != null) {
                int i = 0;
                while (cursor.moveToNext()) {
                    String locationKey = cursor.getString(cursor.getColumnIndex("locationKey"));
                    locationKeyList.add(i++, locationKey);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "getCityListFromDB error :: " + e.getMessage());
        } finally {
            if (cursor != null) {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
        }

        for (String locationKey : locationKeyList) {
            Log.i(TAG, "city : " + locationKey);

            Cursor cursor1 = null;
            try {
                if(null != locationKey) {
                    cursor1 = mDb.rawQuery("SELECT * FROM current WHERE locationKey = ?", new String[]{locationKey});
                }

                ContentValues contentValues0 = new ContentValues();

                if (cursor1 != null && cursor1.moveToFirst()) {
                    String temp = cursor1.getString(cursor1.getColumnIndex("temp"));
                    contentValues0.put("temp", CommonUtils.f2c(temp));
                    String currentHigh = cursor1.getString(cursor1.getColumnIndex("high"));
                    contentValues0.put("high", CommonUtils.f2c(currentHigh));
                    String currentLow = cursor1.getString(cursor1.getColumnIndex("low"));
                    contentValues0.put("low", CommonUtils.f2c(currentLow));
                    String realfeel = cursor1.getString(cursor1.getColumnIndex("realfeel"));
                    contentValues0.put("realfeel", CommonUtils.f2c(realfeel));

                    mDb.update("current", contentValues0, "locationKey = ?", new String[]{locationKey});
                }

            } catch (Exception e) {
                Log.e(TAG, "getWeather error :: " + e.getMessage());
            } finally {
                if (cursor1 != null) {
                    if (!cursor1.isClosed()) {
                        cursor1.close();
                    }
                }
            }

            Cursor c = null;
            try {
                for (int i = 0; i < 5; i++) {
                    ContentValues contentValues1 = new ContentValues();
                    String dayNum = (i + 1) + "";
                    c = mDb.rawQuery("SELECT * FROM forecast WHERE locationKey = ? and dayNum = ?", new String[]{locationKey, dayNum});

                    if (c != null && c.moveToFirst()) {
                        String dayHigh = c.getString(c.getColumnIndex("high"));
                        contentValues1.put("high", CommonUtils.f2c(dayHigh));
                        String dayLow = c.getString(c.getColumnIndex("low"));
                        contentValues1.put("low", CommonUtils.f2c(dayLow));
                    }
                    mDb.update("forecast", contentValues1, "locationKey = ?and dayNum=?", new String[]{locationKey, dayNum});
                }
            } catch (Exception e) {
                Log.e(TAG, "getDay error :: " + e.getMessage());
            } finally {
                if (c != null) {
                    if (!c.isClosed()) {
                        c.close();
                    }
                }
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "on downgrade oldversion=" + oldVersion + " newversion=" + newVersion);
        if (oldVersion > newVersion) {
            db.beginTransaction();
            try {
                db.execSQL(DELETE_CITY_TABLE);
                db.execSQL(DELETE_CURRENT_TABLE);
                db.execSQL(DELETE_FORECAST_TABLE);
                onCreate(db);
            } catch (SQLiteException sqle) {
                Log.e(TAG, "SQLiteException : " + sqle.getMessage());
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    /**
     * get city by locationkey
     *
     * @param locationKey
     * @return
     */
    public synchronized City getCityByLocationKey(String locationKey) {
        if (!mDb.isOpen()) {
            return null;
        }

        Cursor cursor = null;

        try {
            cursor = mDb.query("city", null, "locationkey = ?", new String[]{locationKey}, null, null, null, null);
            if (cursor == null) {
                return null;
            }

            if (cursor.moveToFirst()) {
                String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
                String cityName = cursor.getString(cursor.getColumnIndex("cityName"));
                String country = cursor.getString(cursor.getColumnIndex("country"));
                String state = cursor.getString(cursor.getColumnIndex("state"));
                boolean isAutoLocate = cursor.getInt(cursor.getColumnIndex("isautolocate")) == 1 ? true : false;
                String updateTime = cursor.getString(cursor.getColumnIndex("updateTime"));

                City myCity = new City(locationKey, latitude, longitude, cityName, country, state, updateTime, isAutoLocate);
                return myCity;
            }
        } catch (Exception e) {
            Log.e(TAG, "getCityByLocationKey error :: " + e.getMessage());
        } finally {
            if (cursor != null) {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
                cursor = null;
            }
        }
        return null;
    }

    /**
     * get all the citys from database
     *
     * @return
     */
    //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/17/2015,1176380,[Weather]ACC Widget DEV
    public synchronized String getAutoLocateCity() {
        String locationKey =null;
        boolean isfirst = true;
        if (!mDb.isOpen()) {
            return locationKey;
        }
        Cursor cursor = null;

        try {
            String[] columns = new String[]{"locationKey", "latitude", "longitude", "cityName",
                    "country", "state", "updateTime", "isautolocate"};
            cursor = mDb.query("city", columns, null, null, null, null, null, null);
            if (cursor == null) {
                return locationKey;
            }
            while (cursor.moveToNext()) {
                if (isfirst) {
                    locationKey = cursor.getString(cursor.getColumnIndex("locationKey"));
                    isfirst = false;
                }

                boolean isAutoLocate = cursor.getInt(cursor.getColumnIndex("isautolocate")) == 1 ? true : false;
                if (isAutoLocate){
                    locationKey = cursor.getString(cursor.getColumnIndex("locationKey"));
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getCityListFromDB error :: " + e.getMessage());
            return locationKey;
        } finally {
            if (cursor != null) {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
                cursor = null;
            }
        }
        Log.e(TAG, "return locationKey =  " + locationKey);
        return locationKey;
    }
    //[FEATURE]-Add-END by TSCD.peng.du
    public synchronized ArrayList<City> getCityListFromDB() {
        if (!mDb.isOpen()) {
            return new ArrayList<City>();
        }

        ArrayList<City> citys = new ArrayList<City>();
        Cursor cursor = null;

        try {
            String[] columns = new String[]{"locationKey", "latitude", "longitude", "cityName",
                    "country", "state", "updateTime", "isautolocate"};
            // update by jielong.xing at 2015-3-25 begin
            cursor = mDb.query("city", columns, null, null, null, null, "isautolocate desc", null);
            // update by jielong.xing at 2015-3-25 end
            if (cursor == null) {
                return citys;
            }
            while (cursor.moveToNext()) {
                String locationKey = cursor.getString(cursor.getColumnIndex("locationKey"));
                String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
                String cityName = cursor.getString(cursor.getColumnIndex("cityName"));
                String country = cursor.getString(cursor.getColumnIndex("country"));
                String state = cursor.getString(cursor.getColumnIndex("state"));
                String updateTime = cursor.getString(cursor.getColumnIndex("updateTime"));
                boolean isAutoLocate = cursor.getInt(cursor.getColumnIndex("isautolocate")) == 1 ? true : false;
                citys.add(new City(locationKey, latitude, longitude, cityName, state, updateTime, country, isAutoLocate));
            }
        } catch (Exception e) {
            Log.e(TAG, "getCityListFromDB error :: " + e.getMessage());
            return new ArrayList<City>();
        } finally {
            if (cursor != null) {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
                cursor = null;
            }
        }
        return citys;
    }

    public WeatherForShow getWeatherForShow(String locationKey) {
        WeatherForShow weather = null;
        if (!mDb.isOpen()) {
            return null;
        }

        Cursor cursor = null;
        try {
            //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/17/2015,1176380,[Weather]ACC Widget DEV
            if(null != locationKey) {
                cursor = mDb.rawQuery("SELECT * FROM current WHERE locationKey = ?", new String[]{locationKey});
            }else{
                cursor = mDb.query("current", null, null, null, null, null, null, null);
            }
            //[FEATURE]-Add-END by TSCD.peng.du
            if (cursor == null) {
                return null;
            }

            if (cursor.moveToFirst()) {
                String icon = cursor.getString(cursor.getColumnIndex("icon"));
                String text = cursor.getString(cursor.getColumnIndex("text"));
                String temp = cursor.getString(cursor.getColumnIndex("temp"));
                String high = cursor.getString(cursor.getColumnIndex("high"));
                String low = cursor.getString(cursor.getColumnIndex("low"));
                String realfeel = cursor.getString(cursor.getColumnIndex("realfeel"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String humidity = cursor.getString(cursor.getColumnIndex("humidity"));
                String wind = cursor.getString(cursor.getColumnIndex("wind"));
                String visibility = cursor.getString(cursor.getColumnIndex("visibility"));
                String url = cursor.getString(cursor.getColumnIndex("url"));
                String tempHigh = cursor.getString(cursor.getColumnIndex("tempHigh"));
                String tempLow = cursor.getString(cursor.getColumnIndex("tempLow"));
                String preciptation = cursor.getString(cursor.getColumnIndex("preciptation"));
                String pressure = cursor.getString(cursor.getColumnIndex("pressure"));
                String uv_index = cursor.getString(cursor.getColumnIndex("uv_index"));
                String uv_desc = cursor.getString(cursor.getColumnIndex("uv_desc"));
                //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398,
                //[Android6.0][Weather_v5.2.2.1.0307.0]The weather widget data is not same as weather in L1 screen
                String winddir = cursor.getString(cursor.getColumnIndex("winddir"));
                //[FEATURE]-Add-END by TSCD.peng.du
                int isDayTime = cursor.getInt(cursor.getColumnIndex("isdaytime"));

                weather = new WeatherForShow(icon, text, temp, high, low, tempHigh, tempLow, realfeel, time, humidity, wind, visibility, preciptation, pressure, uv_index, uv_desc, url, winddir, isDayTime);
            }
            cursor.close();

            // Fixed Defect 249495 by jielong.xing at 2015-5-18 begin
            if ((weather != null)&&TextUtils.isEmpty(weather.getIcon())) {
                this.deleteCity(locationKey);
                return null;
            }
            // Fixed Defect 249495 by jielong.xing at 2015-5-18 end
            if(null != locationKey) {
                cursor = mDb.rawQuery("SELECT cityName FROM city WHERE locationKey = ?", new String[]{locationKey});
                if (cursor == null) {
                    return null;
                }
                if (cursor.moveToFirst()) {
                    String city = cursor.getString(cursor.getColumnIndex("cityName"));
                    if (null != weather) {
                        weather.setCity(city);
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "getWeatherForShow error :: " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
                cursor = null;
            }
        }

        return weather;
    }

    public List<Hour> getHourList(String locationKey) {
        if (!mDb.isOpen()) {
            return null;
        }

        List<Hour> hourList = new ArrayList<Hour>();
        Cursor c = null;

        try {
            for (int i = 1; i <= 24; i++) {
                c = mDb.rawQuery("SELECT * FROM hourly WHERE locationKey = ?and timeId = ?", new String[]{locationKey, i + ""});
                if (c == null) {
                    return null;
                }

                if (c.moveToFirst()) {
                    String icon = c.getString(c.getColumnIndex("icon"));
                    String time = c.getString(c.getColumnIndex("time"));
                    String temp = c.getString(c.getColumnIndex("temp"));

                    Hour hour = new Hour(time, temp, icon);
                    hourList.add(hour);
                }
                c.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "getDayForShow error :: " + e.getMessage());
            return null;
        } finally {
            if (c != null) {
                if (!c.isClosed()) {
                    c.close();
                }
                c = null;
            }
        }
        return hourList;
    }

    public List<DayForShow> getDayForShow(String locationKey) {
        if (!mDb.isOpen()) {
            return null;
        }

        List<DayForShow> dayForshow = new ArrayList<DayForShow>();
        Cursor c = null;

        try {
            for (int i = 1; i <= 5; i++) {
                c = mDb.rawQuery("SELECT * FROM forecast WHERE locationKey = ? and dayNum = ?", new String[]{locationKey, i + ""});
                if (c == null) {
                    return null;
                }

                if (c.moveToFirst()) {
                    String icon = c.getString(c.getColumnIndex("icon"));
                    String high = c.getString(c.getColumnIndex("high"));
                    String low = c.getString(c.getColumnIndex("low"));
                    String week = c.getString(c.getColumnIndex("week"));
                    String date = c.getString(c.getColumnIndex("date"));
                    String url = c.getString(c.getColumnIndex("url"));
                    String phrase = c.getString(c.getColumnIndex("phrase"));
                    String precipitation = c.getString(c.getColumnIndex("precipitation"));


                    dayForshow.add(new DayForShow(icon, high, low, week, date, url, phrase, precipitation));
                }
                c.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "getDayForShow error :: " + e.getMessage());
            return null;
        } finally {
            if (c != null) {
                if (!c.isClosed()) {
                    c.close();
                }
                c = null;
            }
        }

        return dayForshow;
    }

    public void updateIsUnitC(boolean isUnitC) {
        if (!mDb.isOpen()) {
            return;
        }

        ContentValues values = new ContentValues();
        if (isUnitC) {
            values.put("isUnitC", 0);
        } else {
            values.put("isUnitC", 1);
        }
        if (checkIsUnitCExists("unitinfo")) {
            mDb.update("unitinfo", values, "id=?", new String[] {"0"});
        } else {
            values.put("id", "0");
            mDb.insert("unitinfo", null, values);
        }
    }

    // delete by jielong.xing at 2015-3-19 begin
    // Get hourly weather data
    /*public List<HourForShow> getHourForShow(String locationKey) {
        if (!mDb.isOpen()) {
			return null;
		}
		
		List<HourForShow> hourForShow = new ArrayList<HourForShow>();
		Cursor c = null;
		
		try {
			c = mDb.rawQuery("SELECT * FROM hourly WHERE locationKey = ? ", new String[] { locationKey });
			if (c == null) {
				return null;
			}
			while (c.moveToNext()) {
				String week = c.getString(c.getColumnIndex("week"));
				String time = c.getString(c.getColumnIndex("time"));
				String icon = c.getString(c.getColumnIndex("icon"));
				String temp = c.getString(c.getColumnIndex("temp"));
				String text = c.getString(c.getColumnIndex("text"));

				hourForShow.add(new HourForShow(week, time, icon, temp, text));
			}
			c.close();
		} catch (Exception e) {
			Log.e(TAG, "getHourForShow error :: " + e.getMessage());
			return null;
		} finally {
			if (c != null) {
				if (!c.isClosed()) {
					c.close();
				}
				c = null;
			}
		}

		return hourForShow;
	}*/
    // delete by jielong.xing at 2015-3-19 end

    public void updateCity(String oldLocationKey, String newLocationKey) {
        if (!mDb.isOpen()) {
            return;
        }

        if (checkDataIfExists("city", oldLocationKey)) {
            //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/01/2016,1539263,[jrdlogger]com.tct.weather ANR
            if (checkDataIfExists("city", newLocationKey)) {
                ContentValues values = new ContentValues();
                values.put("isautolocate", false);
                mDb.update("city", values, "locationKey=?", new String[]{oldLocationKey});
            } else {
                ContentValues values = new ContentValues();
                values.put("locationKey", newLocationKey);
//            String time = String.valueOf(System.currentTimeMillis());
//            values.put("updateTime", time);

                mDb.update("city", values, "locationKey=?", new String[]{oldLocationKey});
            }
            //[BUGFIX]-Add-END by TSCD.qian-li
        }
    }


    public synchronized void insertCity(City city) {
        if (!mDb.isOpen()) {
            return;
        }
        String locationKey = city.getLocationKey();
        String locationKey2 = locationKey;
        if (!checkDataIfExists("city", locationKey)) {
//            if (locationKey2 != null && locationKey2.startsWith(CITYID)) {
//                locationKey2 = locationKey2.substring(CITYID.length());
//            }
            ContentValues values = new ContentValues();
            values.put("locationKey", locationKey2);
            values.put("latitude", city.getLatitude());
            values.put("longitude", city.getLongitude());
            values.put("cityName", city.getCityName());
            values.put("country", city.getCountry());
            values.put("state", city.getState());
            values.put("isautolocate", city.isAutoLocate());

            String time = String.valueOf(System.currentTimeMillis());
            values.put("updateTime", time);

            mDb.insert("city", null, values);
        } else {
//            if (locationKey2 != null && locationKey2.startsWith(CITYID)) {
//                locationKey2 = locationKey2.substring(CITYID.length());
//            }
            ContentValues values = new ContentValues();
            values.put("locationKey", locationKey2);
            values.put("latitude", city.getLatitude());
            values.put("longitude", city.getLongitude());
            values.put("cityName", city.getCityName());
            values.put("country", city.getCountry());
            values.put("state", city.getState());
            values.put("isautolocate", city.isAutoLocate());

            String time = String.valueOf(System.currentTimeMillis());
            values.put("updateTime", time);

            mDb.update("city", values, "locationKey=?", new String[]{locationKey});
        }
    }

    public void updateCityTime() {
        if (!mDb.isOpen()) {
            return;
        }

        List<City> citys = getCityListFromDB();
        ContentValues values = new ContentValues();
        String time = String.valueOf(System.currentTimeMillis());
        values.put("updateTime", time);

        if (citys.size() > 0) {
            for (City var : citys) {
                mDb.update("city", values, "locationKey=?", new String[]{var.getLocationKey()});
            }
        }
    }

    /**
     * update city exception the autolocate city
     */
    public void updateNotAutoLocateCityTime() {
        if (!mDb.isOpen()) {
            return;
        }

        List<City> citys = getCityListFromDB();
        ContentValues values = new ContentValues();
        String time = String.valueOf(System.currentTimeMillis());
        values.put("updateTime", time);

        if (citys.size() > 0) {
            for (City var : citys) {
                if (!var.isAutoLocate()) {
                    mDb.update("city", values, "locationKey=?", new String[]{var.getLocationKey()});
                }
            }
        }
    }

    public void updateCityTimeByLocationKey(String locationKey) {
        if (!mDb.isOpen()) {
            return;
        }
        ContentValues values = new ContentValues();
        String time = String.valueOf(System.currentTimeMillis());
        values.put("updateTime", time);
        mDb.update("city", values, "locationKey=?", new String[]{locationKey});
    }


    public synchronized void updateWeather(String locationKey, Weather weather) {
        if (!mDb.isOpen()) {
            return;
        }
        String locationKey2 = locationKey;
        ContentValues values = new ContentValues();
//		Local local = weather.getLocal();
        WeatherForShow weatherForShow = weather.getWeatherForShow();
        if (locationKey2 != null && locationKey2.startsWith(CITYID)) {
            locationKey2 = locationKey2.substring(CITYID.length());
        }

        values.put("locationKey", locationKey2);
        values.put("icon", weatherForShow.getIcon());
        values.put("text", weatherForShow.getText());
        values.put("temp", weatherForShow.getTemp());
        values.put("high", weatherForShow.getDailyTempH());
        values.put("low", weatherForShow.getDailyTempL());
        values.put("realfeel", weatherForShow.getRealfeel());
        values.put("time", weatherForShow.getTime());
        values.put("humidity", weatherForShow.getHumidity());
        values.put("visibility", weatherForShow.getVisibility());
        values.put("wind", weatherForShow.getWind());
        values.put("tempHigh", weatherForShow.getCurrentTempH());
        values.put("tempLow", weatherForShow.getCurrentTempL());
        values.put("preciptation", weatherForShow.getPreciptation());
        values.put("pressure", weatherForShow.getPressure());
        values.put("uv_index", weatherForShow.getUv_index());
        values.put("uv_desc", weatherForShow.getUv_desc());
        values.put("url", weatherForShow.getUrl());
        //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398,
        //[Android6.0][Weather_v5.2.2.1.0307.0]The weather widget data is not same as weather in L1 screen
        values.put("winddir", weatherForShow.getWindDir());
        //[FEATURE]-Add-END by TSCD.peng.du
        values.put("isdaytime", weatherForShow.getIsDayTime());


        if (!checkDataIfExists("current", locationKey)) {
            mDb.insert("current", null, values);
        } else {
            mDb.update("current", values, "locationKey = ?", new String[]{locationKey});
        }

        // ---------------------------------------------------------------------------
        //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/27/2015,983895,delete weather_24forecast_card
        if (CustomizeUtils.getBoolean(mContext, "use_twc_weather")) {
            for (int i = 0; i < 24; i++) {
                values = new ContentValues();
                Hour hour = weather.getHourList().get(i);

                String timeId = (i + 1) + "";
                values.put("locationKey", locationKey2);
                values.put("timeId", timeId);
                values.put("temp", hour.getTemperature());
                values.put("icon", hour.getIcon());
                values.put("time", hour.getTime());

                if (!checkHourlyIfExists(locationKey, timeId)) {
                    mDb.insert("hourly", null, values);
                } else {
                    mDb.update("hourly", values, "locationKey = ?and timeId=?", new String[]{locationKey, timeId});
                }
            }
        }
        //[FEATURE]-Add-END by TSCD.peng.du

        List<DayForShow> dayForShowList = weather.getDayForShow();
        if (dayForShowList == null || dayForShowList.size() == 0) {
            return;
        }

        for (int i = 0; i < 5; i++) {
            values = new ContentValues();
            DayForShow dayForShow = dayForShowList.get(i);

            String dayNum = (i + 1) + "";
            values.put("locationKey", locationKey2);
            values.put("icon", dayForShow.getIcon());
            values.put("dayNum", dayNum);
            values.put("high", dayForShow.getTemph());
            values.put("low", dayForShow.getTempl());
            values.put("week", dayForShow.getWeek());
            values.put("date", dayForShow.getDate());
            values.put("url", dayForShow.getUrl());
            values.put("phrase", dayForShow.getPhrase());
            values.put("precipitation", dayForShow.getPrecipitation());

            if (!checkDailyIfExists(locationKey, dayNum)) {
                mDb.insert("forecast", null, values);
            } else {
                mDb.update("forecast", values, "locationKey = ?and dayNum=?", new String[]{locationKey, dayNum});
            }
        }

        // ---------------------------------------------------------------------------
        // delete by jielong.xing at 2015-3-19 begin
        /*mDb.delete("hourly", "locationKey=?", new String[] { locationKey });
        for (int i = 0; i < 24; i++) {
			values = new ContentValues();
			HourForShow hourForShow = weather.getHourForShow(i);
			values.put("locationKey", locationKey2);
			values.put("week", hourForShow.getWeek());
			values.put("time", hourForShow.getTime());
			values.put("icon", hourForShow.getIcon());
			values.put("temp", hourForShow.getTemp());
			values.put("text", hourForShow.getText());

			mDb.insert("hourly", null, values);
		}*/
        // delete by jielong.xing at 2015-3-19 end
    }

//    public synchronized void updateWeatherForShow(String locationKey, Weather weather) {
//        if (!mDb.isOpen()) {
//            return;
//        }
//        String locationKey2 = locationKey;
//        ContentValues values = new ContentValues();
////		Local local = weather.getLocal();
//        WeatherForShow weatherForShow = weather.getWeatherForShow();
//        if (locationKey2 != null && locationKey2.startsWith(CITYID)) {
//            locationKey2 = locationKey2.substring(CITYID.length());
//        }
//        values.put("locationKey", locationKey2);
//        values.put("icon", weatherForShow.getIcon());
//        values.put("text", weatherForShow.getText());
//        values.put("temp", weatherForShow.getTemp());
//        values.put("high", weatherForShow.getDailyTempH());
//        values.put("low", weatherForShow.getDailyTempL());
//        values.put("realfeel", weatherForShow.getRealfeel());
//        values.put("time", weatherForShow.getTime());
//        values.put("humidity", weatherForShow.getHumidity());
//        values.put("wind", weatherForShow.getWind());
//        values.put("visibility", weatherForShow.getVisibility());
//        values.put("url", weatherForShow.getUrl());
//
//        if (!checkDataIfExists("current", locationKey)) {
//            mDb.insert("current", null, values);
//        } else {
//            mDb.update("current", values, "locationKey = ?", new String[]{locationKey});
//        }
//    }

    public synchronized void updateCurrentWeather(String locationKey, Weather weather) {
        if (!mDb.isOpen()) {
            return;
        }
        String locationKey2 = locationKey;
        ContentValues values = new ContentValues();
//		Local local = weather.getLocal();
        //WeatherForShow weatherForShow = weather.getWeatherForShow();
        Currentconditions currentconditions = weather.getCurrentconditions();
        Local local = weather.getLocal();
        if (locationKey2 != null && locationKey2.startsWith(CITYID)) {
            locationKey2 = locationKey2.substring(CITYID.length());
        }
        values.put("locationKey", locationKey2);
        values.put("icon", currentconditions.getWeathericon());
        values.put("text", currentconditions.getWeathertext());
        values.put("temp", currentconditions.getTemperature());
//		values.put("high", weatherForShow.getTemph());
//		values.put("low", weatherForShow.getTempl());
        values.put("realfeel", currentconditions.getRealfeel());
        values.put("time", local.getTime());
        values.put("humidity", currentconditions.getHumidity());
        values.put("wind", currentconditions.getWindspeed());
        values.put("visibility", currentconditions.getVisibility());
        values.put("tempHigh", currentconditions.getHighTemp());
        values.put("tempLow", currentconditions.getLowTemp());
        values.put("preciptation", currentconditions.getPrecip());
        values.put("pressure", currentconditions.getPressure());
        values.put("uv_index", currentconditions.getUvindex());
        values.put("uv_desc", currentconditions.getUvDesc());
        values.put("url", currentconditions.getUrl());
        //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398,
        //[Android6.0][Weather_v5.2.2.1.0307.0]The weather widget data is not same as weather in L1 screen
        values.put("winddir", currentconditions.getWinddirection());
        values.put("isdaytime", currentconditions.getIsDayTime());
        //[FEATURE]-Add-END by TSCD.peng.du
        if (!checkDataIfExists("current", locationKey)) {
            mDb.insert("current", null, values);
        } else {
            mDb.update("current", values, "locationKey = ?", new String[]{locationKey});
        }
    }

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/26/2016,1487609,[Weather]The widget weather show moon in daytime and the local weather disappear
    public synchronized void updateCurrentLocationKey(String oldLocationKey, String newLocationKey) {
        if (!mDb.isOpen()) {
            return;
        }
        ContentValues values = new ContentValues();
        if (oldLocationKey != null && oldLocationKey.startsWith(CITYID)) {
            oldLocationKey = oldLocationKey.substring(CITYID.length());
        }
        if (newLocationKey != null && newLocationKey.startsWith(CITYID)) {
            newLocationKey = newLocationKey.substring(CITYID.length());
        }
        values.put("locationKey", newLocationKey);
        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/01/2016,1539263,[jrdlogger]com.tct.weather ANR
        if (checkDataIfExists("current", oldLocationKey) && !checkDataIfExists("current", newLocationKey)) {
            //[BUGFIX]-Add-END by TSCD.qian-li
            mDb.update("current", values, "locationKey = ?", new String[]{oldLocationKey});
        }
    }
    //[BUGFIX]-Add-END by TSCD.qian-li

    public synchronized void updateHourly(String locationKey, Weather weather) {
        if (!mDb.isOpen()) {
            return;
        }
        String locationKey2 = locationKey;
        ContentValues values = new ContentValues();

        for (int i = 0; i < 24; i++) {
            values = new ContentValues();
            Hour hour = weather.getHourList().get(i);

            String timeId = (i + 1) + "";
            values.put("locationKey", locationKey2);
            values.put("timeId", timeId);
            values.put("temp", hour.getTemperature());
            values.put("icon", hour.getIcon());
            values.put("time", hour.getTime());

            if (!checkHourlyIfExists(locationKey, timeId)) {
                mDb.insert("hourly", null, values);
            } else {
                mDb.update("hourly", values, "locationKey = ?and timeId=?", new String[]{locationKey, timeId});
            }
        }
    }

    public synchronized void updateForecast(String locationKey, Weather weather) {
        if (!mDb.isOpen()) {
            return;
        }
        String locationKey2 = locationKey;
        ContentValues values = new ContentValues();

        List<DayForShow> dayForShowList = weather.getDayForShow();
        if (dayForShowList == null || dayForShowList.size() == 0) {
            return;
        }
        for (int i = 0; i < 5; i++) {
            values = new ContentValues();
            DayForShow dayForShow = dayForShowList.get(i);
            String dayNum = (i + 1) + "";
            values.put("locationKey", locationKey2);
            values.put("icon", dayForShow.getIcon());
            values.put("dayNum", dayNum);
            values.put("high", dayForShow.getTemph());
            values.put("low", dayForShow.getTempl());
            values.put("week", dayForShow.getWeek());
            values.put("date", dayForShow.getDate());
            values.put("url", dayForShow.getUrl());
            values.put("phrase", dayForShow.getPhrase());
            values.put("precipitation", dayForShow.getPrecipitation());

            if (!checkDailyIfExists(locationKey, dayNum)) {
                mDb.insert("forecast", null, values);
            } else {
                mDb.update("forecast", values, "locationKey = ?and dayNum=?", new String[]{locationKey, dayNum});
            }
        }
    }

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/26/2016,1487609,[Weather]The widget weather show moon in daytime and the local weather disappear
    public synchronized void updateForecastLocationKey(String oldLocationKey, String newLocationKey) {
        if (!mDb.isOpen()) {
            return;
        }
        if (oldLocationKey != null && oldLocationKey.startsWith(CITYID)) {
            oldLocationKey = oldLocationKey.substring(CITYID.length());
        }
        if (newLocationKey != null && newLocationKey.startsWith(CITYID)) {
            newLocationKey = newLocationKey.substring(CITYID.length());
        }
        ContentValues values;

        for (int i = 1; i <= 5; i++) {
            values = new ContentValues();
            values.put("locationKey", newLocationKey);
            //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/01/2016,1539263,[jrdlogger]com.tct.weather ANR
            if (checkDailyIfExists(oldLocationKey, i + "") && !checkDailyIfExists(newLocationKey, i + "")) {
                //[BUGFIX]-Add-END by TSCD.qian-li
                mDb.update("forecast", values, "locationKey = ?and dayNum=?", new String[]{oldLocationKey, i + ""});
            }
        }
    }
    //[BUGFIX]-Add-END by TSCD.qian-li

    public void deleteCity(String locationKey) {
        if (!mDb.isOpen()) {
            return;
        }
        mDb.delete("city", "locationKey = ?", new String[]{locationKey});
        mDb.delete("current", "locationKey = ?", new String[]{locationKey});
        mDb.delete("forecast", "locationKey = ?", new String[]{locationKey});
        // delete by jielong.xing at 2015-3-19 begin
//		mDb.delete("hourly", "locationKey = ?", new String[] { locationKey });
        // delete by jielong.xing at 2015-3-19 end
    }

    /**
     * Get strWeatherIcon for ad.
     *
     * @param locationKey
     * @return
     */
    public String getStrWeatherIcon(String locationKey) {
        String icon = null;
        Cursor c = null;

        try {
            c = mDb.rawQuery("SELECT icon FROM current WHERE locationKey = ?", new String[]{locationKey});
            if (c == null) {
                return null;
            }
            if (c.moveToFirst()) {
                icon = c.getString(0);
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "getStrWeatherIcon error :: " + e.getMessage());
            return null;
        } finally {
            if (c != null) {
                if (!c.isClosed()) {
                    c.close();
                }
                c = null;
            }
        }

        return icon;
    }

    public boolean checkIsUnitCExists(String tableName) {
        if (!mDb.isOpen()) {
            return false;
        }

        int count = 0;
        Cursor c = null;
        try {
            c = mDb.rawQuery("SELECT COUNT(*) FROM " + tableName + " WHERE id = ?", new String[]{"0"});
            if (c == null) {
                return false;
            }
            if (c.moveToFirst()) {
                count = c.getInt(0);
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "checkIsUnitCExists error :: " + e.getMessage());
            return false;
        } finally {
            if (c != null) {
                if (!c.isClosed()) {
                    c.close();
                }
                c = null;
            }
        }
        return count != 0;
    }

    public boolean checkDataIfExists(String tableName, String locationKey) {
        if (!mDb.isOpen()) {
            return false;
        }

        int count = 0;
        Cursor c = null;

        try {
            c = mDb.rawQuery("SELECT COUNT(*) FROM " + tableName + " WHERE locationKey = ?", new String[]{locationKey});
            if (c == null) {
                return false;
            }
            if (c.moveToFirst()) {
                count = c.getInt(0);
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "checkDataIfExists error :: " + e.getMessage());
            return false;
        } finally {
            if (c != null) {
                if (!c.isClosed()) {
                    c.close();
                }
                c = null;
            }
        }
        return count != 0;
    }

    public boolean checkDailyIfExists(String locationKey, String dayNum) {
        if (!mDb.isOpen()) {
            return false;
        }

        int count = 0;
        Cursor c = null;

        try {
            c = mDb.rawQuery("SELECT COUNT(*) FROM forecast WHERE locationKey = ? and dayNum = ?", new String[]{locationKey, dayNum});
            if (null == c) {
                return false;
            }
            if (c.moveToFirst()) {
                count = c.getInt(0);
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "checkDailyIfExists error :: " + e.getMessage());
            return false;
        } finally {
            if (c != null) {
                if (!c.isClosed()) {
                    c.close();
                }
                c = null;
            }
        }
        return count != 0;
    }

    // delete by jielong.xing at 2015-3-19 begin
    public boolean checkHourlyIfExists(String locationKey, String timeId) {
        if (!mDb.isOpen()) {
            return false;
        }

        int count = 0;
        Cursor c = null;
        try {
            c = mDb.rawQuery("SELECT COUNT(*) FROM hourly WHERE locationKey = ? and timeId = ?", new String[]{locationKey, timeId});
            if (c == null) {
                return false;
            }
            if (c.moveToFirst()) {
                count = c.getInt(0);
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "checkHourlyIfExists error :: " + e.getMessage());
            return false;
        } finally {
            if (c != null) {
                if (!c.isClosed()) {
                    c.close();
                }
                c = null;
            }
        }
        return count != 0;
    }
    // delete by jielong.xing at 2015-3-19 end
}
