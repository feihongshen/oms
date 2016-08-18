package cn.explink.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


public class XssFilter  extends HttpRequestWordFilter{

		@Override
		protected String filterParamValue(String paramValue) {
			return xssEncode(paramValue);
		}   


		/** 
	     * 将容易引起xss漏洞的半角字符直接替换成全角字符 </br>
	     * 如需再过滤其他HTML标签可在此配置
	     * @param s 
	     * @return 
	     */  
	    private  String xssEncode(String s) {
	    	if(StringUtils.isEmpty(s)){
	    		return s;
	    	}
	    	String tempStr = s;
	    	tempStr= tempStr.replaceAll("(?i)javascript", "ｊａｖａscript")
			    			.replaceAll("void", "ｖｏｉｄ")
			    			.replaceAll("alert", "ａｌｅｒｔ")
			    			.replaceAll("(?i)onabort","ｏｎabort")
			    			.replaceAll("(?i)onblur","ｏｎblur")
			    			.replaceAll("(?i)onchange","ｏｎchange")
			    			.replaceAll("(?i)onclick","ｏｎclick")
			    			.replaceAll("(?i)ondblclick","ｏｎdblclick")
			    			.replaceAll("(?i)onerror","ｏｎerror")
			    			.replaceAll("(?i)onfocus","ｏｎfocus")
			    			.replaceAll("(?i)onkeydown","ｏｎkeydown")
			    			.replaceAll("(?i)onkeypress","ｏｎkeypres")
			    			.replaceAll("(?i)onkeyup","ｏｎkeyup")
			    			.replaceAll("(?i)onload","ｏｎload")
			    			.replaceAll("(?i)onmousedown","ｏｎmousedown")
			    			.replaceAll("(?i)onmousemove","ｏｎmousemove")
			    			.replaceAll("(?i)onmouseout","ｏｎmouseout")
			    			.replaceAll("(?i)onmouseover","ｏｎmouseover")
			    			.replaceAll("(?i)onmouseup","ｏｎmouseup")
			    			.replaceAll("(?i)onreset","ｏｎreset")
			    			.replaceAll("(?i)onresize","ｏｎresize")
			    			.replaceAll("(?i)onselect","ｏｎselect")
			    			.replaceAll("(?i)onsubmit","ｏｎsubmit")
			    			.replaceAll("(?i)onunload","ｏｎunload");
	        return encodeHtmlTag(tempStr, "script");
	    }
	    
	    /**
		 * 将HTML标签里的尖括号转换为html转义字符 
		 * @param content 待处理字符串内容
		 * @param tag html标签名
		 * @return
		 */
	    private  String encodeHtmlTag(String content, String tag) {
	    	//如果为空字符串，则直接返回
	    	if(StringUtils.isEmpty(content)){
	    		return content;
	    	}
	        //String regxp = "<\\s*"+tag+"[^>]*?>"+"|"+"<\\s*\\/\\s*"+tag+"[^>]*?>"; 
	    	String regxp = "([\\s\t\n\r>]*)<\\s*"+tag+"[^>]*?>"+"|"+"<\\s*\\/\\s*"+tag+"[^>]*?>([\\s\t\n\r<]*)";
	        Pattern pattern = Pattern.compile(regxp, Pattern.CASE_INSENSITIVE);   
	        Matcher matcher = pattern.matcher(content); 
	        boolean hasNext = matcher.find();  
	        if(!hasNext){
	        	return content;
	        }
	        StringBuffer encodeSb = new StringBuffer();  
	        do{ 
	        	//获取字符串里对应的tag
	        	String tagIncontent = matcher.group();
	        	tagIncontent = tagIncontent.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	            matcher.appendReplacement(encodeSb, tagIncontent);     
	        }while (matcher.find());
	        
	        matcher.appendTail(encodeSb);   
	        return encodeSb.toString();   
	    }
		

}
