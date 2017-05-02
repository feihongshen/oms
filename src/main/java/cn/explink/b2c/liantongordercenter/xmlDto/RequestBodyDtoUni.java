package cn.explink.b2c.liantongordercenter.xmlDto;

import javax.xml.bind.annotation.XmlElement;

public class RequestBodyDtoUni {
	private String message;
	private String status;
	
	@XmlElement(name = "message")
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	@XmlElement(name = "status")
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	

}
