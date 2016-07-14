package cn.explink.b2c.shenzhoushuma.xmldto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * 轨迹数据实体
 * @author yurong.liang 2016-04-28
 */
@XmlRootElement(name = "Step")
@XmlType(propOrder = { "opPoint", "opDescribe", "operateTime", "operator",
		"busiOperateTime", "busiOperator", 
		"deliveryMan", "deliveryManPhone","memo"})
public class StepNode {
	private String doId; // 科捷交接单号
	private String opPoint;// 节点编码
	private String opDescribe;// 节点描述
	private String operateTime;// 系统操作时间
	private String operator;// 系统操作人
	private String busiOperateTime;// 业务操作时间
	private String busiOperator;// 业务操作人
	private String deliveryMan;// 配送员
	private String deliveryManPhone;// 配送员手机号
	private String memo;// 备注

	@XmlElement(name = "OpPoint")
	public String getOpPoint() {
		return opPoint;
	}

	@XmlElement(name = "OpDescribe")
	public String getOpDescribe() {
		return opDescribe;
	}

	@XmlElement(name = "OperateTime")
	public String getOperateTime() {
		return operateTime;
	}

	@XmlElement(name = "Operator")
	public String getOperator() {
		return operator;
	}

	@XmlElement(name = "BusiOperateTime")
	public String getBusiOperateTime() {
		return busiOperateTime;
	}

	@XmlElement(name = "BusiOperator")
	public String getBusiOperator() {
		return busiOperator;
	}

	@XmlElement(name = "DeliveryMan")
	public String getDeliveryMan() {
		return deliveryMan;
	}

	@XmlElement(name = "DeliveryManPhone")
	public String getDeliveryManPhone() {
		return deliveryManPhone;
	}

	@XmlElement(name = "Memo") 
	public String getMemo() {
		return memo; 
	}
	 

	@XmlTransient
	// 不需要被映射为XML
	public String getDoId() {
		return doId;
	}

	// =======setter============
	public void setOpPoint(String opPoint) {
		this.opPoint = opPoint;
	}

	public void setOpDescribe(String opDescribe) {
		this.opDescribe = opDescribe;
	}

	public void setOperateTime(String operateTime) {
		this.operateTime = operateTime;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public void setBusiOperateTime(String busiOperateTime) {
		this.busiOperateTime = busiOperateTime;
	}

	public void setBusiOperator(String busiOperator) {
		this.busiOperator = busiOperator;
	}

	public void setDeliveryMan(String deliveryMan) {
		this.deliveryMan = deliveryMan;
	}

	public void setDeliveryManPhone(String deliveryManPhone) {
		this.deliveryManPhone = deliveryManPhone;
	}

	public void setMemo(String memo) {
		this.memo = memo; 
	}
	 

	public void setDoId(String doId) {
		this.doId = doId;
	}
}
