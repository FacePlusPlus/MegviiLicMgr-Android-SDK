package com.megvii.licensemanager.sdk.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.provider.Settings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class NetWorkHelper {

    /**
     * 生成编译后的url
     */
    public static final String makeEncodeURL(String path) {
        if (path != null && path.startsWith("http://") && path.endsWith(".png")) {
            int promix = path.lastIndexOf('/') + 1;
            // 中文路径需修改
            try {
                path = path.substring(0, promix)
                        + URLEncoder.encode(path.substring(promix), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    public static final int NETWORK_DISCONNETED = 0;
    public static final int NETWORK_CONNETED_WIFI = 1;
    public static final int NETWORK_CONNETED_GPRS = 2;
    public static final int NETWORK_AIRPLANE = 3;

    public static int checkNetWorkConnection(Context context) {
        int dataState = NetWorkHelper.isDataConnection(context);
        if (dataState == 1) {
            // 网络连接通畅
            int type = NetWorkHelper.getNetworkType(context);
            return type;
        } else if (dataState == 0) {
            // 网络不通
            return NETWORK_DISCONNETED;
        } else if (dataState == -1) {
            // 飞行模式，无网络
            return NETWORK_AIRPLANE;
        }
        return NETWORK_DISCONNETED;
    }

    /**
     * 获取网络是否可用的状态
     */
    public static boolean isNetworkAvailable(Context mContext) {
        ConnectivityManager mConnectManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnectManager.getActiveNetworkInfo() != null
                && mConnectManager.getActiveNetworkInfo().isAvailable()) {
            return true;
        }
        return false;
    }

    /**
     * 网络连接是否通畅
     */
    public static int isDataConnection(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        String Mode_airpln = Settings.System.getString(
                context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON);
        // Log.i("CommonHelper", "isDataConnection >> 飞行模式 = "+Mode_airpln);
        if (Mode_airpln != null && Mode_airpln.equalsIgnoreCase("1")) {
            // Log.i("CommonHelper", "isDataConnection >> 飞行模式：开");
            return -1;
        }

        if (networkInfo != null && networkInfo.isConnected()) {
            // Log.i("CommonHelper", "isDataConnection >> 网络状态：已连接");
            return 1;
        } else {
            // Log.i("CommonHelper", "isDataConnection >> 网络状态：未连接");
            return 0;
        }
    }

    /**
     * 获取网络连接状态
     */
    public static int getNetworkType(Context context) {
        // NetworkInfo 有以下方法
        // getDetailedState()：获取详细状态。
        // getExtraInfo()：获取附加信息(3gnet/3gwap/uninet/uniwap/cmnet/cmwap/ctnet/ctwap)
        // getReason()：获取连接失败的原因。
        // getType()：获取网络类型(一般为移动或Wi-Fi)。
        // getTypeName()：获取网络类型名称(一般取值“WIFI”或“MOBILE”)。
        // isAvailable()：判断该网络是否可用。
        // isConnected()：判断是否已经连接。
        // isConnectedOrConnecting()：判断是否已经连接或正在连接。
        // isFailover()：判断是否连接失败。
        // isRoaming()：判断是否漫游

        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
        if (networkInfo != null) {
            int type = networkInfo.getType();
            State state = networkInfo.getState();

            if (type == ConnectivityManager.TYPE_WIFI
                    && state == State.CONNECTED) {
                // wifi连接通畅
                return NETWORK_CONNETED_WIFI;
            } else if (type == ConnectivityManager.TYPE_MOBILE
                    && state == State.CONNECTED) {
                return NETWORK_CONNETED_GPRS;
            }
        }
        return NETWORK_DISCONNETED;
    }
}