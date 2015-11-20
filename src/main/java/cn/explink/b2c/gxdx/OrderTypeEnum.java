/**
 * 
 */
package cn.explink.b2c.gxdx;

import cn.explink.enumutil.CwbOrderTypeIdEnum;

/**
 * @ClassName: OrderTypeEnum 
 * @Description: TODO
 * @Author: 王强
 * @Date: 2015年11月16日下午3:53:46
 */
public enum OrderTypeEnum {
	//运单类型
	T0("T0",CwbOrderTypeIdEnum.Peisong.getValue(),"配送订单"),
	T1("T1",CwbOrderTypeIdEnum.Shangmenhuan.getValue(),"上门换货单"),
	T2("T2",CwbOrderTypeIdEnum.Shangmentui.getValue(),"上门退货单");
	
	private String state;
	private int orderType;
	private String text;
	/**
	 * 
	 */
	private OrderTypeEnum() {
		// TODO Auto-generated constructor stub
	}
	private OrderTypeEnum(String state,int orderType,String text) {
		this.state = state;
		this.orderType = orderType;
		this.text = text;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public int getOrderType() {
		return orderType;
	}
	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}
