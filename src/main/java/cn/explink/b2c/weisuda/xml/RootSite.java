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
public class RootSite {
	private Sites sites;

	/**
	 * @return the sites
	 */
	@XmlElement(name = "sites")
	public Sites getSites() {
		return this.sites;
	}

	/**
	 * @param sites
	 *            the sites to set
	 */
	public void setSites(Sites sites) {
		this.sites = sites;
	}

}
