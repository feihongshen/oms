/**
 * 
 */
package cn.explink.b2c.weisuda.xml;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Administrator
 * 
 */
public class Order {
	private String order_id;
	private String handleCode;
	private String msg;

	/**
	 * @return the order_id
	 */
	@XmlElement(name = "order_id")
	public String getOrder_id() {
		return this.order_id;
	}

	/**
	 * @param order_id
	 *            the order_id to set
	 */
	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}

	/**
	 * @return the handleCode
	 */
	@XmlElement(name = "handleCode")
	public String getHandleCode() {
		return this.handleCode;
	}

	/**
	 * @param handleCode
	 *            the handleCode to set
	 */
	public void setHandleCode(String handleCode) {
		this.handleCode = handleCode;
	}

	/**
	 * @return the msg
	 */
	@XmlElement(name = "msg")
	public String getMsg() {
		return this.msg;
	}

	/**
	 * @param msg
	 *            the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

}
