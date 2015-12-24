package cn.explink.b2c.zhongliang.xml;

import javax.xml.bind.annotation.XmlElement;


public class Response_Order {
	private String LogID="";
	private String SendOrderID="";
	private String MailNO="";
	private String DealStatus="";
	private String Reason="";
	@XmlElement(name="LogID")
	public String getLogID() {
		return LogID;
	}
	
	public void setLogID(String logID) {
		LogID = logID;
	}

	@XmlElement(name="SendOrderID")
	public String getSendOrderID() {
		return SendOrderID;
	}
	
	public void setSendOrderID(String sendOrderID) {
		SendOrderID = sendOrderID;
	}

	@XmlElement(name="MailNO")
	public String getMailNO() {
		return MailNO;
	}

	public void setMailNO(String mailNO) {
		MailNO = mailNO;
	}

	@XmlElement(name="DealStatus")
	public String getDealStatus() {
		return DealStatus;
	}

	public void setDealStatus(String dealStatus) {
		DealStatus = dealStatus;
	}

	
	@XmlElement(name="Reason")
	public String getReason() {
		return Reason;
	}

	public void setReason(String reason) {
		Reason = reason;
	}

}
