/**
 * 
 */
package cn.explink.b2c.weisuda.xml.bound;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Administrator
 * 
 */
@XmlRootElement(name = "root")
public class RespRootOrder {
	
	private List<String> order_id;
	
	private List<ErrorItem> errors;

	@XmlElement(name = "order_id")
	public List<String> getOrder_id() {
		return order_id;
	}

	public void setOrder_id(List<String> order_id) {
		this.order_id = order_id;
	}
	
	@XmlElementWrapper(name = "errors")
	@XmlElement(name = "error")
	public List<ErrorItem> getErrors() {
		return errors;
	}

	public void setErrors(List<ErrorItem> errors) {
		this.errors = errors;
	}

	public static class ErrorItem{
		
		private String 			orderId;
		
		private String			errorCode;
		
		private String			errorMsg;

		@XmlElement(name = "order_id")
		public String getOrderId() {
			return orderId;
		}

		public void setOrderId(String orderId) {
			this.orderId = orderId;
		}

		@XmlElement(name = "error_code")
		public String getErrorCode() {
			return errorCode;
		}

		public void setErrorCode(String errorCode) {
			this.errorCode = errorCode;
		}

		@XmlElement(name = "error_msg")
		public String getErrorMsg() {
			return errorMsg;
		}

		public void setErrorMsg(String errorMsg) {
			this.errorMsg = errorMsg;
		}
		
	}

}
