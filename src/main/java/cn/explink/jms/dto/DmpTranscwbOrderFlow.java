package cn.explink.jms.dto;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import cn.explink.enumutil.FlowOrderTypeEnum;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DmpTranscwbOrderFlow implements Serializable  {
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected long floworderid;
	protected String cwb;
	protected String scancwb;
	protected long branchid;
	protected Date credate;
	protected long userid;
	protected int flowordertype;
	protected int isnow;
	protected String floworderdetail;
	String comment = "";

	public DmpTranscwbOrderFlow() {
	}

	public DmpTranscwbOrderFlow(int floworderid, String cwb, String scancwb, long branchid, Timestamp credate, long userid, String floworderdetail, int flowordertype, String comment) {
		this.floworderid = floworderid;
		this.cwb = cwb;
		this.scancwb = scancwb;
		this.branchid = branchid;
		this.credate = credate;
		this.userid = userid;
		this.floworderdetail = floworderdetail;
		this.flowordertype = flowordertype;
		this.comment = comment;
	}

	/**
	 * @return 获得flowordertype的值对应的文字
	 */
	public String getFlowordertypeText() {
		for (FlowOrderTypeEnum fote : FlowOrderTypeEnum.values()) {
			if (fote.getValue() == flowordertype)
				return fote.getText();
		}
		return "";
	}
	
	public String getFlowordertypeMethod() {
		for (FlowOrderTypeEnum fote : FlowOrderTypeEnum.values()) {
			if (fote.getValue() == flowordertype)
				return fote.getMethod();
		}
		return "";
	}

	public long getFloworderid() {
		return floworderid;
	}

	public void setFloworderid(long floworderid) {
		this.floworderid = floworderid;
	}

	public String getCwb() {
		return cwb;
	}

	public void setCwb(String cwb) {
		this.cwb = cwb;
	}

	public String getScancwb() {
		return scancwb;
	}

	public void setScancwb(String scancwb) {
		this.scancwb = scancwb;
	}

	public long getBranchid() {
		return branchid;
	}

	public void setBranchid(long branchid) {
		this.branchid = branchid;
	}

	public Date getCredate() {
		return credate;
	}

	public void setCredate(Date credate) {
		this.credate = credate;
	}

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public String getFloworderdetail() {
		return floworderdetail;
	}

	public void setFloworderdetail(String floworderdetail) {
		this.floworderdetail = floworderdetail;
	}

	public int getFlowordertype() {
		return flowordertype;
	}

	public void setFlowordertype(int flowordertype) {
		this.flowordertype = flowordertype;
	}

	public int getIsnow() {
		return isnow;
	}

	public void setIsnow(int isnow) {
		this.isnow = isnow;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
