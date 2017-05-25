package com.megvii.licensemanager.sdk;

import android.content.Context;

import com.megvii.licensemanager.sdk.jni.NativeLicenseAPI;
import com.megvii.licensemanager.sdk.util.RequestManager;
import com.megvii.licensemanager.sdk.util.RequestManager.IHttpRequestRelult;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * @brief Face++ 联网授权 SDK 的 Android 接口
 *
 * 在完成初始化后，调用 takeLicenseFromNetwork 方法即可对 SDK 进行网络授权；
 *
 * 使用该 SDK 需要网络权限；
 *
 * 建议在使用 takeLicenseFromNetwork 方法之前，先调用 setExpirationMillis 传入当前SDK的过期时间，
 * SDK 会通过剩余授权天数来自动选择是否调用联网授权。
 */
public class LicenseManager {

    public static final int DURATION_30DAYS = 30;       ///< 单次授权30天
    public static final int DURATION_365DAYS = 365;     ///< 单次授权365天

    private int lastErrorCode = 0;
    private Context context;
    private long authTimeBufferMillis = 24 * 60 * 60 * 1000;
    private long expirationMillis = 0;
    private static final String US_URL = "https://api-us.faceplusplus.com/sdk/v2/auth";
    private static final String CN_URL = "https://api-cn.faceplusplus.com/sdk/v2/auth";

    /**
     * @brief 联网授权 SDK 的构造方法
     * @param[in] context android 上下文
     */
    public LicenseManager(Context context) {
        this.context = context;
    }

    /**
     * @brief 设置联网授权策略
     *
     * 单次联网授权可以授权 30 或 365 天，在此期间可以不需要重复进行授权。
     * 此函数可以控制联网授权的策略，但剩余授权时间少于 authTimeBufferMillis 时，才进行联网授权否则不需要
     *
     * 传入的时间戳要求是毫秒，默认为一天
     * @param[in] authTimeBufferMillis 传入一个以毫秒为单位的时间长度
     */
    public void setAuthTimeBufferMillis(long authTimeBufferMillis) {
        this.authTimeBufferMillis = authTimeBufferMillis;
    }

    /**
     * @brief 设置当前 SDK 过期时间
     *
     * 传入当前 SDK 过期时间戳，时间戳要求是毫秒
     * @param[in] expirationMillis 时间戳
     */
    public void setExpirationMillis(long expirationMillis) {
        this.expirationMillis = expirationMillis;
    }


    /**
     * @return 用于联网请求的文本信息
     * @brief 获取一个用于授权请求的文本信息
     * @param[in] uuid 标示不同用户的唯一 id，可以为空字符串。如果 uuid 有具体意义，则可以享受由 Face++
     * 提供的各种统计服务。
     * @param[in] durationTime 申请的授权时长（以当前时间开始计算，向后 30 或 365 天）
     * @param[in] apiName API 标识
     */
    public String getContext(String uuid, int duration, long apiName) {
        lastErrorCode = MG_RETCODE_OK;
        if (context == null) {
            lastErrorCode = MG_RETCODE_INVALID_ARGUMENT;
            return null;
        }
        String content = NativeLicenseAPI.nativeGetLicense(context, uuid, duration, apiName);
        if (isNumeric(content)) {
            lastErrorCode = Integer.parseInt(content);
            return null;
        }
        return content;
    }

    /**
     * @return 授权是否成功
     * @brief 设置一个许可字符串，进行授权
     * @param[in] content 将 GetContext 获取的上下文信息，发送给 Face++ 的授权 API，获取 license
     * 信息后，通过该函数对算法进行授权。
     */
    public boolean setLicense(String content) {
        lastErrorCode = MG_RETCODE_OK;
        if (context == null || content == null) {
            lastErrorCode = MG_RETCODE_INVALID_ARGUMENT;
            return false;
        }

        lastErrorCode = NativeLicenseAPI.nativeSetLicense(context, content);

        if (lastErrorCode == MG_RETCODE_OK)
            return true;

        return false;
    }

    /**
     * @return 失败原因
     * @brief 获取最后调用方法错误原因
     */
    public String getLastError() {
        return getErrorType(lastErrorCode);
    }

    /**
     * @return 是否需要再次请求
     * @brief 获取是否需要再次联网授权请求
     */
    public boolean needToTakeLicense() {
        long nowTime = System.currentTimeMillis();
        if (expirationMillis >= (nowTime + authTimeBufferMillis)) {
            // 在授权期内，不用请求授权
            return true;
        }

        return false;
    }

    /**
     * @brief 联网授权请求
     * @param[in] uuid 标示不同用户的唯一 id，可以为空字符串。如果 uuid 有具体意义，则可以享受由 Face++
     * 提供的各种统计服务。
     * @param[in] apiKey 申请的授权时长（以当前时间开始计算，向后30或365天）
     * @param[in] apiSecret 申请的授权时长（以当前时间开始计算，向后30或365天）
     * @param[in] apiName API 标识
     * @param[in] durationTime 申请的授权时长（以当前时间开始计算，向后30或365天）
     * @param[in] isCN 是否在中国地区
     * @param[out] takeLicenseCallback 授权成功或者失败返回
     */
    public void takeLicenseFromNetwork(String uuid, String apiKey, String apiSecret, long apiName, int durationTime,
                                       String sdkType, String duration, boolean isCN,
                                       final TakeLicenseCallback takeLicenseCallback) {
        boolean isAuthSuccess = needToTakeLicense();
        if (isAuthSuccess) {
            if (takeLicenseCallback != null)
                takeLicenseCallback.onSuccess();
        } else {
            String content = getContext(uuid, durationTime, apiName);
            String errorStr = getLastError();
            RequestManager requestManager = new RequestManager(context);
            String params = "";
            try {
                params = "api_key=" + URLEncoder.encode(apiKey, "utf-8") + "&api_secret="
                        + URLEncoder.encode(apiSecret, "utf-8") + "&auth_msg=" + URLEncoder.encode(content, "utf-8")
                        + "&sdk_type=" + URLEncoder.encode(sdkType, "utf-8")
                        + "&auth_duration=" + URLEncoder.encode(duration, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("Content-Type", "application/json");
            map.put("Charset", "UTF-8");

            requestManager.postRequest(isCN ? CN_URL : US_URL, params.getBytes(), null, new IHttpRequestRelult() {
                @Override
                public void onDownLoadComplete(int code, byte[] date, HashMap<String, String> headers) {
                    String successStr = new String(date);
                    boolean isSuccess = setLicense(successStr);
                    if (isSuccess) {// 授权成功
                        if (takeLicenseCallback != null)
                            takeLicenseCallback.onSuccess();
                    } else {// 授权失败
                        if (takeLicenseCallback != null)
                            takeLicenseCallback.onFailed(-1, getLastError().getBytes());
                    }
                }

                @Override
                public void onDownLoadError(int code, byte[] date, HashMap<String, String> headers) {
                    if (takeLicenseCallback != null)
                        takeLicenseCallback.onFailed(code, date);
                }
            });
        }
    }

    /**
     * @brief 联网授权请求回调接口
     */
    public interface TakeLicenseCallback {
        /**
         * @brief 联网授权请求成功回调接口
         */
        public void onSuccess();

        /**
         * @param code 联网请求返回 http code
         * @param date 联网请求返回 body
         * @brief 联网授权请求失败回调接口
         */
        public void onFailed(int code, byte[] date);
    }

    private boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    private static final int MG_RETCODE_FAILED = -1;
    private static final int MG_RETCODE_OK = 0;
    private static final int MG_RETCODE_INVALID_ARGUMENT = 1;
    private static final int MG_RETCODE_INVALID_HANDLE = 2;
    private static final int MG_RETCODE_INDEX_OUT_OF_RANGE = 3;
    private static final int MG_RETCODE_EXPIRE = 101;
    private static final int MG_RETCODE_INVALID_BUNDLEID = 102;
    private static final int MG_RETCODE_INVALID_LICENSE = 103;
    private static final int MG_RETCODE_INVALID_MODEL = 104;

    public static String getErrorType(int retCode) {
        switch (retCode) {
            case MG_RETCODE_FAILED:
                return "MG_RETCODE_FAILED";
            case MG_RETCODE_OK:
                return "MG_RETCODE_OK";
            case MG_RETCODE_INVALID_ARGUMENT:
                return "MG_RETCODE_INVALID_ARGUMENT";
            case MG_RETCODE_INVALID_HANDLE:
                return "MG_RETCODE_INVALID_HANDLE";
            case MG_RETCODE_INDEX_OUT_OF_RANGE:
                return "MG_RETCODE_INDEX_OUT_OF_RANGE";
            case MG_RETCODE_EXPIRE:
                return "MG_RETCODE_EXPIRE";
            case MG_RETCODE_INVALID_BUNDLEID:
                return "MG_RETCODE_INVALID_BUNDLEID";
            case MG_RETCODE_INVALID_LICENSE:
                return "MG_RETCODE_INVALID_LICENSE";
            case MG_RETCODE_INVALID_MODEL:
                return "MG_RETCODE_INVALID_MODEL";
        }

        return null;
    }
}