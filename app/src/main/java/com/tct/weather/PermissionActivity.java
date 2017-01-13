package com.tct.weather;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tct.weather.util.CustomizeUtils;

import java.security.Permission;
import java.util.ArrayList;

/**
 * Created by user on 15-11-18.
 */
/*BUGFIX-1167118 2015/12/16       lin-zhou      [Android6.0][Weather_v5.2.8.1.0301.0]After give the permission,weather didn't locate auto*/
public class PermissionActivity extends AppCompatActivity {

    private final int PERMISSION_REQUEST_LOCATION = 0x001;
    public static final int CHECK_REQUEST_PERMISSION_RESULT = 3;
    private boolean isFirstUse = true;
    private TextView btnGotIt;
    private boolean isMiddleManAvavible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        isMiddleManAvavible = CustomizeUtils.isMiddleManAvavible(getApplicationContext());
        btnGotIt = (TextView) findViewById(R.id.btn_got_it);
        String mGotIt = getResources().getString(R.string.gotit);
        btnGotIt.setText(mGotIt.toUpperCase());
        ((TextView)findViewById(R.id.permisson_des)).setText(
                isMiddleManAvavible ? getResources().getString(R.string.location_permission_description_no_phone): getResources().getString(R.string.location_permission_description));

        SharedPreferences sharedata = getSharedPreferences("firstuse", MODE_PRIVATE);
        isFirstUse = sharedata.getBoolean("firstUse", true);
        findViewById(R.id.btn_got_it).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestLocationPermission();
                    }
                }
        );
    }

    private void requestLocationPermission() {
        String[] permissionarray = new String[3];
        ArrayList<String> permissions = new ArrayList();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!isMiddleManAvavible && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_DENIED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (permissions.size() > 0) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]),
                    CHECK_REQUEST_PERMISSION_RESULT);
        } else {
            refreshWidgetView();
            startActivity(new Intent(PermissionActivity.this, LocateActivity.class));
            this.finish();
        }

        /*ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_PHONE_STATE},
                CHECK_REQUEST_PERMISSION_RESULT);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            finish();
        }*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CHECK_REQUEST_PERMISSION_RESULT) {
            if (isFirstUse) {

                // 1167118 -lin.zhou, modify -001 , begin
                SharedPreferences.Editor sharedata = getSharedPreferences("firstuse", MODE_PRIVATE).edit();
                sharedata.putBoolean("isPermission", true);
                sharedata.commit();
                // 1167118 -lin.zhou, modify -001 , end

//                isFirstUse = false;
//                SharedPreferences.Editor sharedata = getSharedPreferences("firstuse", MODE_PRIVATE).edit();
//                sharedata.putBoolean("firstUse", isFirstUse);
//                sharedata.commit();
            }
            refreshWidgetView();
            startActivity(new Intent(PermissionActivity.this, LocateActivity.class));
            this.finish();
        }
    }

    private void refreshWidgetView() {
        Intent intent = new Intent("android.intent.action.UPDATE_WIDGET");
        sendBroadcast(intent);
    }
}
