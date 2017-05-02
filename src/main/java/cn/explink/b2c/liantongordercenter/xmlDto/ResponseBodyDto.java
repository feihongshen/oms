package cn.explink.b2c.liantongordercenter.xmlDto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Body")
public class ResponseBodyDto {
	private RouteResponse routeResponse;

	@XmlElement(name = "RouteResponse")
	public RouteResponse getRouteResponse() {
		return this.routeResponse;
	}

	public void setRouteResponse(RouteResponse routeResponse) {
		this.routeResponse = routeResponse;
	}

}
