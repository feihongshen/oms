package cn.explink.b2c.tpsdo.bean;

import java.sql.Timestamp;

public class OtherOrderTrackVo {
	private String cwb;
	private long floworderid;
	private String orderFlowJson;
	private String deliveryStateJson;
	private Timestamp tracktime;
	private int status;
	private String errinfo;
	private int trytime;
	
	private String tpsno;//tps运单号
	
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
	public String getDeliveryStateJson() {
		return deliveryStateJson;
	}
	public void setDeliveryStateJson(String deliveryStateJson) {
		this.deliveryStateJson = deliveryStateJson;
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
	public String getTpsno() {
		return tpsno;
	}
	public void setTpsno(String tpsno) {
		this.tpsno = tpsno;
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
	
	
}
