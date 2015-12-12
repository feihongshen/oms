package cn.explink.util;

import java.io.IOException;
import net.sf.json.JSONObject;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.explink.b2c.tools.B2cTools;

/**
 * 对接常用处理
 * @author LX
 *
 */
@Service
public class B2cUtil {
	@Autowired
	B2cTools b2ctools;
	//获取配置信息==LX==
	@SuppressWarnings("unchecked")
	public <T> T getEntitySettingMethod(int key,Class<T> obj) {
		//b2ctools.getObjectMethod(key)为获取b2c 配置信息的接口
		String objectMethod = this.b2ctools.getObjectMethod(key).getJoint_property();
		if (objectMethod!=null) {
			JSONObject jsonObj = JSONObject.fromObject(objectMethod);
			return (T) JSONObject.toBean(jsonObj, obj);//将json对象转化为bean
		} else {
			return null;
		}
	}
	
	//将json字符串反序列化为任一个对象 ====LX=====(首先定义好一个javabean) 
	public <T> T getMLKdataMethod(String jsoncontent,Class<T> cla) throws JsonParseException, JsonMappingException, IOException {
		return (T) new ObjectMapper().readValue(jsoncontent,cla);
	}
}
