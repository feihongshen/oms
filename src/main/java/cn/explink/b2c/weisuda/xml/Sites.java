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
public class Sites {
	private List<Site> site;

	/**
	 * @return the site
	 */
	@XmlElement(name = "order")
	public List<Site> getSite() {
		return this.site;
	}

	/**
	 * @param site
	 *            the site to set
	 */
	public void setSite(List<Site> site) {
		this.site = site;
	}

}
