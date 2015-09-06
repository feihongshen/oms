/**
 * 
 */
package cn.explink.b2c.weisuda.xml;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Administrator
 * 
 */
public class Delivery {

	private String code;
	private String handleCode;
	private String msg;

	/**
	 * @return the code
	 */
	@XmlElement(name = "code")
	public String getCode() {
		return this.code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
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
