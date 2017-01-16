package com.megvii.licensemanager.sdk.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * 异步请求下载器
 */
public final class HttpURLClient {

    private IHttpResponse mHttpResponse;
    public int httpTimeCount; // 联网计数

    public HttpURLClient(IHttpResponse httpResponse) {
        this.mHttpResponse = httpResponse;
    }

    /**
     * 处理网络文件请求
     */
    public final boolean processNetworkRequest(boolean isPostRequest, String urlStr, byte[] postData,
                                               HashMap<String, String> headers) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            // 打开URL连接
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (conn == null)
            throw new NullPointerException("conn");

        // 添加请求头
        attachRequestProperty(conn, headers);

        conn.setConnectTimeout(5000);

        // 针对get和post做不同的处理
        if (!isPostRequest) {
            try {
                conn.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
        } else {
            try {
                conn.setRequestMethod("POST");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }

            byte[] postContent = null;
            int len = 0;
            if (postData != null) {
                postContent = postData;
                len = postContent.length;
            }

            if (postContent != null) {
                conn.setRequestProperty("Content-Length", len + "");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);// 忽略缓存
                OutputStream os = null;
                try {
                    os = conn.getOutputStream();
                    os.write(postContent);
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    postContent = null;
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        os = null;
                    }
                }
            }
        }

        int respCode = 0;
        byte[] receiveData = null;
        try {
            // 此处开始请求了
            conn.connect();
            respCode = conn.getResponseCode();

            if (respCode == -1) {// 如果无法从响应中识别任何代码（即响应不是有效的 HTTP），则返回 -1
                if (httpTimeCount < 2) {
                    // 重新联网一次
                    httpTimeCount++;
                    // 断开这条连接
                    connectionClose(conn);
                    return processNetworkRequest(isPostRequest, urlStr, postData, headers);
                } else {
                    httpTimeCount = 0;
                    // 断开这条连接
                    connectionClose(conn);
                    mHttpResponse.onDownLoadResult(respCode, receiveData, null);
                    return false;
                }
            } else if (respCode == HttpURLConnection.HTTP_INTERNAL_ERROR || respCode == HttpURLConnection
                    .HTTP_NOT_FOUND
                    || respCode == HttpURLConnection.HTTP_VERSION || respCode == HttpURLConnection.HTTP_BAD_GATEWAY
                    || respCode == HttpURLConnection.HTTP_UNAUTHORIZED
                    || respCode == HttpURLConnection.HTTP_GATEWAY_TIMEOUT) {

                connectionClose(conn);
                mHttpResponse.onDownLoadResult(respCode, receiveData, null);
                return false;
            } else {
                // 遍历获取服务器回复的头信息
                HashMap<String, String> reposeHeaders = storeResponseHeaders(conn);

                // 打开http连接的返回流
                InputStream ms = conn.getInputStream();

                int len = 0;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buf = new byte[1024 * 2];
                while ((len = ms.read(buf)) > 0) {
                    baos.write(buf, 0, len);
                }
                // 关闭流和连接
                try {
                    ms.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                // 断开这条连接
                connectionClose(conn);
                // 获取返回的数据流
                receiveData = baos.toByteArray();

                try {
                    baos.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                mHttpResponse.onDownLoadResult(respCode, receiveData, reposeHeaders);
                return true;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            connectionClose(conn);
            // 请求出现异常
            mHttpResponse.onDownLoadResult(respCode, receiveData, null);
        }
        return false;
    }

    /**
     * 处理连接失败(断开这条连接)
     */
    public static final void connectionClose(HttpURLConnection conn) {
        if (conn != null) {
            // 这条连接断开
            conn.disconnect();
        }
    }

    /**
     * 保存头信息
     *
     */
    private static final HashMap<String, String> storeResponseHeaders(HttpURLConnection conn) {
        if (conn == null)
            throw new NullPointerException("conn");

        HashMap<String, String> hearders = new HashMap<String, String>();

        for (int n = 0; ; ++n) {
            String key = conn.getHeaderFieldKey(n);
            if (key == null && n != 0) {
                // http恶心的地方，返回头信息第一个居然是null
                break;
            } else {
                String value = conn.getHeaderField(n);
                hearders.put(key, value);
            }
        }

        return hearders;
    }

    /**
     * 附加请求属性
     */
    private final void attachRequestProperty(HttpURLConnection conn, HashMap<String, String> headers) {
        if (conn == null)
            throw new NullPointerException("conn");

        if (headers != null) {
            synchronized (headers) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public interface IHttpResponse {
        public void onDownLoadResult(int code, byte[] date, HashMap<String, String> headers);
    }
}
