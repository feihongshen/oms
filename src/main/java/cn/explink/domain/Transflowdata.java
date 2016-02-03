package cn.explink.domain;

public class Transflowdata {

	private long id;
	private String transcwb;
	private String cwb;
	private String createtime;//创建时间
	private String flowtime; //状态产生时间
	private String sendtime;  //推送时间
	private int  flowordertype;
	private String jsoncontent;
	private int  send_b2c_flag;// 推送状态:0未推送，1.推送成功，2推送失败 ,3(垃圾数据标识) 系统自动屏蔽
	private int  customerid;
	private int select_b2c_flag; //查询次数
	private String remark;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTranscwb() {
		return transcwb;
	}
	public void setTranscwb(String transcwb) {
		this.transcwb = transcwb;
	}
	public String getCwb() {
		return cwb;
	}
	public void setCwb(String cwb) {
		this.cwb = cwb;
	}
	public String getCreatetime() {
		return createtime;
	}
	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}
	public String getFlowtime() {
		return flowtime;
	}
	public void setFlowtime(String flowtime) {
		this.flowtime = flowtime;
	}
	public String getSendtime() {
		return sendtime;
	}
	public void setSendtime(String sendtime) {
		this.sendtime = sendtime;
	}
	public int getFlowordertype() {
		return flowordertype;
	}
	public void setFlowordertype(int flowordertype) {
		this.flowordertype = flowordertype;
	}
	public String getJsoncontent() {
		return jsoncontent;
	}
	public void setJsoncontent(String jsoncontent) {
		this.jsoncontent = jsoncontent;
	}
	public int getSend_b2c_flag() {
		return send_b2c_flag;
	}
	public void setSend_b2c_flag(int send_b2c_flag) {
		this.send_b2c_flag = send_b2c_flag;
	}
	public int getCustomerid() {
		return customerid;
	}
	public void setCustomerid(int customerid) {
		this.customerid = customerid;
	}
	public int getSelect_b2c_flag() {
		return select_b2c_flag;
	}
	public void setSelect_b2c_flag(int select_b2c_flag) {
		this.select_b2c_flag = select_b2c_flag;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	
}
