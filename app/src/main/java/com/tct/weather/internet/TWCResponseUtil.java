package com.tct.weather.internet;

import android.text.TextUtils;

import com.tct.weather.bean.City;
import com.tct.weather.bean.Currentconditions;
import com.tct.weather.bean.Day;
import com.tct.weather.bean.HalfDay;
import com.tct.weather.bean.Hour;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiajun.shen on 8/28/15.
 */
public class TWCResponseUtil implements ResponseUtil<JSONObject>{
     public Currentconditions getCurrentWeather(JSONObject jsonObject) {
        Currentconditions current = new Currentconditions();
//        JSONArray resultArray = new JSONArray(strEntity);

        if (jsonObject != null) {
            JSONObject obersavationObject = jsonObject.optJSONObject("observation");
            String icon = obersavationObject.optString("icon_code");
            String phrase = obersavationObject.optString("phrase_32char");
            String uv_index = obersavationObject.optString("uv_index");
            String uv_desc = obersavationObject.optString("uv_desc");

            JSONObject metricObject = obersavationObject.optJSONObject("metric");
            //JSONObject metricObject = obersavationObject.optJSONObject("imperial");
            String temp = metricObject.optString("temp");
            String realfeel = metricObject.optString("feels_like");
            String windSpeed = metricObject.optString("wspd");
            String visibility = metricObject.optString("vis");
            String humidity = metricObject.optString("rh");
            String precipitation = metricObject.optString("precip_24hour");
            String tempHigh = metricObject.optString("temp_max_24hour");
            String tempLow = metricObject.optString("temp_min_24hour");
            String pressure = metricObject.optString("mslp");


            current.setWindspeed(windSpeed);// + windObj.optString("Unit"));

            current.setVisibility(visibility);// + visibilityObj.optString("Unit"));
            current.setHumidity(humidity);// + "%");
            //current.setUrl(jsonObject.optString("MobileLink"));
            current.setWeathericon(icon);
            current.setWeathertext(phrase);
            current.setTemperature(temp);
            current.setRealfeel(realfeel);
            current.setUvindex(uv_index);
            current.setPrecip(precipitation);
            current.setPressure(pressure);
            current.setHighTemp(tempHigh);
            current.setLowTemp(tempLow);
            current.setUvDesc(uv_desc);
        } else {
            return null;
        }
        return current;
    }

     public List<Hour> getHourlyForecastWeather(JSONObject jsonObject) {
        List<Hour> hourList = new ArrayList<Hour>();
        if (jsonObject != null) {
            JSONArray array = null;
            try {
                array = jsonObject.getJSONArray("forecasts");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (null != array && array.length() > 0) {
                int len = array.length();

                // Fixed PR990514 by jielong.xing at 2015-4-30 begin
                if (len < 23) {
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
                        String time = obj.optString("fcst_valid_local");
                        if (time != null) {
                            time = time.substring(11, 16);//from 2015­03­30T16:00:00­0400 get 16:00
                        }
                        String icon = obj.optString("icon_code");
                        String temp = obj.optString("temp");
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
        }
        return hourList;
    }

     public List<Day> getDailyForecastWeather(JSONObject jsonObject) {
        List<Day> dayList = new ArrayList<Day>();

        //JSONObject resultObj = new JSONObject();
        if (jsonObject != null) {
            JSONArray array = null;
            try {
                array = jsonObject.getJSONArray("forecasts");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (null != array && array.length() > 0) {
                int len = array.length();

                // Fixed PR990514 by jielong.xing at 2015-4-30 begin
                if (len < 5) {
                    return null;
                }
                // Fixed PR990514 by jielong.xing at 2015-4-30 end
                for (int i = 1; i < len; i++) {
                    JSONObject obj = null;
                    try {
                        obj = array.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (obj != null) {
                        //Log.i("TWC SJJ", "obj=" + obj.toString());
                        String dateStr = obj.optString("fcst_valid_local");
                        if (dateStr != null) {
                            dateStr = dateStr.substring(0, 10);
                        }
                        String week = obj.optString("dow");
                        String highTemp = obj.optString("max_temp");
                        String lowTemp = obj.optString("min_temp");

                        JSONObject dayObj = obj.optJSONObject("day");
                        String phraseDay = dayObj.optString("phrase_32char");
                        String iconDay = dayObj.optString("icon_code");
                        String precipitation = dayObj.optString("qpf");

                        JSONObject nightObj = obj.optJSONObject("night");


                        //Log.i("TWC SJJ", "data=" + dateStr + " phrase=" + phraseDay);
                        Day day = new Day();
                        day.setDaycode(week);
                        day.setLowTemp(highTemp);
                        day.setLowTemp(lowTemp);
                        day.setPrecipitation(precipitation);
                        //day.setUrl(obj.optString("MobileLink"));
                        HalfDay dayTime = new HalfDay();
                        dayTime.setHightemperature(highTemp);
                        dayTime.setWeathericon(iconDay);
                        dayTime.setTxtshort(phraseDay);
                        HalfDay nightTime = new HalfDay();
                        nightTime.setLowtemperature(lowTemp);
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

//    private static String getWeek(String date) {
//        Calendar calendar = Calendar.getInstance();
//        String[] str = date.split("-");
//
//        int year = Integer.parseInt(str[0]);
//        int month = Integer.parseInt(str[1]);
//        int day = Integer.parseInt(str[2]);
//        calendar.set(year, month - 1, day);
//        int number = calendar.get(Calendar.DAY_OF_WEEK) - 1;
//        String[] weekStr = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",};
//        return weekStr[number];
//    }

     public List<City> getCityList(JSONObject jsonObject) {
        List<City> cityList = new ArrayList<City>();
        if (jsonObject != null) {
            JSONObject metadataObject = jsonObject.optJSONObject("metadata");
            String address = metadataObject.optString("address");

            JSONArray addressArray = jsonObject.optJSONArray("addresses");
            if (addressArray != null) {
                for (int i = 0; i < addressArray.length(); i++) {
                    boolean isDuplicateCity = false;
                    JSONObject obj = null;
                    try {
                        obj = addressArray.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (obj != null) {
                        String latitude = obj.optString("latitude");
                        String longitude = obj.optString("longitude");
                        String locationKey = latitude + longitude;
                        String cityName = obj.optString("locality");
                        String state = obj.optString("admin_district");
                        if (TextUtils.isEmpty(cityName)) {
                            if (!TextUtils.isEmpty(state)) {
                                cityName = state;
                            } else {
                                cityName = address;
                                state = address;
                            }
                        }
                        if (TextUtils.isEmpty(state)) {
                            if (!TextUtils.isEmpty(cityName)) {
                                state = cityName;
                            } else {
                                state = address;
                            }
                        }
                        String country = obj.optString("country");

                        //filter duplicate citys
                        for (int j = 0; j < cityList.size(); j++) {
                            if (TextUtils.equals(cityName, cityList.get(j).getCityName())
                                    && TextUtils.equals(country, cityList.get(j).getCountry())
                                    && TextUtils.equals(state, cityList.get(j).getState())
                                    ) {
                                isDuplicateCity = true;
                            }
                        }
                        if (!isDuplicateCity) {
                            City city = new City();
                            city.setLocationKey(locationKey);
                            city.setCityName(cityName);
                            city.setCountry(country);
                            city.setState(state);
                            city.setUpdateTime("");
                            city.setLatitude(latitude);
                            city.setLongitude(longitude);
                            cityList.add(city);
                        }
                    }
                }
            }
        }
        return cityList;
    }

//    static public List<City> getCityListFromArray(JSONArray jsonArray) {
//        List<City> cityList = new ArrayList<City>();
//        try {
//            if (jsonArray != null && jsonArray.length() > 0) {
//                int len = jsonArray.length();
//                for (int i = 0; i < len; i++) {
//                    JSONObject obj = null;
//                    obj = jsonArray.getJSONObject(i);
//                    if (obj != null) {
//                        parseJsonObject(obj, cityList);
//                    } else {
//                        return new ArrayList<City>();
//                    }
//                }
//            } else {
//                return new ArrayList<City>();
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return cityList;
//    }
//
//    private static void parseJsonObject(JSONObject obj, List<City> cityList) throws Exception {
//        JSONObject countryObj = obj.getJSONObject("Country");
//        JSONObject adminObj = obj.getJSONObject("AdministrativeArea");
//        City city = new City();
//        city.setLocationKey(obj.getString("Key"));
//        city.setCityName(obj.getString("LocalizedName"));
//        city.setCountry(countryObj.getString("LocalizedName"));
//        city.setState(adminObj.getString("LocalizedName"));
//        city.setUpdateTime("");
//        cityList.add(city);
//    }

    @Override
    public List<City> getCityListFromArray(JSONArray jsonArray) {
        return null;
    }

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/12/2016,1313172,[Weather]weather not translate to russian
    public void getCityNameFromObject(City city, JSONObject jsonObject) {
        // this method need modify by TWC service data
        if (null == jsonObject || null == city) {
            return;
        } else {
//            try {
//                JSONObject country = jsonObject.getJSONObject("Country");
//                JSONObject area = jsonObject.getJSONObject("AdministrativeArea");
//                city.setLocationKey(jsonObject.getString("Key"));
//                city.setCityName(jsonObject.getString("LocalizedName"));
//                city.setCityName(jsonObject.getString("LocalizedName"));
//                city.setCountry(country.getString("LocalizedName"));
//                city.setState(area.getString("LocalizedName"));
//                city.setUpdateTime("");
//            } catch (JSONException je) {
//                return;
//            }
        }
    }
    //[BUGFIX]-Add-END by TSCD.qian-li
}
