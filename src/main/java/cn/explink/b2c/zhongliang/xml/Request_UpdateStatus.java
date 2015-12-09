package cn.explink.b2c.zhongliang.xml;

import javax.xml.bind.annotation.XmlElement;

public class Request_UpdateStatus {
	private String SendorderID = "";
	private String InfoType = "";
	private String OrderStatus = "";
	private String ChangeTime = "";
	private String MailNo = "";
	private String Remark = "";

	@XmlElement(name = "SendorderID")
	public String getSendorderID() {
		return this.SendorderID;
	}

	public void setSendorderID(String sendorderID) {
		this.SendorderID = sendorderID;
	}

	@XmlElement(name = "InfoType")
	public String getInfoType() {
		return this.InfoType;
	}

	public void setInfoType(String infoType) {
		this.InfoType = infoType;
	}

	@XmlElement(name = "OrderStatus")
	public String getOrderStatus() {
		return this.OrderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.OrderStatus = orderStatus;
	}

	@XmlElement(name = "ChangeTime")
	public String getChangeTime() {
		return this.ChangeTime;
	}

	public void setChangeTime(String changeTime) {
		this.ChangeTime = changeTime;
	}

	@XmlElement(name = "MailNo")
	public String getMailNo() {
		return this.MailNo;
	}

	public void setMailNo(String mailNo) {
		this.MailNo = mailNo;
	}

	@XmlElement(name = "Remark")
	public String getRemark() {
		return this.Remark;
	}

	public void setRemark(String remark) {
		this.Remark = remark;
	}
}
