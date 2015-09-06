/**
 * 
 */
package cn.explink.b2c.weisuda.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Administrator
 * 
 */
public class Orders {
	private List<Order> order;

	/**
	 * @return the order
	 */
	@XmlElement(name = "order")
	public List<Order> getOrder() {
		return this.order;
	}

	/**
	 * @param order
	 *            the order to set
	 */
	public void setOrder(List<Order> order) {
		this.order = order;
	}
}
