package cn.explink.b2c.meilinkai;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tools.B2CDataDAO;

@Service
public class SOAPService {
	@Autowired
	B2CDataDAO b2CDataDAO;
	private Logger logger = LoggerFactory.getLogger(SOAPService.class); 
	//请求校验用户
	public Map<String,Cookie> checkUser(String hdTOlipsUrl,String username,String pwd,String methodCheck) throws HttpException, IOException{
		String soapRequestData = 
				  "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
				+ "xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"
				+ "<soap12:Body>"
				+ "<CheckUser xmlns=\"http://tempuri.org/\">"
				+ "<strUserID>"+username+"</strUserID> "//用户名
				+ "<strPwd>"+pwd+"</strPwd>"//密码
				+ "</CheckUser>"
				+ "</soap12:Body>"
				+ "</soap12:Envelope>";
		
		this.logger.info("【玫琳凯】请求信息:"+soapRequestData);
		//"http://www.mk-lips.com/HDToLIPS.asmx?op=CheckUser"
		PostMethod postMethod = new PostMethod(hdTOlipsUrl+"?op="+methodCheck);

		// 然后把Soap请求数据添加到PostMethod中
		byte[] b = soapRequestData.getBytes("utf-8");
		InputStream is = new ByteArrayInputStream(b, 0, b.length);
		RequestEntity re = new InputStreamRequestEntity(is, b.length,"application/soap+xml; charset=utf-8");
		postMethod.setRequestEntity(re);

		// 最后生成一个HttpClient对象，并发出postMethod请求
		HttpClient httpClient = new HttpClient();
		int statusCode = httpClient.executeMethod(postMethod);
		if (statusCode == 200) {
			this.logger.info("调用成功！");
			String soapResponseData = postMethod.getResponseBodyAsString();
			String intStr = soapResponseData.substring(soapResponseData.indexOf("<CheckUserResult>")+17, soapResponseData.indexOf("</CheckUserResult>"));
			if("".equals(intStr)){
				logger.info("用户校验返回值为空,请查找原因,用户名:{},密码:{}",username,pwd);
				return null;
			}
			int inte = Integer.valueOf(intStr);
			if(inte != HDtoLipsEnum.REASON1.getResult()){
				return null;
			}
			Cookie[] cookies = httpClient.getState().getCookies();
			if(cookies == null||cookies.length==0){
				return null;
			}
			Cookie cookie = cookies[0];
			this.logger.info("用户请求时返回的Cookie信息:"+cookie.toString());
			Map<String,Cookie> cookieMap = new HashMap<String, Cookie>();
			cookieMap.put("cookie", cookie);
			return cookieMap;
		} else {
			this.logger.info("调用失败！错误码：" + statusCode);
			return null;
		}
	}
	
	public Integer hdTOlips(String hdTOlipsUrl,String methodHDToLIPSByWebService,Object[] objec,Cookie cookie) throws HttpException, IOException{
		String hdtolipsrequest = 
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>"
		      + "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"
		      + "<soap12:Body>"
		      + "<HDToLIPSByWebService xmlns=\"http://tempuri.org/\">"
		      + "<strDataID>"+objec[0]+"</strDataID>"
		      + "<strReceiveDate>"+objec[1]+"</strReceiveDate>"
		      + "<strComment>"+objec[2]+"</strComment>"
		      + "<strTrasactiontype>"+objec[3]+"</strTrasactiontype>"
		      + "</HDToLIPSByWebService>"
		      + "</soap12:Body>"
		      + "</soap12:Envelope>";
		
		logger.info("【玫琳凯】请求信息:"+hdtolipsrequest);
		//"http://www.mk-lips.com/HDToLIPS.asmx?op=CheckUser"
		PostMethod postMethod = new PostMethod(hdTOlipsUrl+"?op="+methodHDToLIPSByWebService);

		// 然后把Soap请求数据添加到PostMethod中
		byte[] b = hdtolipsrequest.getBytes("utf-8");
		InputStream is = new ByteArrayInputStream(b, 0, b.length);
		RequestEntity re = new InputStreamRequestEntity(is, b.length,"application/soap+xml; charset=utf-8");
		postMethod.setRequestEntity(re);

		// 最后生成一个HttpClient对象，并发出postMethod请求
		HttpClient httpClient = new HttpClient();
		HttpState state =new HttpState();
		state.addCookie(cookie);
		httpClient.setState(state);
		int statusCode = httpClient.executeMethod(postMethod);
		Integer inte = 1;
		if (statusCode == 200) {
			System.out.println("调用成功！");
			String soapResponseData = postMethod.getResponseBodyAsString();
			// <HDToLIPSByWebServiceResult>int</HDToLIPSByWebServiceResult>
			String intStr = soapResponseData.substring(soapResponseData.indexOf("<HDToLIPSByWebServiceResult>")+28, soapResponseData.indexOf("</HDToLIPSByWebServiceResult>"));
			if((intStr==null)||(intStr!= null&&("".equals(intStr)))){
				inte = -4;
			}else{
				inte = Integer.valueOf(intStr);
			}
		} else {
			inte = -5;
			this.logger.info("调用失败！错误码：" + statusCode);
		}
		return inte;
	}
	
	
	
}
