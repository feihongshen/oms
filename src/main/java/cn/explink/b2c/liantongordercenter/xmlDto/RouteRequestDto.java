package cn.explink.b2c.liantongordercenter.xmlDto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RouteRequest")
public class RouteRequestDto {

	private int trackingType;
	private String trackingNumber;

	@XmlAttribute(name = "tracking_number")
	public String getTrackingNumber() {
		return this.trackingNumber;
	}

	@XmlAttribute(name = "tracking_type")
	public int getTrackingType() {
		return this.trackingType;
	}

	public void setTrackingType(int trackingType) {
		this.trackingType = trackingType;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

}
