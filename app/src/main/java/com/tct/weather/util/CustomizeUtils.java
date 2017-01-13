package com.tct.weather.util;
/*
 ==========================================================================
 *HISTORY
 *
 *Tag            Date              Author        Description
 *============== ============ =============== ==============================
 *BUGFIX-1470934 2016/1/20       xing.zhao     [Weather]MiddleMan Runtime permission Phone group
 *===========================================================================
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Xml;

public class CustomizeUtils {
    private static final String TAG = "CustomizeUtils";
    // path of file isdm_JrdCustTest_defaults.xml
    private static final String PATH = "/custpack/plf/JrdWeather/";
    private static final String FILE = "isdm_JrdWeather_defaults.xml";

    //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,01/20/2016,1470934,[Weather]MiddleMan Runtime permission Phone group.
    private static final String MIDDLE_MAN_SCHEME = "content://com.tct.gapp.middleman";
    private static final String MIDDLE_MAN_GET_SUBSCRIBER_ID = "getSubscriberId";
    private static final String EXTRA_RESULT_CODE = "result_code";
    private static final String EXTRA_RESULT = "result";
    private static final int MIDDLE_MAN_RESULT_OK = 1;
    //[BUGFIX]-Add-END by TSCD.xing.zhao

    /**
     * get isdm value which is bool
     * 
     * @param mContext
     * @param def_name : the name of isdmID
     * @return
     */
    public static boolean getBoolean(Context mContext, String def_name) {
        Resources res = mContext.getResources();
        int id = res.getIdentifier(def_name, "bool", mContext.getPackageName());
        // get the native isdmID value
        boolean result = mContext.getResources().getBoolean(id);
        try {
            String bool_frameworks = getISDMString(new File(PATH + FILE), def_name, "bool");
            if (null != bool_frameworks) {
                result = Boolean.parseBoolean(bool_frameworks);
            }
        } catch (XmlPullParserException e) {           
            e.printStackTrace();
        } catch (IOException e) {            
            e.printStackTrace();
        }
        return result;
    }

    /**
     * get isdm value which is string
     * 
     * @param mContext
     * @param def_name : the name of isdmID
     * @return
     */
    public static String getString(Context mContext, String def_name) {
        Resources res = mContext.getResources();
        int id = res.getIdentifier(def_name, "string", mContext.getPackageName());
        // get the native isdmID value
        String result = mContext.getResources().getString(id);
        try {
            String string_frameworks = getISDMString(new File(PATH + FILE), def_name, "string");
            if (null != string_frameworks) {
                result = string_frameworks;
            }
        } catch (XmlPullParserException e) {            
            e.printStackTrace();
        } catch (IOException e) {            
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * get isdm value which is integer
     * 
     * @param mContext
     * @param def_name : the name of isdmID
     * @return
     */
    public static int getInteger(Context mContext, String def_name) {
        Resources res = mContext.getResources();
        int id = res.getIdentifier(def_name, "integer", mContext.getPackageName());
        // get the native isdmID value
        int result = (int)mContext.getResources().getInteger(id);
        try {
            String string_frameworks = getISDMString(new File(PATH + FILE), def_name, "integer");
            if (null != string_frameworks) {
                result = Integer.getInteger(string_frameworks);
            }
        } catch (XmlPullParserException e) {            
            e.printStackTrace();
        } catch (IOException e) {            
            e.printStackTrace();
        }
        return result;
    }

    /**
     * parser the XML file to get the isdmID value
     * 
     * @param file : xml file
     * @param name : isdmID
     * @param type : isdmID type like bool and string
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static String getISDMString(File file, String name, String type)
            throws XmlPullParserException,
            IOException {
        if (!file.exists() || null == file) {
            Log.e(TAG, "the perso file not exist" );
            return null;
        }
        String result = null;
        InputStream inputStream = new FileInputStream(file);
        XmlPullParser xmlParser = Xml.newPullParser();
        xmlParser.setInput(inputStream, "utf-8");

        int evtType = xmlParser.getEventType();
        boolean query_end = false;
        while (evtType != XmlPullParser.END_DOCUMENT && !query_end) {

            switch (evtType) {
                case XmlPullParser.START_TAG:

                    String start_tag = xmlParser.getAttributeValue(null, "name");
                    String start_type = xmlParser.getName();
                    if (null != start_tag && type.equals(start_type) && start_tag.equals(name)) {
                        result = xmlParser.nextText();                       
                        query_end = true;
                    }
                    break;

                case XmlPullParser.END_TAG:

                    break;

                default:
                    break;
            }
            // move to next node if not tail
            evtType = xmlParser.next();
        }
        inputStream.close();
        return result;
    }

    /**
     * 
     * @param str like "aaa"
     * @return
     */
    public static String splitQuotationMarks(String str){
        if(null != str && str.length() > 2 && str.startsWith("\"") && str.endsWith("\"")){
			str = str.substring(1, str.length()-1);
        }
        Log.d(TAG, "!--->After splitQuotationMarks: " + str);
        return str;
    }

    //[BUGFIX]-Add-BEGIN by TSCD.xing.zhao,01/20/2016,1470934,[Weather]MiddleMan Runtime permission Phone group.
    private static String getMiddleManResult(Context context,String method){
        String result = null;
        if (context!=null){
            Uri uri = Uri.parse(MIDDLE_MAN_SCHEME + "/" + context.getPackageName());
            Log.i(TAG, uri.toString());
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, null, method, null, null);
                if (cursor != null && cursor.getExtras() != null) {
                    Log.i(TAG, "cursor is not null");
                    Bundle bundle = cursor.getExtras();
                    int resultCode = bundle.getInt(EXTRA_RESULT_CODE);
                    if (resultCode == MIDDLE_MAN_RESULT_OK) {
                        result = bundle.getString(EXTRA_RESULT);
                    } else {
                        Log.i(TAG, "can not "+method+", result code = " + resultCode);
                    }
                }
            } catch (Exception e){
                Log.e(TAG, "Error in "+ method+"(): "
                        + e.getMessage());
            }finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return result;
    }

    public static String getSubscriberId(Context context){
        return getMiddleManResult(context,MIDDLE_MAN_GET_SUBSCRIBER_ID);
    }

    public static boolean isMiddleManAvavible(Context context) {
        return getMiddleManResult(context,MIDDLE_MAN_GET_SUBSCRIBER_ID) != null;
    }
    //[BUGFIX]-Add-END by TSCD.xing.zhao

    //[BUGFIX]-Add-BEGIN by TSCD.qian-li,01/22/2016,1313172,[Android6.0][Weather_v5.2.8.1.0305.0][FT test][Monitor]Weather locate auto failed
    public static boolean isUseBaiDuLocation(Context context, boolean isMiddleManAvavible) {
        String mimsi = null;
        if (isMiddleManAvavible) {
            mimsi = CustomizeUtils.getSubscriberId(context);
            Log.i(TAG, "imsi from middleman:" + mimsi);
        } else {
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                        == PackageManager.PERMISSION_DENIED) {
                    mimsi = null;
                } else {
                    mimsi = tm.getSubscriberId();
                }
            }
        }
        Log.e(TAG, " imsi = " + mimsi);
        if((mimsi!= null)&&mimsi.startsWith("460")){
            return true;
        } else {
            return false;
        }
    }
    //[BUGFIX]-Add-END by TSCD.qian-li
}