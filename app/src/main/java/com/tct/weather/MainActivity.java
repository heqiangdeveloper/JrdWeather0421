package com.tct.weather;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tct.weather.bean.City;
import com.tct.weather.bean.DayForShow;
import com.tct.weather.bean.Hour;
import com.tct.weather.bean.WeatherForShow;
import com.tct.weather.bean.WeatherInfo;
import com.tct.weather.service.UpdateService;
import com.tct.weather.util.Blur;
import com.tct.weather.util.CommonUtils;
import com.tct.weather.util.CustomizeUtils;
import com.tct.weather.util.IconBackgroundUtil;
import com.tct.weather.util.SharePreferenceUtils;
import com.tct.weather.view.VerticalSlidingView;

/*BUGFIX-1069893 2015/12/17       lin-zhou    [Android6.0][Weather_v5.2.8.1.0301.0]There is no prompt in weather main screen for permission */
/*BUGFIX-1192935 2015/12/21       lin-zhou    [Android6.0][Weather_v5.2.8.1.0305.0]Click the accuweather enter TWC page */
/*BUGFIX-9992933 2015/12/24       xing.zhao   [Android6.0][Weather_v5.2.8.1.0305.0]The locate button has no use after delete all cities */
/*BUGFIX-1173717 2015/12/25       xing.zhao   [Weather] Some content in setting screen does not match ergo*/
/*BUGFIX-1305396 2016/01/11       xing.zhao   [GAPP][Android 6.0][Weather]It will flash one second when entering the weather by MIE*/
/*BUGFIX-1391550 2016/01/12       xing.zhao   [GAPP][Android6.0][Weather]The weather card is in different level after remove city.*/
/*BUGFIX-1284454 2016/01/13       xing.zhao   [Weather][Force Close]It pop up FC ,When refresh weather comming phone*/
/*BUGFIX-1284454 2016/01/19       xing.zhao   [Weather][Force Close]It pop up FC ,When refresh weather comming phone*/
/*BUGFIX-1470934 2016/1/20       xing.zhao     [Weather]MiddleMan Runtime permission Phone group*/
/*BUGFIX-1468263 2016/1/21       xing.zhao     [GAPP][Android 6.0][Weather][Monitor]The Weather has stopped when enter weather in MIE screen*/

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener
        , SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private static final String TAG = "MainActivity";

    private final String AUTO_UPDATE_KEY = "settings_auto_update";
    private final String TEMP_KEY = "settings_temp";
    private final String DISTANCE_KEY = "settings_distance";
    private final String ADVANCE_DATA_KEY = "settings_advance_data";
    private final String SHOW_FEELS_LIKE_KEY = "settings_feel_like";

    private final int cityLimit = 6;

    private View mLocationLayout;

    private ImageView mIvMenu = null;
    private ViewGroup mViewPoints = null;
    private ViewPager mViewPager = null;
    private ImageView mIvAccuWeather = null;
    private ImageView mIvRefresh = null;
    private TextView mTvRefreshTime = null;

    private UpdateService updateService = null;
    private ArrayList<City> mCityList = null;
    private int mPosition = 0;

    private boolean isUnitC = false;
    private String mTempKey = null;

    private boolean isWifiConnected = false;
    private boolean isMobileConnected = false;
    private boolean isOtherConnected = false;
    private boolean isMiddleManAvavible = false;

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,03/04/2016,1719109,[Performance][Flip cover][Weather]The sound choose screen is very slow when with weather app open
    private boolean isNeedPre = false;
    private boolean isNeedResetMedia = false;
    //[BUGFIX]-Add-END by TSCD.qian-li

    private SwipeRefreshLayout mSwipeRefreshLayout = null;

    private ArrayList<FrameLayout> mViews = null;
    private ArrayList<ImageView> mImageViews = null;
    private ArrayList<WeatherInfo> mWeatherInfoList = null;

    private MyBroadcasReceiver mBroadcastReceiver = null;
    private BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_TICK)) {
                if (null != mTvRefreshTime) {
                    setCurrentRefreshTime();
                }
            }
        }
    };

    private ProgressDialog pDialog;
    private boolean isUpdating = false;

    // add by jielong.xing at 2015-3-17 for new feature begin
    private boolean isUnitKm = true;
    // add by jielong.xing at 2015-3-17 for new feature end
    private boolean isBgShow1 = true;
    private boolean showAdvData = true;
    private boolean showFeelsLike = false;
    private float mLastScrollPercent = 0.0f;
    private int mScrollPosition = 0;
    private float mScrollPercent = 0;
    private boolean mIsScrollToBottom = true;

    private ImageView mIvSnapShot;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private MediaPlayer mMediaplayer;
    private Uri mLastBgUri;
    private SwitchVideoTask switchVideoTask;
    private MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

    private boolean isBackgoundDynamic = false;
    private boolean mediaOnCreated = true;

    private boolean isTwcweather = false;//[FEATURE]-Add-BEGIN by TSCD.peng.du,11/27/2015,983895
    private long beginTime, endTime = 0;

    private boolean isShowActivity = false;//[FEATURE]-Add-BEGIN by TSCD.lin.zhou,12/22/2015,1190946

    private ArrayMap<Integer, WeatherForShow> mSlidingViewWeatherMap = new ArrayMap<Integer, WeatherForShow>();
    private ArrayMap<Integer, List<DayForShow>> mSlidingViewDayMap = new ArrayMap<Integer, List<DayForShow>>();
    private ArrayMap<Integer, List<Hour>> mSlidingViewHourMap = new ArrayMap<Integer, List<Hour>>();
    private ArrayMap<Integer, int[]> mSlidingViewColorMap = new ArrayMap<Integer, int[]>();
    private ArrayMap<Integer, Integer> mViewPageBgMap = new ArrayMap<Integer, Integer>();
    private ArrayMap<Integer, Uri> mViewBgUrlMap = new ArrayMap<Integer, Uri>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        beginTime = System.currentTimeMillis();
        // 1069893 -lin.zhou, modify -001 , begin
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 1069893 -lin.zhou, modify -001 , begin
        super.onCreate(savedInstanceState);
        if (!CommonUtils.isSupportHorizontal(this)) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/27/2015,983895,delete weather_24forecast_card
        isTwcweather = CustomizeUtils.getBoolean(this, "use_twc_weather"); 
        isMiddleManAvavible = CustomizeUtils.isMiddleManAvavible(this);
        Log.i(TAG, "isMiddleManAvavible:" + isMiddleManAvavible);
        //[FEATURE]-Add-END by TSCD.peng.du
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            mTempKey = intent.getStringExtra("newCityKey");
        }
        /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
        if (isTwcweather) {
            setContentView(R.layout.main_activity);
        } else {
        /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
            setContentView(R.layout.main_acc_activity);
        }

        mLocationLayout = findViewById(R.id.main_activity_layout);

        mIvMenu = (ImageView) findViewById(R.id.iv_menu);
        mIvMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenuLayout();
            }
        });

        mTvRefreshTime = (TextView) findViewById(R.id.refresh_date_text);

        mIvAccuWeather = (ImageView) findViewById(R.id.iv_accu_logo);
        //Fixed PR1002223 by tingma at 2015-05-19
        mIvAccuWeather.setAlpha(0.4f);

        mIvRefresh = (ImageView) findViewById(R.id.refresh_date_icon);
        mIvRefresh.setAlpha(0.4f);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_widget);
        mSwipeRefreshLayout.setOnRefreshListener(this);


        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setMessage(getResources().getString(R.string.loading));

        Intent bindServiceIntent = new Intent(MainActivity.this, UpdateService.class);
        bindService(bindServiceIntent, conn, Context.BIND_AUTO_CREATE);

        getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.DATE_FORMAT), true,
                mDateContentObserver);
        getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.TIME_12_24), true,
                mTimeContentObserver);
        registerBoradcastReceiver();


        isBackgoundDynamic = isBackGroundDynamic(); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334

        if (isBackgoundDynamic) {
            mediaOnCreated = true;
            mIvSnapShot = (ImageView) findViewById(R.id.iv_snapshot);
            mIvSnapShot.setVisibility(View.VISIBLE);

            int width = getWindowManager().getDefaultDisplay().getWidth();
            int height = getWindowManager().getDefaultDisplay().getHeight();
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_white);
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            mIvSnapShot.setImageBitmap(bitmap);

//            mIvSnapShot.setBackgroundResource(R.drawable.bg_white);


            mMediaplayer = new MediaPlayer();
            //[BUGFIX]-Add-BEGIN by TSCD.qian-li,03/04/2016,1719109,[Performance][Flip cover][Weather]The sound choose screen is very slow when with weather app open
            Log.i(TAG, "onCreate mediaplayer.reset");
            mMediaplayer.reset();
            //[BUGFIX]-Add-END by TSCD.qian-li
            mMediaplayer.setOnCompletionListener(this);
            mMediaplayer.setOnErrorListener(this);
//        mMediaplayer.setOnInfoListener(this);
            mMediaplayer.setOnPreparedListener(this);
//        mMediaplayer.setOnSeekCompleteListener(this);
//        mMediaplayer.setOnVideoSizeChangedListener(this);

            surfaceView = (SurfaceView) findViewById(R.id.surface_view);
            holder = surfaceView.getHolder();
            holder.addCallback(this);
            //GPU  accelerate
            holder.setType(SurfaceHolder.SURFACE_TYPE_HARDWARE);

            surfaceView.setVisibility(View.VISIBLE);
        } else {
            mImgBg1 = (ImageView) findViewById(R.id.iv_bg1);
            mImgBg2 = (ImageView) findViewById(R.id.iv_bg2);
            mImgBgBlur1 = (ImageView) findViewById(R.id.iv_bg_blur1);
            mImgBgBlur2 = (ImageView) findViewById(R.id.iv_bg_blur2);
//		mImgFgBlur = (ImageView)findViewById(R.id.iv_fg_blur);
            mImgBg1.setVisibility(View.VISIBLE);
            mImgBgBlur1.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefresh() {
        if (getConnectedStatus()) {
            Log.d("xjl_test", "onRefresh isUpdating = " + isUpdating + ", isRefreshing = " + mSwipeRefreshLayout.isRefreshing());
            if (!isUpdating) {
                Log.d("xjl_test", "onRefresh start");
                isUpdating = true;

                updateService.setUpdateManue();
                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/23/2016,1443957,[Weather]It can't automatically recognize new site when move to new site
                updateService.updateAllWeatherWithAutoLocation();
                //[BUGFIX]-Add-END by TSCD.qian-li
            }
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(MainActivity.this,
                    getResources().getString(R.string.locate_connect_error),
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        endTime = System.currentTimeMillis();
        Log.i(TAG, (endTime - beginTime) + "");
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,03/04/2016,1719109,[Performance][Flip cover][Weather]The sound choose screen is very slow when with weather app open
        if (isBackgoundDynamic) {
            surfaceView.setVisibility(View.VISIBLE);
        }
        //[BUGFIX]-Add-END by TSCD.qian-li
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        isShowActivity = true;

        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,03/04/2016,1719109,[Performance][Flip cover][Weather]The sound choose screen is very slow when with weather app open
        if (isBackgoundDynamic) {
            if (isNeedResetMedia && mViewBgUrlMap != null) {
                Uri currentBgUri = mViewBgUrlMap.get(mViewPager.getCurrentItem());
                Log.d(TAG, "on resume mLasturi=" + mLastBgUri.toString() + ", currentBgUri=" + currentBgUri.toString());
                mLastBgUri = currentBgUri;
                isNeedResetMedia = false;
                try {
                    mMediaplayer.reset();
                    mMediaplayer.setDataSource(MainActivity.this, mLastBgUri);
                    isNeedPre = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (isNeedPre) {
                isNeedPre = false;
                try {
                    Log.i(TAG, "onResume mMediaplayer.prepareAsync");
                    mMediaplayer.prepareAsync();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                    try {
                        Log.i(TAG, "onResume exception mediaplayer.reset");
                        mMediaplayer.reset();
                        mMediaplayer.setDataSource(MainActivity.this, mLastBgUri);
                        mMediaplayer.prepareAsync();
                    } catch (Exception e0) {
                        e0.printStackTrace();
                        Log.e(TAG, e0.toString());
                    }
                }
            }
        }
        //[BUGFIX]-Add-END by TSCD.qian-li

//        SharedPreferences shareUnit = getSharedPreferences("weather", Context.MODE_WORLD_READABLE);
//        String unit = CustomizeUtils.getString(MainActivity.this, "def_weather_unit_name");
//        unit = CustomizeUtils.splitQuotationMarks(unit);
//        if ("isUnitF".equals(unit)) {
//            isUnitC = shareUnit.getBoolean("unit", false);
//        } else {
//            isUnitC = shareUnit.getBoolean("unit", true);
//        }
//
//        // add by jielong.xing at 2015-3-17 for new feature begin
//        String unitKm = CustomizeUtils.getString(MainActivity.this, "def_weather_wind_visibility_unit_name");
//        if ("km".equals(unitKm.toLowerCase())) {
//            isUnitKm = shareUnit.getBoolean("unitKm", true);
//        } else {
//            isUnitKm = shareUnit.getBoolean("unitKm", false);
//        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String autoUpdate = settings.getString(AUTO_UPDATE_KEY, "0");
        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,11/16/2015,909061,[Weather_v5.2.2.1.0307.0]
        //Remove "Advanced weather data" and  "show sensible temperature" settings as Ergo_5.2.4
        showAdvData = false;//settings.getBoolean(ADVANCE_DATA_KEY, true);
        showFeelsLike = false;//settings.getBoolean(SHOW_FEELS_LIKE_KEY, true);
        //[BUGFIX]-Add-END by TSCD.qian-li
        //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,12/25/2015,1173717,[Weather] Some content in setting screen does not match ergo
        String tempUnit = settings.getString(TEMP_KEY, "1");
        String distanceUnit = settings.getString(DISTANCE_KEY, "1");
        if (TextUtils.equals(tempUnit, "1")) {
            isUnitC = true;
        } else {
            isUnitC = false;
        }
        if (TextUtils.equals(distanceUnit, "1")) {
            isUnitKm = true;
        } else {
            isUnitKm = false;
        }
        //[BUGFIX]-Add-END by TSCD.xing.zhao


        // add by jielong.xing at 2015-3-17 for new feature end
        //initViewPager();//add by jiajun.shen for 1031027
        setCurrentRefreshTime();//add by jiajun.shen for 1043058

//        if (isBackgoundDynamic) {
//            if (mLastBgUri != null) {
//                Log.d(TAG, "on resume mLasturi=" + mLastBgUri.toString());

        /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
        //[BUGFIX]-Add-BEGIN by TSCD.jian.xu,12/11/2015,1058845,[Weather] When tap weather apk, It pop up fc
        // Donot many things in mainThread
        /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
//                asyncResetplayer();
//                try {
//                    mMediaplayer.reset();
//                    mMediaplayer.setDataSource(MainActivity.this, mLastBgUri);
//                    mMediaplayer.prepareAsync();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Log.e(TAG, e.toString());
//                }
        //[BUGFIX]-Add-END by TSCD.jian.xu // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
//            }
//        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(timeReceiver, intentFilter);
        //[BUGFIX]-Add-BEGIN by xinlei.sheng,2016/1/5,1293407
        updateWeatherView();
        //[BUGFIX]-Add-END by xinlei.sheng,2016/1/5,1293407
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause "); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
        super.onPause();
        isShowActivity = false;
        if (mViewPager != null && updateService != null) {
            int item = mViewPager.getCurrentItem();
            if (item >= 0) {
                mTempKey = updateService.getTempkeyByItem(item);
                SharePreferenceUtils.getInstance().saveCurrentCityKey(getApplicationContext(), mTempKey);
            }
        }
        // update by jielong.xing for PR930314 at 2014-2-12 begin
        if (mCityList != null && mCityList.size() != 0) {
            // update by jielong.xing for PR930314 at 2014-2-12 end
            String locationKey = mCityList.get(mPosition).getLocationKey();
            Editor sharedata = getSharedPreferences("weather", Context.MODE_WORLD_READABLE).edit();
            sharedata.putString("currentcity", locationKey);
            sharedata.putString("currentLocationKey", locationKey);
            sharedata.commit();

            //delete for PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 begin
            // add by jielong.xing for PR921851 at 2015-2-3 begin
            /*Intent intent = new Intent();
            intent.setAction("com.tct.weather.synclocation");
			sendBroadcast(intent);*/
            // add by jielong.xing for PR921851 at 2015-2-3 end
            //delete for PR933743 Mini App function do not like as Ergo by ting.ma at 2015-03-07 end
            if (isBackgoundDynamic) {
                if (switchVideoTask != null) {
                    switchVideoTask.cancel(true);
                }
                Log.e(TAG, "onPause  stop the mLastBgUri =  " + mLastBgUri); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                Log.i(TAG, "onPause mediaplayer.stop");
                mMediaplayer.stop();
                isNeedPre = true;
                //mediaPosition = mMediaplayer.getCurrentPosition();
            }
        }
        unregisterReceiver(timeReceiver);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,03/04/2016,1719109,[Performance][Flip cover][Weather]The sound choose screen is very slow when with weather app open
        if (isBackgoundDynamic) {
            surfaceView.setVisibility(View.GONE);
            isNeedPre = false;
        }
        //[BUGFIX]-Add-END by TSCD.qian-li
    }

    @Override
    public void onBackPressed() {
        if (popupMenuLayout != null && popupMenuLayout.getVisibility() == View.VISIBLE) {
            popupMenuLayout.setVisibility(View.GONE);
        } else {
            //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,01/19/2016,1284454,[Weather][Force Close]It pop up FC ,When refresh weather comming phone.
            try {
                super.onBackPressed();
            } catch (IllegalStateException e) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                Log.e(TAG, e.getMessage());
                finish();
            }
            //[BUGFIX]-Add-END by TSCD.xing.zhao
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        getContentResolver().unregisterContentObserver(mDateContentObserver);
        getContentResolver().unregisterContentObserver(mTimeContentObserver);
        unregisterReceiver(mBroadcastReceiver);

        if (isBackgoundDynamic) {
            Log.i(TAG, "onDestroy mediaplayer.stop");
            mMediaplayer.stop();
            Log.i(TAG, "onDestroy mediaplayer.release");
            mMediaplayer.release();
            mMediaplayer = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 当SurfaceView中的Surface被创建的时候被调用
        //在这里我们指定MediaPlayer在当前的Surface中进行播放
        Log.d(TAG, "surfaceCreated");
        mMediaplayer.setDisplay(holder);
        //在指定了MediaPlayer播放的容器后，我们就可以使用prepare或者prepareAsync来准备播放了
//        mMediaplayer.prepareAsync();
        if (mLastBgUri != null && !isNeedPre) {
            Log.d(TAG, "on surfaceCreated mLasturi=" + mLastBgUri.toString());
            try {
                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,03/04/2016,1719109,[Performance][Flip cover][Weather]The sound choose screen is very slow when with weather app open
//                mMediaplayer.reset();
//                mMediaplayer.setDataSource(MainActivity.this, mLastBgUri);
                //[BUGFIX]-Add-END by TSCD.qian-li
                Log.i(TAG, "surfaceCreated mediaplayer.prepareAsync");
                mMediaplayer.prepareAsync();
                isNeedPre = false;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
                try {
                    Log.i(TAG, "surfaceCreated exception mediaplayer.reset");
                    mMediaplayer.reset();
                    mMediaplayer.setDataSource(MainActivity.this, mLastBgUri);
                    mMediaplayer.prepareAsync();
                    isNeedPre = false;
                } catch (Exception e0) {
                    e0.printStackTrace();
                    Log.e(TAG, e0.toString());
                }
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
// 当Surface尺寸等参数改变时触发
        Log.v(TAG, "Surface Change::: surfaceChanged called mLasturi=" + mLastBgUri); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "Surface Destory::: surfaceDestroyed called");
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mLastBgUri != null && !mLastBgUri.equals(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.am_frost))) {
            mMediaplayer.setLooping(true);
        }
        Log.i(TAG, "onPrepared mediaplayer.start");
        mMediaplayer.start();
        if (mediaOnCreated) {
            mHandler.sendEmptyMessageDelayed(MEDIAPLAYER_STARTED, 200);
            mediaOnCreated = false;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
// 当MediaPlayer播放完成后触发
        Log.v(TAG, "Play Over::: onComletion called");
        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,03/04/2016,1719109,[Performance][Flip cover][Weather]The sound choose screen is very slow when with weather app open
//        mMediaplayer.start();
        //[BUGFIX]-Add-END by TSCD.qian-li
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.v(TAG, "Play Error::: onError called what = " + what); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
        //[BUGFIX]-Add-BEGIN by TSCD.peng.du,01/04/2016,1243223,[Weather][Force Close]
        // The background display broken picture.after enter again ,the backgound become black and then pop up FC
        boolean isDone = false;
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.v(TAG, "Play Error::: MEDIA_ERROR_SERVER_DIED");
                isDone = true;
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.v(TAG, "Play Error:::MEDIA_ERROR_UNKNOWN");
                isDone = true;
                break;
            default:
                break;
        }
        return isDone;
        //[BUGFIX]-Add-END by TSCD.peng.du
    }

    private class CustomContentObserver extends ContentObserver {
        public CustomContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            initViewPager(true);
        }
    }

    private ContentObserver mDateContentObserver = new CustomContentObserver();
    private ContentObserver mTimeContentObserver = new CustomContentObserver();

    private void registerBoradcastReceiver() {
        mBroadcastReceiver = new MyBroadcasReceiver();
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("android.intent.action.WEATHER_BROADCAST");
        myIntentFilter.addAction("com.jrdcom.jrdweather.switchdisplay");
        myIntentFilter.addAction("com.tct.weather.shownolocationsnackbar"); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private class MyBroadcasReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.WEATHER_BROADCAST")) {
                Log.d("xjl_test", "onReceive isUpdating = " + isUpdating + ", isRefreshing = " + mSwipeRefreshLayout.isRefreshing());
                if (isUpdating) {
                    Log.d("xjl_test", "onReceive start");
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                Bundle b = intent.getExtras();
                if (isUpdating && b.getBoolean("stopUpdating")) {
                    Log.d(TAG, "stopUpdating");
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                boolean isDataGot = b.getBoolean("weather");
                if (isDataGot) {
                    if (checkDataBase()) {
                        if (mViewPager != null && updateService != null) {
                            int item = mViewPager.getCurrentItem();
                            if (item >= 0) {
                                mTempKey = updateService.getTempkeyByItem(item);
                            }
                        }
                        // modify end

                        mHandler.sendEmptyMessageDelayed(UPDATE_FINISH, 1500);
                        //initViewPager();

                        boolean locationError = b.getBoolean("locationerror", false);
                        if (locationError) {
                            if (isShowActivity) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                                Toast.makeText(MainActivity.this,
                                        getResources().getString(R.string.msg_unable_obtain_location),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
                boolean connect_faild = b.getBoolean("connect_faild");
                if (connect_faild) {
                    pDialog.dismiss();
                    if (isShowActivity) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                        Toast.makeText(MainActivity.this,
                                getResources().getString(R.string.locate_connect_error),
                                Toast.LENGTH_LONG).show();
                    }
                }
                boolean connect_timeout = b.getBoolean("connect_timeout");
                if (connect_timeout) {
                    pDialog.dismiss();
                    if (isShowActivity) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                        Toast.makeText(MainActivity.this,
                                getResources().getString(R.string.obtain_data_failed),
                                Toast.LENGTH_LONG).show();
                    }
                }
                isUpdating = false;
            } else if (action.equals("com.jrdcom.jrdweather.switchdisplay")) {
                // remove unknow broadcast caused initViewPager twice
                boolean fromAutoLote = intent.getBooleanExtra("fromautolocate", false);
                if (!fromAutoLote) {
                    return;
                }

                // do not change the index of paper when refresh
                if (mViewPager != null && updateService != null) {
                    int item = mViewPager.getCurrentItem();
                    if (item >= 0) {
                        mTempKey = updateService.getTempkeyByItem(item);
                    }
                }
                initViewPager(true);
            /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
            } else if (action.equals("com.tct.weather.shownolocationsnackbar")) {
                Log.i("receive action","receive action shownolocationsnackbar isShowActivity="+isShowActivity);
                if (isShowActivity) {
                    String tips = getResources().getString(R.string.turnon_locationservice);
                    Snackbar.make(findViewById(R.id.main_activity_layout), tips, 5000).setAction(getResources().getString(R.string.bt_turnon_location), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).show();
                }
                /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
            }
        }
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            updateService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            updateService = ((UpdateService.UpdateBinder) service).getService();
            if (updateService != null) {
                if (checkDataBase()) {
                    initViewPager(true);
                    // 1069893 -lin.zhou, modify -002 , begin
                    checkPermission();
                    // 1069893 -lin.zhou, modify -002 , end
                    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/20/2016,1313172,[Weather]weather not translate to russian
                    Log.i(TAG, "updateService.updateAllWeatherWithLanguageChanged");
                    updateService.updateAllWeatherWithLanguageChanged();
                    //[BUGFIX]-Add-END by TSCD.qian-li
                }
            }
        }
    };

    // 1069893 -lin.zhou, modify -003 , begin
    private void checkPermission() { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
        //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,01/20/2016,1470934,[Weather]MiddleMan Runtime permission Phone group.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_DENIED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED
                || (!isMiddleManAvavible && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_DENIED)) {
            //[BUGFIX]-Add-END by TSCD.xing.zhao
            String tips = null;
            if (isMiddleManAvavible) {
                tips = getResources().getString(R.string.gotoSettingTips_no_phone);
            } else {
                tips = getResources().getString(R.string.gotoSettingTips);
            }
            Snackbar.make(mLocationLayout, tips, 5000).setAction(getResources().getString(R.string.settings), new View.OnClickListener() { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                @Override
                public void onClick(View view) {
                    //layout_offset.setVisibility(View.GONE);
                    ForwardUtil.gotoSettings(MainActivity.this);
                }
            }).show();
            //requestLocationPermission();
        }

    }
    // 1069893 -lin.zhou, modify -003 , begin


    private void initViewPager(boolean isNeedPrepare) {
        if (null == updateService) {
            return;
        }
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mViews = new ArrayList<FrameLayout>();

        mImageViews = new ArrayList<ImageView>();
        mWeatherInfoList = new ArrayList<WeatherInfo>();
        mViewPoints = (ViewGroup) findViewById(R.id.pointGroup);

        mViewPoints.removeAllViews();
        mViewPager.removeAllViews();
        mPosition = 0;

        // add by jielong.xing at 2015-3-14 begin
        //mBlurImgList.clear();//removed by jiajun.shen for 1038936
        // add by jielong.xing at 2015-3-14 end

        // update by jielong.xing at 2015-3-26 begin
        mViewPageBgMap.clear();
        mLastBg = 0;
        // update by jielong.xing at 2015-3-26 end

        mSlidingViewWeatherMap.clear();
        mSlidingViewDayMap.clear();
        mSlidingViewHourMap.clear();
        mSlidingViewColorMap.clear();
        mViewBgUrlMap.clear();

        // Fixed PR965435 by jielong.xing at 2015-4-1 begin
        mIsInitViewPager = true;
        // Fixed PR965435 by jielong.xing at 2015-4-1 end
        mIsScrollToBottom = true;
        enableDisableSwipeRefresh(true);
        mCityList = updateService.getCityListFromDB();

        if (mCityList == null || mCityList.size() == 0) {
            // Fixed PR974952,974932,958494 by jielong.xing at 2015-4-15 begin
            mViews.clear();
            mCityList.clear();
            mViewPager.setAdapter(null);
            //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,10/24/2015,9992933,[Android6.0][Weather_v5.2.8.1.0305.0]The locate button has no use after delete all cities
            //startActivity(new Intent(MainActivity.this, LocateActivity.class), ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
            //mHandler.sendEmptyMessageDelayed(FINISH_ACTIVITY, 500);
            startActivity(new Intent(MainActivity.this, LocateActivity.class));
            MainActivity.this.finish();
            MainActivity.this.overridePendingTransition(R.anim.fab_in, R.anim.fab_out);
            //[BUGFIX]-Add-END by TSCD.xing.zhao
            // Fixed PR974952,974932,958494 by jielong.xing at 2015-4-15 end
            return;
        }

        //autolocate should be the first city if there is autoLocate added by ting.ma at 2015-03-14 begin
        /*int citySize = mCityList.size();
        for(int i = 0; i < citySize; i++) {
			if (mCityList.get(i).isAutoLocate() && i != 0) {
				mCityList.add(0, mCityList.get(i));
				mCityList.remove(i+1);
				break;
			}
		}*/
        //autolocate should be the first city if there is autoLocate added by ting.ma at 2015-03-14 end

        for (int i = 0; i < mCityList.size(); i++) {
            City city = mCityList.get(i);
            WeatherInfo weatherInfo = updateService.getWeatherFromDB(city.getLocationKey());

            WeatherForShow weatherForShow = weatherInfo.getWeatherForShow();
            List<DayForShow> dayForShow = weatherInfo.getDayForShow();
            List<Hour> hourList = weatherInfo.getHourList();
            // delete by jielong.xing at 2015-3-19 begin
//			List<HourForShow> hourForShow = weatherInfo.getHours();
            // delete by jielong.xing at 2015-3-19 end

            if (weatherForShow == null || dayForShow == null) {// || hourList == null) {// || hourForShow == null) {
                updateService.deleteCity(city.getLocationKey());
                mCityList.clear();
                initViewPager(true);
                return;
            }
            mWeatherInfoList.add(weatherInfo);

            ImageView iv_point = new ImageView(MainActivity.this);
            // Fixed PR988842 by jielong.xing at 2015-4-28 begin
//			iv_point.setPadding(4, 0, 4, 0);
            // Fixed PR988842 by jielong.xing at 2015-4-28 end
            LinearLayout.LayoutParams pointParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            // Fixed PR988842 by jielong.xing at 2015-4-28 begin
            pointParams.setMargins(5, 5, 5, 5);
            // Fixed PR988842 by jielong.xing at 2015-4-28 end
            pointParams.gravity = Gravity.CENTER_VERTICAL;
            iv_point.setLayoutParams(pointParams);
            if (i == 0) {
                if (city.isAutoLocate()) {
                    iv_point.setBackgroundResource(R.drawable.ic_the_navigation_progress);
                } else {
                    iv_point.setBackgroundResource(R.drawable.ic_the_current_progress);
                }
            } else {
                if (city.isAutoLocate()) {
                    iv_point.setBackgroundResource(R.drawable.ic_non_navigation_progress);
                } else {
                    iv_point.setBackgroundResource(R.drawable.ic_non_current_progress);
                }
            }
            mImageViews.add(iv_point);

            mViewPoints.addView(mImageViews.get(i));

            //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/04/2016,1554364,[GAPP][Android 6.0][Weather][Monitor]Time show innormal when add a existed city
//            long time = Long.parseLong(city.getUpdateTime());
            //String newUpdateTime = formatTime(time);
//            String newUpdateTime = getRefreshTime(time);//modifed by jiajun.shen for 1010042
            //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/27/2015,983895,delete weather_24forecast_card
            //[BUGFIX]-Add-END by TSCD.qian-li
            /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
            FrameLayout weatherView = null;
            if (isTwcweather) {
                weatherView = (FrameLayout) inflater.inflate(R.layout.weather_scrollview_layout, null);
            } else {
                weatherView = (FrameLayout) inflater.inflate(R.layout.weather_scrollview_acc_layout, null);
            }
            //[FEATURE]-Add-END by TSCD.peng.du
            /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
//removed by jiajun.shen for 1038936
//			final ImageView blurImg = (ImageView)weatherView.findViewById(R.id.iv_blurimg);
//			mBlurImgList.add(blurImg);

            final VerticalSlidingView slidingView = (VerticalSlidingView) weatherView.findViewById(R.id.slidingView);


            // update by jielong.xing at 2015-3-26 begin
            int currentBg = getCurrentBackgroundWeatherIcon(weatherForShow.getIcon());
            mViewPageBgMap.put(i, currentBg);
            int[] colors = getColorFromBackground(currentBg);
            textColor = colors[0];
            backgroundColor = colors[1];

            Uri currentBgUri = getCurrentBgUri(weatherForShow.getIcon());
            mViewBgUrlMap.put(i, currentBgUri);

            mSlidingViewWeatherMap.put(i, weatherForShow);
            mSlidingViewDayMap.put(i, dayForShow);
            mSlidingViewHourMap.put(i, hourList);
            mSlidingViewColorMap.put(i, colors);

//			weatherView.setBackgroundResource(getCurrentBackgroundWeatherIcon(weatherForShow.getIcon()));
            //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/04/2016,1554364,[GAPP][Android 6.0][Weather][Monitor]Time show innormal when add a existed city
//            mTvRefreshTime.setText(newUpdateTime);//add by jiajun.shen for 1010042
//            setCurrentRefreshTime();//add by jiajun.shen for 1031027
            //[BUGFIX]-Add-END by TSCD.qian-li
            // update by jielong.xing at 2015-3-26 end
            slidingView.setWeatherData(weatherForShow, isUnitC, null, city.isAutoLocate(), isUnitKm, showAdvData, showFeelsLike);
            slidingView.setDayData(dayForShow, isUnitC);
            //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/27/2015,983895,delete weather_24forecast_card
            if (isTwcweather) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                slidingView.setHourlyData(hourList, isUnitC, textColor);
            }
            //[FEATURE]-Add-END by TSCD.peng.du
            slidingView.setDayColor(backgroundColor);
            slidingView.setWeatherColor(textColor);
            slidingView.setOnPageScrollListener(new VerticalSlidingView.OnPageScrollListener() {
                // Fixed PR965866 by jielong.xing at 2015-5-28 begin
                @Override
                public void onViewScrollStateChange(int state) {
                    if (!isViewPagerScroll) {
                        enableDisableSwipeRefresh(state == VerticalSlidingView.SCROLL_STATE_IDLE);
                    }
                }
                // Fixed PR965866 by jielong.xing at 2015-5-28 end

                @Override
                public void onPageChanged(int position) {
                }

                @Override
                public void onScrollToBottom(boolean isScrollToBottom) {
                    if (isScrollToBottom) {
                        //mLastScrollPercent = 0;
                        mIsScrollToBottom = true;
                        //isScrollToBottom = false;
                        mSwipeRefreshLayout.setEnabled(true);
                        mScrollPosition = 0;
                        updateRefreshViewList();
                    } else {
                        //mLastScrollPercent = 1;
                        mIsScrollToBottom = false;
                        //isScrollToBottom = true;
                        mSwipeRefreshLayout.setEnabled(false);
                    }
                }

                @Override
                public void onActionDown() {
                    //removed by jiajun.shen for 1038936
                    //blurImg.clearAnimation();
                }

                @Override
                public void onScrolling(int scrollPostion, float scrollPercent) {
                    mScrollPosition = scrollPostion;
                    mScrollPercent = scrollPercent;
                    //[BUGFIX]-Add-BEGIN by TSCD.xiangnan.zhou,01/15/2016,1430537,
                    // [Weather]The menu button dotted grey but can still be opened.
//                    if (scrollPercent > 0.8) {
//                        mIvMenu.setVisibility(View.GONE);
//                    } else {
//                        mIvMenu.setVisibility(View.VISIBLE);
//                        //mIvMenu.setImageAlpha((int) ((1 - scrollPercent) * 255));
//                    }
                    //[BUGFIX]-Add-END by TSCD.xiangnan.zhou
                    //[BUGFIX]-Mod-BEGIN by xinlei.sheng, 2016/01/21,1442774
                    setFadeAnimationForStaticBg(mLastScrollPercent, mScrollPercent, 100);
                    //[BUGFIX]-Mod-END by xinlei.sheng, 2016/01/21,1442774
                }

                @Override
                public void onStopScroll(float fromAlpha, float toAlpha) {
//                    if (toAlpha == 1.0f) {
//                        setFadeAnimation(fromAlpha, 1.0f, 500);
//                    } else {
//                        setFadeAnimation(fromAlpha, 0.0f, 500);
//                    }
                    //Log.e(TAG, "onStopScroll mLastScrollPercent=" + mLastScrollPercent);
                    updateRefreshViewList();
                }
            });
            mViews.add(weatherView);
        }
//		mPagerAdapter = new MyPagerAdapter(MainActivity.this, mViews);

        PagerAdapter adapter = new PagerAdapter() {
            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                mPosition = position;
                super.setPrimaryItem(container, position, object);
            }

            @Override
            public int getCount() {
                if (mViews != null && mViews.size() > 0) {
                    return mViews.size();
                } else {
                    return 0;
                }
            }

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                if (position < mViews.size()) {
                    container.removeView(mViews.get(position));
                }
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(mViews.get(position));
                return mViews.get(position);
            }

            @Override
            public void finishUpdate(ViewGroup container) {
                for (int i = 0; i < mViews.size(); i++) {
                    City var = mCityList.get(i);
                    if (i == mPosition) {
                        if (var.isAutoLocate()) {
                            mImageViews.get(i).setBackgroundResource(R.drawable.ic_the_navigation_progress);
                        } else {
                            mImageViews.get(i).setBackgroundResource(R.drawable.ic_the_current_progress);
                        }
                    } else {
                        if (var.isAutoLocate()) {
                            mImageViews.get(i).setBackgroundResource(R.drawable.ic_non_navigation_progress);
                        } else {
                            mImageViews.get(i).setBackgroundResource(R.drawable.ic_non_current_progress);
                        }
                    }
                }
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }
        };

        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(cityLimit);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
                // Fixed PR965866 by jielong.xing at 2015-5-28 begin
                if (arg0 == ViewPager.SCROLL_STATE_IDLE) {
                    isViewPagerScroll = false;
                    if (isBackgoundDynamic) {
//                        if (!mMediaplayer.isPlaying()) {
//                            mMediaplayer.start();
//                        }
                    }

                } else if (arg0 == ViewPager.SCROLL_STATE_DRAGGING) {
                    isViewPagerScroll = true;
//                    if (isBackgoundDynamic) {
//                        mMediaplayer.pause();
//                    }
                }
                // Fixed PR965866 by jielong.xing at 2015-5-28 end
                // fix PR950414 by jielong.xing at 2014-3-16 begin
                enableDisableSwipeRefresh(arg0 == ViewPager.SCROLL_STATE_IDLE);
                // fix PR950414 by jielong.xing at 2014-3-16 end
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,12/10/2015,1043597,[Weather]When Slide right / left ,two city  weather info box isn't in a level line
                if (arg1 <= 0.15 || arg1 >= 0.85) {
                    updateBeforeAfterRefreshView(mViewPager.getCurrentItem());
                }
                //[BUGFIX]-Add-END by TSCD.qian-li
            }

            @Override
            public void onPageSelected(int position) {
                mPosition = position;
                // fix PR952994 by jielong.xing at 2015-3-18 begin
                if (mCityList != null && mCityList.size() > 0) {
                    mTempKey = mCityList.get(mPosition).getLocationKey();
                }
                // fix PR952994 by jielong.xing at 2015-3-18 end
                setAccuWeatherLogoAction(position);
                setRefreshTimeAt(position);//add by jiajun.shen for 1031027

                if (isBackgoundDynamic) {
                    if (mLastBgUri != null) {
                        // Fixed PR965435 by jielong.xing at 2015-4-1 begin
                        mIsInitViewPager = false;
                        // Fixed PR965435 by jielong.xing at 2015-4-1 end
                        Uri currentBgUri = mViewBgUrlMap.get(position);
                        //if (currentBgUri != mLastBgUri) {
                        //take a snapshot of current frame
                        if (switchVideoTask != null) {
                            switchVideoTask.cancel(true);
                        }
                        switchVideoTask = new SwitchVideoTask();
                        switchVideoTask.execute(currentBgUri);
                        // }
                    }
                } else {
                    if (mLastBg != 0) {
                        // Fixed PR965435 by jielong.xing at 2015-4-1 begin
                        mIsInitViewPager = false;
                        // Fixed PR965435 by jielong.xing at 2015-4-1 end
                        int currentBg = mViewPageBgMap.get(mPosition).intValue();
                        if (currentBg != mLastBg) {
                            mSwitchBgAnimHandler.removeMessages(0);
                            Message msg = mSwitchBgAnimHandler.obtainMessage(0);
                            msg.arg1 = currentBg;
                            mSwitchBgAnimHandler.sendMessageDelayed(msg, 300);
                        } else {
                            mSwitchBgAnimHandler.removeMessages(0);
                        }
                    }
                }
//				FrameLayout var = mViews.get(position);
//				VerticalSlidingView slidingView = (VerticalSlidingView)var.findViewById(R.id.slidingView);
//				setColor(slidingView, position);
            }

        });

        SharedPreferences pref = getSharedPreferences("weather", Context.MODE_WORLD_READABLE);
        String cityKey = pref.getString("currentcity", null);

        int position = 0;
        if (null == mTempKey || mTempKey.equals("")) {
            if (null != cityKey && !"".equals(cityKey)) {
                mTempKey = cityKey;
                position = updateService.getCurrentPosition(mCityList, cityKey);
                mViewPager.setCurrentItem(position);
            } else {
                mViewPager.setCurrentItem(0);
            }
        } else {
            position = updateService.getCurrentPosition(mCityList, mTempKey);
            mViewPager.setCurrentItem(position);
        }

        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/04/2016,1554364,[GAPP][Android 6.0][Weather][Monitor]Time show innormal when add a existed city
        setRefreshTimeAt(position);
        //[BUGFIX]-Add-END by TSCD.qian-li

        if (isBackgoundDynamic) {
            /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
            Log.d(TAG, "initViewPager the mLastBgUri = " + mLastBgUri);
            Uri currentBgUri = mViewBgUrlMap.get(position);
            mLastBgUri = currentBgUri;
            Log.d(TAG, "initViewPager the currentBgUri = " + currentBgUri);
            /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
            try {
                if (switchVideoTask != null) {
                    switchVideoTask.cancel(true);
                }
                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,03/04/2016,1719109,[Performance][Flip cover][Weather]The sound choose screen is very slow when with weather app open
                if (!isNeedPrepare) {
                    Log.i(TAG, "initViewPager mediaplayer.reset");
                    mMediaplayer.reset();
                }
//                mMediaplayer.reset();
                Log.i(TAG, "initViewPager mediaplayer.setDataSource");
                mMediaplayer.setDataSource(this, mLastBgUri);
                if (isNeedPrepare) {
                    Log.i(TAG, "initViewPager mediaplayer.prepareAsync");
                    mMediaplayer.prepareAsync();
                    isNeedPre = true;
                }
                //[BUGFIX]-Add-END by TSCD.qian-li
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            int currentBg = mViewPageBgMap.get(position).intValue();
            mLastBg = currentBg;
            mImgBg1.setVisibility(View.VISIBLE);
            mImgBg2.setVisibility(View.GONE);
            mImgBg1.setBackgroundResource(currentBg);
            mImgBgBlur1.setVisibility(View.VISIBLE);
            mImgBgBlur2.setVisibility(View.INVISIBLE);
            mImgBgBlur1.setImageBitmap(getBitmap(currentBg));
            mImgBgBlur1.setImageAlpha(0);
//        mImgFgBlur.setImageAlpha(0);
//        isScrollToBottom = false;//add by jiajun.shen for 1043016
            isBgShow1 = true;//add by jiajun.shen for 1043016

        }


//		if(mLastBg==R.drawable.bg_clear)
//		{
//			mImgFgBlur.setVisibility(View.GONE);
//		}else {
//			mImgFgBlur.setVisibility(View.VISIBLE);
//		}
        setAccuWeatherLogoAction(position);
//		mCurrentView.startVideoView();
//		mCurrentView.setBackground(null);
    }

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,12/24/2015,1102917,[Android6.0][Weather_v5.2.8.1.0301.0]Refresh weather will cause a short time stuck and the forcast will be hidden
    private void updateViewPagerWeather() {
        if (updateService == null) {
            return;
        }
        if (mViewPager != null && mViews != null && mViews.size() > 0) {
            mCityList = updateService.getCityListFromDB();
            if (mCityList == null || mCityList.size() == 0) {
                mViews.clear();
                mCityList.clear();
                mViewPager.setAdapter(null);
                //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,10/24/2015,9992933,[Android6.0][Weather_v5.2.8.1.0305.0]The locate button has no use after delete all cities
                startActivity(new Intent(MainActivity.this, LocateActivity.class));
                MainActivity.this.finish();
                MainActivity.this.overridePendingTransition(R.anim.fab_in, R.anim.fab_out);
                //[BUGFIX]-Add-END by TSCD.xing.zhao
                return;
            }
            //[BUGFIX]-Add-BEGIN by TSCD.peng.du,02/01/2016,1539248,[jrdlogger]com.tct.weather JE
//            int cnt = 0;
//            if (mViews != null && mViews.size() > 0) {
//                cnt = mViews.size();
//            }
            int cnt = mViews.size();
            /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
            if (cnt != mCityList.size()) {
                Log.d(TAG, " the size of mViews is not equre mCityList size");
                /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
                initViewPager(true);
                return;
            }
            //[BUGFIX]-Add-END by TSCD.peng.du

            for (int i = 0; i < cnt; i++) {
                City city = mCityList.get(i);
                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/04/2016,1554364,[GAPP][Android 6.0][Weather][Monitor]Time show innormal when add a existed city
//                long time = System.currentTimeMillis();
//                try {
//                    time = Long.parseLong(city.getUpdateTime());
//                } catch (Exception e) {
//                }
//                String newUpdateTime = getRefreshTime(time);
                //[BUGFIX]-Add-END by TSCD.qian-li
                FrameLayout var = mViews.get(i);
                VerticalSlidingView slidingView = (VerticalSlidingView) var.findViewById(R.id.slidingView);

                WeatherInfo weatherInfo = updateService.getWeatherFromDB(city.getLocationKey());
                mWeatherInfoList.add(weatherInfo);
                WeatherForShow weatherForShow = weatherInfo.getWeatherForShow();
                List<DayForShow> dayForShow = weatherInfo.getDayForShow();
                if (isTwcweather) {
                    List<Hour> hourList = weatherInfo.getHourList();
                    mSlidingViewHourMap.put(i, hourList);
                    slidingView.setHourlyData(hourList, isUnitC, textColor);
                }
                if (weatherForShow == null || dayForShow == null) {
                    updateService.deleteCity(city.getLocationKey());
                    mCityList.clear();
                    updateViewPagerWeather();
                    return;
                }

                int currentBg = getCurrentBackgroundWeatherIcon(weatherForShow.getIcon());
                mViewPageBgMap.put(i, currentBg);
                int[] colors = getColorFromBackground(currentBg);
                textColor = colors[0];
                backgroundColor = colors[1];

                Uri currentBgUri = getCurrentBgUri(weatherForShow.getIcon());
                mViewBgUrlMap.put(i, currentBgUri);

                mSlidingViewWeatherMap.put(i, weatherForShow);
                mSlidingViewDayMap.put(i, dayForShow);
                mSlidingViewColorMap.put(i, colors);

                slidingView.setWeatherData(weatherForShow, isUnitC, null, city.isAutoLocate(), isUnitKm, showAdvData, showFeelsLike);
                slidingView.setDayData(dayForShow, isUnitC);
                slidingView.setDayColor(backgroundColor);
                slidingView.setWeatherColor(textColor);

                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/04/2016,1554364,[GAPP][Android 6.0][Weather][Monitor]Time show innormal when add a existed city
//                mTvRefreshTime.setText(newUpdateTime);
                //[BUGFIX]-Add-END by TSCD.qian-li
            }

            int position = mViewPager.getCurrentItem();

            //[BUGFIX]-Add-BEGIN by TSCD.qian-li,02/04/2016,1554364,[GAPP][Android 6.0][Weather][Monitor]Time show innormal when add a existed city
            setRefreshTimeAt(position);
            //[BUGFIX]-Add-END by TSCD.qian-li

            if (isBackgoundDynamic) {
                if (mLastBgUri != null) {
                    mIsInitViewPager = false;
                    Uri currentBgUri = mViewBgUrlMap.get(position);
                    if (currentBgUri != mLastBgUri) {
                        mLastBgUri = currentBgUri;
                        if (isShowActivity) {
                            if (switchVideoTask != null) {
                                switchVideoTask.cancel(true);
                            }
                            switchVideoTask = new SwitchVideoTask();
                            switchVideoTask.execute(currentBgUri);
                        } else {
                            isNeedResetMedia = true;
                        }
                    }
                }
            } else {
                if (mLastBg != 0) {
                    mIsInitViewPager = false;
                    int currentBg = mViewPageBgMap.get(mPosition).intValue();
                    if (currentBg != mLastBg) {
                        mSwitchBgAnimHandler.removeMessages(0);
                        Message msg = mSwitchBgAnimHandler.obtainMessage(0);
                        msg.arg1 = currentBg;
                        mSwitchBgAnimHandler.sendMessageDelayed(msg, 300);
                    } else {
                        mSwitchBgAnimHandler.removeMessages(0);
                    }
                }
            }
        }

    }
    //[BUGFIX]-Add-END by TSCD.qian-li

    // add by jielong.xing at 2015-3-11 begin

    class SwitchVideoTask extends AsyncTask<Uri, Integer, Bitmap> {
        @Override
        protected Bitmap doInBackground(Uri... params) {
            Uri currentBgUri = params[0];
            Log.d(TAG, "SwitchVideoTask currentBgUri = " + currentBgUri);
            //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,01/21/2016,1468263,[GAPP][Android 6.0][Weather][Monitor]The Weather has stopped when enter weather in MIE screen.
            if (mMediaplayer == null) {
                mMediaplayer = new MediaPlayer();
                mLastBgUri = currentBgUri;
                Log.d(TAG, "SwitchVideoTask mMediaplayer == null mLastBgUri = " + mLastBgUri); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                if (!isCancelled()) {
                    try {
                        Log.i(TAG, "doinbackground mediaplayer.reset1");
                        mMediaplayer.reset();
                        mMediaplayer.setDataSource(MainActivity.this, mLastBgUri);
                        mMediaplayer.prepareAsync();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }
                }
                return null;
            }
            //[BUGFIX]-Add-END by TSCD.xing.zhao
            mMediaplayer.pause();
//            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(MainActivity.this, mLastBgUri);
            int time = mMediaplayer.getCurrentPosition();
            Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(time * 1000, MediaMetadataRetriever.OPTION_CLOSEST);

            if (!isCancelled()) {
                try {
                    mLastBgUri = currentBgUri;
                    mMediaplayer.reset();
                    mMediaplayer.setDataSource(MainActivity.this, mLastBgUri);
                    mMediaplayer.prepareAsync();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }
            }
            return bitmap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (null != bitmap) {
                try {
                    bitmap = Bitmap.createScaledBitmap(bitmap, mIvSnapShot.getMeasuredWidth(), mIvSnapShot.getMeasuredHeight(), true);
                    if (bitmap != null) {
                        mIvSnapShot.setImageBitmap(bitmap);
                        //mIvSnapShot.setVisibility(View.VISIBLE);
                        mIvSnapShot.setImageAlpha(255);
                    }
                    setFadeAnimation(1, 0, 800);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    private int mLastBg = 0;

    private int textColor;
    private int backgroundColor;

    private ImageView mImgBg1;
    private ImageView mImgBg2;
    private ImageView mImgBgBlur1;
    private ImageView mImgBgBlur2;
    //private ImageView mImgFgBlur;
    // Fixed PR965435 by jielong.xing at 2015-4-1 begin
    private boolean mIsInitViewPager = true;
    // Fixed PR965435 by jielong.xing at 2015-4-1 end

    // Fixed PR965866 by jielong.xing at 2015-5-28 begin
    private boolean isViewPagerScroll = false;
    // Fixed PR965866 by jielong.xing at 2015-5-28 end

    private Handler mSwitchBgAnimHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            mLastBg = msg.arg1;
            if (mImgBg1.getVisibility() == View.VISIBLE && mImgBg2.getVisibility() == View.GONE) {
                AlphaAnimation animation1 = new AlphaAnimation(1.0f, 0.0f);
                animation1.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // Fixed PR965435 by jielong.xing at 2015-4-1 begin
                        if (!mIsInitViewPager) {
                            mImgBg1.setVisibility(View.GONE);
                            mImgBg1.setBackground(null);
                            mImgBgBlur1.setVisibility(View.GONE);
                            mImgBgBlur1.setImageBitmap(null);
                        }
                        // Fixed PR965435 by jielong.xing at 2015-4-1 end
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }

                });
                animation1.setDuration(1000);
                AlphaAnimation animation2 = new AlphaAnimation(0.0f, 1.0f);
                animation2.setDuration(1000);
                mImgBg1.startAnimation(animation1);
                mImgBgBlur1.startAnimation(animation1);
                mImgBg2.setVisibility(View.VISIBLE);
                mImgBg2.setBackgroundResource(mLastBg);
                mImgBgBlur2.setVisibility(View.VISIBLE);
                mImgBgBlur2.setImageBitmap(getBitmap(mLastBg));
                mImgBgBlur2.setImageAlpha((int) (mScrollPercent * 255));
                mImgBgBlur2.startAnimation(animation2);
                mImgBg2.startAnimation(animation2);
                isBgShow1 = false;
            } else {
                AlphaAnimation animation1 = new AlphaAnimation(1.0f, 0.0f);
                animation1.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mImgBg2.setVisibility(View.GONE);
                        mImgBg2.setBackground(null);
                        mImgBgBlur2.setVisibility(View.GONE);
                        mImgBgBlur2.setImageBitmap(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }

                });
                animation1.setDuration(1000);
                AlphaAnimation animation2 = new AlphaAnimation(0.0f, 1.0f);
                animation2.setDuration(1000);
                mImgBg2.startAnimation(animation1);
                mImgBgBlur2.startAnimation(animation1);
                mImgBg1.setVisibility(View.VISIBLE);
                mImgBg1.setBackgroundResource(mLastBg);
                mImgBgBlur1.setVisibility(View.VISIBLE);
                mImgBgBlur1.setImageBitmap(getBitmap(mLastBg));
                mImgBgBlur1.setImageAlpha((int) (mScrollPercent * 255));
                mImgBg1.startAnimation(animation2);
                mImgBgBlur1.startAnimation(animation2);
                isBgShow1 = true;
            }
        }
    };


    //removed by jiajun.shen for 1038936
    //private ArrayList<ImageView> mBlurImgList = new ArrayList<ImageView>();

    // fix PR950414 by jielong.xing at 2014-3-16 begin
    protected void enableDisableSwipeRefresh(boolean enable) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(enable && mIsScrollToBottom);
        }
    }
    // fix PR950414 by jielong.xing at 2014-3-16 end

//	private MyPagerAdapter mPagerAdapter;

    private void updateRefreshViewList() {
        int cnt = 0;
        if (mViews != null && mViews.size() > 0) {
            cnt = mViews.size();
        }

        for (int i = 0; i < cnt; i++) {
            FrameLayout var = mViews.get(i);
            VerticalSlidingView slidingView = (VerticalSlidingView) var.findViewById(R.id.slidingView);
            if (i != mPosition) {
                slidingView.updateView(mScrollPosition);
                //ImageView img = mBlurImgList.get(i);//removed by jiajun.shen for 1038936
//                if (isScrollToBottom) {
//                    //mImgFgBlur.setImageAlpha(255);
//                    if (isBgShow1) {
//                        mImgBgBlur1.setImageAlpha(255);
//                    } else {
//                        mImgBgBlur2.setImageAlpha(255);
//                    }
//                } else {
//                    //mImgFgBlur.setImageAlpha(0);
//                    if (isBgShow1) {
//                        mImgBgBlur1.setImageAlpha(0);
//                    } else {
//                        mImgBgBlur2.setImageAlpha(0);
//                    }
//                }
            }
        }
    }

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,12/10/2015,1043597,[Weather]When Slide right / left ,two city  weather info box isn't in a level line
    private void updateBeforeAfterRefreshView(int currentPosition) {
        int cnt = 0;
        FrameLayout var = null;
        VerticalSlidingView slidingView = null;

        if (mViews != null & mViews.size() > 1) {
            cnt = mViews.size();
        } else {
            return;
        }

        if (currentPosition == 0) {
            var = mViews.get(currentPosition + 1);
            slidingView = (VerticalSlidingView) var.findViewById(R.id.slidingView);
            slidingView.updateView(mScrollPosition);
            return;
        } else if (currentPosition == cnt - 1) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
            var = mViews.get(currentPosition - 1);
            slidingView = (VerticalSlidingView) var.findViewById(R.id.slidingView);
            slidingView.updateView(mScrollPosition);
            return;
        } else {
            var = mViews.get(currentPosition - 1);
            slidingView = (VerticalSlidingView) var.findViewById(R.id.slidingView);
            slidingView.updateView(mScrollPosition);
            var = mViews.get(currentPosition + 1);
            slidingView = (VerticalSlidingView) var.findViewById(R.id.slidingView);
            slidingView.updateView(mScrollPosition);
            return;
        }
    }
    //[BUGFIX]-Add-END by TSCD.qian-li

    private class MyPagerAdapter extends PagerAdapter {
        private ArrayList<FrameLayout> mSlidingViewList = null;

        public MyPagerAdapter(Context context, ArrayList<FrameLayout> list) {
            mSlidingViewList = list;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            mPosition = position;
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public int getCount() {
            if (mSlidingViewList != null && mSlidingViewList.size() > 0) {
                return mSlidingViewList.size();
            } else {
                return 0;
            }
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (position < mSlidingViewList.size()) {
                container.removeView(mSlidingViewList.get(position));
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mSlidingViewList.get(position));
            return mSlidingViewList.get(position);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            for (int i = 0; i < mSlidingViewList.size(); i++) {
                if (i == mPosition) {
                    mImageViews.get(i).setBackgroundResource(R.drawable.ic_the_current_progress);
                } else {
                    mImageViews.get(i).setBackgroundResource(R.drawable.ic_non_current_progress);
                }
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    private void setAccuWeatherLogoAction(int position) {
        if (position >= 0 && mWeatherInfoList != null && mWeatherInfoList.size() > 0) {
            final WeatherForShow weather = mWeatherInfoList.get(position).getWeatherForShow();
            mIvAccuWeather.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //[BUGFIX]-1192935-fix-start by TSCD.lin-zhou
                    String url = isTwcweather ? "http://www.weather.com/" : "http://m.accuweather.com/"; // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                    //[BUGFIX]-1192935-fix-END by TSCD.lin-zhou
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(url);
                    intent.setData(content_url);
                    //[BUGFIX]-Add-BEGIN by TSCD.peng.du,11/30/2015,1115058,[MONKEY][Weatherl][CRASH]CRASH:com.tct.weather
                    try {
                        startActivity(intent);
                    /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "there is not activity to be found ");
                        /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
                    }
                    //[BUGFIX]-Add-END by TSCD.peng.du
                }
            });
        }
    }
    // add by jielong.xing at 2015-3-11 end

    private boolean getConnectedStatus() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();

        boolean isConnected = false;

        if (activeInfo != null && activeInfo.isConnected()) {
            isWifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            isMobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if (!isWifiConnected && !isMobileConnected) {
                isOtherConnected = true;
            }
            isConnected = true;
        } else {
            isWifiConnected = false;
            isMobileConnected = false;
            isOtherConnected = false;
            isConnected = false;
        }

        Log.d(TAG, "jielong_isWifiConnected == " + isWifiConnected + ", isMobileConnected == " + isMobileConnected + ", isOtherConnected == " + isOtherConnected);
        return isConnected;
    }

    // Fixed PR974952,974932,958494 by jielong.xing at 2015-4-15 begin
    private static final int FINISH_ACTIVITY = 111;
    private static final int STORE_IMG_DONE = 112;
    private static final int UPDATE_FINISH = 113;
    private static final int MEDIAPLAYER_STARTED = 114;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FINISH_ACTIVITY: {
                    MainActivity.this.finish();
                    MainActivity.this.overridePendingTransition(R.anim.activity_open, R.anim.activity_close);
                    break;
                }
                case STORE_IMG_DONE: {
                    if (isBgShow1) {
                        mImgBgBlur1.setImageBitmap(getBitmap(mLastBg));
                    } else {
                        mImgBgBlur2.setImageBitmap(getBitmap(mLastBg));
                    }
                    break;
                }
                case UPDATE_FINISH: {
                    //[BUGFIX]-Add-BEGIN by TSCD.,12/22/2015,1102917,[Android6.0][Weather_v5.2.8.1.0301.0]Refresh weather will cause a short time stuck and the forcast will be hidden
//                    initViewPager();
                    updateViewPagerWeather();
                    //[BUGFIX]-Add-END by TSCD.
                    break;
                }
                case MEDIAPLAYER_STARTED: {
                    setFadeAnimation(1, 0, 500);
                }
            }
        }
    };
    // Fixed PR974952,974932,958494 by jielong.xing at 2015-4-15 end

    private boolean checkDataBase() {
        mCityList = updateService.getCityListFromDB();

        if (mCityList.size() == 0) {
            // Fixed PR974952,974932,958494 by jielong.xing at 2015-4-15 begin
            if (mViewPager == null) {
                //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,01/11/2016,1305396,[GAPP][Android 6.0][Weather]It will flash one second when entering the weather by MIE
                Intent mIntent = new Intent(MainActivity.this, LocateActivity.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mIntent);
                mHandler.sendEmptyMessageDelayed(FINISH_ACTIVITY, 500);
                //[BUGFIX]-Add-END by TSCD.xing.zhao
                /*startActivity(new Intent(MainActivity.this, LocateActivity.class));
                MainActivity.this.finish();*/
            } else {
                mViews.clear();
                mCityList.clear();
                mViewPager.setAdapter(null);
                //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,10/24/2015,9992933,[Android6.0][Weather_v5.2.8.1.0305.0]The locate button has no use after delete all cities
                //startActivity(new Intent(MainActivity.this, LocateActivity.class), ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                //mHandler.sendEmptyMessageDelayed(FINISH_ACTIVITY, 500);
                startActivity(new Intent(MainActivity.this, LocateActivity.class));
                MainActivity.this.finish();
                MainActivity.this.overridePendingTransition(R.anim.fab_in, R.anim.fab_out);
                //[BUGFIX]-Add-END by TSCD.xing.zhao
            }
            // Fixed PR974952,974932,958494 by jielong.xing at 2015-4-15 end
            return false;
        } else {
            return true;
        }
    }

    private View popupMenuLayout = null;

    private void showPopupMenuLayout() {
        if (null == popupMenuLayout) {
            popupMenuLayout = findViewById(R.id.popup_menu_layout);
        }
        popupMenuLayout.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRect(0, 0, view.getWidth(), view.getHeight());
            }
        });
        popupMenuLayout.setTranslationZ(
                getResources().getDimensionPixelSize(
                        R.dimen.floating_action_menu_translation_z));
        TextView addLocation = (TextView) popupMenuLayout.findViewById(R.id.menu_addlocation);
        TextView delLocation = (TextView) popupMenuLayout.findViewById(R.id.menu_deletelocation);
        TextView refreshView = (TextView) popupMenuLayout.findViewById(R.id.menu_refresh);
//        TextView changeUnit = (TextView) popupMenuLayout.findViewById(R.id.menu_changeunit);
//        TextView changeKmUnit = (TextView) popupMenuLayout.findViewById(R.id.menu_change_km_mi);
        TextView settings = (TextView) popupMenuLayout.findViewById(R.id.menu_setting);

        //PR944927 Overflow menu should use system contrlos correctly by ting.ma at 2015-03-13 begin
        addLocation.setBackground(getResources().getDrawable(R.drawable.menu_default_material));
        delLocation.setBackground(getResources().getDrawable(R.drawable.menu_default_material));
        refreshView.setBackground(getResources().getDrawable(R.drawable.menu_default_material));
//        changeUnit.setBackground(getResources().getDrawable(R.drawable.menu_default_material));
//        changeKmUnit.setBackground(getResources().getDrawable(R.drawable.menu_default_material));
        settings.setBackground(getResources().getDrawable(R.drawable.menu_default_material));
        //PR944927 Overflow menu should use system contrlos correctly by ting.ma at 2015-03-13 end

        addLocation.setOnClickListener(this);
        delLocation.setOnClickListener(this);
        refreshView.setOnClickListener(this);
//        changeUnit.setOnClickListener(this);
//        changeKmUnit.setOnClickListener(this);
        settings.setOnClickListener(this);

////		TextView tv_menu_unit = (TextView) popupMenuLayout.findViewById(R.id.tv_menu_unit);
//        if (isUnitC) {
//            changeUnit.setText(getResources().getString(R.string.change_to_F));
//        } else {
//            changeUnit.setText(getResources().getString(R.string.change_to_C));
//        }
//
////		TextView tv_menu_km_unit = (TextView)popupMenuLayout.findViewById(R.id.tv_menu_km_mi);
//        if (isUnitKm) {
//            changeKmUnit.setText(getResources().getString(R.string.change_to_mi));
//        } else {
//            changeKmUnit.setText(getResources().getString(R.string.change_to_km));
//        }

        //Begin add by jiajun.shen for 444427
//        if (CustomizeUtils.getBoolean(MainActivity.this, "hide_unit_setting")) {
//            changeUnit.setVisibility(View.GONE);
//            changeKmUnit.setVisibility(View.GONE);
//        }
        //End add by jiajun.shen for 444427

        int visible = popupMenuLayout.getVisibility();
        if (visible == View.VISIBLE) {
            popupMenuLayout.setVisibility(View.GONE);
        } else if (visible == View.GONE) {
            popupMenuLayout.setVisibility(View.VISIBLE);
        }
        popupMenuLayout.setPivotX(mIvMenu.getX());
        popupMenuLayout.setPivotY(mIvMenu.getY());
        popupMenuLayout.setScaleY(0.1f);
        popupMenuLayout.setScaleX(0.1f);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(300).setInterpolator(new AccelerateInterpolator());
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Float value = (Float) valueAnimator.getAnimatedValue();
                popupMenuLayout.setScaleX(value);
                popupMenuLayout.setScaleY(value);
            }
        });
    }

    // add by jielong.xing for pr928049、927559 at 2015-2-11 begin
    private static final int ACTIVITY_BACK = 0x1000;
    private static final int ACTIVITY_REFRESH = 0x1001;
    private static final int SETTING_RESULT = 0x1002;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case ACTIVITY_BACK:
                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,03/04/2016,1719109,[Performance][Flip cover][Weather]The sound choose screen is very slow when with weather app open
                Log.i(TAG, "onActivityResult ACTIVITY_BACK");
                isNeedPre = true;
                //[BUGFIX]-Add-END by TSCD.qian-li
                break;
            case ACTIVITY_REFRESH:
                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,03/04/2016,1719109,[Performance][Flip cover][Weather]The sound choose screen is very slow when with weather app open
                Log.i(TAG, "onActivityResult ACTIVITY_REFRESH");
                isNeedPre = true;
                //[BUGFIX]-Add-END by TSCD.qian-li

                if (null != data) {
                    mTempKey = data.getStringExtra("newCityKey");
                }
                mScrollPosition = 0;
                initViewPager(false);
                break;
            case SETTING_RESULT:
                Log.i(TAG, "onActivityResult SETTING_RESULT");
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                String autoUpdate = settings.getString(AUTO_UPDATE_KEY, "0");
                //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/27/2015,983895,delete weather_24forecast_card
                if (isTwcweather) {
                    showAdvData = settings.getBoolean(ADVANCE_DATA_KEY, false);
                /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
                } else {
                    showAdvData = false;
                    /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
                }
                //[BUGFIX]-Add-BEGIN by TSCD.peng.du,12/29/2015,1239853,
                // [weather]the temperature of app is different with weather unit
                showFeelsLike = false;//settings.getBoolean(SHOW_FEELS_LIKE_KEY, true);
                //[FEATURE]-Add-END by TSCD.peng.du
                //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,12/25/2015,1173717,[Weather] Some content in setting screen does not match ergo
                String tempUnit = settings.getString(TEMP_KEY, "1");
                String distanceUnit = settings.getString(DISTANCE_KEY, "1");
                if (TextUtils.equals(tempUnit, "1")) {
                    isUnitC = true;
                } else {
                    isUnitC = false;
                }
                if (TextUtils.equals(distanceUnit, "1")) {
                    isUnitKm = true;
                } else {
                    isUnitKm = false;
                }
                //[BUGFIX]-Add-END by TSCD.xing.zhao
                updateWeatherView();
                break;
        }
    }
    // add by jielong.xing for pr928049、927559 at 2015-2-11 end

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.menu_addlocation:
                if (mCityList.size() >= cityLimit) {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.citys_max_full),
                            Toast.LENGTH_LONG).show();
                } else {
                    // add by jielong.xing for pr928049、927559 at 2015-2-11 begin
                    Intent locateActivity = new Intent(MainActivity.this, LocateActivity.class);
                    locateActivity.putExtra("needBackToMainScreen", true);
                    // Fixed PR974952,974932,958494 by jielong.xing at 2015-4-15 begin
                    //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,10/24/2015,9992933,[Android6.0][Weather_v5.2.8.1.0305.0]The locate button has no use after delete all cities
                    //startActivityForResult(locateActivity, 0, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                    startActivityForResult(locateActivity, 0);
                    //[BUGFIX]-Add-END by TSCD.xing.zhao
                    // Fixed PR974952,974932,958494 by jielong.xing at 2015-4-15 end

                    // add by jielong.xing for pr928049、927559 at 2015-2-11 end
                }

                if (popupMenuLayout != null) {
                    popupMenuLayout.setVisibility(View.GONE);
                }

                break;
            case R.id.menu_deletelocation:
                //add by jiajun.shen for 1094136
                if (mCityList != null && mCityList.size() > 0) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                    updateService.deleteCity(mCityList.get(mPosition).getLocationKey());
                }
                //added by tingma at 2015-03-16 begin
                if (null != mCityList) {
                    if (mPosition + 1 >= mCityList.size()) {
                        if (mCityList.size() != 0) {
                            mTempKey = mCityList.get(0).getLocationKey();
                        }
                    } else {
                        mTempKey = mCityList.get(mPosition + 1).getLocationKey();
                    }
                } else {
                    mTempKey = null;
                }
                //added by tingma at 2015-03-16 end

                if (checkDataBase()) {

                    //deleted by tingma at 2015-03-16 begin
                /*if (mPosition >= mCityList.size()) {
                    if (mCityList.size() != 0) {
						mTempKey = mCityList.get(0).getLocationKey();
					}
				} else {
					mTempKey = mCityList.get(mPosition).getLocationKey();
				}*/
                    //deleted by tingma at 2015-03-16 end
                    //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,01/12/2016,1391550,[GAPP][Android6.0][Weather]The weather card is in different level after remove city.
                    mScrollPosition = 0;
                    //[BUGFIX]-Add-END by TSCD.xing.zhao
                    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,03/04/2016,1719109,[Performance][Flip cover][Weather]The sound choose screen is very slow when with weather app open
                    if (isBackgoundDynamic) {
                        mMediaplayer.reset();
                    }
                    initViewPager(true);
                    //[BUGFIX]-Add-END by TSCD.qian-li
                }

                if (popupMenuLayout != null) {
                    popupMenuLayout.setVisibility(View.GONE);
                }

                break;
            case R.id.menu_changeunit:
                if (mCityList == null || mCityList.size() == 0) {
                    return;
                }
                isUnitC = !isUnitC;

                Editor sharedata = getSharedPreferences("weather", Context.MODE_WORLD_READABLE).edit();
                sharedata.putBoolean("unit", isUnitC);
                sharedata.commit();

                Intent it = new Intent("android.intent.action.UNIT_BROADCAST");
                it.putExtra("isUnitC", isUnitC);
                sendBroadcast(it);

//			mTempKey = mCityList.get(mPosition).getLocationKey();
//			initViewPager();
                updateWeatherViewByTemperatureUnit(isUnitC);
                if (popupMenuLayout != null) {
                    popupMenuLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.menu_change_km_mi: {
                if (mCityList == null || mCityList.size() == 0) {
                    return;
                }
                isUnitKm = !isUnitKm;
                Editor editor = getSharedPreferences("weather", Context.MODE_WORLD_READABLE).edit();
                editor.putBoolean("unitKm", isUnitKm);
                editor.commit();

//			initViewPager();
                updateWeatherViewBySpeechUnit(isUnitKm);
                if (popupMenuLayout != null) {
                    popupMenuLayout.setVisibility(View.GONE);
                }
                break;
            }
            case R.id.menu_refresh: {
                if (getConnectedStatus()) {
                    Log.d("xjl_test", "menuClick isUpdating = " + isUpdating + ", isRefreshing = " + mSwipeRefreshLayout.isRefreshing());
                    if (!isUpdating) {
                        Log.d("xjl_test", "menuClick start");
                        isUpdating = true;
                        mSwipeRefreshLayout.setRefreshing(true);

                        updateService.setUpdateManue();
                        //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/23/2016,1443957,[Weather]It can't automatically recognize new site when move to new site
                        updateService.updateAllWeatherWithAutoLocation();
                        //[BUGFIX]-Add-END by TSCD.qian-li
                    }
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.locate_connect_error),
                            Toast.LENGTH_LONG).show();
                }
                if (popupMenuLayout != null) {
                    popupMenuLayout.setVisibility(View.GONE);
                }
                break;
            }
            case R.id.menu_setting: {
                Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
//                int[] location=new int[2];
//                location[0]=view.getRight();
//                location[1]=view.getTop();
//                settingIntent.putExtra("location",location);
                startActivityForResult(settingIntent, 0);
                if (popupMenuLayout != null) {
                    popupMenuLayout.setVisibility(View.GONE);
                }
                break;
            }
            default:
                break;
        }
    }

    private void updateWeatherView() {
        int cnt = 0;
        if (mViews != null && mViews.size() > 0) {
            cnt = mViews.size();
        }

        for (int i = 0; i < cnt; i++) {
            City city = mCityList.get(i);
            long time = System.currentTimeMillis();
            try {
                time = Long.parseLong(city.getUpdateTime());
            } catch (Exception e) {
            }
            String newUpdateTime = formatTime(time);
            FrameLayout var = mViews.get(i);
            VerticalSlidingView slidingView = (VerticalSlidingView) var.findViewById(R.id.slidingView);
            WeatherForShow weather = mSlidingViewWeatherMap.get(i);
            List<DayForShow> dayList = mSlidingViewDayMap.get(i);
            List<Hour> hourList = mSlidingViewHourMap.get(i);
            if (weather == null || dayList == null) {
                updateService.deleteCity(city.getLocationKey());
                mCityList.clear();
                initViewPager(true);
                return;
            }

            int[] colors = mSlidingViewColorMap.get(i);
            textColor = colors[0];
            backgroundColor = colors[1];

            slidingView.setWeatherData(weather, isUnitC, newUpdateTime, city.isAutoLocate(), isUnitKm, showAdvData, showFeelsLike);
            slidingView.setDayData(dayList, isUnitC);
            //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/27/2015,983895,delete weather_24forecast_card
            if (isTwcweather) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                slidingView.setHourlyData(hourList, isUnitC, textColor);
            }
            //[FEATURE]-Add-END by TSCD.peng.du

            slidingView.setDayColor(backgroundColor);
            slidingView.setWeatherColor(textColor);
        }
    }

    private void updateWeatherViewByTemperatureUnit(boolean isUnitC) {
        int cnt = 0;
        if (mViews != null && mViews.size() > 0) {
            cnt = mViews.size();
        }

        for (int i = 0; i < cnt; i++) {
            City city = mCityList.get(i);
            long time = System.currentTimeMillis();
            try {
                time = Long.parseLong(city.getUpdateTime());
            } catch (Exception e) {
            }
            String newUpdateTime = formatTime(time);
            FrameLayout var = mViews.get(i);
            VerticalSlidingView slidingView = (VerticalSlidingView) var.findViewById(R.id.slidingView);
            WeatherForShow weather = mSlidingViewWeatherMap.get(i);
            List<DayForShow> dayList = mSlidingViewDayMap.get(i);
            List<Hour> hourList = mSlidingViewHourMap.get(i);
            int[] colors = mSlidingViewColorMap.get(i);
            textColor = colors[0];
            backgroundColor = colors[1];

            slidingView.setWeatherData(weather, isUnitC, newUpdateTime, city.isAutoLocate(), isUnitKm, showAdvData, showFeelsLike);
            slidingView.setDayData(dayList, isUnitC);
            //[FEATURE]-Add-BEGIN by TSCD.peng.du,11/27/2015,983895,deleta weather_24forecast_card
            /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
            if (isTwcweather) {
                slidingView.setHourlyData(hourList, isUnitC, textColor);
            }
            //[FEATURE]-Add-END by TSCD.peng.du
            /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
            slidingView.setDayColor(backgroundColor);
            slidingView.setWeatherColor(textColor);
        }
    }

    private void updateWeatherViewBySpeechUnit(boolean isUnitKm) {
        int cnt = 0;
        if (mViews != null && mViews.size() > 0) {
            cnt = mViews.size();
        }

        for (int i = 0; i < cnt; i++) {
            City city = mCityList.get(i);
            long time = System.currentTimeMillis();
            try {
                time = Long.parseLong(city.getUpdateTime());
            } catch (Exception e) {
            }
            String newUpdateTime = formatTime(time);
            FrameLayout var = mViews.get(i);

            VerticalSlidingView slidingView = (VerticalSlidingView) var.findViewById(R.id.slidingView);
            WeatherForShow weather = mSlidingViewWeatherMap.get(i);
            slidingView.setWeatherData(weather, isUnitC, newUpdateTime, city.isAutoLocate(), isUnitKm, showAdvData, showFeelsLike);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();

            if (null != popupMenuLayout && popupMenuLayout.getVisibility() == View.VISIBLE) {
                Rect hitRect = new Rect();
                popupMenuLayout.getGlobalVisibleRect(hitRect);
                if (!hitRect.contains(x, y)) {
                    popupMenuLayout.setVisibility(View.GONE);
                    return true;
                }
            }
        }
        //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,01/13/2016,1284454,[Weather][Force Close]It pop up FC ,When refresh weather comming phone
        try {
            return super.dispatchTouchEvent(ev);
        } catch (IndexOutOfBoundsException e) {
            /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
            Log.i(TAG, "IndexOutOfBoundsException " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "IllegalArgumentException " + e.getMessage());
            return false;
        } catch (Exception e) {
        /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
            Log.i(TAG, e.getMessage());
            return false;
        }
        //[BUGFIX]-Add-END by TSCD.xing.zhao
    }

    private String formatTime(long time) {
        String language = getResources().getConfiguration().locale.getCountry();
        boolean isJapanese = ("jp".equals(language.toLowerCase()));//add by jiajun.shen for 1016688 at 2015.6.5
        String newUpdateTime = "";
        SimpleDateFormat format12 = new SimpleDateFormat("hh:mmaa");
        SimpleDateFormat format24 = new SimpleDateFormat("HH:mm");
        SimpleDateFormat weekFormat = new SimpleDateFormat("EEE,");
        SimpleDateFormat jaWeekFormat = new SimpleDateFormat("(EEE)");

        java.text.DateFormat format = null;
        boolean isGetLongDateFormat = isGetLongDateFormat();
        if (isGetLongDateFormat) {
            format = android.text.format.DateFormat.getLongDateFormat(this);
        } else {
            format = DateFormat.getDateFormat(this);
        }

        java.util.Calendar currentTime = java.util.Calendar.getInstance();
        currentTime.setTimeInMillis(time);
        if (isGetLongDateFormat) {
            if (DateFormat.is24HourFormat(this)) {
                newUpdateTime = format24.format(time) + " " + format.format(currentTime.getTime());
            } else {
                newUpdateTime = format12.format(time) + " " + format.format(currentTime.getTime());
            }
        } else {
            if (DateFormat.is24HourFormat(this)) {
                //add by jiajun.shen for 1016688 at 2015.6.5 start
                if (isJapanese) {
                    newUpdateTime = format24.format(time) + " " + format.format(currentTime.getTime()) + " " + jaWeekFormat.format(time);
                } else {
                    newUpdateTime = format24.format(time) + " " + weekFormat.format(time) + " " + format.format(currentTime.getTime());
                }
            } else {
                if (isJapanese) {
                    newUpdateTime = format12.format(time) + " " + format.format(currentTime.getTime()) + " " + jaWeekFormat.format(time);
                } else {
                    newUpdateTime = format12.format(time) + " " + weekFormat.format(time) + " " + format.format(currentTime.getTime());
                }
            }
            //add by jiajun.shen for 1016688 at 2015.6.5 end
        }

        return newUpdateTime;
    }

    private boolean isGetLongDateFormat() {
        return CustomizeUtils.getBoolean(MainActivity.this, "def_weather_dateformat_long");
    }

    //Begin add by jiajun.shen for 1010042
    private String getRefreshTime(long tempTime) {
        // [BUGFIX]-Add-BEGIN by qian-li,11/03/2015,702164,[Android5.1][Weather_v5.1.3.4.0303.0]
        // The weather update time is overlap
        long SECOND = 1000L;
        long MINUTE = 60 * 1000L;
        long HOUR = 60 * 60 * 1000L;
        long DAY = 24 * 60 * 60 * 1000L;
        long WEEK = 7 * 24 * 60 * 60 * 1000L;
        long MONTH = 4 * 7 * 24 * 60 * 60 * 1000L;
        if (tempTime > MONTH) {
            int months = (int) (tempTime / MONTH);
            if (months > 1) {
                return getResources().getQuantityString(R.plurals.months_ago, months, months);
            } else {
                return getResources().getQuantityString(R.plurals.months_ago, 1, 1);
            }
        } else if (tempTime > WEEK) {
//            return formatTime(time);
            int weeks = (int) (tempTime / WEEK);
            if (weeks > 1) {
                return getResources().getQuantityString(R.plurals.weeks_ago, weeks, weeks);
            } else {
                return getResources().getQuantityString(R.plurals.weeks_ago, 1, 1);
            }
            // [BUGFIX]-Add-END by qian-li
        } else if (tempTime > DAY) {
            int days = (int) (tempTime / DAY);
            if (days > 1) {
                return getResources().getQuantityString(R.plurals.days_ago, days, days);
            } else {
                return getResources().getQuantityString(R.plurals.days_ago, 1, 1);
            }
        } else if (tempTime > HOUR) {
            int hours = (int) (tempTime / HOUR);
            if (hours > 1) {
                return getResources().getQuantityString(R.plurals.hours_ago, hours, hours);
            } else {
                return getResources().getQuantityString(R.plurals.hours_ago, 1, 1);
            }
        } else if (tempTime > MINUTE) {
            int minutes = (int) (tempTime / MINUTE);
            if (minutes > 1) {
                return getResources().getQuantityString(R.plurals.minutes_ago, minutes, minutes);
            } else {
                return getResources().getQuantityString(R.plurals.minutes_ago, 1, 1);
            }
        } else {
            // [BUGFIX]-Add-BEGIN by qian-li,11/03/2015,702201,[Android5.1][Weather_v5.1.3.4.0303.0]
            // It should display just now when the update time is less than 60s
            return getResources().getString(R.string.just_now);
//            int seconds = (int) (tempTime / SECOND);
//            if (seconds > 1) {
//                return getResources().getQuantityString(R.plurals.seconds_ago, seconds, seconds);
//            } else {
//                return getResources().getQuantityString(R.plurals.seconds_ago, 1, 1);
//            }
            // [BUGFIX]-Add-END by qian-li
        }
    }
    //End add by jiajun.shen for 1010042

    //add by jiajun.shen for 1031027
    private void setRefreshTimeAt(int position) {
        City city = mCityList.get(position);
        long time1, time2;
        time1 = time2 = System.currentTimeMillis();
        try {
            time1 = Long.parseLong(city.getUpdateTime());
        } catch (Exception e) {
            Log.e(TAG, "get city update time exception : " + e.getMessage());
        }
        long tempTime = time2 - time1;
        String newUpdateTime = getRefreshTime(tempTime);
        mTvRefreshTime.setText(newUpdateTime);
    }

    private void setCurrentRefreshTime() {
        if (mCityList != null && mCityList.size() > 0) {
            int position = 0;
            if (null == mTempKey || mTempKey.equals("")) {
                SharedPreferences pref = getSharedPreferences("weather", Context.MODE_WORLD_READABLE);
                String cityKey = pref.getString("currentcity", null);
                if (null != cityKey && !"".equals(cityKey)) {
                    mTempKey = cityKey;
//                    position = updateService.getCurrentPosition(mCityList, cityKey);
                    position = getPosition(mCityList, cityKey);
                    setRefreshTimeAt(position);
                } else {
                    setRefreshTimeAt(0);
                }
            } else {
//                position = updateService.getCurrentPosition(mCityList, mTempKey);
                position = getPosition(mCityList, mTempKey);
                setRefreshTimeAt(position);
            }
        }
    }
    //add by jiajun.shen for 1031027

    private int getPosition(ArrayList<City> cityList, String tempKey) {
        try {
            for (int i = 0; i < cityList.size(); i++) {
                if (tempKey.equals(cityList.get(i).getLocationKey())) {
                    return i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    //Begin added by jiajun.shen for blur effect at 2015.7.10
    class CreateBlurThread extends Thread {
        int resid;

        public CreateBlurThread(int res) {
            resid = res;
        }

        @Override
        public void run() {
            File blurredImage = new File(getFilesDir() + (resid + ".png"));
            if (blurredImage.exists()) {
                return;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap image = BitmapFactory.decodeResource(getResources(), resid, options);
            Bitmap newImg = Blur.fastblur(MainActivity.this, image, 24);
            storeImage(newImg, blurredImage);
        }
    }

    private Bitmap getBitmap(int resid) {
        Bitmap bitmap = BitmapFactory.decodeFile(getFilesDir() + (resid + ".png"));
        if (bitmap != null) {
            return bitmap;
        } else {
            return BitmapFactory.decodeResource(getResources(), R.drawable.weather_bg_0);
        }
    }

    private void storeImage(Bitmap image, File pictureFile) {
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return;
        }
        try {
            if (pictureFile.exists()) {
                return;
            }
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            mHandler.sendEmptyMessage(STORE_IMG_DONE);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private void setFadeAnimation(float fromPox, final float toPox, int duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromPox, toPox);
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                //mImgFgBlur.setImageAlpha((int) (value*255));
                if (!isBackgoundDynamic) {
                    if (isBgShow1) {
                        mImgBgBlur1.setImageAlpha((int) (value * 255));
                    } else {
                        mImgBgBlur2.setImageAlpha((int) (value * 255));
                    }
                } else {
                    mIvSnapShot.setImageAlpha((int) (value * 255));
                }
                //[BUGFIX]-Add-BEGIN by TSCD.xiangnan.zhou,01/15/2016,1430537,
                // [Weather]The menu button dotted grey but can still be opened.
                //mIvMenu.setImageAlpha((int) ((1 - value) * 255));
                //[BUGFIX]-Add-END by TSCD.xiangnan.zhou
                if (value == toPox) {
                    mLastScrollPercent = toPox;
                    if (isBackgoundDynamic) {
                        //mIvSnapShot.setVisibility(View.GONE);
                        mIvSnapShot.setImageBitmap(null);
                    }
                }
            }
        });
    }
    //End added by jiajun.shen for blur effect at 2015.7.10

    //[BUGFIX]-Add-BEGIN by xinlei.sheng, 2016/01/21,1442774
    private void setFadeAnimationForStaticBg(float fromPox, final float toPox, int duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromPox, toPox);
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                if (!isBackgoundDynamic) {
                    if (isBgShow1) {
                        mImgBgBlur1.setImageAlpha((int) (value * 255));
                    } else {
                        mImgBgBlur2.setImageAlpha((int) (value * 255));
                    }
                }
                if (value == toPox) {
                    mLastScrollPercent = toPox;
                }
            }
        });
    }
    //[BUGFIX]-Add-END by xinlei.sheng, 2016/01/21,1442774

    public int[] getRightColor(int currentBg) {
        int textColor = SharePreferenceUtils.getInstance().getInt(MainActivity.this, currentBg + "text", 0);
        int backgroundColor = SharePreferenceUtils.getInstance().getInt(MainActivity.this, currentBg + "background", 0);
        int[] colors = new int[2];
        if (textColor == 0 || backgroundColor == 0) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), currentBg);
            //Palette.Builder builder = new Palette.Builder(bitmap);
            Palette palette = Palette.generate(bitmap);
            Palette.Swatch swatch = palette.getDarkVibrantSwatch();
            if (swatch != null) {
                textColor = swatch.getTitleTextColor();
                backgroundColor = palette.getDarkVibrantColor(swatch.getRgb());
                SharePreferenceUtils.getInstance().saveInt(MainActivity.this, currentBg + "text", textColor);
                SharePreferenceUtils.getInstance().saveInt(MainActivity.this, currentBg + "background", backgroundColor);
                colors[0] = textColor;
                colors[1] = backgroundColor;
            } else {
                textColor = getResources().getColor(R.color.text_color);
                backgroundColor = getResources().getColor(R.color.background_color);
                SharePreferenceUtils.getInstance().saveInt(MainActivity.this, currentBg + "text", textColor);
                SharePreferenceUtils.getInstance().saveInt(MainActivity.this, currentBg + "background", backgroundColor);
                colors[0] = textColor;
                colors[1] = backgroundColor;
            }
        } else {
            colors[0] = textColor;
            colors[1] = backgroundColor;
        }
        return colors;
    }

    public int[] getColorFromBackground(int currentBg) {
        int[] colors = new int[2];
        int textColor;
        int backgroundColor;
        switch (currentBg) {
            case R.drawable.bg_sunny:
                textColor = getResources().getColor(R.color.text_color_purple);
                backgroundColor = getResources().getColor(R.color.background_sunny);
                break;
            case R.drawable.bg_cloudy:
                textColor = getResources().getColor(R.color.text_color_purple);
                backgroundColor = getResources().getColor(R.color.background_cloundy);
                break;
            case R.drawable.bg_fog:
                textColor = getResources().getColor(R.color.text_color_black);
                backgroundColor = getResources().getColor(R.color.background_fog);
                break;
            case R.drawable.bg_frost:
                textColor = getResources().getColor(R.color.text_color_black);
                backgroundColor = getResources().getColor(R.color.background_frost);
                break;
            case R.drawable.bg_rainy:
                textColor = getResources().getColor(R.color.text_color_black);
                backgroundColor = getResources().getColor(R.color.background_rainny);
                break;
            case R.drawable.bg_snow:
                textColor = getResources().getColor(R.color.text_color_black);
                backgroundColor = getResources().getColor(R.color.background_snow);
                break;
            case R.drawable.bg_storm:
                textColor = getResources().getColor(R.color.text_color_black);
                backgroundColor = getResources().getColor(R.color.background_storm);
                break;
            case R.drawable.bg_clear:
                textColor = getResources().getColor(R.color.text_color_purple);
                backgroundColor = getResources().getColor(R.color.background_clear);
                break;
            default:
                textColor = getResources().getColor(R.color.text_color_black);
                backgroundColor = getResources().getColor(R.color.background_fog);
                break;
        }
        colors[0] = textColor;
        colors[1] = backgroundColor;
        return colors;
    }

    private Uri getCurrentBgUri(String iconId) {
        //Log.i(TAG, "getCurrentBackgroundWeatherIcon iconId == " + iconId);
        // Fixed PR965957 by jielong.xing at 2015-4-2 begin
        try {
            Integer icon = Integer.parseInt(iconId);
            if (icon > 0) {
                if (isTwcweather) {
                    if (IconBackgroundUtil.TWCBackGroundUriArray.containsKey(icon)) {
                        Uri resId = IconBackgroundUtil.TWCBackGroundUriArray.get(icon);

                        return resId;
                    } else {
                        Uri videoUri = Uri.parse("android.resource://"
                                + getPackageName() + "/"
                                + R.raw.am_storm);
                        return videoUri;
                    }
                } else {
                    if (IconBackgroundUtil.ACCBackGroundUriArray.containsKey(icon)) {
                        Uri resId = IconBackgroundUtil.ACCBackGroundUriArray.get(icon);

                        return resId;
                    } else {
                        Uri videoUri = Uri.parse("android.resource://"
                                + getPackageName() + "/"
                                + R.raw.am_storm);
                        return videoUri;
                    }
                }

            }
        } catch (Exception e) {
            Uri videoUri = Uri.parse("android.resource://"
                    + getPackageName() + "/"
                    + R.raw.am_sunny);
            return videoUri;
        }
        // Fixed PR965957 by jielong.xing at 2015-4-2 end
        Uri videoUri = Uri.parse("android.resource://"
                + getPackageName() + "/"
                + R.raw.am_sunny);
        return videoUri;
    }

    private int getCurrentBackgroundWeatherIcon(String iconId) {
        //Log.i(TAG, "getCurrentBackgroundWeatherIcon iconId == " + iconId);
        // Fixed PR965957 by jielong.xing at 2015-4-2 begin
        try {
            Integer icon = Integer.parseInt(iconId);
            if (icon > 0) {
                if (isTwcweather) {
                    if (IconBackgroundUtil.TWCBackGroundArray.containsKey(icon)) {
                        int resId = IconBackgroundUtil.TWCBackGroundArray.get(icon);
                        if (!isBackgoundDynamic) {
                            CreateBlurThread createBlurThread = new CreateBlurThread(resId);
                            createBlurThread.start();
                        }
                        return resId;
                    } else {
                        if (!isBackgoundDynamic) {
                            CreateBlurThread createBlurThread = new CreateBlurThread(R.drawable.bg_sunny);
                            createBlurThread.start();
                        }
                        return R.drawable.bg_sunny;
                    }
                } else {
                    if (IconBackgroundUtil.ACCBackGroundArray.containsKey(icon)) {
                        int resId = IconBackgroundUtil.ACCBackGroundArray.get(icon);
                        if (!isBackgoundDynamic) {
                            CreateBlurThread createBlurThread = new CreateBlurThread(resId);
                            createBlurThread.start();
                        }
                        return resId;
                    } else {
                        if (!isBackgoundDynamic) {
                            CreateBlurThread createBlurThread = new CreateBlurThread(R.drawable.bg_sunny);
                            createBlurThread.start();
                        }
                        return R.drawable.bg_sunny;
                    }
                }

            }
        } catch (Exception e) {
            if (!isBackgoundDynamic) {
                CreateBlurThread createBlurThread = new CreateBlurThread(R.drawable.bg_sunny);
                createBlurThread.start();
            }
            return R.drawable.bg_sunny;
        }
        // Fixed PR965957 by jielong.xing at 2015-4-2 end
        if (!isBackgoundDynamic) {
            CreateBlurThread createBlurThread = new CreateBlurThread(R.drawable.bg_sunny);
            createBlurThread.start();
        }
        return R.drawable.bg_sunny;
    }

    //[BUGFIX]-Add-BEGIN by TSCD.jian.xu,12/11/2015,1058845,[Weather] When tap weather apk, It pop up fc
    private void asyncResetplayer() {
        final AsyncTask<Void, Void, Void> resetTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... parameters) {
                if (null != mMediaplayer) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                    try {
//                        mMediaplayer.reset();
                        Log.i(TAG, "onResume mediaplayer.setDataSource");
                        mMediaplayer.setDataSource(MainActivity.this, mLastBgUri);
                        Log.i(TAG, "onResume mediaplayer.prepareAsync");
                        mMediaplayer.prepareAsync();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }
                }
                return null;
            }
        };
        resetTask.execute();
    }

    //[BUGFIX]-Add-END by TSCD.jian.xu
    private boolean isBackGroundDynamic() {
        return CustomizeUtils.getBoolean(MainActivity.this, "is_background_dynamic");
    }
}
