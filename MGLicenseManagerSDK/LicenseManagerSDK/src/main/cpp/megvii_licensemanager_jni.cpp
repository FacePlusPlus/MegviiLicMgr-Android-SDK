#include <android/log.h>
#include "megvii_licensemanager_jni.h"
#include <jni.h>
#include <include/MG_LicenseManager.h>

#include <vector>
#include <algorithm>
#include <string>
#include <chrono>
#include <cmath>

#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,"mgf-c",__VA_ARGS__)

#define DURATION_30DAYS 30
#define DURATION_365DAYS 365

jstring Java_com_megvii_licensemanager_sdk_jni_NativeLicenseAPI_nativeGetLicense(
        JNIEnv *env, jobject, jobject ctx, jstring juuid,
        jlong apiName) {

    const char *uuid = env->GetStringUTFChars(juuid, 0);
    const char *context_data = nullptr;
    MG_INT32 context_length = 0;


    typedef const char *(*pfunc)();
    const char *version = ((pfunc) (apiName))();
    mg_licmgr.GetContext( uuid, &context_data,
                         &context_length, version,
                         MG_END_ARG);

    std::string tmp_str(context_data, context_data + context_length);
    env->ReleaseStringUTFChars(juuid, uuid);

    return env->NewStringUTF(tmp_str.c_str());
}

jint Java_com_megvii_licensemanager_sdk_jni_NativeLicenseAPI_nativeSetLicense(
        JNIEnv *env, jobject, jobject ctx, jstring jhandle) {
    const char *handle = env->GetStringUTFChars(jhandle, 0);
    MG_INT32 handle_leanth = env->GetStringUTFLength(jhandle);
    MG_RETCODE retcode = mg_licmgr.SetLicence(handle, handle_leanth);

    env->ReleaseStringUTFChars(jhandle, handle);

    return (int) retcode;
}



jlong Java_com_megvii_licensemanager_sdk_jni_NativeLicenseAPI_nativeGetExpiretime(JNIEnv *env,
                                                                            jclass type,
                                                                            jlong apiName) {
    typedef const char *(*pfunc)();
    const char *version = ((pfunc) (apiName))();
    MG_UINT64 expiration_time;
    mg_licmgr.GetExpiretime(version,&expiration_time);
    return expiration_time;

}