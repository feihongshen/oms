/**
 *
 */
package cn.explink.b2c.weisuda.xml;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Administrator
 *
 */
public class Getback_Item extends Item{

	private String collection_map;
	private Goods goods;



	/**
	 * @return the collection_map
	 */
	@XmlElement(name = "collection_map")
	public String getCollection_map() {
		return this.collection_map;
	}

	/**
	 * @param collection_map the collection_map to set
	 */
	public void setCollection_map(String collection_map) {
		this.collection_map = collection_map;
	}

	/**
	 * @return the goods
	 */
	@XmlElement(name = "goods")
	public Goods getGoods() {
		return this.goods;
	}

	/**
	 * @param goods the goods to set
	 */
	public void setGoods(Goods goods) {
		this.goods = goods;
	}

}
