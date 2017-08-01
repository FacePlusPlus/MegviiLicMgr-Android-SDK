
package com.megvii.licensemanager.sdk.util;

import android.content.Context;
import android.os.Handler;

import com.megvii.licensemanager.sdk.util.HttpURLClient.IHttpResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * 数据请求管理
 */
public class RequestManager implements IHttpResponse {

	private Context mContext;
	private IHttpRequestRelult mIRequestRelult;
	private MyHander myHander;

	public RequestManager(Context context) {
		this.mContext = context;
		myHander = new MyHander(context);
	}

	public void postRequest(final String url, final byte[] postData, final HashMap<String, String> headers,
							IHttpRequestRelult iRequestRelult) {
		boolean isConnect = isConnectSuccess(iRequestRelult);
		if (!isConnect)
			return;

		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLClient mHttpURLClient = new HttpURLClient(RequestManager.this);
				mHttpURLClient.processNetworkRequest(true, url, postData, headers);
			}
		}).start();
	}

	public void getRequest(final String urlStr, final HashMap<String, String> map,
						   final HashMap<String, String> headers, IHttpRequestRelult iRequestRelult) {
		boolean isConnect = isConnectSuccess(iRequestRelult);
		if (!isConnect)
			return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = getUrlWithParams(urlStr, map);
				HttpURLClient mHttpURLClient = new HttpURLClient(RequestManager.this);
				mHttpURLClient.processNetworkRequest(true, url, null, headers);
			}
		}).start();
	}

	private boolean isConnectSuccess(IHttpRequestRelult iRequestRelult) {
		this.mIRequestRelult = iRequestRelult;
		// 去下载
		// 获取网络状态
		int network_type = NetWorkHelper.checkNetWorkConnection(mContext);

		// 如果手机是飞行模式或者是断开网络状态（停止请求返回失败）
		if (network_type == NetWorkHelper.NETWORK_AIRPLANE || network_type == NetWorkHelper.NETWORK_DISCONNETED) {
			// 返回失败
			iRequestRelult.onDownLoadError(0, null, null);
			return false;
		}

		return true;
	}

	/**
	 * 拼接url
	 */
	private String getUrlWithParams(String url, HashMap<String, String> map) {
		if (url != null && map != null) {
			StringBuffer requestUrl = new StringBuffer(url).append("?");
			for (Entry<String, String> entry : map.entrySet()) {
				try {
					requestUrl.append(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8") + "&");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			if (requestUrl.length() != 0) {
				requestUrl.deleteCharAt(requestUrl.length() - 1);
			}
			return requestUrl.toString().trim();
		}
		return url;
	}

	public interface IHttpRequestRelult {
		/**
		 * 请求数据完成
		 */
		public void onDownLoadComplete(int code, byte[] date, HashMap<String, String> headers);

		/**
		 * 请求数据异常
		 */
		public void onDownLoadError(int code, byte[] date, HashMap<String, String> headers);
	}

	@Override
	public void onDownLoadResult(final int code, final byte[] date, final HashMap<String, String> headers) {
		if (mIRequestRelult == null)
			return;
		// 需要handler变主线程输出
		myHander.post(new Runnable() {
			@Override
			public void run() {
				if (code == 200)
					mIRequestRelult.onDownLoadComplete(code, date, headers);
				else
					mIRequestRelult.onDownLoadError(code, date, headers);
			}
		});
	}

	class MyHander extends Handler {
		public MyHander(Context context) {
			super(context.getMainLooper());
		}
	}
}