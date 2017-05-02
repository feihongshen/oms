package cn.explink.b2c.liantongordercenter;

public class LianTongOrderCenterXMLNote {
	private int id;// flowordertypid
	private String mailNO;// 物流运单号
	private String orderId;// 客户订单号
	private String acceptTime;// 路由节点发生的时间
	private String acceptAddress;// 路由节点发生的地点
	private String remark;// 路由节点具体描述
	private String opcode;// 路由节点操作码

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMailNO() {
		return this.mailNO;
	}

	public void setMailNO(String mailNO) {
		this.mailNO = mailNO;
	}

	public String getOrderId() {
		return this.orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getAcceptTime() {
		return this.acceptTime;
	}

	public void setAcceptTime(String acceptTime) {
		this.acceptTime = acceptTime;
	}

	public String getAcceptAddress() {
		return this.acceptAddress;
	}

	public void setAcceptAddress(String acceptAddress) {
		this.acceptAddress = acceptAddress;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getOpcode() {
		return this.opcode;
	}

	public void setOpcode(String opcode) {
		this.opcode = opcode;
	}

}
