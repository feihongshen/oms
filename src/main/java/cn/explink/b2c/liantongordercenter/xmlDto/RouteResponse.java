package cn.explink.b2c.liantongordercenter.xmlDto;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RouteResponse")
public class RouteResponse {
	private String mailNo;
	private List<RouteDto> routeDtos;

	@XmlAttribute(name = "mailno")
	public String getMailNo() {
		return this.mailNo;
	}

	public void setMailNo(String mailNo) {
		this.mailNo = mailNo;
	}

	@XmlElement(name = "Route")
	public List<RouteDto> getRouteDtos() {
		return this.routeDtos;
	}

	public void setRouteDtos(List<RouteDto> routeDtos) {
		this.routeDtos = routeDtos;
	}

}
