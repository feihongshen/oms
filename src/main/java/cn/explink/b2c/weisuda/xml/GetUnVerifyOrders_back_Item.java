package cn.explink.b2c.weisuda.xml;

import javax.xml.bind.annotation.XmlElement;

public class GetUnVerifyOrders_back_Item extends Item {
	private String consignee;
	private String reject_map;
	private String delay_reason;
	private String is_open;
	private String transport_no;

	@XmlElement(name = "consignee")
	public String getConsignee() {
		return this.consignee;
	}

	public void setConsignee(String consignee) {
		this.consignee = consignee;
	}


	@XmlElement(name = "reject_map")
	public String getReject_map() {
		return this.reject_map;
	}

	public void setReject_map(String reject_map) {
		this.reject_map = reject_map;
	}

	@XmlElement(name = "delay_reason")
	public String getDelay_reason() {
		return this.delay_reason;
	}

	public void setDelay_reason(String delay_reason) {
		this.delay_reason = delay_reason;
	}

	/**
	 * @return the is_open
	 */
	public String getIs_open() {
		return this.is_open;
	}

	/**
	 * @param is_open the is_open to set
	 */
	public void setIs_open(String is_open) {
		this.is_open = is_open;
	}
	
	@XmlElement(name = "transport_no")
	public String getTransport_no() {
		return transport_no;
	}

	public void setTransport_no(String transport_no) {
		this.transport_no = transport_no;
	}

}
