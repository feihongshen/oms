package cn.explink.b2c.liantongordercenter.xmlDto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Route")
public class RouteDto {
	private String acceptTime;
	private String acceptAddress;
	private String remark;
	private String opcode;

	@XmlAttribute(name = "accept_time")
	public String getAcceptTime() {
		return this.acceptTime;
	}

	public void setAcceptTime(String acceptTime) {
		this.acceptTime = acceptTime;
	}

	@XmlAttribute(name = "accept_address")
	public String getAcceptAddress() {
		return this.acceptAddress;
	}

	public void setAcceptAddress(String acceptAddress) {
		this.acceptAddress = acceptAddress;
	}

	@XmlAttribute(name = "remark")
	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@XmlAttribute(name = "opcode")
	public String getOpcode() {
		return this.opcode;
	}

	public void setOpcode(String opcode) {
		this.opcode = opcode;
	}

}
