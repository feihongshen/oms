package cn.explink.b2c.jiuye.jsondto;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 *  XML的节点
 * @author Administrator
 *
 */
public class JiuyeXMLNote {
	
	@JsonProperty(value = "WorkCode") 
	private String workCode; 
	@JsonProperty(value = "OperationPerson")
	private String operationPerson; 
	@JsonProperty(value = "OperationPhone")
	private String operationPhone; 
	@JsonProperty(value = "DelveryTel")
	private String delveryTel; 
	@JsonProperty(value = "OperationTime")
	private String operationTime; 
	@JsonProperty(value = "WorkStatus")
	private String workStatus; 
	@JsonProperty(value = "OperationDesc")
	private String operationDesc; 
	@JsonProperty(value = "ClientCode")
	private String clientCode;


	public String getWorkCode() {
		return workCode;
	}


	public void setWorkCode(String workCode) {
		this.workCode = workCode;
	}


	public String getOperationPerson() {
		return operationPerson;
	}


	public void setOperationPerson(String operationPerson) {
		this.operationPerson = operationPerson;
	}


	public String getOperationPhone() {
		return operationPhone;
	}


	public void setOperationPhone(String operationPhone) {
		this.operationPhone = operationPhone;
	}


	public String getDelveryTel() {
		return delveryTel;
	}


	public void setDelveryTel(String delveryTel) {
		this.delveryTel = delveryTel;
	}


	public String getOperationTime() {
		return operationTime;
	}


	public void setOperationTime(String operationTime) {
		this.operationTime = operationTime;
	}


	public String getWorkStatus() {
		return workStatus;
	}


	public void setWorkStatus(String workStatus) {
		this.workStatus = workStatus;
	}


	public String getOperationDesc() {
		return operationDesc;
	}


	public void setOperationDesc(String operationDesc) {
		this.operationDesc = operationDesc;
	}


	public String getClientCode() {
		return clientCode;
	}


	public void setClientCode(String clientCode) {
		this.clientCode = clientCode;
	} 
	
}
