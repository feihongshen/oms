/**
 * 
 */
package cn.explink.b2c.weisuda.xml;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Administrator
 * 
 */
public class RootDelivery {
	private Lists lists;

	/**
	 * @return the lists
	 */
	@XmlElement(name = "lists")
	public Lists getLists() {
		return this.lists;
	}

	/**
	 * @param lists
	 *            the lists to set
	 */
	public void setLists(Lists lists) {
		this.lists = lists;
	}

}
