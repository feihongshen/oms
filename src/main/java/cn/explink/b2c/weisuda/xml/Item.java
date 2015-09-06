/**
 *
 */
package cn.explink.b2c.weisuda.xml;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Administrator
 *
 */
public class Item {
	private String order_id;
	private String courier_code;
	private String order_status;
	private String opertime;
	private String reason;
	private String memo;
	private String paymethod;
	private String money;
	private String pay_status;

	@XmlElement(name = "order_id")
	public String getOrder_id() {
		return this.order_id;
	}

	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}

	@XmlElement(name = "order_status")
	public String getOrder_status() {
		return this.order_status;
	}

	public void setOrder_status(String order_status) {
		this.order_status = order_status;
	}

	@XmlElement(name = "courier_code")
	public String getCourier_code() {
		return this.courier_code;
	}

	public void setCourier_code(String courier_code) {
		this.courier_code = courier_code;
	}

	@XmlElement(name = "opertime")
	public String getOpertime() {
		return this.opertime;
	}

	public void setOpertime(String opertime) {
		this.opertime = opertime;
	}

	@XmlElement(name = "reason")
	public String getReason() {
		return this.reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@XmlElement(name = "memo")
	public String getMemo() {
		return this.memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	@XmlElement(name = "paymethod")
	public String getPaymethod() {
		return this.paymethod;
	}

	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
	}

	@XmlElement(name = "money")
	public String getMoney() {
		return this.money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	@XmlElement(name = "pay_status")
	public String getPay_status() {
		return this.pay_status;
	}

	public void setPay_status(String pay_status) {
		this.pay_status = pay_status;
	}

}
