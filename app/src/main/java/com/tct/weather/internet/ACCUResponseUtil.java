package com.tct.weather.internet;

import android.util.Log;

import com.tct.weather.bean.City;
import com.tct.weather.bean.Currentconditions;
import com.tct.weather.bean.Day;
import com.tct.weather.bean.HalfDay;
import com.tct.weather.bean.Hour;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by jiajun.shen on 11/12/15.
 */
public class ACCUResponseUtil implements ResponseUtil<JSONArray> {
    public Currentconditions getCurrentWeather(JSONArray jsonArray) {
        Currentconditions current = new Currentconditions();
//        JSONArray resultArray = new JSONArray(strEntity);
        if (jsonArray != null && jsonArray.length() > 0) {
            JSONObject obj = null;
            try {
                obj = jsonArray.getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (obj != null) {
                String icon = obj.optString("WeatherIcon");
                String phrase = obj.optString("WeatherText");
                //[FEATURE]-Add-BEGIN by TSCD.qian-li,01/22/2016,1490227,[Launcher][Ergo v5.2.6]Boom Key
                int isDayTime = 0;
                boolean tempBoolean = obj.optBoolean("IsDayTime");
                if (!tempBoolean) {
                    isDayTime = 1;
                }
                //[FEATURE]-Add-END by TSCD.qian-li
                JSONObject tempObj = obj.optJSONObject("Temperature").optJSONObject("Metric");
                String temp = tempObj.optString("Value");
                JSONObject realfellObj = obj.optJSONObject("RealFeelTemperature").optJSONObject("Metric");
                String realfeel = realfellObj.optString("Value");

                //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398,
                //[Android6.0][Weather_v5.2.2.1.0307.0]The weather widget data is not same as weather in L1 screen
                JSONObject windDir = obj.optJSONObject("Wind").optJSONObject("Direction");
                String dir_l =windDir.optString("Degrees");
                current.setWinddirection(dir_l);
                //[FEATURE]-Add-END by TSCD.peng.du
                JSONObject windObj = obj.optJSONObject("Wind").optJSONObject("Speed").optJSONObject("Metric");
                current.setWindspeed(windObj.optString("Value"));// + windObj.optString("Unit"));
                JSONObject visibilityObj = obj.optJSONObject("Visibility").optJSONObject("Metric");

                String tempHigh = obj.optJSONObject("TemperatureSummary").optJSONObject("Past24HourRange").optJSONObject("Maximum").optJSONObject("Metric").optString("Value");
                String tempLow = obj.optJSONObject("TemperatureSummary").optJSONObject("Past24HourRange").optJSONObject("Minimum").optJSONObject("Metric").optString("Value");

                String uvIndex=obj.optString("UVIndex");
                String uvDec=obj.optString("UVIndexText");

                String pressure=obj.optJSONObject("Pressure").optJSONObject("Metric").optString("Value");

                String preciptation=obj.optJSONObject("PrecipitationSummary").optJSONObject("Precipitation").optJSONObject("Metric").optString("Value");

                current.setVisibility(visibilityObj.optString("Value"));// + visibilityObj.optString("Unit"));
                current.setHumidity(obj.optString("RelativeHumidity"));// + "%");
                current.setIsDayTime(isDayTime);
                current.setUrl(obj.optString("MobileLink"));
                current.setWeathericon(icon);
                current.setWeathertext(phrase);
                current.setTemperature(temp);
                current.setHighTemp(tempHigh);
                current.setLowTemp(tempLow);
                current.setRealfeel(realfeel);
                current.setPressure(pressure);
                current.setPrecip(preciptation);
                current.setUvindex(uvIndex);
                current.setUvDesc(uvDec);
            } else {
                return null;
            }
        } else {
            return null;
        }
        return current;
    }

    public List<Day> getDailyForecastWeather(JSONObject jsonObject) {
        List<Day> dayList = new ArrayList<Day>();

        //JSONObject resultObj = new JSONObject();
        if (jsonObject != null) {
            JSONArray array = null;
            try {
                array = jsonObject.getJSONArray("DailyForecasts");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (null != array && array.length() > 0) {
                int len = array.length();
                // Fixed PR990514 by jielong.xing at 2015-4-30 begin
                if (len != 5) {
                    return null;
                }
                // Fixed PR990514 by jielong.xing at 2015-4-30 end
                for (int i = 0; i < len; i++) {
                    JSONObject obj = null;
                    try {
                        obj = array.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (obj != null) {
                        String dateStr = obj.optString("Date");
                        if (dateStr != null) {
                            dateStr = dateStr.substring(0, 10);
                        }
                        JSONObject dayObj = obj.optJSONObject("Day");
                        JSONObject minTempObj = obj.optJSONObject("Temperature").optJSONObject("Minimum");
                        JSONObject maxTempObj = obj.optJSONObject("Temperature").optJSONObject("Maximum");
                        Day day = new Day();
                        day.setDaycode(getWeek(dateStr));
                        day.setUrl(obj.optString("MobileLink"));
                        HalfDay dayTime = new HalfDay();
                        dayTime.setHightemperature(maxTempObj.optString("Value"));
                        dayTime.setWeathericon(dayObj.optString("Icon"));
                        dayTime.setTxtshort(dayObj.optString("IconPhrase"));
                        HalfDay nightTime = new HalfDay();
                        nightTime.setLowtemperature(minTempObj.optString("Value"));
                        day.setDaytime(dayTime);
                        day.setNightday(nightTime);
                        day.setObsdate(dateStr);
                        dayList.add(day);
                    } else {
                        return null;
                    }
                }
            }
            // Fixed PR965957 by jielong.xing at 2015-4-2 begin
            else {
                return null;
            }
            // Fixed PR965957 by jielong.xing at 2015-4-2 end
        }
        return dayList;
    }

    private String getWeek(String date) {
        Calendar calendar = Calendar.getInstance();
        String[] str = date.split("-");

        int year = Integer.parseInt(str[0]);
        int month = Integer.parseInt(str[1]);
        int day = Integer.parseInt(str[2]);
        calendar.set(year, month - 1, day);
        int number = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String[] weekStr = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",};
        return weekStr[number];
    }

    public List<City> getCityList(JSONObject jsonObject) {
        List<City> cityList = new ArrayList<City>();
        try {
            parseJsonObject(jsonObject, cityList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cityList;
    }

    public List<City> getCityListFromArray(JSONArray jsonArray) {
        List<City> cityList = new ArrayList<City>();
        try {
            if (jsonArray != null && jsonArray.length() > 0) {
                int len = jsonArray.length();
                for (int i = 0; i < len; i++) {
                    JSONObject obj = null;
                    obj = jsonArray.getJSONObject(i);
                    if (obj != null) {
                        parseJsonObject(obj, cityList);
                    } else {
                        return new ArrayList<City>();
                    }
                }
            } else {
                return new ArrayList<City>();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cityList;
    }

    private void parseJsonObject(JSONObject obj, List<City> cityList) throws Exception {
        JSONObject countryObj = obj.getJSONObject("Country");
        JSONObject adminObj = obj.getJSONObject("AdministrativeArea");
        City city = new City();
        city.setLocationKey(obj.getString("Key"));
        city.setCityName(obj.getString("LocalizedName"));
        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/29/2016,1537191,[3rd APK][Weather v5.2.8.1.0313.0_video]Weather condition and location can't sync change unless click "Refresh"  when change system language
        if (obj.has("EnglishName")) {
            city.setEnglishName(obj.getString("EnglishName"));
        }
        //[BUGFIX]-Add-END by TSCD.qian-li
        city.setCountry(countryObj.getString("LocalizedName"));
        city.setState(adminObj.getString("LocalizedName"));
        city.setUpdateTime("");
        cityList.add(city);
    }


    @Override
    public List<Hour> getHourlyForecastWeather(JSONArray jsonArray) {
        List<Hour> hourList = new ArrayList<Hour>();
        if (null != jsonArray && jsonArray.length() > 0) {
            int len = jsonArray.length();
            if (len < 23) {
                return null;
            }
            for (int i = 0; i < len; i++) {
                JSONObject obj = null;
                try {
                    obj = jsonArray.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (obj != null) {
                    String time = obj.optString("DateTime");
                    if (time != null) {
                        time = time.substring(11, 16);//from 2015­03­30T16:00:00­0400 get 16:00
                    }
                    String icon = obj.optString("WeatherIcon");
                    JSONObject tempObject = obj.optJSONObject("Temperature");
//                    try {
//                        tempObject = obj.getJSONObject("Temperature");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                    String temp = tempObject.optString("Value");
                    Hour hour = new Hour();
                    hour.setTemperature(temp);
                    hour.setTime(time);
                    hour.setIcon(icon);
                    hourList.add(hour);
                } else {
                    return null;
                }
            }
        }
        // Fixed PR965957 by jielong.xing at 2015-4-2 begin
        else {
            return null;
        }
        // Fixed PR965957 by jielong.xing at 2015-4-2 end

        return hourList;
    }

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/12/2016,1313172,[Weather]weather not translate to russian
    public void getCityNameFromObject(City city, JSONObject jsonObject) {
        if (null == jsonObject || null == city) {
            return;
        } else {
            try {
                JSONObject country = jsonObject.getJSONObject("Country");
                JSONObject area = jsonObject.getJSONObject("AdministrativeArea");
                city.setLocationKey(jsonObject.getString("Key"));
                city.setCityName(jsonObject.getString("LocalizedName"));
                if (jsonObject.has("EnglishName")) {
                    city.setEnglishName(jsonObject.getString("EnglishName"));
                }
                city.setCountry(country.getString("LocalizedName"));
                city.setState(area.getString("LocalizedName"));
                city.setUpdateTime("");
            } catch (JSONException je) {
                Log.e("ACCUResponseUtil", "JSONException : " + je.toString());
                return;
            }
        }
    }
    //[BUGFIX]-Add-END by TSCD.qian-li

}
