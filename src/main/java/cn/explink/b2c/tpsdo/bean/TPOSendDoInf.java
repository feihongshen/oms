package cn.explink.b2c.tpsdo.bean;

import java.util.Date;

/**
 * 外单（第三方订单） 接口表PO_SEND_DO_INF实体类
 * @author gordon.zhou
 *
 */
public class TPOSendDoInf {
	private long id;
	private String cwb; //dmp订单号
	private String custcode; //客户编码
	private String transportno; //TPS运单号
	private String reqObjJson; //请求参数JSON串
	private String remark; //备注
	private int trytime; //已尝试推送次数
	private int isSent; //是否已发生成功0.未发送/不成功，1.已发送成功
	private Date createTime; //创建时间
	private Date updateTime; //更新时间
	private int operateType; //操作类型: 0 新增，1 修改，-1 取消  
	private int state; //是否失效。0.失效。1.有效  
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
	public String getCustcode() {
		return custcode;
	}
	public void setCustcode(String custcode) {
		this.custcode = custcode;
	}
	public String getTransportno() {
		return transportno;
	}
	public void setTransportno(String transportno) {
		this.transportno = transportno;
	}
	public String getReqObjJson() {
		return reqObjJson;
	}
	public void setReqObjJson(String reqObjJson) {
		this.reqObjJson = reqObjJson;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public int getTrytime() {
		return trytime;
	}
	public void setTrytime(int trytime) {
		this.trytime = trytime;
	}
	public int getIsSent() {
		return isSent;
	}
	public void setIsSent(int isSent) {
		this.isSent = isSent;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getOperateType() {
		return operateType;
	}
	public void setOperateType(int operateType) {
		this.operateType = operateType;
	}
	
	
	
}
