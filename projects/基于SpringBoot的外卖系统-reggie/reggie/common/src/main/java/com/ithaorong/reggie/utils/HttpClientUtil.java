package com.ithaorong.reggie.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;

/** Http请求工具类
 * @author Administrator
 * @date crete by on 2019/3/8 18:08
 * @description
 */
public class HttpClientUtil {

    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url
     * 发送请求的URL
     * @param param
     * 请求参数,请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {

        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            System.out.println(urlNameString);
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {

                System.out.println(key + "--->" + map.get(key));
            }

            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {

                result += line;
            }

        } catch (Exception e) {

            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }

        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }

            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }
        return result;
    }


    private static final String CHARSET_UTF_8 = "UTF-8";
    private static final String CONTENT_TYPE= "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_TYPE_XML = "text/xml";
    public static String doPostXml(String url, String xml) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader(CONTENT_TYPE, CONTENT_TYPE_XML);
            // 创建请求内容,得指明使用UTF-8编码，否则到API服务器XML的中文不能被成功识别
            StringEntity entity = new StringEntity(xml, CHARSET_UTF_8);
            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), CHARSET_UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }




    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     * 发送请求的 URL
     * @param param
     * 请求参数,请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {

        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {

            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",  "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//       conn.setRequestHeader("charset","utf-8");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
//          line = new String(line.getBytes("ISO-8859-1"),"UTF-8");
                result += line;
            }

        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }

        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }

            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * https双向签名认证，用于支付申请退款
     *
     * */
    public static String SSLCERT_PATH="C:\\Users\\Administrator\\Desktop\\jar\\p12\\apiclient_cert.p12";//证书的路径
    public static String SSLCERT_PASSWORD="1356530102";//证书的密籍   自己的商户号
    @SuppressWarnings("deprecation")
    public static String doRefund(String url,String data) throws Exception {
        //注意PKCS12证书 是从微信商户平台-》账户设置-》 API安全 中下载的
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        //指向你的证书的绝对路径，带着证书去访问
        FileInputStream instream = new FileInputStream(new File(SSLCERT_PATH));//P12文件目录
        try {
            //下载证书时的密码、默认密码是你的MCHID mch_id
            keyStore.load(instream, SSLCERT_PASSWORD.toCharArray());//这里写密码
        } finally {
            instream.close();
        }
        //下载证书时的密码、默认密码是你的MCHID mch_id
        SSLContext sslcontext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, SSLCERT_PASSWORD.toCharArray())//这里也是写密码的
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1" },
                null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
        try {
            HttpPost httpost = new HttpPost(url); // 设置响应头信息
            httpost.addHeader("Connection", "keep-alive");
            httpost.addHeader("Accept", "*/*");
            httpost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpost.addHeader("Host", "api.mch.weixin.qq.com");
            httpost.addHeader("X-Requested-With", "XMLHttpRequest");
            httpost.addHeader("Cache-Control", "max-age=0");
            httpost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
            httpost.setEntity(new StringEntity(data, "UTF-8"));
            CloseableHttpResponse response = httpclient.execute(httpost);
            try {
                HttpEntity entity = response.getEntity();
                String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
                EntityUtils.consume(entity);
                return jsonStr;
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

}
