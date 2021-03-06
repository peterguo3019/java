package com.refill.util.proxy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 代理核心类。包括模拟get、post请求。请求网页。请求图片（验证码）
 * 
 * @author killfen
 * @version 1.0.0
 */
public class Proxy {

	// 记录日志
	public static Logger log = LoggerFactory.getLogger(Proxy.class);

	/**
	 * get请求
	 * 
	 * @param url
	 *            请求地址<br>
	 * @param header
	 *            头部信息。创建一个Header对象 new Header()<br>
	 * @param params
	 *            包体参数<br>
	 * @param encoding
	 *            页面编码<br>
	 * @return Result 对象
	 */
	public static Result get(String url, com.refill.util.proxy.Header header, Map<String, String> params,
                             String encoding) {
		log.info("请求地址：" + url);
		// 实例化Httpclient
		DefaultHttpClient client = new DefaultHttpClient();
		// 如果有参数的就拼装起来
		url = url + (null == params ? "" : assemblyParameter(params));
		// 实例化get请求
		HttpGet httpGet =null;
		URL _url;
		try {
			_url = new URL(url);
			URI uri;
			uri = new URI(_url.getProtocol(), _url.getHost(), _url.getPath(), _url.getQuery(), null);
		    httpGet = new HttpGet(uri);  
		} catch (MalformedURLException e1 ) {
			// TODO Auto-generated catch block
			return null;
		}  catch( URISyntaxException e){
			return null;
		}
       
		// 增加cookie内容的容错处理(兼容性)。
		httpGet.getParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
		// 组装头部信息
		if (null != header && header.getHeaders().size() > 0) {
			httpGet.setHeaders(Proxy.assemblyHeader(header.getHeaders()));
		}
		// 执行请求后返回一个HttpResponse
		HttpResponse response;
		try {
			response = client.execute(httpGet);
			// 断开连接
			// httpGet.abort();
			// 返回一个HttpEntity
			HttpEntity entity = response.getEntity();
			// 封装返回的参数
			Result result = new Result();
			// 设置返回的cookie
			result.setCookie(assemblyCookie(client.getCookieStore().getCookies()));
			// 设置返回的状态
			result.setStatusCode(response.getStatusLine().getStatusCode());
			// 设置返回的头部信心
			result.setHeaders(response.getAllHeaders());
			// 设置返回的信息
			result.setHttpEntity(entity);
			return result;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		return null;
	}

	/**
	 * 获取文件
	 * 
	 * @param url
	 *            地址
	 * @param path
	 *            保证地址
	 * @throws HttpException
	 * @throws IOException
	 */
	public static void get(String url, String path) {

		try {
			HttpClient hc = HttpClientBuilder.create().build();
			HttpGet gm = new HttpGet(url);
			HttpResponse response = hc.execute(gm);
			InputStream is = response.getEntity().getContent();
			OutputStream os = new FileOutputStream(path);
			int c = -1;
			while ((c = is.read()) != -1) {
				os.write(c);
			}
			is.close();
			os.flush();
			os.close();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	/**
	 * post请求
	 * 
	 * @param url
	 *            请求地址<br>
	 * @param header
	 *            头部信息。创建一个Header对象 new Header()<br>
	 * @param params
	 *            包体参数<br>
	 * @param encoding
	 *            页面编码<br>
	 * @return Result 对象
	 */
	public static Result post(String url, com.refill.util.proxy.Header header, Map params, String encoding) {
		log.info("请求地址：" + url);
		// 实例化Httpclient的
		DefaultHttpClient client = new DefaultHttpClient();
		// 实例化post请求
		HttpPost httpPost = new HttpPost(url);
		// 增加cookie内容的容错处理(兼容性)。
		httpPost.getParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
		// 设置需要提交的参数
		if (params != null) {
			List list = new ArrayList();

			Iterator it = params.keySet().iterator();
			while (it.hasNext()) {
				String temp = (String) it.next();
				list.add(new BasicNameValuePair(temp, (String) params.get(temp)));
			}

			try {
				httpPost.setEntity(new UrlEncodedFormEntity(list, encoding));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				log.error(e.getMessage());
			}
		}
		// 设置头部
		if (null != header && header.getHeaders().size() > 0) {
			httpPost.setHeaders(Proxy.assemblyHeader(header.getHeaders()));
		}
		// 实行请求并返回
		HttpResponse response = null;
		try {
			response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			// 断开连接
			// httpPost.abort();
			// 封装返回的参数
			Result result = new Result();
			// 设置返回状态代码
			result.setStatusCode(response.getStatusLine().getStatusCode());
			// 设置返回的头部信息
			result.setHeaders(response.getAllHeaders());
			// 设置返回的cookie信心
			result.setCookie(assemblyCookie(client.getCookieStore().getCookies()));
			// 设置返回到信息
			result.setHttpEntity(entity);
			return result;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		return null;
	}

	/**
	 * get显示验证码
	 * 
	 * @param url
	 *            请求地址
	 * @param fileUrl
	 *            验证码生成的路径便于应用程序使用
	 * @param header
	 *            头部信息。创建一个Header对象 new Header()
	 */
	public static void getRandCode(String url, com.refill.util.proxy.Header header, String fileUrl) {
		DefaultHttpClient client = new DefaultHttpClient();

		log.info("请求地址：" + url);
		// 实例化get请求
		HttpGet get = new HttpGet(url);
		// 增加cookie内容的容错处理(兼容性)。
		get.getParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
		Map _headers = header.getHeaders();
		// 组装头部信息
		if (null != header && _headers.size() > 0) {
			get.setHeaders(Proxy.assemblyHeader(_headers));
		}
		HttpResponse response;
		try {
			response = client.execute(get);
			HttpEntity entity = response.getEntity();
			InputStream in = entity.getContent();
			// 将cookie返回头部，这样每次请求都能获得由服务端传过来的cookie值
			header.setCookie(assemblyCookie(client.getCookieStore().getCookies()));
			int temp = 0;
			// 创建文件
			File file = new File(fileUrl);
			// 打开数据流
			FileOutputStream out = new FileOutputStream(file);
			while ((temp = in.read()) != -1) {
				out.write(temp);
			}
			in.close();
			out.close();

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}

	/**
	 * 组装头部信息
	 * 
	 * @param headers
	 *            有Header对象提供
	 * @return Header 组装后的头部信息
	 */
	public static BasicHeader[] assemblyHeader(Map<String, String> headers) {
		BasicHeader[] allHeader = new BasicHeader[headers.size()];
		int i = 0;
		log.info("开始组装头部信息");

		Iterator<String> it = headers.keySet().iterator();
		while (it.hasNext()) {
			String str = (String) it.next();
			allHeader[i] = new BasicHeader(str, (String) headers.get(str));
			i++;
			log.info(str + ":" + headers.get(str));
		}
		log.info("结束组装头部信息");
		return allHeader;
	}

	/**
	 * 对cookie值进行组装
	 * 
	 * @param cookies
	 *            服务端返回的cookie值
	 * @return 字符串的cookie
	 */
	public static String assemblyCookie(List<Cookie> cookies) {
		StringBuffer sbu = new StringBuffer();
		log.info("开始返回cookie信息");
		for (int i = 0; i < cookies.size(); i++) {
			Cookie cookie = cookies.get(i);
			sbu.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
			log.info(cookie.getName() + ":" + cookie.getValue());
		}
		log.info("结束返回cookie信息");
		if (sbu.length() > 0)
			sbu.deleteCharAt(sbu.length() - 1);
		return sbu.toString();
	}

	/**
	 * 对参数进行组装
	 * 
	 * @param parameters
	 *            客户端提交的参数<br>
	 * @return 字符串的参数
	 */
	public static String assemblyParameter(Map<String, String> parameters) {
		String para = "?";
		log.info("开始组装参数");
		Iterator<String> it = parameters.keySet().iterator();
		while (it.hasNext()) {
			String str = it.next();
			para += str + "=" + parameters.get(str) + "&";
			log.info(str + ":" + parameters.get(str));
		}
		log.info("结束组装参数");
		return para.substring(0, para.length() - 1);
	}

}
