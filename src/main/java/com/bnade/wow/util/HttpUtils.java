package com.bnade.wow.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * 
 * 一些简单的http操作
 * 
 * @author liufeng0103
 *
 */
public class HttpUtils {
	
	/**
	 * 获取url返回的http响应头
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static Map<String,List<String>> getHeaderFields(String url) throws IOException{
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			// 设置超时，防止网络不好时阻塞线程
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			// Request Headers
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			conn.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch, br");
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			// Request Method
			conn.setRequestMethod("HEAD");
			return conn.getHeaderFields();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	
	/**
	 * 获取url的内容
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String get(String url) throws IOException {
		HttpURLConnection conn = null;
		InputStream is = null;
		String result = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			// 设置超时，防止网络不好时阻塞线程
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			// Request Headers
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			conn.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch, br");
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			// 查看Response Headers是否通过gzip压缩
			if ("gzip".equals(conn.getHeaderField("Content-Encoding"))) {
				is = new GZIPInputStream(conn.getInputStream());
			} else {
				is = conn.getInputStream();
			}
			result = IOUtils.toString(is, "utf-8");
		} finally {
			/*
			According to http://docs.oracle.com/javase/6/docs/technotes/guides/net/http-keepalive.html and OpenJDK source code.

			(When keepAlive == true)

			If client called HttpURLConnection.getInputSteam().close(), the later call to HttpURLConnection.disconnect() will NOT close the Socket. i.e. The Socket is reused (cached)

			If client does not call close(), call disconnect() will close the InputSteam and close the Socket.

			So in order to reuse the Socket, just call InputStream close(). Do not call HttpURLConnection disconnect().
			 */
			if (is != null) {
				is.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return result;
	}
    
    public static void main(String[] args) throws Exception {
    	System.out.println(HttpUtils.getHeaderFields("http://auction-api-cn.worldofwarcraft.com/auction-data/330beb217242022e18398ae252e513c0/auctions.json"));
	}
}
