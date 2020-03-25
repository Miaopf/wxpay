package com.zhangye.wxpay.modules.common.http;

import com.alibaba.fastjson.JSONObject;
import com.zhangye.wxpay.modules.common.wx.WxConfig;
import com.zhangye.wxpay.modules.common.wx.WxConstants;
import com.zhangye.wxpay.modules.common.wx.WxUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * @author zhangye
 * @version 1.0
 * @description HttpsClient类
 * @date 2019/12/19
 */
public class HttpsClient {

    /**
     * GET请求方式
     */
    public static final String METHOD_GET = "GET";
    /**
     * POST请求方式
     */
    public static final String METHOD_POST = "POST";
    /**
     * 连接超时时间
     */
    private static Integer CONNECTION_TIMEOUT = WxConfig.connectionTimeout;
    /**
     * 请求超时时间
     */
    private static Integer READ_TIMEOUT = WxConfig.readTimeout;

    /**
     * 发起https请求
     *
     * @param requestUrl    请求地址
     * @param requestMethod 请求方式（Get或者post）
     * @param postData      提交数据
     * @return JSONObject
     */
    public static JSONObject httpsRequestReturnJSONObject(String requestUrl, String requestMethod, String postData) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(HttpsClient.httpsRequestReturnString(requestUrl, requestMethod, postData));
        System.out.println("jsonObjectDate:  " + jsonObject);
        return jsonObject;
    }


    /**
     * 发起https请求
     *
     * @param requestUrl    请求地址
     * @param requestMethod 请求方式（Get或者post）
     * @param postData      提交数据
     * @return String
     */
    public static String httpsRequestReturnString(String requestUrl, String requestMethod, String postData) throws Exception {
        String response;
        HttpsURLConnection httpsUrlConnection = null;
        try {
            /*//创建https请求证书
            TrustManager[] tm = {new MyX509TrustManager()};
            //创建SSLContext管理器对像，使用我们指定的信任管理器初始化
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            SSLSocketFactory ssf = sslContext.getSocketFactory();*/

            // 创建URL对象
            URL url = new URL(requestUrl);
            // 创建HttpsURLConnection对象，并设置其SSLSocketFactory对象
            httpsUrlConnection = (HttpsURLConnection) url.openConnection();
            //设置ssl证书
            //httpsUrlConnection.setSSLSocketFactory(ssf);

            //设置header信息
            httpsUrlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //设置User-Agent信息
            httpsUrlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36");
            //设置可接受信息
            httpsUrlConnection.setDoOutput(true);
            //设置可输入信息
            httpsUrlConnection.setDoInput(true);
            //不使用缓存
            httpsUrlConnection.setUseCaches(false);
            //设置请求方式（GET/POST）
            httpsUrlConnection.setRequestMethod(requestMethod);
            //设置连接超时时间
            if (CONNECTION_TIMEOUT > 0) {
                httpsUrlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            } else {
                //默认10秒超时
                httpsUrlConnection.setConnectTimeout(10000);
            }
            //设置请求超时
            if (READ_TIMEOUT > 0) {
                httpsUrlConnection.setReadTimeout(READ_TIMEOUT);
            } else {
                //默认10秒超时
                httpsUrlConnection.setReadTimeout(10000);
            }
            //设置编码
            httpsUrlConnection.setRequestProperty("Charsert", WxConstants.DEFAULT_CHARSET);

            //判断是否需要提交数据
            if (StringUtils.equals(requestMethod, HttpsClient.METHOD_POST) && StringUtils.isNotBlank(postData)) {
                //讲参数转换为字节提交
                byte[] bytes = postData.getBytes(WxConstants.DEFAULT_CHARSET);
                //设置头信息
                httpsUrlConnection.setRequestProperty("Content-Length", Integer.toString(bytes.length));
                //开始连接
                httpsUrlConnection.connect();
                //防止中文乱码
                OutputStream outputStream = httpsUrlConnection.getOutputStream();
                outputStream.write(postData.getBytes(WxConstants.DEFAULT_CHARSET));
                outputStream.flush();
                outputStream.close();
            } else {
                //开始连接
                httpsUrlConnection.connect();
            }
            response = WxUtil.getStreamString(httpsUrlConnection.getInputStream());
        } catch (Exception e) {
        	e.printStackTrace();
            throw new Exception();
        } finally {
            if (httpsUrlConnection != null) {
                // 关闭连接
                httpsUrlConnection.disconnect();
            }
        }
        return response;
    }
    
    public static String httpPost(String url, String jsonString) {
        // 设置HTTP请求参数
    String result = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        try {
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);//设置请求超时时间 10s
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            BasicNameValuePair basicNameValuePair = new BasicNameValuePair("params", jsonString);
            list.add(basicNameValuePair);
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(list);
    StringEntity entity = new StringEntity(jsonString);
    		formEntity.setContentEncoding("UTF-8");
    		formEntity.setContentType("application/x-www-form-urlencoded");
            httpPost.setEntity(formEntity);
            HttpEntity resEntity = httpClient.execute(httpPost).getEntity();
            result = EntityUtils.toString(resEntity, "UTF-8");
        } catch (Exception e) {
            return null;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        return result;
    }


}
