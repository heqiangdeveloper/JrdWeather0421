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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/Forecast.java            */
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

public class Forecast {
    public List<Day> getDays() {
        return days;
    }

    public void setDays(List<Day> days) {
        this.days = days;
    }

    // delete by jielong.xing at 2015-3-19 begin
    /*public List<Hour> getHours() {
        return hours;
    }

    public void setHours(List<Hour> hours) {
        this.hours = hours;
    }*/
    // delete by jielong.xing at 2015-3-19 end

    private List<Day> days = new ArrayList<Day>();
    // delete by jielong.xing at 2015-3-19 begin
//    private List<Hour> hours = new ArrayList<Hour>();
    // delete by jielong.xing at 2015-3-19 end

}
