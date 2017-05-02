package cn.explink.b2c.liantongordercenter.xmlDto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Response")
public class LianTongResponseDto {
	private String service;
	private String head;
	private ResponseBodyDto responseBodyDto;

	@XmlAttribute(name = "service")
	public String getService() {
		return this.service;
	}

	public void setService(String service) {
		this.service = service;
	}

	@XmlElement(name = "Head")
	public String getHead() {
		return this.head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	@XmlElement(name = "Body")
	public ResponseBodyDto getResponseBodyDto() {
		return this.responseBodyDto;
	}

	public void setResponseBodyDto(ResponseBodyDto responseBodyDto) {
		this.responseBodyDto = responseBodyDto;
	}

}
