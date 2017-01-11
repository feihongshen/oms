package cn.explink.b2c.mss;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnData {
	@JsonProperty(value = "partner_order_id")
	private String partnerOrderId;
	@JsonProperty(value = "o3_order_id")
	private String o3OrderId;
	@JsonProperty(value="message")
	private String message;
	public String getPartnerOrderId() {
		return partnerOrderId;
	}
	public void setPartnerOrderId(String partnerOrderId) {
		this.partnerOrderId = partnerOrderId;
	}
	public String getO3OrderId() {
		return o3OrderId;
	}
	public void setO3OrderId(String o3OrderId) {
		this.o3OrderId = o3OrderId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
