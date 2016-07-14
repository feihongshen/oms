package cn.explink.b2c.tpsdo.bean;

import java.sql.Timestamp;

public class OrderTrackToTPSVo {
	private String cwb;
	private long floworderid;//'流程环节id'
	private String orderFlowJson;//'轨迹报文',
	private Timestamp tracktime;//'轨迹实际时间',
	private int status;//'状态：1未处理，2完成转业务，3错误，4 忽略'
	private String errinfo;// '异常信息',
	private int trytime;//推送次数
	private long customerid;//客户id
	
	private String tpstranscwb;//tps运单号
	private long flowordertype;
	
	public String getCwb() {
		return cwb;
	}
	public void setCwb(String cwb) {
		this.cwb = cwb;
	}
	public long getFloworderid() {
		return floworderid;
	}
	public void setFloworderid(long floworderid) {
		this.floworderid = floworderid;
	}
	public String getOrderFlowJson() {
		return orderFlowJson;
	}
	public void setOrderFlowJson(String orderFlowJson) {
		this.orderFlowJson = orderFlowJson;
	}

	public Timestamp getTracktime() {
		return tracktime;
	}
	public void setTracktime(Timestamp tracktime) {
		this.tracktime = tracktime;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getTpstranscwb() {
		return tpstranscwb;
	}
	public void setTpstranscwb(String tpstranscwb) {
		this.tpstranscwb = tpstranscwb;
	}
	public String getErrinfo() {
		return errinfo;
	}
	public void setErrinfo(String errinfo) {
		this.errinfo = errinfo;
	}
	public int getTrytime() {
		return trytime;
	}
	public void setTrytime(int trytime) {
		this.trytime = trytime;
	}
	public long getFlowordertype() {
		return flowordertype;
	}
	public void setFlowordertype(long flowordertype) {
		this.flowordertype = flowordertype;
	}
	public long getCustomerid() {
		return customerid;
	}
	public void setCustomerid(long customerid) {
		this.customerid = customerid;
	}
	
	
}
