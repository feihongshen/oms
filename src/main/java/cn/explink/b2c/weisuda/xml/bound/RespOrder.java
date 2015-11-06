/**
 * 
 */
package cn.explink.b2c.weisuda.xml.bound;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Administrator
 * 
 */
public class RespOrder {
	private String order_id;

	@XmlElement(name = "order_id")
	public String getOrder_id() {
		return order_id;
	}

	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}

}
