package cn.explink.domain;

import java.io.Serializable;
import java.util.Date;

public class OrderflowException implements Serializable {

	private static final long serialVersionUID = 3204289871578525521L;

	private long id;
	private String cwb; // 订单号
	private String orderflow; // 订单轨迹(json格式)
	private int sendCount; // 重发次数
	private int sendResult; // 发送结果(0未发送,1发送成功,2发送出错)
	private String remarks; // 备注
	private Date createdDtmLoc; // 创建时间
	private Date updatedDtmLoc; // 修改时间

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCwb() {
		return cwb;
	}

	public void setCwb(String cwb) {
		this.cwb = cwb;
	}

	public String getOrderflow() {
		return orderflow;
	}

	public void setOrderflow(String orderflow) {
		this.orderflow = orderflow;
	}

	public int getSendCount() {
		return sendCount;
	}

	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}

	public int getSendResult() {
		return sendResult;
	}

	public void setSendResult(int sendResult) {
		this.sendResult = sendResult;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Date getCreatedDtmLoc() {
		return createdDtmLoc;
	}

	public void setCreatedDtmLoc(Date createdDtmLoc) {
		this.createdDtmLoc = createdDtmLoc;
	}

	public Date getUpdatedDtmLoc() {
		return updatedDtmLoc;
	}

	public void setUpdatedDtmLoc(Date updatedDtmLoc) {
		this.updatedDtmLoc = updatedDtmLoc;
	}

}
