/**
 * 
 */
package cn.explink.b2c.gxdx;

import javax.xml.bind.annotation.XmlElement;

/**
 * @ClassName: OrderState
 * @Description: TODO
 * @Author: 王强
 * @Date: 2015年11月11日上午10:25:00
 */
public class OrderState {
	// 运单号
	private String waybillNo;
	// 状态
	private String state;
	// 状态产生时间
	private String stateTime;
	// 操作人
	private String operName;
	// 操作时间
	private String operateTime;

	private String operatorUnit;
	// 配送员
	private String deliveryMan;

	private String deliveryMobile;
	// 原因编号
	private String reason;

	private String remark;

	@XmlElement(name = "WaybillNo")
	public String getWaybillNo() {
		return waybillNo;
	}

	public void setWaybillNo(String waybillNo) {
		this.waybillNo = waybillNo;
	}

	@XmlElement(name = "State")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@XmlElement(name = "StateTime")
	public String getStateTime() {
		return stateTime;
	}

	public void setStateTime(String stateTime) {
		this.stateTime = stateTime;
	}

	@XmlElement(name = "OperName")
	public String getOperName() {
		return operName;
	}

	public void setOperName(String operName) {
		this.operName = operName;
	}

	@XmlElement(name = "OperateTime")
	public String getOperateTime() {
		return operateTime;
	}

	public void setOperateTime(String operateTime) {
		this.operateTime = operateTime;
	}

	@XmlElement(name = "OperatorUnit")
	public String getOperatorUnit() {
		return operatorUnit;
	}

	public void setOperatorUnit(String operatorUnit) {
		this.operatorUnit = operatorUnit;
	}

	@XmlElement(name = "DeliveryMan")
	public String getDeliveryMan() {
		return deliveryMan;
	}

	public void setDeliveryMan(String deliveryMan) {
		this.deliveryMan = deliveryMan;
	}

	@XmlElement(name = "DeliveryMobile")
	public String getDeliveryMobile() {
		return deliveryMobile;
	}

	public void setDeliveryMobile(String deliveryMobile) {
		this.deliveryMobile = deliveryMobile;
	}

	@XmlElement(name = "Reason")
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@XmlElement(name = "Remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
