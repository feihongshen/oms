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
public class RootPS {

	private List<GetUnVerifyOrders_back_Item> item;

	@XmlElement(name = "item")
	public List<GetUnVerifyOrders_back_Item> getItem() {
		return this.item;
	}

	public void setItem(List<GetUnVerifyOrders_back_Item> item) {
		this.item = item;
	}
}
