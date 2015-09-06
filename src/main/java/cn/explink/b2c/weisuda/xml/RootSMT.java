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
public class RootSMT{

	private List<Getback_Item> item;

	@XmlElement(name = "item")
	public List<Getback_Item> getItem() {
		return this.item;
	}

	public void setItem(List<Getback_Item> item) {
		this.item = item;
	}
}
