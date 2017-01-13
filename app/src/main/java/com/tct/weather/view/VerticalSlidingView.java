package com.tct.weather.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.Space;
import android.widget.TextView;

import com.tct.weather.bean.DayForShow;
import com.tct.weather.bean.Hour;
import com.tct.weather.bean.WeatherForShow;
import com.tct.weather.util.CommonUtils;
import com.tct.weather.R;
import com.tct.weather.util.CustomizeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jiajun.shen on 8/4/15.
 */
public class VerticalSlidingView extends LinearLayout {
    private static final String TAG = "weather VerticalView";

    private Context mContext = null;
    private TextView mTvWeatherText = null;
    private TextView mTvWeatherTemp = null;
    // private TextView mTvWeatherUnit = null;
    private TextView mTvWeatherCity = null;
    private TextView mTvWeatherTime = null;


    private TextView mTvWeatherRealFeelKey = null;
    private TextView mTvWeatherRealFeelTail = null;
    private TextView mTvWeatherRealFeelVal = null;
    private TextView mTvWeatherRealFeelUnit = null;
    private TextView mTvWeatherHumidityLabel = null;
    private TextView mTvWeatherHumidityVal = null;
    private TextView mTvWeatherHumidityUnit = null;
    private ImageView mIvWeatherHumidityIcon = null;
    private TextView mTvWeatherWindLabel = null;
    private TextView mTvWeatherWindVal = null;
    private TextView mTvWeatherWindUnit = null;
    private ImageView mIvWeatherWindIcon = null;
//    private TextView mTvWeatherVisibilityLabel = null;
    private TextView mTvWeatherVisibilityVal = null;
    private TextView mTvWeatherVisibilityUnit = null;
    private ImageView mIvWeatherVisibilityIcon = null;
    private TextView mTvWeatherHighVal = null;
//    private TextView mTvWeatherHighLabel = null;
    private TextView mTvWeatherHighUnit = null;
    private ImageView mIvWeatherHighIcon = null;
    private TextView mTvWeatherLowLabel = null;
    private TextView mTvWeatherLowVal = null;
    private TextView mTvWeatherLowUnit = null;
    private ImageView mIvWeatherLowIcon = null;
//    private TextView mTvWeatherPreciptationLabel = null;
    private TextView mTvWeatherPreciptationVal = null;
//    private TextView mTvWeatherPreciptationUnit = null;
    private ImageView mIvWeatherreciptationIcon = null;
    private TextView mTvWeatherPressureLabel = null;
    private TextView mTvWeatherPressureVal = null;
    private TextView mTvWeatherPressureUnit = null;
    private ImageView mIvWeatherPressureIcon = null;
    private TextView mTvWeatherUvLabel = null;
    private TextView mTvWeatherUvVal = null;
    private TextView mTvWeatherUvUnit = null;
    private ImageView mIvWeatherUvIcon = null;

    private CardView weather_info_layout = null;
    private RecyclerView hourlyRecyclerView = null;
    private RecyclerView.Adapter hourlyAdapter;
    private RecyclerView.LayoutManager hourlyLayoutManager;
    private Space betweenSpace = null;
    private LinearLayout ll_day = null;
    private LinearLayout weatherDetails = null;
    private LinearLayout weatherBaseInfo = null;
    private ExpandButton expandButton = null;
    private LinearLayout weatherMoreInfo = null;

    private String mCurrentWeatherUrl = "";
    private ArrayList<String> mForecastWeatherUrlList = new ArrayList<String>();

    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mTopHeight;
    private int mTotalHeight;
    private int mPageHeight;
    private int mBottomHeight;
    private int mStartYPosition;
    private int mEndYPosition;
    private int mCurrentPage = 0;
    private int mMoreInfoHeight = 0;
    private int mTopSpaceHeight = 0;
    private int mShadowSpaceHeight = 0;
    private int mBetweenSpaceHeight = 0;

    private boolean mIsScrolling = false;

    private boolean mIsExpanded = false;

    private boolean mIsTouchBottom = true;
    private boolean tempIsTouchBottom = true;
    private int mScrollPosition = 0;
    //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/26/2015,983828,3.1weather screen
    private boolean isTwcWeather = false;
    //[FEATURE]-Add-END by TSCD.peng.du
    private Space topSpace;

    private Space shadowSpace;

    private OnPageScrollListener mPageScrollListener;

    public VerticalSlidingView(Context context) {
        super(context);
        mContext = context;
        //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/26/2015,983828,3.1weather screen
        isTwcWeather =CustomizeUtils.getBoolean(context, "use_twc_weather");
        //[FEATURE]-Add-END by TSCD.peng.du
        init(context);
    }

    public VerticalSlidingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/26/2015,983828,3.1weather screen
        isTwcWeather =CustomizeUtils.getBoolean(context, "use_twc_weather");
        //[FEATURE]-Add-END by TSCD.peng.du
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        mScroller = new OverScroller(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        weatherDetails = (LinearLayout) findViewById(R.id.weather_details);

        weatherMoreInfo = (LinearLayout) findViewById(R.id.weather_more_info_layout);
        weatherBaseInfo = (LinearLayout) findViewById(R.id.weather_base_info_layout);
        weatherBaseInfo.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMoreInfoHeight = weatherBaseInfo.getMeasuredHeight();
                weatherBaseInfo.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        mMoreInfoHeight = weatherBaseInfo.getMeasuredHeight();

        weather_info_layout = (CardView) findViewById(R.id.weather_info_layout);
//        weather_info_layout.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Message msg = mHandler.obtainMessage();
//                msg.what = SHOW_URL;
//                msg.obj = mCurrentWeatherUrl;
//                mHandler.sendMessageDelayed(msg, 200);
//            }
//        });
//[FEATURE]-Add-BEGIN by TSCD.peng.du,11/27/2015,983895,deleta weather_24forecast_card
        if(isTwcWeather) {
            hourlyRecyclerView = (RecyclerView) findViewById(R.id.rv_hourly);
            hourlyRecyclerView.setHasFixedSize(true);
            hourlyLayoutManager = new LinearLayoutManager(mContext, HORIZONTAL, false);
            hourlyRecyclerView.setLayoutManager(hourlyLayoutManager);
        }
//[FEATURE]-Add-END by TSCD.peng.du

        mTvWeatherText = (TextView) findViewById(R.id.tv_desc);

        expandButton = (ExpandButton) findViewById(R.id.expand_button);
        expandButton.setExpandButtonListener(new ExpandButton.ExpandButtonListener() {
            @Override
            public void onClick() {
                showWeatherInfo();
            }
        });
//        expandButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                ((Activity) mContext).setTheme(R.style.AppSunTheme);
////                ((Activity) mContext).recreate();
//                showWeatherInfo();
//
//            }
//        });


        mTvWeatherTemp = (TextView) findViewById(R.id.tv_temp);
        mTvWeatherCity = (TextView) findViewById(R.id.tv_city);
        mTvWeatherTime = (TextView) findViewById(R.id.tv_date);

        mTvWeatherRealFeelKey = (TextView) findViewById(R.id.tv_realfeel_key);
        mTvWeatherRealFeelTail = (TextView) findViewById(R.id.tv_realfeel_tail);
        mTvWeatherRealFeelVal = (TextView) findViewById(R.id.tv_realfeel_val);
        mTvWeatherRealFeelUnit = (TextView) findViewById(R.id.tv_realfeel_unit);
        mTvWeatherHumidityLabel = (TextView) findViewById(R.id.tv_humidity_label);
        mTvWeatherHumidityVal = (TextView) findViewById(R.id.tv_humidity_val);
        mTvWeatherHumidityUnit = (TextView) findViewById(R.id.tv_humidity_unit);
        mIvWeatherHumidityIcon = (ImageView) findViewById(R.id.tv_humidity_icon);
        mTvWeatherWindLabel = (TextView) findViewById(R.id.tv_wind_label);
        mTvWeatherWindVal = (TextView) findViewById(R.id.tv_wind_val);
        mTvWeatherWindUnit = (TextView) findViewById(R.id.tv_wind_unit);
        mIvWeatherWindIcon = (ImageView) findViewById(R.id.tv_wind_icon);
//        mTvWeatherVisibilityLabel = (TextView) findViewById(R.id.tv_visibility_label);
        mTvWeatherVisibilityVal = (TextView) findViewById(R.id.tv_visibility_val);
        mTvWeatherVisibilityUnit = (TextView) findViewById(R.id.tv_visibility_unit);
        mIvWeatherVisibilityIcon = (ImageView) findViewById(R.id.tv_visibility_icon);
//        mTvWeatherHighLabel = (TextView) findViewById(R.id.tv_temp_high_label);
        mTvWeatherHighVal = (TextView) findViewById(R.id.tv_temp_high_val);
        mTvWeatherHighUnit = (TextView) findViewById(R.id.tv_temp_high_unit);
        mIvWeatherHighIcon = (ImageView) findViewById(R.id.tv_temp_high_icon);
        mTvWeatherLowLabel = (TextView) findViewById(R.id.tv_temp_low_label);
        mTvWeatherLowVal = (TextView) findViewById(R.id.tv_temp_low_val);
        mTvWeatherLowUnit = (TextView) findViewById(R.id.tv_temp_low_unit);
        mIvWeatherLowIcon = (ImageView) findViewById(R.id.tv_temp_low_icon);
//        mTvWeatherPreciptationLabel = (TextView) findViewById(R.id.tv_preciptation_label);
        mTvWeatherPreciptationVal = (TextView) findViewById(R.id.tv_preciptation_val);
//        mTvWeatherPreciptationUnit = (TextView) findViewById(R.id.tv_preciptation_unit);
        mIvWeatherreciptationIcon = (ImageView) findViewById(R.id.tv_preciptation_icon);
        mTvWeatherPressureLabel = (TextView) findViewById(R.id.tv_pressure_label);
        mTvWeatherPressureVal = (TextView) findViewById(R.id.tv_pressure_val);
        mTvWeatherPressureUnit = (TextView) findViewById(R.id.tv_pressure_unit);
        mIvWeatherPressureIcon = (ImageView) findViewById(R.id.tv_pressure_icon);
        mTvWeatherUvLabel = (TextView) findViewById(R.id.tv_uv_index_label);
        mTvWeatherUvVal = (TextView) findViewById(R.id.tv_uv_index_val);
        mTvWeatherUvUnit = (TextView) findViewById(R.id.tv_uv_index_unit);
        mIvWeatherUvIcon = (ImageView) findViewById(R.id.tv_uv_index_icon);

        ll_day = (LinearLayout) findViewById(R.id.ll_day);

    }

    public void setWeatherData(final WeatherForShow weather, boolean isUnitC,
                               String refreshTime, boolean isAutoLocate,
                               boolean isUnitKm, boolean showAdvData, boolean showFeelsLike) {
        String icon = weather.getIcon();
        Log.d(TAG, "weather icon == " + icon + " isUnitC = " + isUnitC + " isUnitKm = " + isUnitKm);
        mTvWeatherTime.setText(refreshTime);
        mTvWeatherCity.setText(weather.getCity());
        mTvWeatherCity.setSelected(true);

        if (isUnitC) {
            //Log.e(TAG,"temp="+weather.getTemp()+" realfell="+weather.getRealfeel());
            if (showFeelsLike) {
                mTvWeatherTemp.setText(CommonUtils.deletaDec(weather.getRealfeel()) + "°");
            } else {
//                mTvWeatherTemp.setText((int) Math.round(Double.parseDouble(weather.getTemp())) + "°");
                mTvWeatherTemp.setText(CommonUtils.deletaDec(weather.getTemp()) + "°");
            }
            //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/26/2015,983828,3.1weather screen
            if (isTwcWeather) {
//                int tempH = (int) Math.round(Double.parseDouble(weather.getCurrentTempH()));
//                mTvWeatherHighVal.setText(Integer.toString(tempH));
                mTvWeatherHighVal.setText(CommonUtils.deletaDec(weather.getCurrentTempH()));
            }else {
//                int realfeel = (int) Math.round(Double.parseDouble(weather.getRealfeel()));
                mTvWeatherHighVal.setText(CommonUtils.deletaDec(weather.getRealfeel()));
            }
            //[FEATURE]-Add-END by TSCD.peng.du
            mTvWeatherLowVal.setText(weather.getCurrentTempL());
            //Fixed PR1002054 Centigrade symbol should be together with the temperature on the next line by tingma at 2015-05-18
            //mTvWeatherRealFeelVal.setText(CommonUtils.f2c(weather.getRealfeel()) + "°");
//            mTvWeatherRealFeelVal.setText((int) Double.parseDouble(weather.getRealfeel()) + "");
//            mTvWeatherRealFeelUnit.setText(getResources().getString(R.string.label_temp_unit_celsius));
            //[BUGFIX]-Add-BEGIN by TSCD.peng.du,11/09/2015,860910,The Unit should be updated
            mTvWeatherHighUnit.setText(getResources().getString(R.string.label_temp_unit_celsius));
            mTvWeatherLowUnit.setText(getResources().getString(R.string.label_temp_unit_celsius));
            //[BUGFIX]-Add-END by TSCD.peng.du
        } else {
            if (showFeelsLike) {
                mTvWeatherTemp.setText(CommonUtils.c2f(weather.getRealfeel()) + "°");
            } else {
                mTvWeatherTemp.setText(CommonUtils.c2f(weather.getTemp()) + "°");
            }
            //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/26/2015,983828,3.1weather screen
            if (isTwcWeather) {
                mTvWeatherHighVal.setText(CommonUtils.c2f(weather.getCurrentTempH()));
            }else{
                mTvWeatherHighVal.setText(CommonUtils.c2f(weather.getRealfeel()));
            }
            //[FEATURE]-Add-END by TSCD.peng.du
            mTvWeatherLowVal.setText(CommonUtils.c2f(weather.getCurrentTempL()));

            //Fixed PR1002054 Centigrade symbol should be together with the temperature on the next line by tingma at 2015-05-18
            //mTvWeatherRealFeelVal.setText((int)Double.parseDouble(weather.getRealfeel()) + "°");
//            mTvWeatherRealFeelVal.setText(CommonUtils.c2f(weather.getRealfeel()) + "");

            //[BUGFIX]-Add-BEGIN by TSCD.peng.du,11/09/2015,860910,The Unit should be updated
            mTvWeatherHighUnit.setText(getResources().getString(R.string.label_temp_unit_fahrenheit));
            mTvWeatherLowUnit.setText(getResources().getString(R.string.label_temp_unit_fahrenheit));
            //[BUGFIX]-Add-END by TSCD.peng.du
        }
        mTvWeatherText.setText(weather.getText());
        mTvWeatherRealFeelKey.setText(getResources().getString(R.string.real_feel));
        mTvWeatherHumidityVal.setText(weather.getHumidity());
        mTvWeatherPreciptationVal.setText(CommonUtils.deletaDec(weather.getPreciptation()));
        //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/26/2015,983828,3.1weather screen

        if (isUnitKm) {
            mTvWeatherVisibilityVal.setText(CommonUtils.deletaDec(weather.getVisibility()));
            mTvWeatherVisibilityUnit.setText(getResources().getString(R.string.label_visibility_unit_km));
        } else {
            mTvWeatherVisibilityVal.setText(CommonUtils.km2mi(weather.getVisibility()));
            mTvWeatherVisibilityUnit.setText(getResources().getString(R.string.label_visibility_unit_mi));
        }
        //[FEATURE]-Add-END by TSCD.peng.du
        if (showAdvData) {
            expandButton.setVisibility(VISIBLE);
            weatherMoreInfo.setVisibility(VISIBLE);
            mTvWeatherPressureVal.setText(weather.getPressure());
            mTvWeatherUvVal.setText(weather.getUv_index());
            mTvWeatherUvUnit.setText(weather.getUv_desc());
            if (isUnitKm) {
                Boolean unitM = CustomizeUtils.getBoolean(mContext, "wind_speed_unit_m");
                if (!unitM) {
                    mTvWeatherWindVal.setText(weather.getWind());
                    mTvWeatherWindUnit.setText(getResources().getString(R.string.label_wind_unit_km));

                    mTvWeatherVisibilityVal.setText(weather.getVisibility());
                    mTvWeatherVisibilityUnit.setText(getResources().getString(R.string.label_visibility_unit_km));
                } else {
                    mTvWeatherWindVal.setText(CommonUtils.km2m(weather.getWind()));
                    mTvWeatherWindUnit.setText(getResources().getString(R.string.label_wind_unit_m));
                    mTvWeatherVisibilityVal.setText(weather.getVisibility());
                    mTvWeatherVisibilityUnit.setText(getResources().getString(R.string.label_visibility_unit_km));
                }
            } else {
                mTvWeatherWindVal.setText(CommonUtils.km2mi(weather.getWind()));
                //[BUGFIX]-Add-BEGIN by TSCD.peng.du,11/09/2015,860910,The Unit should be updated
                mTvWeatherWindUnit.setText(getResources().getString(R.string.label_wind_unit_mi_rel));
                //[BUGFIX]-Add-END by TSCD.peng.du
                mTvWeatherVisibilityVal.setText(CommonUtils.km2mi(weather.getVisibility()));
                mTvWeatherVisibilityUnit.setText(getResources().getString(R.string.label_visibility_unit_mi));
            }
        } else {
            expandButton.setVisibility(GONE);
            weatherMoreInfo.setVisibility(GONE);
        }

        mCurrentWeatherUrl = weather.getUrl();
    }

    public void setDayData(List<DayForShow> dayList, boolean isUnitC) {
        ll_day.removeAllViews();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER_VERTICAL;

        LayoutParams paramsDivider = new LayoutParams(LayoutParams.MATCH_PARENT, 2);
        //Modified by tingma at 2015-05-21
        //params.setMargins(10, 6, 10, 6);
//        params.setMargins(10, getResources().getDimensionPixelSize(R.dimen.llday_params_topbottom), 10,
//                getResources().getDimensionPixelSize(R.dimen.llday_params_topbottom));
        mForecastWeatherUrlList.clear();

        /**
         * if use TWC data source, traversal the daylist from 0
         * if use ACC data source, traversal the daylist from 1;
         **/
        for (int i = isTwcWeather ? 0 : 1; i < dayList.size(); i++) {
            final DayForShow var = dayList.get(i);
            DayView view = new DayView(getContext(), null);
            //view.setBackground(getResources().getDrawable(R.drawable.menu_default_material));
            view.setDayView(var, isUnitC, isTwcWeather);
            mForecastWeatherUrlList.add(var.getUrl());

            if (i == dayList.size() - 1) {
                view.setDivideViewGone();
            }
            ll_day.addView(view, params);
//            if (i < dayList.size() - 1) {
//                View divier = new View(getContext(), null);
//                ll_day.addView(divier, paramsDivider);
//            }
        }
    }

    public void setHourlyData(List<Hour> hourList, boolean isUnitC, int color) {
        hourlyAdapter = new HourlyAdapter(mContext, hourList, isUnitC, color);
        hourlyRecyclerView.setAdapter(hourlyAdapter);
        hourlyAdapter.notifyDataSetChanged();

        //sometimes the TWC will return wrong highest temp and lowest temp,so we get right temp from hourlist
        checkHighAndLowTemp(hourList, isUnitC);
    }

    public void setDayColor(int backgroundColor) {
        ll_day.setBackgroundColor(backgroundColor);
//        int count = ll_day.getChildCount();
//        for (int i = 0; i < count; i++) {
//            View view = ll_day.getChildAt(i);
//            if (i % 2 == 0) {
//                view.setBackgroundColor(backgroundColor);
//            } else {
//                view.setBackgroundColor(colorBurn(backgroundColor));
//            }
//
//        }
    }


    private String getWeatherText(String weatherID) {
        int iconID = Integer.parseInt(weatherID) - 1;
        String[] weatherDescriptions = this.getResources().getStringArray(
                R.array.weather_icon_desc);
        return weatherDescriptions[iconID];
    }

    private void showWeatherWebView(String uri) {
        if (null == uri) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(uri);
        intent.setData(content_url);

        try {
            mContext.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "web uri is invalid, uri = " + uri);
        }
    }

//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        int count = getChildCount();
//        int height = getMeasuredHeight();
//        int top = 0;
////
////		int h = 0;
////		ViewGroup subView0 = (ViewGroup)getChildAt(0);
////		if (subView0.getVisibility() != View.GONE) {
////			subView0.layout(l, t, r, b);
////			h = subView0.getChildAt(0).getMeasuredHeight();
////			Log.e(tag, "h = " + h);
////			top += height;
////		}
////		View subView1 = getChildAt(1);
////		subView1.layout(l, top, r, top + height - h);
//
//        for (int i = 0; i < count; i++) {
//            height += getChildAt(i).getMeasuredHeight();
//        }
//        mTotalHeight = height;
//        mPageHeight = getMeasuredHeight();
//        mTolerance = mPageHeight / 2;
//    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int count = getChildCount();
        int height = getMeasuredHeight();
        int hourlyHeight = 0;
        int dailyHeight = 0;
        int top = 0;
        for (int i = 0; i < count; i++) {
            height += getChildAt(i).getMeasuredHeight();
        }
        mTotalHeight = height;
        //FrameLayout topFrame = (FrameLayout) getChildAt(0);
//        if (topFrame.getVisibility() != View.GONE) {
//            mTopHeight = topFrame.getChildAt(0).getMeasuredHeight();
//        }
        mTopHeight = getChildAt(0).getMeasuredHeight();
        //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/27/2015,983895,deleta weather_24forecast_card
        if(isTwcWeather) {
            hourlyHeight = getChildAt(1).getMeasuredHeight();
            dailyHeight = getChildAt(2).getMeasuredHeight();
            mBetweenSpaceHeight = getChildAt(3).getMeasuredHeight();
        }else{
//            hourlyHeight = getChildAt(1).getMeasuredHeight();
            dailyHeight = getChildAt(1).getMeasuredHeight();
            mBetweenSpaceHeight = getChildAt(2).getMeasuredHeight();
        }
        //[FEATURE]-Add-END by TSCD.peng.du
        mPageHeight = getMeasuredHeight() + mBetweenSpaceHeight;
        mBottomHeight = hourlyHeight + dailyHeight + mBetweenSpaceHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = getChildCount();
        for (int i = 0; i < count; ++i) {
            View childView = getChildAt(i);
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        }
    }

    private int mLastY;

    private float mLastDownX, mLastDownY;
    private float mInitialDownX, mInitialDownY;
    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,12/29/2015,1072625,[Android6.0][Weather_v5.2.8.1.0301.0]Click the weather card,it's hard to open browser
    private static final int minDistance = 30;
    //[BUGFIX]-Add-END by TSCD.qian-li
    private boolean isMove = false;
    private boolean isTouchFirst = true;
    private float mFromAlpha = 0.0f;
    private float mToAlpha = 0.0f;
    private float mLastToAlpha = 0.0f;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastDownX = mInitialDownX = ev.getX();
                mLastDownY = mInitialDownY = ev.getY();
                isMove = false;
                isTouchFirst = true;
                mLastY = (int) ev.getY();
                mStartYPosition = getScrollY();
                if (mPageScrollListener != null) {
                    mPageScrollListener.onActionDown();
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                mLastDownX = ev.getX();
                mLastDownY = ev.getY();
                int dy = (int) (mLastDownY - mInitialDownY);
                int dx = (int) (mLastDownX - mInitialDownX);
                if (Math.abs(dx) >= minDistance) {
                    return false;
                } else if (Math.abs(dy) >= minDistance) {
                    return true;
                } else {
                    return false;
                }
            case MotionEvent.ACTION_CANCEL:
                return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        int dx = 0;
        int dy = 0;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                mLastDownX = mInitialDownX = event.getX();
                mLastDownY = mInitialDownY = event.getY();
                isMove = false;
                isTouchFirst = true;
                mLastY = (int) event.getY();
                mStartYPosition = getScrollY();
                if (mPageScrollListener != null) {
                    mPageScrollListener.onActionDown();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int y = (int) event.getY();
                int distance = mLastY - y;
                int scrollY = getScrollY();

                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,12/29/2015,1072625,[Android6.0][Weather_v5.2.8.1.0301.0]Click the weather card,it's hard to open browser
                mLastDownX = (int) event.getX();
                mLastDownY = (int) event.getY();
                dx = (int) (mLastDownX - mInitialDownX);
                dy = (int) (mLastDownY - mInitialDownY);
                if (Math.abs(dx) < minDistance && Math.abs(dy) < minDistance) {
                    return true;
                }

                isTouchFirst = false;
                //[BUGFIX]-Add-END by TSCD.qian-li

//                boolean isChange = (Math.abs(distance) >= 1);
//                if (/*isChange && */isTouchFirst) {
//                    mHandler.sendEmptyMessage(SHOW_LINE);
//                    isTouchFirst = false;
//                }
                // 边界检查
                int computeScrollY = 0;
                if (distance < 0 && scrollY + distance < 0) {
                    distance = 0 - scrollY;
                    computeScrollY = 0;
                    // Fixed PR965866 by jielong.xing at 2015-5-28 begin
                    if (mCurrentPage != 0) {
                        if (mPageScrollListener != null) {
                            isSetToIdleWhenActionUp = true;
                            mPageScrollListener.onViewScrollStateChange(SCROLL_STATE_DRAGGING);
                        }
                    }
                    // Fixed PR965866 by jielong.xing at 2015-5-28 end
                } else if (distance > 0 && scrollY + distance > mTotalHeight) {
                    distance = mTotalHeight - scrollY;
                    computeScrollY = mTotalHeight;
                    // Fixed PR965866 by jielong.xing at 2015-5-28 begin
                    if (mPageScrollListener != null) {
                        isSetToIdleWhenActionUp = true;
                        mPageScrollListener.onViewScrollStateChange(SCROLL_STATE_DRAGGING);
                    }
                    // Fixed PR965866 by jielong.xing at 2015-5-28 end
                } else {
                    computeScrollY = scrollY;
                }
                mFromAlpha = (float) (Math.abs(computeScrollY)) / (float) mPageHeight;
                scrollBy(0, distance);

                mLastY = y;
                break;
            // Fixed PR964335 by jielong.xing at 2015-4-1 begin
            case MotionEvent.ACTION_CANCEL:
                //Log.e(TAG,"action cancel");
//                if (mCurrentPage == 0) {
//                    mEndYPosition = getScrollY();
//                    int posDiff = mEndYPosition - mStartYPosition;
//                    mScroller.startScroll(0, 0, 0, -posDiff);
//                    postInvalidate();
//                }
                // Fixed PR965866 by jielong.xing at 2015-5-28 begin
                if (mPageScrollListener != null) {
                    mPageScrollListener.onViewScrollStateChange(SCROLL_STATE_IDLE);
                }
                // Fixed PR965866 by jielong.xing at 2015-5-28 end
                break;
            // Fixed PR964335 by jielong.xing at 2015-4-1 end
            case MotionEvent.ACTION_UP:
                mLastDownX = event.getX();
                mLastDownY = event.getY();
                dy = (int) (mLastDownY - mInitialDownY);
                dx = (int) (mLastDownX - mInitialDownX);
                if (Math.abs(dy) >= minDistance || Math.abs(dx) >= minDistance) {
                    isMove = true;
                }

                if (!isMove && isTouchFirst) {
                    int downX = (int) event.getX();
                    int downY = (int) event.getY();
                    Rect rect = new Rect();
                    weather_info_layout.getGlobalVisibleRect(rect);
                    if (rect.contains(downX, downY)) {
//					showWeatherWebView(mCurrentWeatherUrl);
                        Message msg = mHandler.obtainMessage();
                        msg.what = SHOW_URL;
                        msg.obj = mCurrentWeatherUrl;
                        mHandler.sendMessageDelayed(msg, 200);
//					return false;
                    } else {
                        ll_day.getGlobalVisibleRect(rect);
                        if (rect.contains(downX, downY)) {
                            int cnt = ll_day.getChildCount();
                            for (int i = 0; i < cnt; i++) {
                                View childView = ll_day.getChildAt(i);
                                childView.getGlobalVisibleRect(rect);
                                if (rect.contains(downX, downY)) {
//								showWeatherWebView(mForecastWeatherUrlList.get(i));
                                    Message msg = mHandler.obtainMessage();
                                    msg.what = SHOW_URL;
                                    //[BUGFIX]-Add-BEGIN by TSCD.peng.du,11/19/2015,863553,
                                    //[Android5.1][Weather_v5.2.2.1.0306.0]CRASH:com.tct.weather
                                    msg.obj = mForecastWeatherUrlList.get(i);
                                    //[BUGFIX]-Add-END by TSCD.peng.du
                                    mHandler.sendMessageDelayed(msg, 200);
                                }
                            }
                        }
                    }
                }

                mEndYPosition = getScrollY();
                int posDiff = mEndYPosition - mStartYPosition;

                mVelocityTracker.computeCurrentVelocity(1000, 8000);
                int velocityY = (int) mVelocityTracker.getYVelocity();
                if (Math.abs(velocityY) >= 600) {
                    mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, 0, getHeight(), 0, mTotalHeight);
                    invalidate();
                }
                //postInvalidate();
                mVelocityTracker.recycle();
                mVelocityTracker = null;

//                if (Math.abs(velocityY) >= 600 || Math.abs(posDiff) > mTolerance) {
//                    int dis = 0;
//                    // Fixed PR964859 by jielong.xing at 2015-4-1 begin
//                    if (posDiff > 0) {
//                        dis = mPageHeight - posDiff;
//                        mLastToAlpha = mToAlpha = 1.0f;
//                        // Fixed PR965866 by jielong.xing at 2015-5-28 begin
//                        if (mPageScrollListener != null) {
//                            isSetToIdleWhenActionUp = false;
//                            mPageScrollListener.onViewScrollStateChange(SCROLL_STATE_DRAGGING);
//                        }
//                        // Fixed PR965866 by jielong.xing at 2015-5-28 end
//                    } else if (posDiff < 0) {
//                        dis = -(mPageHeight + posDiff);
//                        mLastToAlpha = mToAlpha = 0.0f;
//                        // Fixed PR965866 by jielong.xing at 2015-5-28 begin
//                        if (mPageScrollListener != null) {
//                            isSetToIdleWhenActionUp = false;
//                            mPageScrollListener.onViewScrollStateChange(SCROLL_STATE_DRAGGING);
//                        }
//                        // Fixed PR965866 by jielong.xing at 2015-5-28 end
//                    } else {
//                        mToAlpha = mLastToAlpha;
//                    }
//                    // Fixed PR964859 by jielong.xing at 2015-4-1 end
//                    mScroller.startScroll(0, 0, 0, dis);
//                    //mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, 0, mPageHeight);
//                    invalidate();
//                } else {
//                    mScroller.startScroll(0, 0, 0, -posDiff);
//                    // Fixed PR964859 by jielong.xing at 2015-3-24 begin
//                    mToAlpha = mLastToAlpha;
//                    // Fixed PR964859 by jielong.xing at 2015-3-24 end
//                }

                if (mPageScrollListener != null) {
                    Log.i(TAG, "mFromAlpha=" + mFromAlpha + "  mLastToAlpha" + mLastToAlpha);
                    mPageScrollListener.onStopScroll(mFromAlpha, mLastToAlpha);
                    // Fixed PR965866 by jielong.xing at 2015-5-28 begin
                    if (isSetToIdleWhenActionUp) {
                        mPageScrollListener.onViewScrollStateChange(SCROLL_STATE_IDLE);
                    }
                    // Fixed PR965866 by jielong.xing at 2015-5-28 end
                    mFromAlpha = mLastToAlpha;
                }
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void computeScroll() {

        if (mScroller.computeScrollOffset()) {
            int scroll = mScroller.getCurrY();
//            if (scroll > 0 && mEndYPosition + scroll > mPageHeight) {
//                scroll = mPageHeight - mEndYPosition;
//            } else if (scroll < 0 && mEndYPosition + scroll < 0) {
//                scroll = -mEndYPosition;
//            }
            scrollTo(0, scroll);
            mIsScrolling = true;
            postInvalidate();
        } else if (mIsScrolling) {
            if (mPageScrollListener != null) {
                int position = getScrollY() / (mPageHeight - 20);

                Message msg = mHandler.obtainMessage();
                msg.what = UPDATE_VIEW;
                msg.arg1 = position;
                mHandler.sendMessage(msg);
            }

            mIsScrolling = false;
        }

//        if (mScroller.computeScrollOffset())
//        {
//            scrollTo(0, mScroller.getCurrY());
//            invalidate();
//        }
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y <= 0) {
            y = 0;
            mIsTouchBottom = true;
        } else if (y > mBottomHeight) {
            y = mBottomHeight;
            mIsTouchBottom = false;
        } else {
            mIsTouchBottom = false;
        }
        //Log.e(TAG, "tempIsTouchBottom=" + tempIsTouchBottom+" mIsTouchBottom"+mIsTouchBottom);
        mScrollPosition = y;
        float scrolledPercent = ((float) y / mPageHeight) * 2;
        if (scrolledPercent > 1) {
            scrolledPercent = 1;
        }
        if (mPageScrollListener != null) {
            if (tempIsTouchBottom != mIsTouchBottom) {
                tempIsTouchBottom = mIsTouchBottom;
                mPageScrollListener.onScrollToBottom(mIsTouchBottom);
            }
            mPageScrollListener.onScrolling(mScrollPosition, scrolledPercent);
        }
        if (y != getScrollY()) {
            super.scrollTo(x, y);
        }
    }

    public void updateView(int scrollPosition) {
//        if (isScrollToBottom) {
//            // Fixed PR964859 by jielong.xing at 2015-4-1 begin
//            mLastToAlpha = 1.0f;
//            // Fixed PR964859 by jielong.xing at 2015-4-1 end
//            mCurrentPage = 1;
//            scrollTo(0, mPageHeight);
//            mLine.setVisibility(View.VISIBLE);
//        } else {
//            // Fixed PR964859 by jielong.xing at 2015-4-1 begin
//            mLastToAlpha = 0.0f;
//            // Fixed PR964859 by jielong.xing at 2015-4-1 end
//            mCurrentPage = 0;
//            scrollTo(0, 0);
//            mLine.setVisibility(View.INVISIBLE);
//        }
        mScrollPosition = scrollPosition;
        scrollTo(0, mScrollPosition);
        postInvalidate();
    }

    public void setOnPageScrollListener(OnPageScrollListener listener) {
        mPageScrollListener = listener;
    }

    public interface OnPageScrollListener {
        public void onPageChanged(int position);

        public void onScrollToBottom(boolean isScrollToBottom);

        public void onActionDown();

        public void onScrolling(int scrollPostion, float scrollPercent);

        public void onStopScroll(float fromAlpha, float toAlpha);

        // Fixed PR965866 by jielong.xing at 2015-5-28 begin
        public void onViewScrollStateChange(int state);
        // Fixed PR965866 by jielong.xing at 2015-5-28 end
    }

    private static final int SHOW_LINE = 0x01;
    private static final int SHOW_URL = 0x02;
    private static final int UPDATE_VIEW = 0x03;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_URL:
                    String url = (String) msg.obj;
                    showWeatherWebView(url);
                    break;
                case UPDATE_VIEW:
                    int position = msg.arg1;
                    if (getScrollY() > 0 && getScrollY() < mPageHeight) {
//                        if (position == 0) {
//                            mLastToAlpha = 0.0f;
//                            scrollTo(0, 0);
//                        } else {
//                            mLastToAlpha = 1.0f;
//                            scrollTo(0, mPageHeight);
//                        }
                        if (mPageScrollListener != null) {
                            mPageScrollListener.onStopScroll(mFromAlpha, mLastToAlpha);
                            // Fixed PR965866 by jielong.xing at 2015-5-28 begin
                            mPageScrollListener.onViewScrollStateChange(SCROLL_STATE_IDLE);
                            // Fixed PR965866 by jielong.xing at 2015-5-28 end
                        }
                        postInvalidate();
                    }

                    if (position != mCurrentPage) {
                        mCurrentPage = position;
                        mPageScrollListener.onPageChanged(mCurrentPage);
                    }
            }

        }

    };

    // Fixed PR965866 by jielong.xing at 2015-5-28 begin
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    private boolean isSetToIdleWhenActionUp = false;
    // Fixed PR965866 by jielong.xing at 2015-5-28 end

    public void showWeatherInfo() {
        ValueAnimator valueAnimator;
        if (mIsExpanded) {
            valueAnimator = ValueAnimator.ofFloat(1, 0);
            mIsExpanded = false;
        } else {
            mIsExpanded = true;
            valueAnimator = ValueAnimator.ofFloat(0, 1);
        }

        final int position = mScrollPosition;
        valueAnimator.setDuration(200);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                LayoutParams layoutParams = (LayoutParams) weatherMoreInfo.getLayoutParams();
                layoutParams.height = (int) (mMoreInfoHeight * value);
                weatherMoreInfo.setLayoutParams(layoutParams);

//                if (mIsTouchTop) {
//                    if (mIsExpanded) {
//                        updateView((int) (position + -mMoreInfoHeight * value));
//                    } else {
//                        updateView((int) (position + mMoreInfoHeight * (1 - value)));
//                    }
//                }
            }
        });
    }

    public void setWeatherColor(int color) {
        mTvWeatherText.setTextColor(color);
        mTvWeatherTemp.setTextColor(color);
//        mTvWeatherCity.setTextColor(getResources().getColor(R.color.text_color_grey));
        //mTvWeatherTime.setTextColor(color);
        expandButton.setColor(color);
        mTvWeatherRealFeelKey.setTextColor(color);
        mTvWeatherRealFeelTail.setTextColor(color);
        mTvWeatherRealFeelVal.setTextColor(color);
        mTvWeatherRealFeelUnit.setTextColor(color);
//        mTvWeatherHumidityLabel.setTextColor(getResources().getColor(R.color.text_color_grey));
        mTvWeatherHumidityVal.setTextColor(color);
//        mTvWeatherHumidityUnit.setTextColor(getResources().getColor(R.color.text_color_grey));
        mTvWeatherWindLabel.setTextColor(color);
        mTvWeatherWindVal.setTextColor(color);
        mTvWeatherWindUnit.setTextColor(color);
//        mTvWeatherVisibilityLabel.setTextColor(getResources().getColor(R.color.text_color_grey));
        mTvWeatherVisibilityVal.setTextColor(color);
//        mTvWeatherVisibilityUnit.setTextColor(getResources().getColor(R.color.text_color_grey));
//        mTvWeatherHighLabel.setTextColor(getResources().getColor(R.color.text_color_grey));
        mTvWeatherHighVal.setTextColor(color);
//        mTvWeatherHighUnit.setTextColor(getResources().getColor(R.color.text_color_grey));
        mTvWeatherLowLabel.setTextColor(color);
        mTvWeatherLowVal.setTextColor(color);
        mTvWeatherLowUnit.setTextColor(color);
//        mTvWeatherPreciptationLabel.setTextColor(getResources().getColor(R.color.text_color_grey));
        mTvWeatherPreciptationVal.setTextColor(color);
//        mTvWeatherPreciptationUnit.setTextColor(getResources().getColor(R.color.text_color_grey));
        mTvWeatherPressureLabel.setTextColor(color);
        mTvWeatherPressureVal.setTextColor(color);
        mTvWeatherPressureUnit.setTextColor(color);
        mTvWeatherUvLabel.setTextColor(color);
        mTvWeatherUvVal.setTextColor(color);
        mTvWeatherUvUnit.setTextColor(color);
        if (color != getResources().getColor(R.color.text_color_black)) {
            mIvWeatherHighIcon.setBackgroundResource(R.drawable.currnet_high_p);
            mIvWeatherLowIcon.setBackgroundResource(R.drawable.currnet_low_p);
            mIvWeatherreciptationIcon.setBackgroundResource(R.drawable.currnet_preciptation_p);
            mIvWeatherHumidityIcon.setBackgroundResource(R.drawable.currnet_humidity_p);
            mIvWeatherVisibilityIcon.setBackgroundResource(R.drawable.currnet_visibility_p);
            mIvWeatherWindIcon.setBackgroundResource(R.drawable.currnet_wind_p);
            mIvWeatherPressureIcon.setBackgroundResource(R.drawable.currnet_pressure_p);
            mIvWeatherUvIcon.setBackgroundResource(R.drawable.currnet_uv_p);
        }
    }

    private int colorBurn(int RGBValues) {
        int alpha = RGBValues >> 24;
        int red = RGBValues >> 16 & 0xFF;
        int green = RGBValues >> 8 & 0xFF;
        int blue = RGBValues & 0xFF;
        red = (int) Math.floor(red * (1 - 0.07));
        green = (int) Math.floor(green * (1 - 0.07));
        blue = (int) Math.floor(blue * (1 - 0.07));
        return Color.rgb(red, green, blue);
    }

    private void checkHighAndLowTemp(List<Hour> hourList, boolean isUnitC) {
        String current = (String) mTvWeatherTemp.getText();
        String high = (String) mTvWeatherHighVal.getText();
        String low = (String) mTvWeatherLowVal.getText();
        float tempCurrnet = Float.parseFloat((String) current.subSequence(0, current.length() - 1));
        float tempHigh = Float.parseFloat((String) high.subSequence(0, high.length()));
        float tempLow = Float.parseFloat((String) low.subSequence(0, low.length()));
        if (tempHigh < tempCurrnet || tempLow > tempCurrnet) {
            ArrayList<Float> tempList = new ArrayList<Float>();
            for (int i = 0; i < hourList.size(); i++) {
                tempList.add(Float.parseFloat(hourList.get(i).getTemperature()));
            }
            Collections.sort(tempList);
            Log.e(TAG, "tempCurrnet=" + tempCurrnet + " tempHigh=" + tempHigh + " tempLow=" + tempLow);
            Log.e(TAG, "templist=" + tempList);
            tempLow = tempList.get(0);
            tempHigh = tempList.get(tempList.size() - 1);
            if (isUnitC) {
                mTvWeatherHighVal.setText(tempHigh + "");
                mTvWeatherLowVal.setText(tempLow + "");
            } else {
                tempHigh = Math.round(tempHigh * 9 / 5 + 32);
                tempLow = Math.round(tempLow * 9 / 5 + 32);
                mTvWeatherHighVal.setText(tempHigh + "");
                mTvWeatherLowVal.setText(tempLow + "");
            }
        }
    }
}

