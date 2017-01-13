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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/WeatherInfo.java         */
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

public class WeatherInfo {
	// delete by jielong.xing at 2015-3-19 begin
    /*public List<HourForShow> getHours() {
        return hours;
    }

    public void setHours(List<HourForShow> hours) {
        this.hours = hours;
    }*/
	// delete by jielong.xing at 2015-3-19 end

    private List<Hour> hourList=new ArrayList<Hour>();
    private WeatherForShow weatherForShow;
    private List<DayForShow> dayForShow = new ArrayList<DayForShow>();
    // delete by jielong.xing at 2015-3-19 begin
//    private List<HourForShow> hours = new ArrayList<HourForShow>();
    // delete by jielong.xing at 2015-3-19 end

    public WeatherForShow getWeatherForShow() {
        return weatherForShow;
    }

    public void setWeatherForShow(WeatherForShow weatherForShow) {
        this.weatherForShow = weatherForShow;
    }

    public List<DayForShow> getDayForShow() {
        return dayForShow;
    }

    public void setDayForShow(List<DayForShow> dayForShow) {
        this.dayForShow = dayForShow;
    }

    public List<Hour> getHourList() {
        return hourList;
    }

    public void setHourList(List<Hour> hourList) {
        this.hourList = hourList;
    }
}
