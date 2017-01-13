package com.tct.weather;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.tct.weather.R;
import com.tct.weather.provider.DBHelper;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private String TAG = "weather SettingsActivity";
    private ListPreference autoUpdatePrf;
    private ListPreference tempPrf;
    private ListPreference distancePrf;
    private int RESULT_CODE = 0x1002;
    private final String AUTO_UPDATE_KEY = "settings_auto_update";
    private final String ADVANCE_DATA_KEY = "settings_advance_data";
    private final String SHOW_FEELS_LIKE_KEY = "settings_feel_like";
    private final String TEMP_KEY = "settings_temp";
    private final String DISTANCE_KEY = "settings_distance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        //[BUGFIX]-Add-BEGIN by TSCD.peng.du,02/20/2016,1623781,[language][G05][Farsi]The weather APK translation is not complete
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_1);
        } else {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        }
        //[BUGFIX]-Add-END by TSCD.peng.du
        addPreferencesFromResource(R.xml.settings_preference);

        autoUpdatePrf = (ListPreference) findPreference(AUTO_UPDATE_KEY);
        autoUpdatePrf.setSummary(autoUpdatePrf.getEntry());
        autoUpdatePrf.setOnPreferenceChangeListener(this);

        tempPrf = (ListPreference) findPreference(TEMP_KEY);
        tempPrf.setSummary(tempPrf.getEntry());
        tempPrf.setOnPreferenceChangeListener(this);

        distancePrf = (ListPreference) findPreference(DISTANCE_KEY);
        distancePrf.setSummary(distancePrf.getEntry());
        distancePrf.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// 点击返回图标事件
                setResult(RESULT_CODE);
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CODE);
        this.finish();
        super.onBackPressed();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), AUTO_UPDATE_KEY)) {
            CharSequence[] entries = autoUpdatePrf.getEntries();
            int value = Integer.parseInt(newValue.toString());
            autoUpdatePrf.setSummary(entries[value]);
            return true;
        } else if (TextUtils.equals(preference.getKey(), TEMP_KEY)) {
            CharSequence[] entries = tempPrf.getEntries();
            int value = Integer.parseInt(newValue.toString());
            tempPrf.setSummary(entries[value]);

            DBHelper dbHelper = new DBHelper(this);

            //for dynamic icon
            SharedPreferences.Editor sharedata = getSharedPreferences("weather", Context.MODE_WORLD_READABLE).edit();
            //[BUGFIX]-Add-BEGIN by TSCD.peng.du,01/04/2016,1278695,
            // [Weather]When change  temperature unit ,The temperature of mini app dosen't change with weather apk
            if (TextUtils.equals(newValue.toString(), "0")) {
                sharedata.putBoolean("unit", true);
//                dbHelper.updateIsUnitC(true);
                dbHelper.updateIsUnitC(false);
            } else {
                sharedata.putBoolean("unit", false);
//                dbHelper.updateIsUnitC(false);
                dbHelper.updateIsUnitC(true);
            }
            Intent it = new Intent("android.intent.action.UNIT_BROADCAST");
            sendBroadcast(it);
            //[BUGFIX]-Add-END by TSCD.peng.du
            sharedata.commit();
            return true;
        } else if (TextUtils.equals(preference.getKey(), DISTANCE_KEY)) {
            CharSequence[] entries = distancePrf.getEntries();
            int value = Integer.parseInt(newValue.toString());
            distancePrf.setSummary(entries[value]);
            return true;
        } else {
            return false;
        }
//        switch (preference.getKey()) {
//            case AUTO_UPDATE_KEY: {
//                CharSequence[] entries = autoUpdatePrf.getEntries();
//                int value = Integer.parseInt(newValue.toString());
////                Log.e("SJJ", "value=" + newValue.toString());
//                autoUpdatePrf.setSummary(entries[value]);
//                break;
//            }
//            case TEMP_KEY: {
//                CharSequence[] entries = tempPrf.getEntries();
//                int value = Integer.parseInt(newValue.toString());
////                Log.e("SJJ", "value=" + newValue.toString());
//                tempPrf.setSummary(entries[value]);
//                break;
//            }
//            case DISTANCE_KEY: {
//                CharSequence[] entries = distancePrf.getEntries();
//                int value = Integer.parseInt(newValue.toString());
////                Log.e("SJJ", "value=" + newValue.toString());
//                distancePrf.setSummary(entries[value]);
//                break;
//            }
//            default:
//                return false;
//        }
    }
}
