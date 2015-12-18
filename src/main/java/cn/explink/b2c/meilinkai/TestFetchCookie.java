package cn.explink.b2c.meilinkai;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class TestFetchCookie {
	 public static void main(String args[]) throws IOException{
		   /* String urlString = "http://www.baidu.com";
		    CookieManager manager = new CookieManager();
		    CookieHandler.setDefault(manager);
		    try{
		    	URL url = new URL(urlString);
		    	URLConnection connection = url.openConnection();
		    	Object obj = connection.getContent();
		    	url = new URL(urlString);
		    	connection = url.openConnection();
		    	obj = connection.getContent();
		    	CookieStore cookieJar = manager.getCookieStore();
		    	List<HttpCookie> cookies = cookieJar.getCookies();
		    	for (HttpCookie cookie : cookies) {
		    		System.out.println(cookie);
		    	}
		    }catch(Exception e){
		    	e.printStackTrace();
		    }*/
		 //测试获取数据
		 String soapRequestData = 
				  "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
				+ "xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"
				+ "<soap12:Body>"
				+ 	"<CheckUser xmlns=\"http://tempuri.org/\">"
				+ 		"<strUserID>HDFY</strUserID> "//用户名
				+ 		"<strPwd>FY5TGB</strPwd>"//密码
				+ 	"</CheckUser>"
				+ "</soap12:Body>"
				+ "</soap12:Envelope>";
		 String url = "http://www.mk-lips.com/HDToLIPS.asmx?op=CheckUser";
		 String params = "strUserID=HDFY&strPwd=FY5TGB";
		 String method = "CheckUser";
		 TestFetchCookie.getResultMessage(url, params, method) ;
	}
	 
	 
	 /**
		 * @param url
		 *            请求的新接口路径
		 * @param method
		 *            请求方式（post）
		 * @return
		 * @throws IOException
		 */
		public static String getResultMessage(String url, String params, String method) throws IOException {
			String responseXml = "";
			// HttpClient httpClient = new HttpClient();
			HttpClient httpClient = new HttpClient(new HttpClientParams(), new SimpleHttpConnectionManager(true));
			httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
			httpClient.getHttpConnectionManager().getParams().setSoTimeout(40000);
			PostMethod postMethod = new PostMethod(url);
			postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
			postMethod.getParams().setSoTimeout(30 * 1000);
			String[] paramStr = params.split("&");// 按“&”拆分
			if (paramStr != null && paramStr.length > 0) {
				for (int i = 0; i < paramStr.length; i++) {
					postMethod.addParameter(paramStr[i].split("=")[0], paramStr[i].split("=")[1]);// 按“=”拆分，第一个做为参数名，第二个作为参数值
				}
			}
			try {
				httpClient.executeMethod(postMethod); // post数据
				responseXml = postMethod.getResponseBodyAsString();
			} catch (HttpException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				postMethod.releaseConnection();

			}
			System.out.println("RE : " + method + " : " + url + "?" + params + " : " + responseXml);
			Cookie[] cookies = httpClient.getState().getCookies();
			if(cookies == null||cookies.length==0){
				return null;
			}
			Cookie cookie = cookies[0];
			System.out.println("用户请求时返回的Cookie信息:"+cookie.toString());
			Map<String,Cookie> cookieMap = new HashMap<String, Cookie>();
			cookieMap.put("cookie", cookie);
			return responseXml;

		}
	 
}
