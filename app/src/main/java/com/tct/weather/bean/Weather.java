/**************************************************************************************************/
/*                                                                     Date : 10/2012 */
/*                            PRESENTATION                                            */
/*              Copyright (c) 2012 JRD Communications, Inc.                           */
/**************************************************************************************************/
/*                                                                                                */
/*    This material is company confidential, cannot be reproduced in any              */
/*    form without the written permission of JRD Communications, Inc.                 */
/*                                                                                                */
/*================================================================================================*/
/*   Author :  Feng zhuang                                                            */
/*   Role :   JrdWeather                                                              */
/*================================================================================================*/
/* Comments :                                                                         */
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/Weather.java             */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.tct.weather.bean;

import java.util.ArrayList;
import java.util.List;

public class Weather {

    private Units units;
    private Local local;
    private String currentLocationKey;
    private String hourlyLocationKey;
    private String dailyLocationKey;
    private Currentconditions currentconditions;
    private List<Day> dayList;
    private List<Hour> hourList;
    private static final String MON = "Monday";
    private static final String TUE = "Tuesday";
    private static final String WED = "Wednesday";
    private static final String THU = "Thursday";
    private static final String FRI = "Friday";
    private static final String SAT = "Saturday";
    private static final String SUN = "Sunday";

    public Units getUnits() {
        return units;
    }

    public void setUnits(Units units) {
        this.units = units;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public Currentconditions getCurrentconditions() {
        return currentconditions;
    }

    public void setCurrentconditions(Currentconditions currentconditions) {
        this.currentconditions = currentconditions;
    }

    public List<Day> getDayList() {
        return dayList;
    }

    public void setDayList(List<Day> dayList) {
        this.dayList = dayList;
    }

    public List<Hour> getHourList() {
        return hourList;
    }

    public void setHourList(List<Hour> hourList) {
        this.hourList = hourList;
    }

    public String getDailyLocationKey() {
        return dailyLocationKey;
    }

    public void setDailyLocationKey(String dailyLocationKey) {
        this.dailyLocationKey = dailyLocationKey;
    }

    public String getCurrentLocationKey() {
        return currentLocationKey;
    }

    public void setCurrentLocationKey(String currentLocationKey) {
        this.currentLocationKey = currentLocationKey;
    }

    public String getHourlyLocationKey() {
        return hourlyLocationKey;
    }

    public void setHourlyLocationKey(String hourlyLocationKey) {
        this.hourlyLocationKey = hourlyLocationKey;
    }

    // get the information what needed for display
    public WeatherForShow getWeatherForShow() {
        WeatherForShow w = new WeatherForShow();

        w.setIcon(getCurrentconditions().getWeathericon());
        w.setText(getCurrentconditions().getWeathertext());
        w.setTemp(getCurrentconditions().getTemperature());
        w.setDailyTempH(getDayList().get(0).getDaytime()
                .getHightemperature());
        w.setDailyTempL(getDayList().get(0).getNightday()
                .getLowtemperature());
        w.setRealfeel(getCurrentconditions().getRealfeel());
        w.setTime(getLocal().getTime());
        w.setHumidity(getCurrentconditions().getHumidity());
        w.setWind(getCurrentconditions().getWindspeed());
        //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398,
        //[Android6.0][Weather_v5.2.2.1.0307.0]The weather widget data is not same as weather in L1 screen
        w.setWindDir(getCurrentconditions().getWinddirection());
        //[FEATURE]-Add-END by TSCD.peng.du
        w.setVisibility(getCurrentconditions().getVisibility());
        w.setUrl(getCurrentconditions().getUrl());
        w.setCurrentTempH(getCurrentconditions().getHighTemp());
        w.setCurrentTempL(getCurrentconditions().getLowTemp());
        w.setPressure(getCurrentconditions().getPressure());
        w.setPreciptation(getCurrentconditions().getPrecip());
        w.setUv_index(getCurrentconditions().getUvindex());
        w.setUv_desc(getCurrentconditions().getUvDesc());
        //[FEATURE]-Add-BEGIN by TSCD.qian-li,01/22/2016,1490227,[Launcher][Ergo v5.2.6]Boom Key
        w.setIsDayTime(getCurrentconditions().getIsDayTime());
        //[FEATURE]-Add-END by TSCD.qian-li

        return w;
    }

    public List<DayForShow> getDayForShow() {
        List<DayForShow> days = new ArrayList<DayForShow>();

        if (getDayList() != null) {
            days.add(getDayList().get(0).getDayForShow());
            days.add(getDayList().get(1).getDayForShow());
            days.add(getDayList().get(2).getDayForShow());
            days.add(getDayList().get(3).getDayForShow());
            days.add(getDayList().get(4).getDayForShow());
        }

        return days;
    }

    // delete by jielong.xing at 2015-3-19 begin
    /*public HourForShow getHourForShow(int i) {
        HourForShow hour = getForecast().getHours().get(i).getHourForShow();

        if (i == 0) {
            hour.setWeek(getForecast().getDays().get(0).getDaycode());
        }
        if (hour.getTime().equals("12 AM")) {
            hour.setWeek(getNextDayCode(getForecast().getDays().get(0).getDaycode()));
        }

        return hour;
    }*/
    // delete by jielong.xing at 2015-3-19 end

    private String getNextDayCode(String week) {
        if (week.equals(MON)) {
            return TUE;
        } else if (week.equals(TUE)) {
            return WED;
        } else if (week.equals(WED)) {
            return THU;
        } else if (week.equals(THU)) {
            return FRI;
        } else if (week.equals(FRI)) {
            return SAT;
        } else if (week.equals(SAT)) {
            return SUN;
        } else if (week.equals(SUN)) {
            return MON;
        } else {
            return "";
        }

    }
}
