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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/Currentconditions.java   */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.tct.weather.bean;

public class Currentconditions {

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getObservationtime() {
		return observationtime;
	}

	public void setObservationtime(String observationtime) {
		this.observationtime = observationtime;
	}

	//[FEATURE]-Add-BEGIN by TSCD.qian-li,01/22/2016,1490227,[Launcher][Ergo v5.2.6]Boom Key
	public int getIsDayTime() {
		return isDayTime;
	}

	public void setIsDayTime(int isDayTime) {
		this.isDayTime = isDayTime;
	}
	//[FEATURE]-Add-END by TSCD.qian-li

	public String getPressure() {
		return pressure;
	}

	public void setPressure(String pressure) {
		this.pressure = pressure;
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

	public String getHumidity() {
		return humidity;
	}

	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}

	public String getWeathertext() {
		return weathertext;
	}

	public void setWeathertext(String weathertext) {
		this.weathertext = weathertext;
	}

	public String getWeathericon() {
		return weathericon;
	}

	public void setWeathericon(String weathericon) {
		this.weathericon = weathericon;
	}

	public String getWindgusts() {
		return windgusts;
	}

	public void setWindgusts(String windgusts) {
		this.windgusts = windgusts;
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

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public String getPrecip() {
		return precip;
	}

	public void setPrecip(String precip) {
		this.precip = precip;
	}

	public String getUvindex() {
		return uvindex;
	}

	public void setUvindex(String uvindex) {
		this.uvindex = uvindex;
	}

	public String getDewpoint() {
		return dewpoint;
	}

	public void setDewpoint(String dewpoint) {
		this.dewpoint = dewpoint;
	}

	public String getCloudcover() {
		return cloudcover;
	}

	public void setCloudcover(String cloudcover) {
		this.cloudcover = cloudcover;
	}

	public String getApparenttemp() {
		return apparenttemp;
	}

	public void setApparenttemp(String apparenttemp) {
		this.apparenttemp = apparenttemp;
	}

	public String getWindchill() {
		return windchill;
	}

	public void setWindchill(String windchill) {
		this.windchill = windchill;
	}

	public String getLowTemp() {
		return lowTemp;
	}

	public void setLowTemp(String lowTemp) {
		this.lowTemp = lowTemp;
	}

	public String getHighTemp() {
		return highTemp;
	}

	public void setHighTemp(String highTemp) {
		this.highTemp = highTemp;
	}

	public String getUvDesc() {
		return uvDesc;
	}

	public void setUvDesc(String uvDesc) {
		this.uvDesc = uvDesc;
	}

	@Override
	public String toString() {
		return "Currentconditions{" +
				"url='" + url + '\'' +
				", observationtime='" + observationtime + '\'' +
				", pressure='" + pressure + '\'' +
				", temperature='" + temperature + '\'' +
				", realfeel='" + realfeel + '\'' +
				", humidity='" + humidity + '\'' +
				", weathertext='" + weathertext + '\'' +
				", weathericon='" + weathericon + '\'' +
				", windgusts='" + windgusts + '\'' +
				", windspeed='" + windspeed + '\'' +
				", winddirection='" + winddirection + '\'' +
				", visibility='" + visibility + '\'' +
				", precip='" + precip + '\'' +
				", uvindex='" + uvindex + '\'' +
				", dewpoint='" + dewpoint + '\'' +
				", cloudcover='" + cloudcover + '\'' +
				", apparenttemp='" + apparenttemp + '\'' +
				", windchill='" + windchill + '\'' +
				", highTemp='" + highTemp + '\'' +
				", lowTemp='" + lowTemp + '\'' +
				'}'+"\n";
	}

	private String url;
	private String observationtime;
	private String pressure;
	private String temperature;
	private String realfeel;
	private int isDayTime;
	private String humidity;
	private String weathertext;
	private String weathericon;
	private String windgusts;
	private String windspeed;
	private String winddirection;
	private String visibility;
	private String precip;
	private String uvindex;
	private String dewpoint;
	private String cloudcover;
	private String apparenttemp;
	private String windchill;
	private String highTemp;
	private String lowTemp;
	private String uvDesc;
}
