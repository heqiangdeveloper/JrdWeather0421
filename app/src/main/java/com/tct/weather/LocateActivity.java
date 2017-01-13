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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/weather/LocateActivity.java   */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*BUGFIX-1167118 2015/12/16       lin-zhou      [Android6.0][Weather_v5.2.8.1.0301.0]After give the permission,weather didn't locate auto*/
/*BUGFIX-1175818 2015/12/31       xing.zhao     [Weather]Weather APK  can't  auto match city by zip code*/
/*BUGFIX-1470934 2016/1/20       xing.zhao     [Weather]MiddleMan Runtime permission Phone group*/
/*===============|==============|===============|==================================================*/

package com.tct.weather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/* MODIFIED-BEGIN by peng.du, 2016-03-22,BUG-1842312 */
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/* MODIFIED-END by peng.du,BUG-1842312 */

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.tct.weather.bean.City;
import com.tct.weather.service.UpdateService;
import com.tct.weather.util.CommonUtils;
import com.tct.weather.util.CustomizeUtils;
import com.tct.weather.util.SharePreferenceUtils;
import com.tct.weather.view.MySearchView;

public class LocateActivity extends AppCompatActivity implements BDLocationListener {
    private static final String TAG = "weather LocateActivity";
    // add by jielong.xing for PR928049、927559 at 2015-2-11 begin
    private static final int ACTIVITY_BACK = 0x1000;
    private static final int ACTIVITY_REFRESH = 0x1001;
    // add by jielong.xing for PR928049、927559 at 2015-2-11 end

    private final String AUTO_UPDATE_KEY = "settings_auto_update";
    private final String TEMP_KEY = "settings_temp";
    private final String DISTANCE_KEY = "settings_distance";
    private final String ADVANCE_DATA_KEY = "settings_advance_data";
    private final String SHOW_FEELS_LIKE_KEY = "settings_feel_like";

    private final int PERMISSION_REQUEST_LOCATION = 0x001;

    private View locationLayout;
    private ImageButton bt_locate;
    private UpdateService updateService;
    private boolean isWifiConnected;
    private boolean isMobileConnected;
    private MyBroadcasReceiver mBroadcastReceiver;
    private LocationManager mLocationManager;
    private ProgressDialog pDialog;
    private List<City> mCitys;
    //added for CR 1002210 by tingma at 2015-05-30 begin
    //private Button bt_connect;
    //private SearchView mSearchView;
    //[BUGFIX]-Add-BEGIN by TSCD.peng.du,12/09/2015,1070346,
    //[Android6.0][Weather_v5.2.8.1.0301.0]It display "x" when has nothing in search box
//    private EditText mSearchViewText;
    private MySearchView mSearchViewText;
    //[BUGFIX]-Add-END by TSCD.peng.du
    private ImageView mBackImage;
    //added for CR 1002210 by tingma at 2015-05-30 ends
    private boolean isFirstUse = true;
    //private LinearLayout layout_main, layout_connect;
    private ListView mCityList;
    private View mListFootView = null;
    private View mListHeadView = null;
    private Location mLocation;
    private boolean mPausing = false;

    private LocationListener mGpsListener = null;
    private LocationListener mNetworkListener = null;
    private Location mGpsLocation = null;
    private Location mNetworkLocation = null;

    private boolean mAutoLocateSuccess = false;

    private static final int MSGTIMEOUT = 0x10001; // retry 10 times all failed,then send timeout  message
    private static final int MSGREGETPOSITION = 0x10002; // retry message
    private static final int MSGCONNECTFAILED = 0x10003;

    private boolean isOtherConnected;

    private String mSearchCity;

    private boolean isBindService = false;
    // add by jielong.xing for PR928049、927559 at 2015-2-11 begin
    private boolean needBackToMainScreen = false;
    // add by jielong.xing for PR928049、927559 at 2015-2-11 end

    //Defect 212555 Outdoor auto location is failed by bing.wang.hz begin
    private boolean mIsQcomPlatform = true;
    private boolean mIsFirstLocationTry = true;
    //Defect 212555 Outdoor auto location is failed by bing.wang.hz end

    //added for CR 1002210 by tingma at 2015-05-30 begin
    private boolean hasNewText = true;
    //added for CR 1002210 by tingma at 2015-05-30 end

    private boolean isMiddleManAvavible = false;

    private boolean isUseBaiduAsDefault = false;

    private Intent mPausingIntent = null;

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,12/30/2015,1210037,[Launcher][Lockscreen]The prompts of the "auto locate failure" will flash after unlock screen.
    private AlertDialog mDialog = null;
    //[BUGFIX]-Add-END by TSCD.qian-li
    //[BUGFIX]-Add-BEGIN by peng.du,2016/01/23,1478435
    private boolean isLocateTimeout = false;
    //[BUGFIX]-Add-END by peng.du,2016/01/23,1478435

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Fixed PR974952,974932,958494 by jielong.xing at 2015-4-15 begin
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        android.transition.Fade ts = new android.transition.Fade();
        ts.setDuration(1500);
        getWindow().setEnterTransition(ts);
        // Fixed PR974952,974932,958494 by jielong.xing at 2015-4-15 end
        super.onCreate(savedInstanceState);
        //Defect 212555 Outdoor auto location is failed by bing.wang.hz begin
        mIsQcomPlatform = CommonUtils.isQcomPlatform();
        mIsFirstLocationTry = SharePreferenceUtils.getInstance().getBoolean(this, CommonUtils.FIRST_LOCATION_TRY, true);
        //Defect 212555 Outdoor auto location is failed by bing.wang.hz end
        isMiddleManAvavible = CustomizeUtils.isMiddleManAvavible(this);
        if (!CommonUtils.isSupportHorizontal(this)) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        // add by jielong.xing for PR928049、927559 at 2015-2-11 begin
        Intent intent = getIntent();
        if (null != intent && intent.getExtras() != null) {
            needBackToMainScreen = intent.getBooleanExtra("needBackToMainScreen", false);
        }
        // add by jielong.xing for PR928049、927559 at 2015-2-11 end

        setContentView(R.layout.add_location);

        locationLayout = findViewById(R.id.location_activity_layout);
        //modified for CR 1002210 by tingma at 2015-05-30 begin
        /*Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.weather_logo);
		toolbar.setTitle(" " + getString(R.string.app_name));
		setSupportActionBar(toolbar);*/
        //[BUGFIX]-Add-BEGIN by TSCD.peng.du,12/09/2015,1070346
//        mSearchViewText = (EditText) findViewById(R.id.et_searchview);
        mSearchViewText = (MySearchView) findViewById(R.id.et_searchview);
        //[BUGFIX]-Add-END by TSCD.peng.du
        mSearchViewText.addTextChangedListener(new SearchViewTextWatcher());

        mBackImage = (ImageView) findViewById(R.id.img_back);
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mBackImage.setImageResource(R.drawable.back_arrow1);
        } else {
            mBackImage.setImageResource(R.drawable.back_arrow);
        }
        mBackImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (needBackToMainScreen) {
                    setResult(ACTIVITY_BACK);
                }
                finish();
            }
        });
        //modified for CR 1002210 by tingma at 2015-05-30 end

        bt_locate = (ImageButton) findViewById(R.id.locate_bt_auto);
        bt_locate.setTranslationZ(10);

        //modified for CR 1002210 by tingma at 2015-05-30 begin
        //bt_connect = (Button) findViewById(R.id.locate_connect);

        //bt_locate.setBackgroundResource(R.drawable.button_bg);
        //bt_connect.setBackgroundResource(R.drawable.button_bg);

        //layout_main = (LinearLayout) findViewById(R.id.locate_layout_main);
        //layout_connect = (LinearLayout) findViewById(R.id.locate_layout_connect);
        //modified for CR 1002210 by tingma at 2015-05-30 end

        bt_locate.setOnClickListener(locateListener);

		/*bt_connect.setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View v) {
				startActivity(new Intent(Settings.ACTION_SETTINGS));
			}
		});*/

        pDialog = new ProgressDialog(LocateActivity.this);

        mCitys = new ArrayList<City>();

        mListFootView = (View) findViewById(R.id.search_list_foot);
        mListHeadView = (View) findViewById(R.id.search_list_head);
        mListFootView.setVisibility(View.GONE);
        mListHeadView.setVisibility(View.GONE);

        mCityList = (ListView) findViewById(R.id.search_citylist);
        mCityList.setDividerHeight(0);
        mCityList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // Fixed PR1029747 by jielong.xing at 2015-6-25 begin
                if (mSearchResultCityList != null && mSearchResultCityList.size() > 0) {
                    if (arg2 < mSearchResultCityList.size()) {
                        City city = mSearchResultCityList.get(arg2);

                        updateService.setUpdateManue();
                        updateService.insertCity(city, false);//search current city with Url
                        SharePreferenceUtils.getInstance().checkCommonCity(LocateActivity.this, city.getLocationKey());

                        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pDialog.setCanceledOnTouchOutside(false);
                        pDialog.setMessage(getResources().getString(R.string.loading));
                        pDialog.show();
                    }
                }
                // Fixed PR1029747 by jielong.xing at 2015-6-25 end
            }
        });

        //added for CR 1002210 by tingma at 2015-05-30 begin
        mCityList.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/07/2015,1251830,[Android 6.0][weather][Monkey]CRASH: com.tct.weather
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    View mView = LocateActivity.this.getCurrentFocus();
                    if (mView != null) {
                        ((InputMethodManager) LocateActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE)).
                                hideSoftInputFromWindow(mView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
//                ((InputMethodManager) LocateActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE)).
//                        hideSoftInputFromWindow(LocateActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                //[BUGFIX]-Add-END by TSCD.qian-li
                return false;
            }
        });
        //added for CR 1002210 by tingma at 2015-05-30 end

        mResultAdapter = new SearchResultItemAdapter(LocateActivity.this, mSearchResultList, mSearchCity);
        mCityList.setAdapter(mResultAdapter);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mGpsListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e(TAG, "GPS____onLocationChanged Latitude = "
                        + location.getLatitude() + "Longitude = "
                        + location.getLongitude());
                if (location != null) {
                    mGpsLocation = location;
                    mAutoLocateSuccess = true;
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        mNetworkListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e(TAG, "Network____onLocationChanged Latitude = "
                        + location.getLatitude() + "Longitude = "
                        + location.getLongitude());
                if (location != null) {
                    mNetworkLocation = location;
                    mAutoLocateSuccess = true;
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        
        isUseBaiduAsDefault = CustomizeUtils.isUseBaiDuLocation(LocateActivity.this, isMiddleManAvavible);
        
    }

    // dismiss the progress dlg if activity not pausing,else
    // finish the activity,locate activity do not need to keep activity on
    // background
    private void disMissProgressDlgOrFinish() {
        //[BUGFIX]-Mod-BEGIN by peng.du,2016/01/23,1478435
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
        //[BUGFIX]-Mod-END by peng.du,2016/01/23,1478435
    }

    //added for CR 1002210 by tingma at 2015-05-30 begin
    private class SearchViewTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence newText, int start, int before,
                                  int count) {
            if (isNetworkConnected) {
                if (newText.length() != 0) {
                    hasNewText = true;
                    //[BUGFIX]-Add-BEGIN by TSCD.peng.du,12/09/2015,1070346,
                    //[Android6.0][Weather_v5.2.8.1.0301.0]It display "x" when has nothing in search box
                    mSearchViewText.setClearIconVisible(true);
                    //[BUGFIX]-Add-END by TSCD.peng.du
                    String cityName = mSearchCity = newText.toString();
                    // Fixed PR1022056 by jielong.xing at 2015-6-18 begin
                    mSearchHandler.removeMessages(0);
                    Message msg = mSearchHandler.obtainMessage();
                    msg.obj = cityName;
                    msg.what = 0;
                    mSearchHandler.sendMessageDelayed(msg, 300);
                    // Fixed PR1022056 by jielong.xing at 2015-6-18 end
                } else {
                    hasNewText = false;
                    //[BUGFIX]-Add-BEGIN by TSCD.peng.du,12/09/2015,1070346,
                    //[Android6.0][Weather_v5.2.8.1.0301.0]It display "x" when has nothing in search box
                    mSearchViewText.setClearIconVisible(false);
                    //[BUGFIX]-Add-END by TSCD.peng.du
                    bt_locate.setTranslationZ(10);
                    // Fixed PR1022056 by jielong.xing at 2015-6-18 begin
                    mSearchHandler.removeMessages(0);
                    mSearchResultList.clear();
                    mResultAdapter.notifyDataSetChanged();
                    // Fixed PR1022056 by jielong.xing at 2015-6-18 end
                    mCityList.setVisibility(View.GONE);
                    mListFootView.setVisibility(View.GONE);
                    mListHeadView.setVisibility(View.GONE);
                }
            } else {
                if (!mPausing) {
                    Toast.makeText(LocateActivity.this,
                            getResources().getString(R.string.locate_connect_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub

        }

    }

    // Fixed PR1022056 by jielong.xing at 2015-6-18 begin
    private String mReqId = null;
    private static final String RAND_ARRAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private String getReqId() {
        StringBuffer sb = new StringBuffer();
        int len = RAND_ARRAY.length();
        for (int i = 0; i < 11; i++) {
            java.util.Random rand = new java.util.Random();
            int r = rand.nextInt(len);
            sb.append(RAND_ARRAY.charAt(r));
        }
        return sb.toString();
    }

    private Handler mSearchHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            String cityName = (String) msg.obj;
            mReqId = getReqId();
            getCityListFromService(cityName, mReqId);
            super.handleMessage(msg);
        }

    };
    // Fixed PR1022056 by jielong.xing at 2015-6-18 end
    //added for CR 1002210 by tingma at 2015-05-30 end

    OnClickListener locateListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            updateConnectedFlags();

            // modify by junye.li for PR762484 begin
            if (isWifiConnected || isMobileConnected || isOtherConnected) {
                // modify by junye.li for PR762484 end
                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setMessage(getResources().getString(R.string.locating));
                pDialog.show();

                getLocation();
            } else {
                if (!mPausing) {
                    Toast.makeText(LocateActivity.this,
                            getResources().getString(R.string.locate_connect_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    // Fixed PR1022056 by jielong.xing at 2015-6-18, add param reqId
    private void getCityListFromService(String cityName, String reqId) {
        if (cityName != null && cityName.length() != 0) {
            if (updateService != null)//add by jiajun.shen for 1036657
            {
                //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,12/31/2015,1175818,[Weather]Weather APK  can't  auto match city by zip code
                boolean isPostal = isSearchPostal(cityName);//added by jiajun.shen for 1045406 // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                updateService.sendCityFindRequest(cityName, reqId, isPostal);
                //[BUGFIX]-Add-END by TSCD.xing.zhao
            }
        } else {
            Toast.makeText(LocateActivity.this,
                    getResources().getString(R.string.insert_location),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPausing = false;

        //add by jiajun.shen for 387613
        if (mBroadcastReceiver == null)
            registerBoradcastReceiver();

        //layout_main.setVisibility(View.VISIBLE);

        SharedPreferences sharedata = getSharedPreferences("firstuse", MODE_PRIVATE);
        isFirstUse = sharedata.getBoolean("firstUse", true);

        if (isFirstUse) {
            updateSettings();
        }

        isBindService = (mCityList != null && mCityList.getCount() == 0);
        if (isBindService) {
            Log.d(TAG, "bindservice");
            Intent intent = new Intent(LocateActivity.this, UpdateService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }

        if (mPausingIntent != null) {
            sendBroadcast(mPausingIntent);
            mPausingIntent = null;
        }

        //[BUGFIX]-Add-BEGIN by peng.du,2016/01/23,1478435
        if (isLocateTimeout) {
            isLocateTimeout = false;
            disMissProgressDlgOrFinish();
            if (!mPausing) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                Toast.makeText(LocateActivity.this,
                        getResources().getString(R.string.obtain_data_failed),
                        Toast.LENGTH_LONG).show();
            }
        }
        //[BUGFIX]-Add-END by peng.du,2016/01/23,1478435

		/*updateConnectedFlags();

		if (!isWifiConnected && !isMobileConnected && !isOtherConnected) {
			//layout_main.setVisibility(View.GONE);
			//layout_connect.setVisibility(View.VISIBLE);
			Toast.makeText(LocateActivity.this,
					getResources().getString(R.string.locate_connect_error),
					Toast.LENGTH_LONG).show();
		}*/
    }

    private void updateSettings() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = settings.edit();

        //[BUGFIX]-Add-BEGIN by TSCD.peng.du,03/30/2016,1872473,[PR cross check][PIXI345]][WEATHER]Default Units in Weather App should be in Fahrenheit and Miles, not Celsius and Kilometers
        String unit = CustomizeUtils.getString(LocateActivity.this, "def_weather_unit_name");
        unit = CustomizeUtils.splitQuotationMarks(unit);
        if (!unit.isEmpty() && !"isunitc".equals(unit.toLowerCase())) {
            // 0 : use unit C as default
            editor.putString("settings_temp", "0");
        } else {
            // 0 : use unit F as default
            editor.putString("settings_temp", "1");
        }

        String unitKm = CustomizeUtils.getString(LocateActivity.this, "def_weather_wind_visibility_unit_name");
        if (!unitKm.isEmpty() && !"km".equals(unitKm.toLowerCase())) {
            // 0 : use unit mi as default
            editor.putString("settings_distance", "0");
        } else {
            // 0 : use unit km as default
            editor.putString("settings_distance", "1");
        }
        editor.commit();
        //[BUGFIX]-Add-END by TSCD.peng.du
    }

    // the broadcast need to listen when onCreate and unregister in onDestroy,
    // to avoid when activity is pausing,then click add location,the activity
    // not
    // refreshed
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isUseBaiduAsDefault) {
            mLocationHandler.sendEmptyMessage(STOP_BAIDU_LOCATE);
        } else {
            mHandler.sendEmptyMessage(MSGREMOVELOCATEIONUPDATE);
        }
        unregisterReceiver(mBroadcastReceiver);//add by jiajun.shen for 387613
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPausing = true;

        if (isBindService) {
            Log.d("xjl_test", "unbindservice");
            unbindService(conn);
        }
        //remove by jiajun.shen for 387613
        //unregisterReceiver(mBroadcastReceiver);
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
                mCitys = updateService.getCityListFromDB();
                if (isFirstUse) {
                    firstUseCheck();
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        // update by jielong.xing for PR928049、927559 at 2015-2-11 begin
        if (needBackToMainScreen) {
            setResult(ACTIVITY_BACK);
//			startActivity(new Intent(LocateActivity.this, MainActivity.class));
        }
        // update by jielong.xing for PR928049、927559 at 2015-2-11 end

        finish();
    }

    // When user click the app's icon the first time,turn to auto locate.
    private void firstUseCheck() {
        if (mCitys.size() == 0) {
            updateConnectedFlags();

            if (isWifiConnected || isMobileConnected || isOtherConnected) {
                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setMessage(getResources().getString(R.string.locating));
                pDialog.show();

                getLocation();
            } else {
                //layout_main.setVisibility(View.GONE);
                //layout_connect.setVisibility(View.VISIBLE);
                Toast.makeText(LocateActivity.this,
                        getResources().getString(R.string.locate_connect_error),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // zhaoyun.wu begin
    private boolean checkLocationOn() {
        boolean isLocationOn = (Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF);
        PackageManager pm = getApplicationContext().getPackageManager();
        isNetworkProvideEnable = (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK));
        isGPSProvideEnable = (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS));
        return isLocationOn;
    }
    // zhaoyun.wu end

    private void requestLocationPermission() {
// Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                && ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(locationLayout, "Location access is required.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(LocateActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_LOCATION);
                }
            }).show();

        } else {
            Snackbar.make(locationLayout,
                    "Permission is not available. Requesting location permission.",
                    Snackbar.LENGTH_LONG).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        }
    }

    private static final int MSGREQUESTLOCATIONUPDATE = 0x1001;
    private static final int MSGREMOVELOCATEIONUPDATE = 0x1002;
    /* MODIFIED-BEGIN by peng.du, 2016-03-22,BUG-1842312 */
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    /* MODIFIED-END by peng.du,BUG-1842312 */
    private boolean isRequestLocationUpdate = false;
    private boolean isNetworkProvideEnable = true;
    private boolean isGPSProvideEnable = true;
    private boolean isNetworkRequestOpen = false;
    private boolean isGPSRequestOpen = false;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.e(TAG, "requestLocationUpdate");
            switch (msg.what) {
                case MSGREQUESTLOCATIONUPDATE: {
                    lock.lock();// MODIFIED by peng.du, 2016-03-22,BUG-1842312
                    Log.e(TAG, "jielong_requestLocationUpdate");
                    try {
                        if (isNetworkProvideEnable && ActivityCompat.checkSelfPermission(LocateActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 1, mNetworkListener);
                            isNetworkRequestOpen = true;
                        }

                        if (isGPSProvideEnable && ActivityCompat.checkSelfPermission(LocateActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1, mGpsListener);
                            isGPSRequestOpen = true;
                        }
                    } finally {
                        if (isNetworkRequestOpen || isGPSRequestOpen) {
                            isRequestLocationUpdate = true;
                            /* MODIFIED-BEGIN by peng.du, 2016-03-22,BUG-1842312 */
                            Log.i(TAG, "condition.signal()");
                        }
                        condition.signal();
                        lock.unlock();
                        /* MODIFIED-END by peng.du,BUG-1842312 */
                    }
                    break;
                }
                case MSGREMOVELOCATEIONUPDATE: {
                    Log.e(TAG, "jielong_removeLocationUpdate");
                    try {
                        if (isNetworkRequestOpen) {
                            if (mNetworkListener != null) {
                                mLocationManager.removeUpdates(mNetworkListener);
                            }
                            isNetworkRequestOpen = false;
                        }
                        if (isGPSRequestOpen) {
                            if (mGpsListener != null) {
                                mLocationManager.removeUpdates(mGpsListener);
                            }
                            isGPSRequestOpen = false;
                        }
                    } finally {
                        if (isNetworkRequestOpen || isGPSRequestOpen) {
                            if (mRemoveLocationUpdateRetryTimeCnt > 2) {
                                isRequestLocationUpdate = false;
                                mRemoveLocationUpdateRetryTimeCnt = 0;
                            } else {
                                mRemoveLocationUpdateRetryTimeCnt++;
                                mHandler.sendEmptyMessage(MSGREMOVELOCATEIONUPDATE);
                            }
                        } else {
                            isRequestLocationUpdate = false;
                            mRemoveLocationUpdateRetryTimeCnt = 0;
                        }
                    }
                    break;
                }
            }
        }
    };

    private int mRemoveLocationUpdateRetryTimeCnt = 0; // if removelocateupdate caught exception, we can retry at most 3 time to try to removelocateupdate

    // Fixed PR974952,974932,958494 by jielong.xing at 2015-4-15 begin
    private Handler mAlertDialogHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // Fixed PR1005697 by jielong.xing at 2015-05-19 begin
            if ((getWindow() != null && getWindow().isActive()) && !isFinishing() && !isDestroyed()) {
                //[BUGFIX]-Add-BEGIN by TSCD.qian-li,12/30/2015,1210037,[Launcher][Lockscreen]The prompts of the "auto locate failure" will flash after unlock screen.
                if (mDialog == null) {
                    mDialog = new AlertDialog.Builder(LocateActivity.this)
                            .setMessage(R.string.locate_location_service)
                            .setPositiveButton(getResources().getString(R.string.common_ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                        }
                                    })
                            .setNegativeButton(getResources().getString(R.string.common_cancel),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface arg0, int arg1) {

                                        }
                                    }).show();
                } else {
                    if (!mDialog.isShowing()) {
                        mDialog.show();
                    }
                }
                //[BUGFIX]-Add-END by TSCD.qian-li
            }
            // Fixed PR1005697 by jielong.xing at 2015-05-19 end
        }
    };
    // Fixed PR974952,974932,958494 by jielong.xing at 2015-4-15 end

    private void getLocation() {
        Log.i(TAG, "getLocation");

        // 1167118 -lin.zhou, modify -001 , begin
        SharedPreferences sharedataIsPermission = getSharedPreferences("firstuse", MODE_PRIVATE);
        /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
        boolean isPermission = sharedataIsPermission.getBoolean("isPermission", false);//judge open permission
        if (isPermission) {//delay judge firstUse
        /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
            SharedPreferences.Editor sharedata = getSharedPreferences("firstuse", MODE_PRIVATE).edit();
            sharedata.putBoolean("firstUse", false);
            sharedata.putBoolean("isPermission", false);
            isFirstUse = false;
            sharedata.commit();
        }
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_DENIED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED || (!isMiddleManAvavible && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_DENIED)) {
            //[BUGFIX]-Add-END by TSCD.xing.zhao
            disMissProgressDlgOrFinish();
            SharedPreferences sharedata = getSharedPreferences("firstuse", MODE_PRIVATE);
            isFirstUse = sharedata.getBoolean("firstUse", true);
            if (isFirstUse) {
                Intent mIntent = new Intent(this, PermissionActivity.class);
                startActivity(mIntent);
                this.finish();
            } else {
                //layout_offset.setVisibility(View.VISIBLE);
                String tips = null;
                if (isMiddleManAvavible) {
                    tips = getResources().getString(R.string.gotoSettingTips_no_phone);
                } else {
                    tips = getResources().getString(R.string.gotoSettingTips);
                }
                Snackbar.make(locationLayout, tips, Snackbar.LENGTH_INDEFINITE).setAction(getResources().getString(R.string.settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //layout_offset.setVisibility(View.GONE);
                        ForwardUtil.gotoSettings(LocateActivity.this);
                    }
                }).show();
            }
            //requestLocationPermission();
        } else {
            Log.e(TAG, "jielong_isRequestLocationUpdate :: " + isRequestLocationUpdate);
            if (isRequestLocationUpdate) {
                return;
            }
            new Thread() {
                public void run() {
                    boolean updateSuccess = false;
                    boolean networkException = false;
                    //Defect 212555 Outdoor auto location is failed by bing.wang.hz begin
                    int retryTime;
                    Log.w(CommonUtils.TAG_BING, "------getLocation by mIsFirstLocationTry is: "
                            + mIsFirstLocationTry + " and mIsQcomPlatform is: " + mIsQcomPlatform);
                    if (!mIsQcomPlatform && mIsFirstLocationTry) {
                        retryTime = 20;
                    } else {
                        retryTime = 10;
                    }
                    //Defect 212555 Outdoor auto location is failed by bing.wang.hz end

                    boolean isLocateProcessStart = false;
                    try {
                        if (isUseBaiduAsDefault) {
                            mLocationHandler.sendEmptyMessage(START_BAIDU_LOCATE);
                            isLocateProcessStart = true;
                        } else {
                            if (checkLocationOn()) {
                            /* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
                                mHandler.sendEmptyMessage(MSGREQUESTLOCATIONUPDATE);
                                
                                lock.lock();
                                Log.i(TAG, "condition.await()");
                                condition.await();
                                Log.i(TAG, "jielong_isRequestLocationUpdate :: " + isRequestLocationUpdate);
                                if (isRequestLocationUpdate) {
                                    isLocateProcessStart = true;
                                }
                                //[BUGFIX]-Add-END by TSCD.peng.du
                            /* MODIFIED-BEGIN by xiangnan.zhou, 2016-04-20,BUG-1983334*/
                            } else {
                                disMissProgressDlgOrFinish();
                                String tips = getResources().getString(R.string.turnon_locationservice);
                                Snackbar.make(locationLayout, tips, 5000).setAction(getResources().getString(R.string.bt_turnon_location), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    }
                                }).show();    
                                return;
                            }
                        }

                        if (isLocateProcessStart) {
                            for (int i = 0; i < retryTime; i++) {
                                Log.i(TAG, "mAutoLocateSuccess is " + mAutoLocateSuccess);
                                if (!mAutoLocateSuccess) {
                                    if (!isNetworkConnected) {
                                        Log.i(TAG, "isNetwrokConnected is false");
                                        networkException = true;
                                        break;
                                    }
                                    try {
                                        Thread.sleep(3000);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    updateSuccess = true;
                                    //[BUGFIX]-Add-BEGIN by TSCD.peng.du,03/22/2016,1842312,[REG][Weather][V5.2.8.1.0328.0_0316]The weather can't get weather data during auto fixed position
                                    mAutoLocateSuccess = false;
                                    //[BUGFIX]-Add-END by TSCD.peng.du
                                    break;
                                }
                            }
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        if (isUseBaiduAsDefault) {
                            mLocationHandler.sendEmptyMessage(STOP_BAIDU_LOCATE);
                        } else if(checkLocationOn()){
                            mHandler.sendEmptyMessage(MSGREMOVELOCATEIONUPDATE);
                            lock.unlock();
                        }
                    }
                    Log.e(TAG, "updateSuccess=="+updateSuccess);
                    if (updateSuccess) {
                        if (isBetterLocation(mGpsLocation, mNetworkLocation)) {
                            Log.e(TAG, "jielong____isBetterLocation=true");
                            mLocation = mGpsLocation;
                        } else {
                            mLocation = mNetworkLocation;
                        }

                        //Defect 212555 Outdoor auto location is failed by bing.wang.hz begin
                        if (mIsFirstLocationTry) {
                            SharePreferenceUtils.getInstance().saveBoolean(LocateActivity.this, CommonUtils.FIRST_LOCATION_TRY, false);
                            mIsFirstLocationTry = false;
                        }
                        //Defect 212555 Outdoor auto location is failed by bing.wang.hz end
                        if (mLocation != null) {
                            updateLocation(mLocation);
                        }
                    } else if (networkException) {
                        handler.sendEmptyMessage(MSGCONNECTFAILED);
                    } else {
                        if (ActivityCompat.checkSelfPermission(LocateActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                        LocateActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                handler.sendEmptyMessage(MSGTIMEOUT);
                                // Fixed PR964475 by jielong.xing at 2015-4-1 begin
//								layout_main.setVisibility(View.VISIBLE);
//								layout_connect.setVisibility(View.GONE);
                                // Fixed PR964475 by jielong.xing at 2015-4-1 end
                            }
                        });
                    }
                }
            }.start();
//            }
/* MODIFIED-END by xiangnan.zhou,BUG-1983334*/
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            // Request for camera permission.
            if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isFirstUse) {
                    firstUseCheck();
                }
                // Permission has been granted. Start camera preview Activity.
                Snackbar.make(locationLayout, "Location permission was granted.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                if (isFirstUse) {
                    isFirstUse = false;
                    Editor sharedata = getSharedPreferences("firstuse", MODE_PRIVATE).edit();
                    sharedata.putBoolean("firstUse", isFirstUse);
                    sharedata.commit();
                }
                // Permission request was denied.
                Snackbar.make(locationLayout, "Location permission request was denied.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSGTIMEOUT: {
                    Log.e(TAG, "AutoLocateTimeout");
                    Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                    it.putExtra("connect_timeout", true);
                    sendBroadcast(it);
                    break;
                }
                case MSGREGETPOSITION: {
                    if (handler != null) {
                        handler.removeMessages(MSGREGETPOSITION);
                    }
                    getLocation();
                    break;
                }
                case MSGCONNECTFAILED: {
                    Log.e(TAG, "AutoLocateConnectedFailed");
                    Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                    it.putExtra("connect_faild", true);
                    sendBroadcast(it);
                }
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void updateLocation(Location mLocation) {
        isFirstUse = false;
        Editor sharedata = getSharedPreferences("firstuse", MODE_PRIVATE).edit();
        sharedata.putBoolean("firstUse", isFirstUse);
        sharedata.commit();
        getLocationData(mLocation);
    }

    private void getLocationData(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            updateService.setUpdateManue();
            updateService.autoLocate(latitude, longitude);
        }
    }

    private void registerBoradcastReceiver() {
        mBroadcastReceiver = new MyBroadcasReceiver();
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("android.intent.action.CITY_BROADCAST");
        myIntentFilter.addAction("android.intent.action.WEATHER_BROADCAST");
        myIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mBroadcastReceiver, myIntentFilter);

    }

    // Fixed PR1029139 by jielong.xing at 2015-6-25 begin
    private Toast mCannotFindLocateToast = null;
    // Fixed PR1029139 by jielong.xing at 2015-6-25 end

    private class MyBroadcasReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            if ("android.intent.action.CITY_BROADCAST".equals(action)) {
                boolean isCityGot = b.getBoolean("city");
                if (hasNewText) {
                    if (isCityGot) {
                        // Fixed PR1022056 by jielong.xing at 2015-6-18 begin
                        String reqId = b.getString("reqId");
                        if (reqId != null && !reqId.equals(mReqId)) {
                            disMissProgressDlgOrFinish();
                            return;
                        }
                        // Fixed PR1022056 by jielong.xing at 2015-6-18 end
                        updateService.setUpdateManue();
                        mCitys = updateService.getCityList();
                        //PR1017912 Force clsoe hasppen when add a location by tingma at 2015-06-09 begin
//						ArrayList<String> cityLists = new ArrayList<String>();
                        getData(mCitys);
                        if (mSearchResultList != null && mSearchResultList.size() > 0) {
                            //[BUGFIX]-Add-BEGIN by TSCD.yanhua.chen,12/10/2015,1071572,
                            //[Android6.0][Weather_v5.2.8.1.0301.0]Search result cover the locate button
//                            bt_locate.setTranslationZ(0);
                            bt_locate.setTranslationZ(10);
                            //[BUGFIX]-Add-END by TSCD.yanhua.chen
                            mCityList.setVisibility(View.VISIBLE);
                            //[BUGFIX]-Mod-BEGIN by xinlei.sheng,2015/12/10,PR1101695
                            mListFootView.setVisibility(View.GONE);
                            mListHeadView.setVisibility(View.GONE);
                            //[BUGFIX]-Mod-END by xinlei.sheng,2015/12/10,PR1101695
                            if (mSearchCity != null) {
                                mResultAdapter.setFilter(mSearchCity);
                            }
                            mResultAdapter.notifyDataSetChanged();
//							mCityList.setAdapter(adapter);
                        } else {
                            bt_locate.setTranslationZ(10);
                            mCityList.setVisibility(View.GONE);
                            mListFootView.setVisibility(View.GONE);
                            mListHeadView.setVisibility(View.GONE);
                            mResultAdapter.notifyDataSetChanged();
                            // Fixed PR1029139 by jielong.xing at 2015-6-25 begin
                            if (mCannotFindLocateToast == null) {
                                mCannotFindLocateToast = Toast.makeText(LocateActivity.this,
                                        getResources().getString(R.string.connot_find_location),
                                        Toast.LENGTH_LONG);
                            }
                            mCannotFindLocateToast.show();
                            // Fixed PR1029139 by jielong.xing at 2015-6-25 end
                        }
                        //PR1017912 Force clsoe hasppen when add a location by tingma at 2015-06-09 end

                    } else {
                        bt_locate.setTranslationZ(10);
                        mCityList.setVisibility(View.GONE);
                        mListFootView.setVisibility(View.GONE);
                        mListHeadView.setVisibility(View.GONE);
                        mResultAdapter.notifyDataSetChanged();
                        // Fixed PR1029139 by jielong.xing at 2015-6-25 begin
                        if (mCannotFindLocateToast == null) {
                            mCannotFindLocateToast = Toast.makeText(LocateActivity.this,
                                    getResources().getString(R.string.connot_find_location),
                                    Toast.LENGTH_LONG);
                        }
                        mCannotFindLocateToast.show();
                        // Fixed PR1029139 by jielong.xing at 2015-6-25 end
                    }
                }
                disMissProgressDlgOrFinish();
            } else if ("android.intent.action.WEATHER_BROADCAST".equals(action)) {
                String newLocationKey = b.getString("location_key");

                boolean manu = b.getBoolean("manu", false);

                if (manu && newLocationKey != null) {
                    if (pDialog.isShowing()) {
                        disMissProgressDlgOrFinish();
                    }

                    //add by jiajun.shen for 387613
                    if (mPausing) {
                        mPausingIntent = intent;
                    }

                    // when the locateactivity in background,do not jump to mainactivity
                    if (!mPausing) {
                        // update by jielong.xing for PR928049、927559 at 2015-2-11 begin
                        if (needBackToMainScreen) {
                            Intent i = new Intent();
                            i.putExtra("newCityKey", newLocationKey);
                            LocateActivity.this.setResult(ACTIVITY_REFRESH, i);
                            LocateActivity.this.finish();//add by jiajun.shen for 387613
                        } else {
                            Intent i = new Intent();
                            i.putExtra("newCityKey", newLocationKey);
                            i.setClass(LocateActivity.this, MainActivity.class);
                            startActivity(i);
                            LocateActivity.this.finish();//add by jiajun.shen for 387613
                        }
                        // update by jielong.xing for PR928049、927559 at 2015-2-11 end
                    }

                    //LocateActivity.this.finish();
                }

                boolean connect_faild = b.getBoolean("connect_faild");
                if (connect_faild) {
                    disMissProgressDlgOrFinish();

                    if (!mPausing) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                        Toast.makeText(LocateActivity.this,
                                getResources().getString(R.string.locate_connect_error),
                                Toast.LENGTH_LONG).show();
                    }
                }

                boolean connect_timeout = b.getBoolean("connect_timeout");
                if (connect_timeout) {
                    disMissProgressDlgOrFinish();
                    if (!mPausing) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                        Toast.makeText(LocateActivity.this,
                                getResources().getString(R.string.obtain_data_failed),
                                Toast.LENGTH_LONG).show();
                        //[BUGFIX]-Add-BEGIN by peng.du,2016/01/23,1478435
                        isLocateTimeout = false;
                    } else {
                        isLocateTimeout = true;
                        //[BUGFIX]-Add-END by peng.du,2016/01/23,1478435
                    }
                }
            }
            // add by jielong.xing at 2015-1-7 begin
            else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                Log.i(TAG, "CONNECTIVITY_CHANGE received");
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // Fixed PR964475 by jielong.xing at 2015-4-1 begin
                    isNetworkConnected = true;
                    Log.i(TAG, "CONNECTIVITY_CHANGE isNetworkConnected is true");
                    // Fixed PR964475 by jielong.xing at 2015-4-1 end
                    //layout_main.setVisibility(View.VISIBLE);
                    //layout_connect.setVisibility(View.GONE);
                } else {
                    // Fixed PR964475 by jielong.xing at 2015-4-1 begin
                    isNetworkConnected = false;
                    Log.i(TAG, "CONNECTIVITY_CHANGE isNetworkConnected is false");
                    // Fixed PR964475 by jielong.xing at 2015-4-1 end
                    //layout_main.setVisibility(View.GONE);
                    //layout_connect.setVisibility(View.VISIBLE);
                    if (!mPausing) { // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                        Toast.makeText(LocateActivity.this,
                                getResources().getString(R.string.locate_connect_error),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
            // add by jielong.xing at 2015-1-7 begin
        }
    }

    // Fixed PR964475 by jielong.xing at 2015-4-1 begin
    private boolean isNetworkConnected = true;
    // Fixed PR964475 by jielong.xing at 2015-4-1 end

    private void updateConnectedFlags() {
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();

        if (activeInfo != null && activeInfo.isConnected()) {
            isWifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            isMobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if (!isWifiConnected && !isMobileConnected) {
                isOtherConnected = true;
            }
        } else {
            isWifiConnected = false;
            isMobileConnected = false;
            isOtherConnected = false;
        }
    }

    private ArrayList<HashMap<String, String>> getData(List<City> mCitys, String filter) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        int citySize = 0;
        if (mCitys != null && mCitys.size() > 0) {
            citySize = mCitys.size();
        }

        // Fix PR958392 by jielong.xing at 2015-3-24 begin
        for (int i = 0; i < citySize; i++) {
            City city = mCitys.get(i);
            /*if (filter != null) {
                String cityName = city.getCityInfoForList();
				String locationKey = city.getLocationKey();
				if (null != locationKey && locationKey.startsWith("postalCode")) {
					if (locationKey.contains(filter)) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("cityName", city.getCityInfoForList());
						list.add(map);
						i++;
						continue;
					}
				}
				if (!cityName.replaceAll(" ", "").toLowerCase().contains(filter.replaceAll(" ", "").toLowerCase())) {
					mCitys.remove(city);
					citySize--;
					continue;
				}
			}*/

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("cityName", city.getCityInfoForList());
            list.add(map);

//			i++;
        }
        // Fix PR958392 by jielong.xing at 2015-3-24 end

        return list;
    }

    private ArrayList<String> mSearchResultList = new ArrayList<String>();
    private ArrayList<City> mSearchResultCityList = new ArrayList<City>();
    private SearchResultItemAdapter mResultAdapter = null;

    //added for CR 1002210 by tingma at 2015-05-30 begin
    private void getData(List<City> mCitys) {
//		ArrayList<String> list = new ArrayList<String>();
        mSearchResultCityList.clear();
        mSearchResultList.clear();
        int citySize = 0;
        if (mCitys != null && mCitys.size() > 0) {
            citySize = mCitys.size();
        }

        for (int i = 0; i < citySize; i++) {
            City city = mCitys.get(i);
            //PR1017852 Search list didn't show the best result on the top by tingma at 2015-06-09 begin
            String cityInfo = city.getCityInfoForList();
            if (cityInfo != null && mSearchCity != null) {
                //int index = cityInfo.toLowerCase().indexOf(mSearchCity.toLowerCase());
                mSearchResultList.add(cityInfo);
                mSearchResultCityList.add(city);
            }
            //PR1017852 Search list didn't show the best result on the top by tingma at 2015-06-09 end
        }
//		return list;
    }
    //added for CR 1002210 by tingma at 2015-05-30 end

    private boolean isBetterLocation(Location locationA, Location locationB) {
        if (locationA == null) {
            return false;
        }
        if (locationB == null) {
            return true;
        }
        // A provider is better if the reading is sufficiently newer. Heading
        // underground can cause GPS to stop reporting fixes. In this case it's
        // appropriate to revert to cell, even when its accuracy is less.
        if (locationA.getElapsedRealtimeNanos() > (locationB.getElapsedRealtimeNanos() + 11 * 1000000000)) {
            return true;
        } else if (locationB.getElapsedRealtimeNanos() > (locationA.getElapsedRealtimeNanos() + 11 * 1000000000)) {
            return false;
        }

        // A provider is better if it has better accuracy. Assuming both
        // readings
        // are fresh (and by that accurate), choose the one with the smaller
        // accuracy circle.
        if (!locationA.hasAccuracy()) {
            return false;
        }
        if (!locationB.hasAccuracy()) {
            return true;
        }
        return locationA.getAccuracy() < locationB.getAccuracy();
    }

    //added for CR 1002210 by tingma at 2015-05-30 begin
    private class SearchResultItemAdapter extends BaseAdapter {
        private LayoutInflater mInflater = null;
        private List<String> mResultList = null;
        private String mFilter = null;

        private ViewHolder mHolder = null;

        public SearchResultItemAdapter(Context context, List<String> resultList, String filter) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mResultList = resultList;
            mFilter = filter;
        }

        @Override
        public int getCount() {
            if (mResultList != null) {
                return mResultList.size();
            }
            return 0;
        }

        @Override
        public String getItem(int position) {
            if (mResultList != null) {
                return mResultList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setFilter(String filter) {
            mFilter = filter;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.citylist_layout, null);
                mHolder = new ViewHolder();
                mHolder.mItemView = (TextView) convertView.findViewById(R.id.cityListitem);
                //[BUGFIX]-Add-BEGIN by TSCD..peng.du,11/18/2015,904775,
                //[Android5.1][Weather_v5.2.2.1.0307.0]Has no location icon when search
                mHolder.mImageView = (ImageView) convertView.findViewById(R.id.location_icon); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
                //[BUGFIX]-Add-END by TSCD.peng.du
                convertView.setTag(mHolder);
            } else {
                mHolder = (ViewHolder) convertView.getTag();
            }

            String var = getItem(position);
            if (var != null) {
                int index = -1;
                int strLen = 0;
                if (mFilter != null) {
                    index = var.toLowerCase().indexOf(mFilter.toLowerCase());
                    strLen = mFilter.length();
                }

                SpannableString sp = new SpannableString(var);
                if (index != -1) {
                    sp.setSpan(new ForegroundColorSpan(Color.argb(255, 0, 0, 0)), index,
                            (index + strLen), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    sp.setSpan(new ForegroundColorSpan(Color.argb(221, 117, 117, 117)),
                            (index + strLen), var.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                } else {
                    sp.setSpan(new ForegroundColorSpan(Color.argb(221, 117, 117, 117)), 0,
                            var.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                }

                mHolder.mItemView.setText(sp);
            } else {
                notifyDataSetChanged();
            }

            return convertView;
        }

        private class ViewHolder {
            public TextView mItemView;
            public ImageView mImageView;//[BUGFIX]-Add-BEGIN by TSCD..peng.du,11/18/2015,904775,
        }

    }
    //added for CR 1002210 by tingma at 2015-05-30 end

    private static final int START_BAIDU_LOCATE = 0;
    private static final int STOP_BAIDU_LOCATE = 1;
    private LocationClient mLocationClient = null;

    private boolean isDefUseBaiduApi() {
        return CustomizeUtils.getBoolean(LocateActivity.this, "def_weather_use_baiduapi");
    }

    private Handler mLocationHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e(TAG, "mLocationHandler = " + msg.toString()); // MODIFIED by xiangnan.zhou, 2016-04-20,BUG-1983334
            switch (msg.what) {
                case START_BAIDU_LOCATE:
                    try {
                        if (mLocationClient == null) {
                            mLocationClient = new LocationClient(LocateActivity.this);
                        }

                        mLocationClient.registerLocationListener(LocateActivity.this);

                        LocationClientOption option = new LocationClientOption();
                        option.setLocationMode(LocationMode.Battery_Saving);
                        option.setCoorType("bd09ll");
                        option.setScanSpan(10 * 1000);
                        option.setIsNeedAddress(false);
                        option.setOpenGps(false);
                        mLocationClient.setLocOption(option);

                        mLocationClient.start();
                    } catch (Exception e) {
                        Log.e(TAG, "BaiduLocation start exception = " + e.toString());
                    }
                    break;
                case STOP_BAIDU_LOCATE:
                    try {
                        if (mLocationClient != null) {
                            mLocationClient.stop();
                            mLocationClient.unRegisterLocationListener(LocateActivity.this);
                            mLocationClient = null;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "BaiduLocation stop exception = " + e.toString());
                    }
                    break;
            }
        }
    };


    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        try {
            if (bdLocation != null) {
                String locType = null;
                if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                    locType = " network (" + bdLocation.getNetworkLocationType() + ")";
                } else if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                    locType = " gps (" + bdLocation.getSatelliteNumber() + " satellites)";
                } else {
                    locType = " #" + bdLocation.getLocType();
                }
                /* MODIFIED-BEGIN by peng.du, 2016-03-22,BUG-1842312 */
                Log.e(TAG, "BAIDULocation onReceiveLocation Latitude = "
                        + bdLocation.getLatitude() + "Longitude = "
                        + bdLocation.getLongitude());
                        /* MODIFIED-END by peng.du,BUG-1842312 */
                String provider = "baidu: " + locType;
                Location location = new Location(provider);
                location.setLongitude(bdLocation.getLongitude());
                location.setLatitude(bdLocation.getLatitude());
                location.setAccuracy(bdLocation.getRadius());
                location.setTime(System.currentTimeMillis());
                location.setElapsedRealtimeNanos(System.nanoTime());
                mNetworkLocation = location;

                mAutoLocateSuccess = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "BaiduLocation get data exception = " + e.toString());
        } finally {
            mLocationHandler.sendEmptyMessage(STOP_BAIDU_LOCATE);
        }
    }

    private boolean isSearchPostal(String cityName) {
        if (TextUtils.isDigitsOnly(cityName) && (cityName.length() > 3)) {
            return true;
        } else
            return false;
    }
}
