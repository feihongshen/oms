package cn.explink.b2c.zhemeng.track;

/**
 * 哲盟_轨迹 XML的节点信息实体
 * @author yurong.liang 2016-05-31
 */
public class ZhemengTrackXMLNote {

	private String out_biz_code; // 外部业务编码,用来去重同一个合作伙伴唯一
	private String order_code; // 物流宝订单号 shipcwb
	private String tms_service_code;//快递公司编号
	
	private String operator; // 操作人
	private String operator_date; // 操作时间 格式 yyyy-mm-dd hh:mm:ss
	private String status;
	private String scanstano;
	private String ctrname; 
	private String content; 
	private String remark; 

	
	public String getOut_biz_code() {
		return out_biz_code;
	}
	public void setOut_biz_code(String out_biz_code) {
		this.out_biz_code = out_biz_code;
	}
	public String getOrder_code() {
		return order_code;
	}
	public void setOrder_code(String order_code) {
		this.order_code = order_code;
	}
	public String getTms_service_code() {
		return tms_service_code;
	}
	public void setTms_service_code(String tms_service_code) {
		this.tms_service_code = tms_service_code;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getOperator_date() {
		return operator_date;
	}
	public void setOperator_date(String operator_date) {
		this.operator_date = operator_date;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getScanstano() {
		return scanstano;
	}
	public void setScanstano(String scanstano) {
		this.scanstano = scanstano;
	}
	public String getCtrname() {
		return ctrname;
	}
	public void setCtrname(String ctrname) {
		this.ctrname = ctrname;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}

}
