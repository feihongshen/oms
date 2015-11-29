package cn.explink.b2c.feiniuwang;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnAllData {
	@JsonProperty(value = "logistics_interface")
	private ReturnData logistics_interface;
	@JsonProperty(value = "data_digest")
	private String data_digest;
	@JsonProperty(value = "msg_type")
	private String msg_type;
	@JsonProperty(value = "logistic_provider_id")
	private String logistic_provider_id;
	
	public ReturnData getLogistics_interface() {
		return logistics_interface;
	}
	public void setLogistics_interface(ReturnData logistics_interface) {
		this.logistics_interface = logistics_interface;
	}
	public String getData_digest() {
		return data_digest;
	}
	public void setData_digest(String data_digest) {
		this.data_digest = data_digest;
	}
	public String getMsg_type() {
		return msg_type;
	}
	public void setMsg_type(String msg_type) {
		this.msg_type = msg_type;
	}
	public String getLogistic_provider_id() {
		return logistic_provider_id;
	}
	public void setLogistic_provider_id(String logistic_provider_id) {
		this.logistic_provider_id = logistic_provider_id;
	}
	
}
