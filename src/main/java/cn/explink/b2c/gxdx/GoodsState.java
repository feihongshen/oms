/**
 * 
 */
package cn.explink.b2c.gxdx;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @ClassName: GoodsState
 * @Description: TODO
 * @Author: 王强
 * @Date: 2015年11月11日上午10:08:17
 */

@XmlRootElement(name = "UpdateInfo")
public class GoodsState {

	// 配送公司编号
	private String logisticProviderID;

	private OrderStatesList orderStates;

	@XmlElement(name = "LogisticProviderID")
	public String getLogisticProviderID() {
		return logisticProviderID;
	}
	@XmlElement(name = "OrderStates")
	public OrderStatesList getOrderStates() {
		return orderStates;
	}

	public void setOrderStates(OrderStatesList orderStates) {
		this.orderStates = orderStates;
	}

	public void setLogisticProviderID(String logisticProviderID) {
		this.logisticProviderID = logisticProviderID;
	}

}
