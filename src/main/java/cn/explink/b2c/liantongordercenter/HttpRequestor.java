package cn.explink.b2c.liantongordercenter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

/**
 * http请求工具类
 *
 * @author Administrator
 *
 */
public class HttpRequestor {

	private static final HttpURLConnection URLConnection = null;
	private Integer contentTimeout = null;
	private Integer socketTimeout = null;
	private String proxyHost = null;
	private Integer proxyPort = null;

	public String doGet(String url) throws Exception {
		URL localUrl = new URL(url);
		URLConnection urlConnection = localUrl.openConnection();
		HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
		httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
		httpURLConnection.setRequestProperty("Content-Type", "application/json");
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer();
		String temp = null;
		if (httpURLConnection.getResponseCode() >= 300) {
			throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
		}
		try {
			is = httpURLConnection.getInputStream();
			isr = new InputStreamReader(is);
			reader = new BufferedReader(isr);
			while ((temp = reader.readLine()) != null) {
				sb.append(temp);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (isr != null) {
				isr.close();
			}
			if (is != null) {
				is.close();
			}
		}

		return sb.toString();

	}

	public static String doPost(String url, String context) throws Exception {
		StringBuilder buffer = new StringBuilder();
		BufferedReader reader = null;
		BufferedWriter out = null;
		URL localUrl = new URL(url);
		URLConnection urlConnection = localUrl.openConnection();
		HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
		httpURLConnection.setDoOutput(true);
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setRequestProperty("Content-Type", "application/xml;charset=utf-8");

		httpURLConnection.setDoOutput(true);
		httpURLConnection.setDoInput(true);
		httpURLConnection.setUseCaches(false);
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setConnectTimeout(30000); // 设置延迟为30秒
		httpURLConnection.setReadTimeout(30000);
		httpURLConnection.connect();
		OutputStreamWriter reqOut = null;
		if (context != null) {
			reqOut = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
		}
		out = new BufferedWriter(reqOut);
		out.write(context.toString());
		out.flush();
		out.close();
		if (httpURLConnection != null) {
			httpURLConnection.disconnect();
		}
		// 接收服务器的返回：
		reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));

		String line = null;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}
		return buffer.toString();
	}

	private URLConnection openConnection(URL localUrl) throws IOException {
		URLConnection urlConnection = null;
		if ((this.proxyHost != null) && (this.proxyPort != null)) {
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxyHost, this.proxyPort));
			urlConnection = localUrl.openConnection(proxy);
		} else {
			urlConnection = localUrl.openConnection();
		}
		return urlConnection;
	}

	private void renderRequest(URLConnection connection) {

		if (this.contentTimeout != null) {
			connection.setConnectTimeout(this.contentTimeout);
		}

		if (this.socketTimeout != null) {
			connection.setReadTimeout(this.socketTimeout);
		}

	}

	public Integer getContentTimeout() {
		return this.contentTimeout;
	}

	public void setContentTimeout(Integer contentTimeout) {
		this.contentTimeout = contentTimeout;
	}

	public Integer getSocketTimeout() {
		return this.socketTimeout;
	}

	public void setSocketTimeout(Integer socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public String getProxyHost() {
		return this.proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public Integer getProxyPort() {
		return this.proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

}
