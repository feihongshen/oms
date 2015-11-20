package cn.explink.b2c.gxdx.xmldto.util;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
public class BeanToXml {
	public static String toXml(Object object) throws Exception{
		StringWriter writer = new StringWriter();
		JAXBContext jc = JAXBContext.newInstance(object.getClass());
		Marshaller marshaller = jc.createMarshaller();   
		marshaller.setProperty(Marshaller.JAXB_ENCODING,"UTF-8");//编码格式
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);//是否格式化生成的xml串   
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);//是否省略xml头信息（<?xml version="1.0" encoding="gb2312" standalone="yes"?>）   
		marshaller.marshal(object,writer);   
		return writer.toString();
	}
	
}
