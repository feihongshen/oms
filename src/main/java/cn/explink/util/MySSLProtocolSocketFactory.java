package cn.explink.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

/**
 * 用户发送https协议
 * 
 * @author Administrator
 * 
 */
public class MySSLProtocolSocketFactory implements ProtocolSocketFactory {
	/*
	 * @Test public void test() throws Exception { String url =
	 * "https://newtest.app.yonghui.cn:9004/yonghuio2oocc/v2/yonghuio2o/delivery"
	 * ; String str =
	 * "{\"client_id\": \"delivery_client\",\"secret\": \"oSyL!@1sdA3\",\"orderList\": [{\"sheetid\": \"502\",\"flag\":\"1\",\"bagno\": \"2014041110145224-03-01\",\"sendphone\": \"13917577170\",\"sdate\": \"2015-03-18 10:25:23\",\"note\": \"������������״̬\"}]}"
	 * ;
	 * 
	 * MySSLProtocolSocketFactory.callRestful(str, url); }
	 */

	public static String callRestful(String content, String url, String charset) throws UnsupportedEncodingException {
		if ((charset == null) || (charset.length() == 0)) {
			charset = "UTF-8";
		}
		Protocol myhttps = new Protocol("https", new MySSLProtocolSocketFactory(), 443);
		Protocol.registerProtocol("https", myhttps);
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(url);

		method.setRequestHeader("content-type", "application/json");
		RequestEntity requestEntity = new StringRequestEntity(content, null, charset);
		// method.setRequestBody(content);
		method.setRequestEntity(requestEntity);

		try {
			client.executeMethod(method);
		} catch (IOException e) {
			e.printStackTrace();
		}

		InputStream stream = null;
		try {
			stream = method.getResponseBodyAsStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		StringBuffer buf = new StringBuffer();
		String line;

		try {
			while (null != (line = br.readLine())) {
				buf.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return buf.toString();
	}

	private SSLContext sslcontext = null;

	private SSLContext createSSLContext() {
		SSLContext sslcontext = null;
		try {
			sslcontext = SSLContext.getInstance("SSL");
			sslcontext.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		return sslcontext;
	}

	private SSLContext getSSLContext() {
		if (this.sslcontext == null) {
			this.sslcontext = this.createSSLContext();
		}
		return this.sslcontext;
	}

	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
		return this.getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		return this.getSSLContext().getSocketFactory().createSocket(host, port);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException, UnknownHostException {
		return this.getSSLContext().getSocketFactory().createSocket(host, port, clientHost, clientPort);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
		if (params == null) {
			throw new IllegalArgumentException("Parameters may not be null");
		}
		int timeout = params.getConnectionTimeout();
		SocketFactory socketfactory = this.getSSLContext().getSocketFactory();
		if (timeout == 0) {
			return socketfactory.createSocket(host, port, localAddress, localPort);
		} else {
			Socket socket = socketfactory.createSocket();
			SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
			SocketAddress remoteaddr = new InetSocketAddress(host, port);
			socket.bind(localaddr);
			socket.connect(remoteaddr, timeout);
			return socket;
		}
	}

	private static class TrustAnyTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	}

}