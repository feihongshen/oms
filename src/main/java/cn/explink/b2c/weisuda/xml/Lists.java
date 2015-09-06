/**
 * 
 */
package cn.explink.b2c.weisuda.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Administrator
 * 
 */
@XmlRootElement(name = "root")
public class Lists {
	private List<Delivery> delivery;

	/**
	 * @return the delivery
	 */
	@XmlElement(name = "lists")
	public List<Delivery> getDelivery() {
		return this.delivery;
	}

	/**
	 * @param delivery
	 *            the delivery to set
	 */
	public void setDelivery(List<Delivery> delivery) {
		this.delivery = delivery;
	}
}
