package cn.explink.b2c.huanqiugou.respDto;

import javax.xml.bind.annotation.XmlElement;



public class ReturnDto {
	
	private String returnCode;
	private String returnDesc;
	private int returnFlag;
	private String resultInfo;
	@XmlElement(name="returnCode")
	public String getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}
	@XmlElement(name="returnDesc")
	public String getReturnDesc() {
		return returnDesc;
	}
	public void setReturnDesc(String returnDesc) {
		this.returnDesc = returnDesc;
	}
	@XmlElement(name="returnFlag")
	public int getReturnFlag() {
		return returnFlag;
	}
	public void setReturnFlag(int returnFlag) {
		this.returnFlag = returnFlag;
	}
	@XmlElement(name="resultInfo")
	public String getResultInfo() {
		return resultInfo;
	}
	public void setResultInfo(String resultInfo) {
		this.resultInfo = resultInfo;
	}
	
	
}
