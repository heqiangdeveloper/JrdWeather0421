package com.tct.weather.util;

import android.content.Context;
import android.net.Uri;
import android.support.v4.util.ArrayMap;

import com.tct.weather.R;

/**
 * Created by jiajun.shen on 9/6/15.
 */
public class IconBackgroundUtil {
    public static String TAG = "weather IconBackgroundUtil";

    public static ArrayMap<Integer, Integer> iconACCDailyArray = new ArrayMap<Integer, Integer>() {
        {
            put(1, R.drawable.ic_sunny);
            put(2, R.drawable.ic_mostly_sunny);
            put(3, R.drawable.ic_partly_sunny);
            put(4, R.drawable.ic_intermittent_clouds);
            put(5, R.drawable.ic_hazy_sunshine);
            put(6, R.drawable.ic_mostly_cloudy);
            put(7, R.drawable.ic_cloudy);
            put(8, R.drawable.ic_dreary);
            put(11, R.drawable.ic_fog);
            put(12, R.drawable.ic_showers);
            put(13, R.drawable.ic_mostly_cloudy_w_showers);
            put(14, R.drawable.ic_partly_sunny_w_showers);
            put(15, R.drawable.ic_t_storms);
            put(16, R.drawable.ic_mostly_cloudy_w_t_storms);
            put(17, R.drawable.ic_partly_sunny_w_t_storms);
            put(18, R.drawable.ic_rain);
            put(19, R.drawable.ic_flurries);
            put(20, R.drawable.ic_snow);
            put(21, R.drawable.ic_snow);
            put(22, R.drawable.ic_snow);
            put(23, R.drawable.ic_mostly_cloudy_w_snow);
            put(24, R.drawable.ic_ice);
            put(25, R.drawable.ic_sleet);
            put(26, R.drawable.ic_freezing_rain);
            put(29, R.drawable.ic_rain_snow);
            put(30, R.drawable.ic_hot);
            put(31, R.drawable.ic_cold);
            put(32, R.drawable.ic_windy);
            put(33, R.drawable.ic_clear);
            put(34, R.drawable.ic_partly_cloudy);
            put(35, R.drawable.ic_partly_cloudy);
            put(36, R.drawable.ic_intermittent_clouds);
            put(37, R.drawable.ic_hazy_moonlight);
            put(38, R.drawable.ic_cloudy);
            put(39, R.drawable.ic_partly_cloudy_w_showers);
            put(40, R.drawable.ic_mostly_cloudy_w_showers);
            put(41, R.drawable.ic_partly_cloudy_w_t_storms);
            put(42, R.drawable.ic_mostly_cloudy_w_t_storms);
            put(43, R.drawable.ic_mostly_cloudy_w_snow);
            put(44, R.drawable.ic_mostly_cloudy_w_snow);

        }
    };
    //[FEATURE]-Add-BEGIN by TSCD.peng.du,12/17/2015,1176380,[Weather]ACC Widget DEV
    public static ArrayMap<Integer, Integer> iconWhiteTimeWidgetArray = new ArrayMap<Integer, Integer>() {
        {
            put(1, R.drawable.ic_widget_sunny);
            put(2, R.drawable.ic_widget_mostly_sunny);
            put(3, R.drawable.ic_widget_partly_sunny);
            put(4, R.drawable.ic_widget_intermittent_clouds);
            put(5, R.drawable.ic_widget_hazy_sunshine);
            put(6, R.drawable.ic_widget_mostly_cloudy);
            put(7, R.drawable.ic_widget_cloudy);
            put(8, R.drawable.ic_widget_dreary);
            put(11, R.drawable.ic_widget_fog);
            put(12, R.drawable.ic_widget_showers);
            put(13, R.drawable.ic_widget_mostly_cloudy_w_showers);
            put(14, R.drawable.ic_widget_partly_sunny_w_showers);
            put(15, R.drawable.ic_widget_tstorms);
            put(16, R.drawable.ic_widget_mostly_cloudy_w_tstorms);
            put(17, R.drawable.ic_widget_tstorms);
            put(18, R.drawable.ic_widget_rain);
            put(19, R.drawable.ic_widget_flurries);
            put(20, R.drawable.ic_widget_mostly_cloudy_w_flurries);
            put(21, R.drawable.ic_widget_mostly_cloudy_w_flurries);
            put(22, R.drawable.ic_widget_snow);
            put(23, R.drawable.ic_widget_mostly_cloudy_w_snow);
            put(24, R.drawable.ic_widget_ice);
            put(25, R.drawable.ic_widget_sleet);
            put(26, R.drawable.ic_widget_freezing_rain);
            put(29, R.drawable.ic_widget_rain_snow);
            put(30, R.drawable.ic_widget_hot);
            put(31, R.drawable.ic_widget_cold);
            put(32, R.drawable.ic_widget_windy);
            put(33, R.drawable.ic_widget_clear);
            put(34, R.drawable.ic_widget_mostly_clear);
            put(35, R.drawable.ic_widget_partly_cloudy_night);
            put(36, R.drawable.ic_widget_intermittent_clouds_night);
            put(37, R.drawable.ic_widget_hazy_moonlight_night);
            put(38, R.drawable.ic_widget_mostly_cloudy_night);
            put(39, R.drawable.ic_widget_partly_cloudy_w_showers_night);
            put(40, R.drawable.ic_widget_mostly_cloudy_w_showers_night);
            put(41, R.drawable.ic_widget_partly_cloudy_w_tstorms_night);
            put(42, R.drawable.ic_widget_mostly_cloudy_w_tstorms_night);
            put(43, R.drawable.ic_widget_mostly_cloudy_w_flurries);
            put(44, R.drawable.ic_widget_mostly_cloudy_w_snow_night);
        }
    };

    public static ArrayMap<Integer, Integer> iconBlackTimeWidgetArray = new ArrayMap<Integer, Integer>() {
        {
            put(1, R.drawable.ic_black_sunny);
            put(2, R.drawable.ic_black_mostly_sunny);
            put(3, R.drawable.ic_black_partly_sunny);
            put(4, R.drawable.ic_black_intermittent_clouds);
            put(5, R.drawable.ic_black_hazy_sunshine);
            put(6, R.drawable.ic_black_mostly_cloudy);
            put(7, R.drawable.ic_black_cloudy);
            put(8, R.drawable.ic_black_dreary);
            put(11, R.drawable.ic_black_fog);
            put(12, R.drawable.ic_black_showers);
            put(13, R.drawable.ic_black_mostly_cloudy_w_showers);
            put(14, R.drawable.ic_black_partly_sunny_w_showers);
            put(15, R.drawable.ic_black_tstorms);
            put(16, R.drawable.ic_black_mostly_cloudy_w_tstorms);
            put(17, R.drawable.ic_black_tstorms);
            put(18, R.drawable.ic_black_rain);
            put(19, R.drawable.ic_black_flurries);
            put(20, R.drawable.ic_black_mostly_cloudy_w_flurries);
            put(21, R.drawable.ic_black_mostly_cloudy_w_flurries);
            put(22, R.drawable.ic_black_snow);
            put(23, R.drawable.ic_black_mostly_cloudy_w_snow);
            put(24, R.drawable.ic_black_ice);
            put(25, R.drawable.ic_black_sleet);
            put(26, R.drawable.ic_black_freezing_rain);
            put(29, R.drawable.ic_black_rain_snow);
            put(30, R.drawable.ic_black_hot);
            put(31, R.drawable.ic_black_cold);
            put(32, R.drawable.ic_black_windy);
            put(33, R.drawable.ic_black_clear);
            put(34, R.drawable.ic_black_mostly_clear);
            put(35, R.drawable.ic_black_partly_cloudy_night);
            put(36, R.drawable.ic_black_intermittent_clouds_night);
            put(37, R.drawable.ic_black_hazy_moonlight_night);
            put(38, R.drawable.ic_black_mostly_cloudy_night);
            put(39, R.drawable.ic_black_partly_cloudy_w_showers_night);
            put(40, R.drawable.ic_black_mostly_cloudy_w_showers_night);
            put(41, R.drawable.ic_black_partly_cloudy_w_tstorms_night);
            put(42, R.drawable.ic_black_mostly_cloudy_w_tstorms_night);
            put(43, R.drawable.ic_black_mostly_cloudy_w_flurries);
            put(44, R.drawable.ic_black_mostly_cloudy_w_snow_night);
        }
    };

    /*MODIFIED-BEGIN by qian-li, 2016-04-13,BUG-1940875*/
    public static ArrayMap<Integer, Integer> iconThinBlackLittleWidgetArray = new ArrayMap<Integer, Integer>() {
        {
            put(1, R.drawable.ic_thin_black_sunny);
            put(2, R.drawable.ic_thin_black_mostly_sunny);
            put(3, R.drawable.ic_thin_black_partly_sunny);
            put(4, R.drawable.ic_thin_black_intermittent_clouds);
            put(5, R.drawable.ic_thin_black_hazy_sunshine);
            put(6, R.drawable.ic_thin_black_mostly_cloudy);
            put(7, R.drawable.ic_thin_black_cloudy);
            put(8, R.drawable.ic_thin_black_dreary);
            put(11, R.drawable.ic_thin_black_fog);
            put(12, R.drawable.ic_thin_black_showers);
            put(13, R.drawable.ic_thin_black_mostly_cloudy_w_showers);
            put(14, R.drawable.ic_thin_black_partly_sunny_w_showers);
            put(15, R.drawable.ic_thin_black_tstorms);
            put(16, R.drawable.ic_thin_black_mostly_cloudy_w_tstorms);
            put(17, R.drawable.ic_thin_black_tstorms);
            put(18, R.drawable.ic_thin_black_rain);
            put(19, R.drawable.ic_thin_black_flurries);
            put(20, R.drawable.ic_thin_black_mostly_cloudy_w_flurries);
            put(21, R.drawable.ic_thin_black_mostly_cloudy_w_flurries);
            put(22, R.drawable.ic_thin_black_snow);
            put(23, R.drawable.ic_thin_black_mostly_cloudy_w_snow);
            put(24, R.drawable.ic_thin_black_ice);
            put(25, R.drawable.ic_thin_black_sleet);
            put(26, R.drawable.ic_thin_black_freezing_rain);
            put(29, R.drawable.ic_thin_black_rain_snow);
            put(30, R.drawable.ic_thin_black_hot);
            put(31, R.drawable.ic_thin_black_cold);
            put(32, R.drawable.ic_thin_black_windy);
            put(33, R.drawable.ic_thin_black_clear);
            put(34, R.drawable.ic_thin_black_mostly_clear);
            put(35, R.drawable.ic_thin_black_partly_cloudy_night);
            put(36, R.drawable.ic_thin_black_intermittent_clouds_night);
            put(37, R.drawable.ic_thin_black_hazy_moonlight_night);
            put(38, R.drawable.ic_thin_black_mostly_cloudy_night);
            put(39, R.drawable.ic_thin_black_partly_cloudy_w_showers_night);
            put(40, R.drawable.ic_thin_black_mostly_cloudy_w_showers_night);
            put(41, R.drawable.ic_thin_black_partly_cloudy_w_tstorms_night);
            put(42, R.drawable.ic_thin_black_mostly_cloudy_w_tstorms_night);
            put(43, R.drawable.ic_thin_black_mostly_cloudy_w_flurries);
            put(44, R.drawable.ic_thin_black_mostly_cloudy_w_snow_night);
        }
    };

    public static ArrayMap<Integer, Integer> iconThinWhiteLittleWidgetArray = new ArrayMap<Integer, Integer>() {
        {
            put(1, R.drawable.ic_thin_white_sunny);
            put(2, R.drawable.ic_thin_white_mostly_sunny);
            put(3, R.drawable.ic_thin_white_partly_sunny);
            put(4, R.drawable.ic_thin_white_intermittent_clouds);
            put(5, R.drawable.ic_thin_white_hazy_sunshine);
            put(6, R.drawable.ic_thin_white_mostly_cloudy);
            put(7, R.drawable.ic_thin_white_cloudy);
            put(8, R.drawable.ic_thin_white_dreary);
            put(11, R.drawable.ic_thin_white_fog);
            put(12, R.drawable.ic_thin_white_showers);
            put(13, R.drawable.ic_thin_white_mostly_cloudy_w_showers);
            put(14, R.drawable.ic_thin_white_partly_sunny_w_showers);
            put(15, R.drawable.ic_thin_white_tstorms);
            put(16, R.drawable.ic_thin_white_mostly_cloudy_w_tstorms);
            put(17, R.drawable.ic_thin_white_tstorms);
            put(18, R.drawable.ic_thin_white_rain);
            put(19, R.drawable.ic_thin_white_flurries);
            put(20, R.drawable.ic_thin_white_mostly_cloudy_w_flurries);
            put(21, R.drawable.ic_thin_white_mostly_cloudy_w_flurries);
            put(22, R.drawable.ic_thin_white_snow);
            put(23, R.drawable.ic_thin_white_mostly_cloudy_w_snow);
            put(24, R.drawable.ic_thin_white_ice);
            put(25, R.drawable.ic_thin_white_sleet);
            put(26, R.drawable.ic_thin_white_freezing_rain);
            put(29, R.drawable.ic_thin_white_rain_snow);
            put(30, R.drawable.ic_thin_white_hot);
            put(31, R.drawable.ic_thin_white_cold);
            put(32, R.drawable.ic_thin_white_windy);
            put(33, R.drawable.ic_thin_white_clear);
            put(34, R.drawable.ic_thin_white_mostly_clear);
            put(35, R.drawable.ic_thin_white_partly_cloudy_night);
            put(36, R.drawable.ic_thin_white_intermittent_clouds_night);
            put(37, R.drawable.ic_thin_white_hazy_moonlight_night);
            put(38, R.drawable.ic_thin_white_mostly_cloudy_night);
            put(39, R.drawable.ic_thin_white_partly_cloudy_w_showers_night);
            put(40, R.drawable.ic_thin_white_mostly_cloudy_w_showers_night);
            put(41, R.drawable.ic_thin_white_partly_cloudy_w_tstorms_night);
            put(42, R.drawable.ic_thin_white_mostly_cloudy_w_tstorms_night);
            put(43, R.drawable.ic_thin_white_mostly_cloudy_w_flurries);
            put(44, R.drawable.ic_thin_white_mostly_cloudy_w_snow_night);
        }
    };
    /*MODIFIED-END by qian-li,BUG-1940875*/

    static public int getTimeWidgetWhiteIcon(String icon) {
        Integer id = Integer.valueOf(icon);
        if (iconWhiteTimeWidgetArray.containsKey(id)) {
            return iconWhiteTimeWidgetArray.get(id);
        } else {
            return R.drawable.ic_unknown;
        }
    }

    static public int getTimeWidgetBlackIcon(String icon) {
        Integer id = Integer.valueOf(icon);
        if (iconBlackTimeWidgetArray.containsKey(id)) {
            return iconBlackTimeWidgetArray.get(id);
        } else {
            return R.drawable.ic_black_unknown;
        }
    }

    /*MODIFIED-BEGIN by qian-li, 2016-04-13,BUG-1940875*/
    static public int getLittleWidgetWhiteIcon(String icon) {
        Integer id = Integer.valueOf(icon);
        if (iconThinWhiteLittleWidgetArray.containsKey(id)) {
            return iconThinWhiteLittleWidgetArray.get(id);
        } else {
            return R.drawable.ic_thin_white_unknown;
        }
    }

    static public int getLittleWidgetBlackIcon(String icon) {
        Integer id = Integer.valueOf(icon);
        if (iconThinBlackLittleWidgetArray.containsKey(id)) {
            return iconThinBlackLittleWidgetArray.get(id);
        } else {
            return R.drawable.ic_thin_black_unknown;
        }
    }

    static public int getACCDailyIcon(String icon) {
        Integer id = Integer.valueOf(icon);
        if (iconACCDailyArray.containsKey(id)) {
            return iconACCDailyArray.get(id);
        } else {
            return R.drawable.ic_sunny;
            /*MODIFIED-END by qian-li,BUG-1940875*/
        }
    }

    public static ArrayMap<Integer, Integer> iconACCWidgetArray = new ArrayMap<Integer, Integer>() {
        {
            put(1, R.drawable.ic_grey_sunny);
            put(2, R.drawable.ic_grey_mostly_sunny);
            put(3, R.drawable.ic_grey_partly_sunny);
            put(4, R.drawable.ic_grey_intermittent_clouds);
            put(5, R.drawable.ic_grey_hazy_sunshine);
            put(6, R.drawable.ic_grey_mostly_cloudy);
            put(7, R.drawable.ic_grey_cloudy);
            put(8, R.drawable.ic_grey_dreary);
            put(11, R.drawable.ic_grey_fog);
            put(12, R.drawable.ic_grey_showers);
            put(13, R.drawable.ic_grey_mostly_cloudy_w_showers);
            put(14, R.drawable.ic_grey_partly_sunny_w_showers);
            put(15, R.drawable.ic_grey_t_storms);
            put(16, R.drawable.ic_grey_mostly_cloudy_w_t_storms);
            /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1982493*/
            put(17, R.drawable.ic_grey_mostly_cloudy_w_t_storms);
            put(18, R.drawable.ic_grey_rain);
            put(19, R.drawable.ic_grey_flurries);
            put(20, R.drawable.ic_grey_mostly_cloudy_w_flurries);
            put(21, R.drawable.ic_grey_partly_sunny_w_flurries);
            /* MODIFIED-END by xiangnan.zhou,BUG-1982493*/
            put(22, R.drawable.ic_grey_snow);
            put(23, R.drawable.ic_grey_mostly_cloudy_w_snow);
            put(24, R.drawable.ic_grey_ice);
            put(25, R.drawable.ic_grey_sleet);
            put(26, R.drawable.ic_grey_freezing_rain);
            put(29, R.drawable.ic_grey_rain_snow);
            put(30, R.drawable.ic_grey_hot);
            put(31, R.drawable.ic_grey_cold);
            put(32, R.drawable.ic_grey_windy);
            put(33, R.drawable.ic_grey_clear);
            /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1982493*/
            put(34, R.drawable.ic_grey_mostly_clear);
            put(35, R.drawable.ic_grey_partly_cloudy_night);
            put(36, R.drawable.ic_grey_intermittent_clouds_night);
            put(37, R.drawable.ic_grey_hazy_moonlight_night);
            put(38, R.drawable.ic_grey_cloudy);
            put(39, R.drawable.ic_grey_partly_cloudy_w_showers_night);
            put(40, R.drawable.ic_grey_mostly_cloudy_w_showers_night);
            put(41, R.drawable.ic_grey_mostly_cloudy_w_t_storms);
            put(42, R.drawable.ic_grey_mostly_cloudy_w_t_storms);
            put(43, R.drawable.ic_grey_mostly_cloudy_w_snow_night);
            put(44, R.drawable.ic_grey_mostly_cloudy_w_snow_night);
            /* MODIFIED-END by xiangnan.zhou,BUG-1982493*/

        }
    };

    static public int getACCWidgetIcon(String icon) {
        Integer id = Integer.valueOf(icon);
        if (iconACCWidgetArray.containsKey(id)) {
            return iconACCWidgetArray.get(id);
        } else {
            return R.drawable.ic_grey_sunny;
        }
    }

    public static ArrayMap<Integer, Integer> TWCBackGroundArray = new ArrayMap<Integer, Integer>() {
        {
            put(27, R.drawable.bg_clear);
            put(29, R.drawable.bg_clear);
            put(31, R.drawable.bg_clear);
            put(33, R.drawable.bg_clear);

            put(26, R.drawable.bg_cloudy);
            put(28, R.drawable.bg_cloudy);
            put(30, R.drawable.bg_cloudy);
            put(34, R.drawable.bg_cloudy);

            put(19, R.drawable.bg_fog);
            put(20, R.drawable.bg_fog);
            put(21, R.drawable.bg_fog);
            put(22, R.drawable.bg_fog);
            put(23, R.drawable.bg_fog);
            put(24, R.drawable.bg_fog);

            put(25, R.drawable.bg_frost);

            put(4, R.drawable.bg_rainy);
            put(5, R.drawable.bg_rainy);
            put(6, R.drawable.bg_rainy);
            put(8, R.drawable.bg_rainy);
            put(9, R.drawable.bg_rainy);
            put(10, R.drawable.bg_rainy);
            put(11, R.drawable.bg_rainy);
            put(12, R.drawable.bg_rainy);
            put(40, R.drawable.bg_rainy);

            put(7, R.drawable.bg_snow);
            put(13, R.drawable.bg_snow);
            put(14, R.drawable.bg_snow);
            put(15, R.drawable.bg_snow);
            put(16, R.drawable.bg_snow);
            put(17, R.drawable.bg_snow);
            put(18, R.drawable.bg_snow);
            put(42, R.drawable.bg_snow);

            put(38, R.drawable.bg_storm);
            put(47, R.drawable.bg_storm);

            put(32, R.drawable.bg_sunny);
        }
    };

    public static ArrayMap<Integer, Integer> ACCBackGroundArray = new ArrayMap<Integer, Integer>() {
        {
            put(1, R.drawable.bg_sunny);
            put(2, R.drawable.bg_sunny);
            put(4, R.drawable.bg_sunny);
            put(5, R.drawable.bg_sunny);
            put(14, R.drawable.bg_sunny);
            put(21, R.drawable.bg_sunny);
            put(30, R.drawable.bg_sunny);

            put(33, R.drawable.bg_clear);
            put(34, R.drawable.bg_clear);
            put(36, R.drawable.bg_clear);
            put(37, R.drawable.bg_clear);

            put(35, R.drawable.bg_night_cloudy);
            put(38, R.drawable.bg_night_cloudy);

            put(3, R.drawable.bg_cloudy);
            put(6, R.drawable.bg_cloudy);
            put(7, R.drawable.bg_cloudy);
            put(8, R.drawable.bg_cloudy);
            put(13, R.drawable.bg_cloudy);
            put(16, R.drawable.bg_cloudy);
            put(17, R.drawable.bg_cloudy);
            put(20, R.drawable.bg_cloudy);
            put(23, R.drawable.bg_cloudy);
            put(32, R.drawable.bg_cloudy);

            put(11, R.drawable.bg_fog);

            put(24, R.drawable.bg_frost);
            put(31, R.drawable.bg_frost);

            put(12, R.drawable.bg_rainy);
            put(18, R.drawable.bg_rainy);
            put(25, R.drawable.bg_rainy);
            put(26, R.drawable.bg_rainy);
            put(39, R.drawable.bg_rainy);
            put(40, R.drawable.bg_rainy);

            put(19, R.drawable.bg_snow);
            put(22, R.drawable.bg_snow);
            put(29, R.drawable.bg_snow);
            put(43, R.drawable.bg_snow);
            put(44, R.drawable.bg_snow);

            put(15, R.drawable.bg_storm);
            put(41, R.drawable.bg_storm);
            put(42, R.drawable.bg_storm);
        }
    };

    static Uri uriClear = Uri.parse("android.resource://com.tct.weather/" + R.raw.am_clear);
    static Uri uriNightCloundy = Uri.parse("android.resource://com.tct.weather/" + R.raw.am_night_clundy);
    static Uri uriCloundy = Uri.parse("android.resource://com.tct.weather/" + R.raw.am_cloundy);
    static Uri uriFog = Uri.parse("android.resource://com.tct.weather/" + R.raw.am_fog);
    static Uri uriFrost = Uri.parse("android.resource://com.tct.weather/" + R.raw.am_frost);
    static Uri uriRain = Uri.parse("android.resource://com.tct.weather/" + R.raw.am_rain);
    static Uri uriSnow = Uri.parse("android.resource://com.tct.weather/" + R.raw.am_snow);
    static Uri uriStrom = Uri.parse("android.resource://com.tct.weather/" + R.raw.am_storm);
    static Uri uriSunny = Uri.parse("android.resource://com.tct.weather/" + R.raw.am_sunny);

    public static ArrayMap<Integer, Uri> TWCBackGroundUriArray = new ArrayMap<Integer, Uri>() {
        {
            put(32, uriSunny);
            put(34, uriSunny);

            put(27, uriClear);
            put(29, uriClear);
            put(31, uriClear);
            put(33, uriClear);

            put(26, uriCloundy);
            put(28, uriCloundy);
            put(30, uriCloundy);

            put(19, uriFog);
            put(20, uriFog);
            put(21, uriFog);
            put(22, uriFog);
            put(23, uriFog);
            put(24, uriFog);

            put(25, uriFrost);

            put(4, uriRain);
            put(5, uriRain);
            put(6, uriRain);
            put(8, uriRain);
            put(9, uriRain);
            put(10, uriRain);
            put(11, uriRain);
            put(12, uriRain);
            put(40, uriRain);

            put(7, uriSnow);
            put(13, uriSnow);
            put(14, uriSnow);
            put(15, uriSnow);
            put(16, uriSnow);
            put(17, uriSnow);
            put(18, uriSnow);
            put(42, uriSnow);

            put(38, uriStrom);
            put(47, uriStrom);
        }
    };

    public static ArrayMap<Integer, Uri> ACCBackGroundUriArray = new ArrayMap<Integer, Uri>() {
        {
            put(1, uriSunny);
            put(2, uriSunny);
            put(4, uriSunny);
            put(5, uriSunny);
            put(14, uriSunny);
            put(21, uriSunny);
            put(30, uriSunny);

            put(33, uriClear);
            put(34, uriClear);
            put(36, uriClear);
            put(37, uriClear);

            put(35, uriNightCloundy);
            put(38, uriNightCloundy);

            put(3, uriCloundy);
            put(6, uriCloundy);
            put(7, uriCloundy);
            put(8, uriCloundy);
            put(13, uriCloundy);
            put(16, uriCloundy);
            put(17, uriCloundy);
            put(20, uriCloundy);
            put(23, uriCloundy);
            put(32, uriCloundy);

            put(11, uriFog);

            put(24, uriFrost);
            put(31, uriFrost);

            put(12, uriRain);
            put(18, uriRain);
            put(25, uriRain);
            put(26, uriRain);
            put(39, uriRain);
            put(40, uriRain);

            put(19, uriSnow);
            put(22, uriSnow);
            put(29, uriSnow);
            put(43, uriSnow);
            put(44, uriSnow);

            put(15, uriStrom);
            put(41, uriStrom);
            put(42, uriStrom);
        }
    };
}
