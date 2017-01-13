package com.tct.weather.util;

import java.text.DecimalFormat;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.tct.weather.R;

public class CommonUtils {
    private static final String LOG_TAG = "CommonUtils";
    public static final String TAG_BING = "DebugByBing";
    //Defect 212555 Outdoor auto location is failed by bing.wang.hz
    public static final String FIRST_LOCATION_TRY = "is_first_location_try";

    public static final int LOCATION_TIMER_DURATION = 60 * 60 * 1000;
    public static final String LOCATION_TIMER_TASK_ACTION = "com.tct.action.LOCATION_TIMER_TASK_ACTION";
    public static final String AUTO_LOCATION_TASK_ACTION = "com.tct.weather.START_AUTO_LOCATION_TASK_ACTION";

    public static boolean isSupportHorizontal(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = 0;
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            screenHeight = dm.heightPixels;
        } else if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            screenHeight = dm.widthPixels;
        }

        if (screenHeight <= 480 && getScreenInch(activity) <= 4.5) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isPad(Activity activity) {
        double screenInches = getScreenInch(activity);
        // 大于6尺寸则为Pad
        if (screenInches >= 6.0) {
            return true;
        }
        return false;
    }

    public static double getScreenInch(Activity activity) {
//		WindowManager wm = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
//		Display display = wm.getDefaultDisplay();
//		// 屏幕宽度
//		float screenWidth = display.getWidth();
//		// 屏幕高度
//		float screenHeight = display.getHeight();
//		DisplayMetrics dm = new DisplayMetrics();
//		display.getMetrics(dm);
//		double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
//		double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
//		// 屏幕尺寸
//		double screenInches = Math.sqrt(x + y);
//		return screenInches;
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) + Math.pow(dm.heightPixels, 2));
        int densityDpi = dm.densityDpi;
        double screenInches = diagonalPixels / densityDpi;
        Log.i("jielong", "screenInches == " + screenInches + ", density == " + dm.density + ", densityDpi == " + densityDpi);
        return screenInches;
    }

    public static String getHourOfTime(Context context, boolean is24Hour, String time) {
        if (is24Hour) {
            if (time.equals("12 PM")) {
                time = "12:00";
            } else if (time.equals("12 AM")) {
                time = "0:00";
            } else if (time.contains("PM")) {
                int num = Integer.parseInt(time.substring(0, time.indexOf(" PM")));
                if (num > 0 && num < 12) {
                    time = (num + 12) + ":00";
                }
            } else if (time.contains("AM")) {
                int num = Integer.parseInt(time.substring(0, time.indexOf(" AM")));
                if (num > 0 && num < 12) {
                    time = num + ":00";
                }
            }
        } else {
            if (time.contains("PM")) {
                time = time.replace("PM", context.getResources().getString(R.string.date_pm));
            } else if (time.contains("AM")) {
                time = time.replace("AM", context.getResources().getString(R.string.date_am));
            }
        }
        return time;
    }

    public static String f2c(String fahrenheit) {
        if (!TextUtils.isEmpty(fahrenheit)) {
            int celsius = (int)((Float.parseFloat(fahrenheit) - 32) * 5 / 9);
            return celsius + "";
        } else {
            return "";
        }
    }
    //[BUGFIX]-Add-BEGIN by TSCD.xiangnan.zhou,01/29/2016,1536478,
    //[GAPP][Android6.0][Weather]Please remove decimals from all
    public static String c2f(String celsius) {
        if (!TextUtils.isEmpty(celsius)) {
            int fahrenheit = (int)(Float.parseFloat(celsius) * 9 / 5 + 32);
            return fahrenheit + "";
        } else {
            return "";
        }
    }

    public static String km2mi(String km) {
        try {
            double mi =Float.parseFloat(km) * 0.6214;
            return String.valueOf((int)mi);
        } catch (Exception e) {
            Log.i(LOG_TAG, "km2mi exception : wind = " + km);
            return "0";
        }
    }
    public static String deletaDec(String de) {
        try {
            float tem = Float.parseFloat(de);
            return String.valueOf((int)tem);
        } catch (Exception e) {
            Log.i(LOG_TAG, "deletaDec exception : wind = " + de);
            return "0";
        }
    }
    //[BUGFIX]-Add-END by TSCD.xiangnan.zhou
    public static String km2m(String km) {
        try {
            DecimalFormat fnum = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
            fnum.applyPattern("##0.0");
            return fnum.format(Float.parseFloat(km) * 0.2778);
        } catch (Exception e) {
            Log.e(LOG_TAG, "km2m exception : wind = " + km);
            return "0";
        }
    }

    //Defect 212555 Outdoor auto location is failed by bing.wang.hz begin
    public static boolean isQcomPlatform() {
        try {
            Class<?> managerClass = Class
                    .forName("qcom.fmradio.FmConfig");
            if (managerClass.getClass() != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            Log.i(LOG_TAG, "Can't find the class 'qcom.fmradio.FmConfig',maybe it's not Qcom platform.");
        } catch (LinkageError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    //Defect 212555 Outdoor auto location is failed by bing.wang.hz end
}
