package cn.explink.b2c.liantongordercenter.xmlDto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "UNI_BSS")
public class ResponseDto {

	private RequestHeadDto requestHeadDto;
	private RequestBodyDtoUni requestBodyDtoUni;
	

	@XmlElement(name = "UNI_BSS_HEAD")
	public RequestHeadDto getRequestHeadDto() {
		return this.requestHeadDto;
	}
	

	public void setRequestHeadDto(RequestHeadDto requestHeadDto) {
		this.requestHeadDto = requestHeadDto;
	}

	@XmlElement(name = "UNI_BSS_BODY")
	public RequestBodyDtoUni getRequestBodyDtoUni() {
		return requestBodyDtoUni;
	}


	public void setRequestBodyDtoUni(RequestBodyDtoUni requestBodyDtoUni) {
		this.requestBodyDtoUni = requestBodyDtoUni;
	}
	
	

}
