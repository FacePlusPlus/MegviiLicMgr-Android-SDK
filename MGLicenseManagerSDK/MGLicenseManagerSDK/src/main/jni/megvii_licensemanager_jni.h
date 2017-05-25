#include <jni.h>

#ifndef _Included_com_megvii_fppapidemo_Api
#define _Included_com_megvii_fppapidemo_Api
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jstring JNICALL Java_com_megvii_licensemanager_sdk_jni_NativeLicenseAPI_nativeGetLicense(JNIEnv *,
																								   jobject, jobject,
																								   jstring, jint,
																								   jlong);

JNIEXPORT jint JNICALL Java_com_megvii_licensemanager_sdk_jni_NativeLicenseAPI_nativeSetLicense(JNIEnv *,
																								jobject, jobject,
																								jstring);
#ifdef __cplusplus
}
#endif
#endif
