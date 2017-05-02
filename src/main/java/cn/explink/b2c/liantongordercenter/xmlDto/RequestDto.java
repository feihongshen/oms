package cn.explink.b2c.liantongordercenter.xmlDto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Request")
public class RequestDto {
	private String service;
	private String lang;
	private String head;
	private RequestBodyDto requestBodyDto;

	@XmlAttribute(name = "service")
	public String getService() {
		return this.service;
	}

	public void setService(String service) {
		this.service = service;
	}

	@XmlAttribute(name = "lang")
	public String getLang() {
		return this.lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	@XmlElement(name = "Head")
	public String getHead() {
		return this.head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	@XmlElement(name = "Body")
	public RequestBodyDto getRequestBodyDto() {
		return this.requestBodyDto;
	}

	public void setRequestBodyDto(RequestBodyDto requestBodyDto) {
		this.requestBodyDto = requestBodyDto;
	}

}
