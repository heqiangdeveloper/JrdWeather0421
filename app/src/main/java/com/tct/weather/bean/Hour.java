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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/Hour.java                */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.tct.weather.bean;

public class Hour {

	private String icon;
	private String time;
	private String weatherIcon;
	private String temperature;
	private String realfeel;
	private String dewpoint;
	private String humidity;
	private String precip;
	private String rain;
	private String snow;
	private String ice;
	private String windspeed;
	private String winddirection;
	private String windgust;
	private String txtshort;
	private String mobileLink;

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getMobileLink() {
		return mobileLink;
	}

	public void setMobileLink(String mobileLink) {
		this.mobileLink = mobileLink;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getWeatherIcon() {
		return weatherIcon;
	}

	public void setWeatherIcon(String weatherIcon) {
		this.weatherIcon = weatherIcon;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getRealfeel() {
		return realfeel;
	}

	public void setRealfeel(String realfeel) {
		this.realfeel = realfeel;
	}

	public String getDewpoint() {
		return dewpoint;
	}

	public void setDewpoint(String dewpoint) {
		this.dewpoint = dewpoint;
	}

	public String getHumidity() {
		return humidity;
	}

	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}

	public String getPrecip() {
		return precip;
	}

	public void setPrecip(String precip) {
		this.precip = precip;
	}

	public String getRain() {
		return rain;
	}

	public void setRain(String rain) {
		this.rain = rain;
	}

	public String getSnow() {
		return snow;
	}

	public void setSnow(String snow) {
		this.snow = snow;
	}

	public String getIce() {
		return ice;
	}

	public void setIce(String ice) {
		this.ice = ice;
	}

	public String getWindspeed() {
		return windspeed;
	}

	public void setWindspeed(String windspeed) {
		this.windspeed = windspeed;
	}

	public String getWinddirection() {
		return winddirection;
	}

	public void setWinddirection(String winddirection) {
		this.winddirection = winddirection;
	}

	public String getWindgust() {
		return windgust;
	}

	public void setWindgust(String windgust) {
		this.windgust = windgust;
	}

	public String getTxtshort() {
		return txtshort;
	}

	public void setTxtshort(String txtshort) {
		this.txtshort = txtshort;
	}

	public Hour() {
		super();
	}

	public Hour(String time,String temperature,String icon)
	{
		this.time=time;
		this.temperature=temperature;
		this.icon=icon;
	}

	@Override
	public String toString() {
		return "Hour{" +
				"icon='" + icon + '\'' +
				", time='" + time + '\'' +
				", weatherIcon='" + weatherIcon + '\'' +
				", temperature='" + temperature + '\'' +
				", realfeel='" + realfeel + '\'' +
				", dewpoint='" + dewpoint + '\'' +
				", humidity='" + humidity + '\'' +
				", precip='" + precip + '\'' +
				", rain='" + rain + '\'' +
				", snow='" + snow + '\'' +
				", ice='" + ice + '\'' +
				", windspeed='" + windspeed + '\'' +
				", winddirection='" + winddirection + '\'' +
				", windgust='" + windgust + '\'' +
				", txtshort='" + txtshort + '\'' +
				", mobileLink='" + mobileLink + '\'' +
				'}'+"\n";
	}

}
