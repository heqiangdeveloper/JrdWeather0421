package com.tct.weather.internet;

import com.tct.weather.bean.City;
import com.tct.weather.bean.Currentconditions;
import com.tct.weather.bean.Day;
import com.tct.weather.bean.Hour;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by jiajun.shen on 11/12/15.
 */
public interface ResponseUtil<T> {
    abstract public Currentconditions getCurrentWeather(T jsonObject);

    abstract public List<Hour> getHourlyForecastWeather(T jsonObject);

    abstract public List<Day> getDailyForecastWeather(JSONObject jsonObject);

    abstract public List<City> getCityList(JSONObject jsonObject);

    abstract public List<City> getCityListFromArray(JSONArray jsonArray);

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/12/2016,1313172,[Weather]weather not translate to russian
    abstract public void getCityNameFromObject(City city, JSONObject jsonObject);
    //[BUGFIX]-Add-END by TSCD.qian-li
}
