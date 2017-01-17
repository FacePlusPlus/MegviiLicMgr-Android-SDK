package com.megvii.licensemanager.sdk.jni;

import android.content.Context;

/**
 * @brief jni 接口类
 *
 * 该类加载了 jni 库
 */
public class NativeLicenseAPI {

    public static native String nativeGetLicense(Context context, String uuid, int duration, long[] apiName);

    public static native int nativeSetLicense(Context context, String handle);

    /**
     * 这里加载的名称要根据 so 不同的版本号进行修改
     */
    static {
        System.loadLibrary("MegviiLicenseManager-0.2.0");
        System.loadLibrary("MegviiLicenseManager-jni-0.2.0");
    }
}
