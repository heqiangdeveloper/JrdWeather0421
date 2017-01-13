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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/Day.java                 */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.tct.weather.bean;

public class Day {

    private String url;
    private String obsdate;
    private String daycode;
    private String sunrise;
    private String sunset;
    private String highTemp;
    private String precipitation;


    public String getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(String highTemp) {
        this.highTemp = highTemp;
    }

    public String getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(String lowTemp) {
        this.lowTemp = lowTemp;
    }

    private String lowTemp;
    private HalfDay daytime;
    private HalfDay nightday;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getObsdate() {
        return obsdate;
    }

    public void setObsdate(String obsdate) {
        this.obsdate = obsdate;
    }

    public String getDaycode() {
        return daycode;
    }

    public void setDaycode(String daycode) {
        this.daycode = daycode;
    }

    public String getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    public HalfDay getDaytime() {
        return daytime;
    }

    public void setDaytime(HalfDay daytime) {
        this.daytime = daytime;
    }

    public HalfDay getNightday() {
        return nightday;
    }

    public void setNightday(HalfDay nightday) {
        this.nightday = nightday;
    }

    public String getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(String precipitation) {
        this.precipitation = precipitation;
    }

    @Override
    public String toString() {
        return "Day{" +
                "url='" + url + '\'' +
                ", obsdate='" + obsdate + '\'' +
                ", daycode='" + daycode + '\'' +
                ", sunrise='" + sunrise + '\'' +
                ", sunset='" + sunset + '\'' +
                ", highTemp='" + highTemp + '\'' +
                ", lowTemp='" + lowTemp + '\'' +
                ", daytime=" + daytime +
                ", nightday=" + nightday +
                '}';
    }

    // get the daily weather inforamtion
    public DayForShow getDayForShow() {
        DayForShow day = new DayForShow();

        day.setIcon(getDaytime().getWeathericon());
        day.setTemph(getDaytime().getHightemperature());
        day.setTempl(getNightday().getLowtemperature());
        day.setWeek(getDaycode());
        day.setDate(getObsdate());
        day.setUrl(getUrl());//add by shenxin for PR460544
        day.setPrecipitation(getPrecipitation());
        day.setPhrase(getDaytime().getTxtshort());
        return day;
    }
}
