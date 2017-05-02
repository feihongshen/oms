package cn.explink.b2c.liantongordercenter.xmlDto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Body")
public class RequestBodyDto {
	private RouteRequestDto routeRequestDto;

	@XmlElement(name = "RouteRequest")
	public RouteRequestDto getRouteRequestDto() {
		return this.routeRequestDto;
	}

	public void setRouteRequestDto(RouteRequestDto routeRequestDto) {
		this.routeRequestDto = routeRequestDto;
	}

}
