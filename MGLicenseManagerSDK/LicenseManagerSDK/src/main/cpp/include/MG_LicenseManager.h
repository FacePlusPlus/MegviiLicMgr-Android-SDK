/**
 * @file MG_LicenseManager.h
 * @brief 授权方法头文件
 *
 * 包含 Face++ 的授权管理的方法。
 */

#ifndef _MG_LICENSEMANAGER_H_
#define _MG_LICENSEMANAGER_H_

#include "MG_Common.h"
#define MG_END_ARG NULL
#define _OUT

/**
 * @brief 单次授权时长类型
 *
 * 目前只支持单次授权 30 天或 365 天，具体收费标准参看官网相关内容。
 * 如果自行传入了非这两个值之外的其他值，函数不会运行成功。
 * [facepp]: https://www.faceplusplus.com.cn/ "Face++ 官网"
 */
typedef enum {
    MG_LICMGR_DURATION_1DAY = 1,
    
    MG_LICMGR_DURATION_30DAYS = 30,         ///< 单次授权30天

    MG_LICMGR_DURATION_365DAYS = 365        ///< 单次授权365天
} MG_LICMGR_DURATION;

/**
 * @brief 联网授权算法函数集合
 *
 * 所有的算法函数都表示为该类型的一个变量，可以用形如：
 *   mg_licmgr.Function(...)
 * 的形式进行调用。
 */
typedef struct {

    /**
     * @brief 获取一个用于授权请求的上下文信息
     *
     * 可以获取一个同时对一个或多个 Face++ 的算法进授权的上下文信息。
     *
     * @param[in] env               Android jni 的环境变量，仅在 Android SDK 中使用
     * @param[in] jobj              Android 调用的上下文，仅在 Android SDK 中使用
     * @param[in] duration          申请的授权时长（以当前时间开始计算，向后30或365天）
     * @param[in] uuid              标示不同用户的唯一 id，可以为空。如果 uuid 有具体意义，则可以享受由 Face++ 提供的各种统计服务。
     * @param[in] ...               传入需要授权的算法 GetAPIVersion 的指针，并以 MG_END_ARG 结束。
     *                              例如需要对mg_facepp算法进行授权，则传入参数为：
     *                              mg_licmgr.GetContext(
     *                                  duration,
     *                                  uuid,
     *                                  &context_data,
     *                                  &context_length ,
     *                                  mg_facepp.GetApiVersion,
     *                                  MG_END_ARG)
     *
     * @param[out] context_data     获取的上下文信息，成功创建后会修改其值
     * @param[out] context_length   获取的上下文信息的长度，成功创建后会修改其值
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*GetContext) (
        MG_LICMGR_DURATION duration,
        const char* uuid,
        const char _OUT **context_data,
        MG_INT32 _OUT *context_length,
        ...);

    /**
     * @brief 设置一个许可证，进行授权
     *
     * 将 GetContext 获取的上下文信息，发送给 Face++ 的授权 API，获取 license 信息后，通过该函数对算法进行授权。
     * 授权完成后可以用 GetExpiration 函数查看授权结果。
     * 授权请求涉及到设备的网络权限，需要开发者自己完成。授权相关的 Web API 文档见如下网址：
     * [online-auth] https://console.faceplusplus.com.cn/documents/5671789 "Face++ 联网授权"
     *
     * @param[in] env               Android jni 的环境变量，仅在 Android SDK 中使用
     * @param[in] jobj              Android 调用的上下文，仅在 Android SDK 中使用
     * @param[in] license_data      联网获得的 license 数据
     * @param[in] license_length    License 数据的长度
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*SetLicence) (
        const char *license_data,
        MG_INT32 license_length);
    MG_RETCODE (*GetExpiretime)(const char* version, MG_UINT64 *p_get_expire);

} MG_LICENSE_MANAGER_API_FUNCTIONS_TYPE;

/**
 * @brief 授权算法域
 *
 * Example:
 *      mg_licmgr.SetLicence(...
 */
extern MG_EXPORT MG_LICENSE_MANAGER_API_FUNCTIONS_TYPE mg_licmgr;

#undef _OUT

#endif
