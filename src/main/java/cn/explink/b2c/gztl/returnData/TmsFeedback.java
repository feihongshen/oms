package cn.explink.b2c.gztl.returnData;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TMSFeedback")
@XmlAccessorType(XmlAccessType.FIELD)
public class TmsFeedback {
	/**
	 * <TMSFeedback> <id>275994899,275994898</id> <success>true</success>
	 * <remark/> </TMSFeedback>
	 */
	private String id;
	private String success;
	private String remark;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSuccess() {
		return this.success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
