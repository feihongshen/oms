/**
 * 
 */
package cn.explink.b2c.gxdx;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * @ClassName: OrderStatesList
 * @Description: TODO
 * @Author: 王强
 * @Date: 2015年11月16日上午10:08:14
 */
public class OrderStatesList {
	private List<OrderState> orderState;

	@XmlElement(name = "OrderState")
	public List<OrderState> getOrderState() {
		return orderState;
	}

	public void setOrderState(List<OrderState> orderState) {
		this.orderState = orderState;
	}

}
