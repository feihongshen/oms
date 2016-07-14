package cn.explink.b2c.shenzhoushuma.xmldto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 请求数据实体
 * @author yurong.liang 2016-04-28
 */
@XmlRootElement(name = "UpdateInfo")
@XmlType(propOrder = { "logisticProvider", "logisticProviderId", "doId","mailNo","steps","MD5Key"})
public class RequestXmlData {
	private String logisticProvider; // 代运简称
	private String logisticProviderId; // 代运编码
	private String doId; // 科捷交接单号
	private String mailNo; // 代运运单号
	private StepsNode steps; // 轨迹信息集合
	private String MD5Key;// 秘钥
	
	@XmlElement(name = "LogisticProvider")
	public String getLogisticProvider() {
		return logisticProvider;
	}
	
	@XmlElement(name = "LogisticProviderId")
	public String getLogisticProviderId() {
		return logisticProviderId;
	}
	
	@XmlElement(name = "DoId")
	public String getDoId() {
		return doId;
	}

	@XmlElement(name = "MailNo")
	public String getMailNo() {
		return mailNo;
	}
	
	@XmlElement(name = "Steps")
	public StepsNode getSteps() {
		return steps;
	}

	@XmlElement(name = "MD5Key")
	public String getMD5Key() {
		return MD5Key;
	}

	public void setLogisticProvider(String logisticProvider) {
		this.logisticProvider = logisticProvider;
	}

	public void setLogisticProviderId(String logisticProviderId) {
		this.logisticProviderId = logisticProviderId;
	}

	public void setDoId(String doId) {
		this.doId = doId;
	}

	public void setMailNo(String mailNo) {
		this.mailNo = mailNo;
	}

	public void setSteps(StepsNode steps) {
		this.steps = steps;
	}

	public void setMD5Key(String mD5Key) {
		MD5Key = mD5Key;
	}
}
