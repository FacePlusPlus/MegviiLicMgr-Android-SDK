LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := OnlinAuth
LOCAL_SRC_FILES  := $(LOCAL_PATH)/libs/${TARGET_ARCH_ABI}/libMegviiLicenseManager-0.2.0.so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE    := MegviiLicenseManager-jni-0.2.0
LOCAL_SRC_FILES := megvii_licensemanager_jni.cpp
LOCAL_C_INCLUDES := include
LOCAL_C_INCLUDES += thirdparty security
LOCAL_STATIC_LIBRARIES := cpufeatures 
LOCAL_SHARED_LIBRARIES := OnlinAuth
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog
LOCAL_CPPFLAGS += -std=c++11 -ffunction-sections -fdata-sections -fvisibility=hidden \
		-Wall -Wextra -fweb 
LOCAL_LDFLAGS += -Wl,--gc-sections

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
	LOCAL_ARM_NEON := true
	LOCAL_CPPFLAGS += -mfpu=neon -mfloat-abi=softfp
endif

ifeq ($(TARGET_ARCH_ABI),x86)
	LOCAL_CPPFLAGS += -msse4.2
endif

LOCAL_ARM_MODE := arm
LOCAL_CPPFLAGS := $(LOCAL_CPPFLAGS) -std=c++11 -Wall -Wextra -fweb
include $(BUILD_SHARED_LIBRARY)
