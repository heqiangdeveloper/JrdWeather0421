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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/WeatherForShow.java      */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.tct.weather.bean;

public class WeatherForShow {

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public WeatherForShow() {
        super();
    }

    public WeatherForShow(String icon, String text, String temp, String dailyTempH,
                          String dailyTempL, String currentTempH, String currentTempL, String realfeel, String time,
                          String humidity, String wind, String visibility, String preciptation, String pressure, String uv_index,
                          String uv_desc, String url, String winddir, int isDayTime) {
        super();
        this.icon = icon;
        this.text = text;
        this.temp = temp;
        this.dailyTempH = dailyTempH;
        this.dailyTempL = dailyTempL;
        this.currentTempH = currentTempH;
        this.currentTempL = currentTempL;
        this.realfeel = realfeel;
        this.time = time;
        this.humidity = humidity;
        this.wind = wind;
        this.visibility = visibility;
        this.url = url;
        this.preciptation = preciptation;
        this.pressure = pressure;
        this.uv_index = uv_index;
        this.uv_desc = uv_desc;
        this.winddir =winddir;//[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398
        this.isDayTime = isDayTime;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getRealfeel() {
        return realfeel;
    }

    public void setRealfeel(String realfeel) {
        this.realfeel = realfeel;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }
    //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398,
    //[Android6.0][Weather_v5.2.2.1.0307.0]The weather widget data is not same as weather in L1 screen
    public String getWindDir() {
        return winddir;
    }

    public void setWindDir(String wind) {

        this.winddir = wind;
    }
    //[FEATURE]-Add-END by TSCD.peng.du
    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
    //add by jielong.xing at 2015-1-22 end

    // PR351637-Feng.Zhuang-001 Add begin
    private String city;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPreciptation() {
        return preciptation;
    }

    public void setPreciptation(String preciptation) {
        this.preciptation = preciptation;
    }

    public String getUv_index() {
        return uv_index;
    }

    public void setUv_index(String uv_index) {
        this.uv_index = uv_index;
    }

    public String getDailyTempH() {
        return dailyTempH;
    }

    public void setDailyTempH(String dailyTempH) {
        this.dailyTempH = dailyTempH;
    }

    public String getDailyTempL() {
        return dailyTempL;
    }

    public void setDailyTempL(String dailyTempL) {
        this.dailyTempL = dailyTempL;
    }

    public String getCurrentTempH() {
        return currentTempH;
    }

    public void setCurrentTempH(String currentTempH) {
        this.currentTempH = currentTempH;
    }

    public String getCurrentTempL() {
        return currentTempL;
    }

    public void setCurrentTempL(String currentTempL) {
        this.currentTempL = currentTempL;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getUv_desc() {
        return uv_desc;
    }

    public void setUv_desc(String uv_desc) {
        this.uv_desc = uv_desc;
    }

    public int getIsDayTime() {
        return isDayTime;
    }

    public void setIsDayTime(int isDayTime) {
        this.isDayTime = isDayTime;
    }

    private String icon;
    private String text;
    private String temp;
    private String dailyTempH;
    private String dailyTempL;
    private String realfeel;
    private String time;
    private String humidity;
    private String wind;
    private String visibility;
    private String url;
    private String preciptation;
    private String uv_index;
    private String currentTempH;
    private String currentTempL;
    private String pressure;
    private String uv_desc;
    private String winddir;//[FEATURE]-Add-BEGIN by TSCD.peng.du,12/15/2015,928398
    //[FEATURE]-Add-BEGIN by TSCD.qian-li,01/22/2016,1490227,[Launcher][Ergo v5.2.6]Boom Key
    private int isDayTime;
    //[FEATURE]-Add-END by TSCD.qian-li
}
