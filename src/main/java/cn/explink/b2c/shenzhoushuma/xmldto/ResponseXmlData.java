package cn.explink.b2c.shenzhoushuma.xmldto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 神州数码 返回数据实体
 * @author yurong.liang 2016-04-28
 */
@XmlRootElement(name = "UpdateInfoResponse")
public class ResponseXmlData {
	
	private String doId;//捷科单号
	private String mailNo;//代运运单号
	private boolean flag;//是否成功
	private String desc;//备注
	
	@XmlElement(name = "DoId")
	public String getDoId() {
		return doId;
	}
	
	@XmlElement(name = "MailNo")
	public String getMailNo() {
		return mailNo;
	}
	
	@XmlElement(name = "Flag")
	public boolean isFlag() {
		return flag;
	}
	
	@XmlElement(name = "Desc")
	public String getDesc() {
		return desc;
	}
	
	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public void setDoId(String doId) {
		this.doId = doId;
	}
	public void setMailNo(String mailNo) {
		this.mailNo = mailNo;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
}
