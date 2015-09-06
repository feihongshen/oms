package cn.explink.b2c.Wholeline;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 下游存储易派信息发送上游的json格式
 *
 * @author Administrator
 *
 */
@XmlRootElement(name = "OrderFLow")
public class OrderFlowDto {

	private String userCode;
	private String requestTime;
	private String sign;

	private String cwb;
	private String operatortime; // 操作时间
	private String flowordertype; // 操作状态
	private long deliverystate; // 配送结果
	private int paytype; // 支付方式
	private String payremark;
	private String floworderdetail;// 操作详情描述
	private String exptcode; // 异常码
	private String custid; // 唯一不重复的记录标识,用于异步回传下游最终的状态。
	private String deliverymobile; // 派送员电话
	private String deliveryname; // 派送员
	private String reamrk1;

	/**
	 * @return the reamrk1
	 */
	public String getReamrk1() {
		return this.reamrk1;
	}

	/**
	 * @param reamrk1 the reamrk1 to set
	 */
	public void setReamrk1(String reamrk1) {
		this.reamrk1 = reamrk1;
	}
	public String getDeliverymobile() {
		return this.deliverymobile;
	}

	public void setDeliverymobile(String deliverymobile) {
		this.deliverymobile = deliverymobile;
	}

	public String getDeliveryname() {
		return this.deliveryname;
	}

	public void setDeliveryname(String deliveryname) {
		this.deliveryname = deliveryname;
	}

	public String getUserCode() {
		return this.userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public String getRequestTime() {
		return this.requestTime;
	}

	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}

	public String getSign() {
		return this.sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getCwb() {
		return this.cwb;
	}

	public void setCwb(String cwb) {
		this.cwb = cwb;
	}

	public String getOperatortime() {
		return this.operatortime;
	}

	public void setOperatortime(String operatortime) {
		this.operatortime = operatortime;
	}

	public String getFlowordertype() {
		return this.flowordertype;
	}

	public void setFlowordertype(String flowordertype) {
		this.flowordertype = flowordertype;
	}

	public long getDeliverystate() {
		return this.deliverystate;
	}

	public void setDeliverystate(long deliverystate) {
		this.deliverystate = deliverystate;
	}

	public int getPaytype() {
		return this.paytype;
	}

	public void setPaytype(int paytype) {
		this.paytype = paytype;
	}

	public String getPayremark() {
		return this.payremark;
	}

	public void setPayremark(String payremark) {
		this.payremark = payremark;
	}

	public String getFloworderdetail() {
		return this.floworderdetail;
	}

	public void setFloworderdetail(String floworderdetail) {
		this.floworderdetail = floworderdetail;
	}

	public String getExptcode() {
		return this.exptcode;
	}

	public void setExptcode(String exptcode) {
		this.exptcode = exptcode;
	}

	public String getCustid() {
		return this.custid;
	}

	public void setCustid(String custid) {
		this.custid = custid;
	}

}
