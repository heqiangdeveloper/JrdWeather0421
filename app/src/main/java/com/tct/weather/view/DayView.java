package com.tct.weather.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tct.weather.bean.DayForShow;
import com.tct.weather.util.CommonUtils;
import com.tct.weather.util.IconBackgroundUtil;
import com.tct.weather.R;

import java.util.Locale;

public class DayView extends LinearLayout {
    private static final String TAG = "weather DayView";
    private static final String UNITC = "°";
    private static final String UNITF = "°";

    private Context mContext = null;

    private TextView DayWeek;
    private ImageView DayIcon;
    private TextView DayHighT;
    private TextView DayLowT;
    private TextView mTvPhrase;
    private LinearLayout mLLrecipitation;
    private TextView mTvrecipitation;
    private View mDivideView;

    public DayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        setOrientation(LinearLayout.HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.day_forecast_item, this, true);

        DayWeek = (TextView) findViewById(R.id.tv_week);
        DayIcon = (ImageView) findViewById(R.id.iv_weather_icon);
        DayHighT = (TextView) findViewById(R.id.tv_high);
        DayLowT = (TextView) findViewById(R.id.tv_low);
        mTvPhrase = (TextView) findViewById(R.id.tv_daily_phrase);
        mLLrecipitation = (LinearLayout) findViewById(R.id.ll_daily_precipitation);
//        mTvrecipitation = (TextView) findViewById(R.id.tv_daily_precipitation_val);
        mDivideView = findViewById(R.id.v_divide);
    }

    public void setDayView(DayForShow dayItem, boolean isUnitC, boolean isTwcWeather) {
        String icon = dayItem.getIcon();
        //Log.d(TAG, "day icon == " + icon);
//        String imagepath = "";
//        if (icon.length() == 2) {
//            imagepath = "mini_icons_" + icon;
//        } else if (icon.length() == 1) {
//            imagepath = "mini_icons_" + (Integer.parseInt(dayItem.getIcon()) < 10 ? "0" : "") + icon;
//        }
//        int resId = mContext.getResources().getIdentifier(imagepath, "drawable", "com.tct.weather");
//        int resId= IconBackgroundUtil.getACCDailyIcon(icon);
        DayWeek.setText(getWeekly(dayItem.getWeek()));
//        DayIcon.setBackgroundResource(resId);
        //[BUGFIX]-add-begin by xinlei.sheng,2015/12/19,1170714
        String language = Locale.getDefault().getLanguage();
        if(language.equals("ar") || language.equals("fa")) {
            mTvPhrase.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        }
        //[BUGFIX]-add-end by xinlei.sheng,2015/12/19,1170714
        mTvPhrase.setText(dayItem.getPhrase());
        mLLrecipitation = (LinearLayout) findViewById(R.id.ll_daily_precipitation);
        if (isTwcWeather) {
/*MODIFIED-BEGIN by qian-li, 2016-04-13,BUG-1940875*/
//            int resId= IconBackgroundUtil.getTWCDailyIcon(icon);
//            DayIcon.setBackgroundResource(resId);
/*MODIFIED-END by qian-li,BUG-1940875*/
            mLLrecipitation.setVisibility(VISIBLE);
            mTvrecipitation = (TextView) findViewById(R.id.tv_daily_precipitation_val);
            mTvrecipitation.setText(dayItem.getPrecipitation());
        } else {
            int resId= IconBackgroundUtil.getACCDailyIcon(icon);
            DayIcon.setBackgroundResource(resId);
            mLLrecipitation.setVisibility(GONE);
        }
        if (isUnitC) {
            DayHighT.setText(CommonUtils.deletaDec(dayItem.getTemph()) + UNITC);
            DayLowT.setText(CommonUtils.deletaDec(dayItem.getTempl()) + UNITC);
        } else {
            DayHighT.setText(CommonUtils.c2f(dayItem.getTemph()) + UNITF);
            DayLowT.setText(CommonUtils.c2f(dayItem.getTempl()) + UNITF);
        }
    }

    private String getWeekly(String sWeek) {
        String[] enWeekly = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri",
                "Sat", "Sun"};
        String result = null;
        String[] weekly = this.getResources().getStringArray(R.array.weather_weekly);
        for (int i = 0; i < 7; i++) {
            if (sWeek.contains(enWeekly[i])) {
                result = weekly[i];
            }
        }
        if (result == null) {
            result = sWeek;
        }
        return result;
    }

    public void setTextColor(int color) {
        DayWeek.setTextColor(color);
        DayHighT.setTextColor(color);
        DayLowT.setTextColor(color);
    }

    public void setDivideViewGone() {
        mDivideView.setVisibility(GONE);
    }

}
