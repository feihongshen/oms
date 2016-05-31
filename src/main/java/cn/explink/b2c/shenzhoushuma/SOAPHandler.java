package cn.explink.b2c.shenzhoushuma;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author yurong.liang
 */
public class SOAPHandler {

	public static String httpInvokeWs(String endpointUrl,String requestXML) throws Exception {
		String nameSpace="http://impl.business.opinfo.carrier.service.tms.itl.com";
		String methodName="carrierOpInfo";
		StringBuffer result = null;
		OutputStream out = null;
		BufferedReader in = null;
		try {
			String soapActionString = nameSpace + "/" + methodName;
			String soap="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:impl=\"http://impl.business.opinfo.carrier.service.tms.itl.com\">"
						+"<soapenv:Header/>"
						+"<soapenv:Body>"
						+"<impl:carrierOpInfo>"
						+"<impl:xml>"
						+"<![CDATA["+requestXML+"]]>"
						+" </impl:xml>"
						+"</impl:carrierOpInfo>"
						+" </soapenv:Body>"
						+"</soapenv:Envelope>";
			
			URL url = new URL(endpointUrl);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestProperty("Content-Length", String.valueOf(soap.getBytes("UTF-8").length));
			httpConn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
			httpConn.setRequestProperty("soapActionString", soapActionString);

			httpConn.setRequestMethod("POST");
			httpConn.setReadTimeout(30000);
			httpConn.setConnectTimeout(30000);
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			out = httpConn.getOutputStream();
			out.write(soap.getBytes("UTF-8"));
			out.flush();

			InputStreamReader isr = new InputStreamReader(httpConn.getInputStream(), "UTF-8");

			in = new BufferedReader(isr);
			result = new StringBuffer();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				result.append(inputLine);
			}

		} catch (Throwable e) {
			e.printStackTrace();
			throw new Exception("WebService服务链路异常:" + e.getMessage(), e);
		} finally {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				in.close();
			}

		}
		System.out.println(result.toString());
		return result.toString();
	}

}
