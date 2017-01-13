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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/City.java                */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.tct.weather.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

public class City {

    private String locationKey;
    private String cityName;
    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/29/2016,1537191,[3rd APK][Weather v5.2.8.1.0313.0_video]Weather condition and location can't sync change unless click "Refresh"  when change system language
    private String englishName;
    //[BUGFIX]-Add-END by TSCD.qian-li
    private String state;
    private String updateTime;
    private String country;
    private String latitude;
    private String longitude;

    //CR 447398 - ting.chen@tct-nj.com - 001 added begin
    private boolean isAutoLocate;

    public boolean isAutoLocate() {
        return isAutoLocate;
    }

    public void setAutoLocate(boolean isAutoLocate) {
        this.isAutoLocate = isAutoLocate;
    }
    //CR 447398 - ting.chen@tct-nj.com - 001 added end

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(String locationKey) {
        this.locationKey = locationKey;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/29/2016,1537191,[3rd APK][Weather v5.2.8.1.0313.0_video]Weather condition and location can't sync change unless click "Refresh"  when change system language
    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }
    //[BUGFIX]-Add-END by TSCD.qian-li

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setIsAutoLocate(boolean isAutoLocate) {
        this.isAutoLocate = isAutoLocate;
    }

    public City() {
        super();
    }

    @Override
    public String toString() {
        return "City{" +
                "locationKey='" + locationKey + '\'' +
                ", cityName='" + cityName + '\'' +
                ", state='" + state + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", country='" + country + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", isAutoLocate=" + isAutoLocate +
                '}'+"\n";
    }

    public City(String locationKey,String latitude,String longitude,  String cityName, String state,
                String updateTime) {
        super();
        this.locationKey = locationKey;
        this.latitude=latitude;
        this.longitude=longitude;
        this.cityName = cityName;
        this.state = state;
        this.updateTime = updateTime;
    }

    public City(String locationKey,String latitude,String longitude,  String cityName, String state,
                String updateTime, String country) {
        super();
        this.locationKey = locationKey;
        this.latitude=latitude;
        this.longitude=longitude;
        this.cityName = cityName;
        this.state = state;
        this.updateTime = updateTime;
        this.country = country;
    }

    //CR 447398 - ting.chen@tct-nj.com - 001 added begin
    public City(String locationKey,String latitude,String longitude, String cityName, String state,
                String updateTime, String country, boolean isAutoLocate){
        this(locationKey,latitude,longitude,cityName,state,updateTime,country);
        this.isAutoLocate = isAutoLocate;
    }
    //CR 447398 - ting.chen@tct-nj.com - 001 added end

    public String getCityInfoForList() {
        return cityName + ", " + state + ", " + country ;
    }

    public String getUpdateTimeFormated() {
        // PR351637-Feng.Zhuang-001 Modify begin
        SimpleDateFormat format = new SimpleDateFormat("MM/dd K:mm ");
        // PR351637-Feng.Zhuang-001 Modify begin

        long l = Long.parseLong(this.updateTime);
        Date date = new Date(l);

        return format.format(date);
    }

    public boolean getAMPM()
    {
        SimpleDateFormat format = new SimpleDateFormat("k");

        long l = Long.parseLong(this.updateTime);
        Date date = new Date(l);

        String ampm = format.format(date);

        return Integer.parseInt(ampm) < 12;
    }

    public String getCityIdNum() {
        return locationKey.substring(7);
    }
}
