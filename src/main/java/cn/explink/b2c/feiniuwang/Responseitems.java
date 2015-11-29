package cn.explink.b2c.feiniuwang;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Responseitems {
	@JsonProperty(value = "mailno")
	private String mailno;
	@JsonProperty(value = "returnno")
	private String returnno;
	@JsonProperty(value = "success")
	private String success;
	@JsonProperty(value = "reason")
	private String reason;
	
	public String getMailno() {
		return mailno;
	}
	public void setMailno(String mailno) {
		this.mailno = mailno;
	}
	public String getReturnno() {
		return returnno;
	}
	public void setReturnno(String returnno) {
		this.returnno = returnno;
	}
	public String getSuccess() {
		return success;
	}
	public void setSuccess(String success) {
		this.success = success;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
}
