/**
 * 
 */
package cn.explink.b2c.weisuda.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Administrator
 * 
 */
@XmlRootElement(name = "root")
public class RootOrder {
	private Orders orders;

	/**
	 * @return the orders
	 */
	@XmlElement(name = "orders")
	public Orders getOrders() {
		return this.orders;
	}

	/**
	 * @param orders
	 *            the orders to set
	 */
	public void setOrders(Orders orders) {
		this.orders = orders;
	}

}
