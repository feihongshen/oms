package cn.explink.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 读取文件路径配置文件
 *
 */
public class ResourceBundleUtil {
	
	private static Logger logger = LoggerFactory.getLogger(ResourceBundleUtil.class);
	
	private static ResourceBundle rbint = null;
	//初始化
	 static {   
        String proFilePath = "/apps/conf/javaconf/"+ System.getProperty("company") + "/oms-webapp.properties";
	    //开发使用
        String development = System.getProperty("development");
	    if(null != development && development.length() > 0){
	    	proFilePath = "oms-webapp";//开发直接读取classpath下文件
	    	rbint = ResourceBundle.getBundle(proFilePath);
	    }else{
	    	BufferedInputStream inputStream = null;
	        try {  
	            inputStream = new BufferedInputStream(new FileInputStream(proFilePath));
	            rbint = new PropertyResourceBundle(inputStream);  
	        } catch (FileNotFoundException e) {  
	        	logger.error("生成配置，找不到配置文件");
	            e.printStackTrace();  
	        } catch (IOException e) {  
	        	logger.error("生成配置，读取配置文件失败"); 
	            e.printStackTrace();  
	        }  
	    }
	}  
	
	public static final String dmpUrl = ResourceBundleUtil.rbint.getString("dmpUrl");
	
	//sql_server的数据库配置信息
	public static final String sqlServerDrivers = ResourceBundleUtil.rbint.getString("sqlserver_drivers");
	public static final String sqlServerExpressurlMaxconn = ResourceBundleUtil.rbint.getString("sqlserver_expressurl.maxconn");
	public static final String sqlServerExpressurlUrl = ResourceBundleUtil.rbint.getString("sqlserver_expressurl.url");
	public static final String sqlServerExpressurlUser = ResourceBundleUtil.rbint.getString("sqlserver_expressurl.user");
	public static final String sqlServerExpressurlPassword = ResourceBundleUtil.rbint.getString("sqlserver_expressurl.password");
	public static final String sqlServerExpressurlBatchNum = ResourceBundleUtil.rbint.getString("sqlserver_expressurl.batchNum");
	
	// redis 前缀
	public static final String RedisPrefix = ResourceBundleUtil.rbint.getString("redis.prefix");
}
