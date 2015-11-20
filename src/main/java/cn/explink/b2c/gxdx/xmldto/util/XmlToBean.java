package cn.explink.b2c.gxdx.xmldto.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import cn.explink.b2c.gxdx.GoodsState;

public class XmlToBean {
	public static Object toBean(String xmlstr,Object object) throws Exception{
		JAXBContext jc = JAXBContext.newInstance(object.getClass());
		Unmarshaller m = jc.createUnmarshaller();
		InputStream iStream = new ByteArrayInputStream(xmlstr.getBytes("UTF-8"));
		return m.unmarshal(iStream);
	}
	
}
