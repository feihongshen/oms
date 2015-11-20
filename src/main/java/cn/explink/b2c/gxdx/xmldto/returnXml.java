/**
 * 
 */
package cn.explink.b2c.gxdx.xmldto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @ClassName: returnXml
 * @Description: TODO
 * @Author: 王强
 * @Date: 2015年11月17日上午10:58:46
 */
@XmlRootElement(name = "UpdateInfo")
public class returnXml {
	private String success;
	private String remark;
	@XmlElement(name = "Success")
	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}
	@XmlElement(name = "Remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
