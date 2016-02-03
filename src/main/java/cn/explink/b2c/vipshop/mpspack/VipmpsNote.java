package cn.explink.b2c.vipshop.mpspack;

/**
 * 
 * @author Administrator
 *
 */
public class VipmpsNote {
	private String version;
	private String request_time;
	private String cust_code;

	private String cust_data_id;
	private String order_sn; //订单号
	private String box_id; //箱号，一票多件运单号
	private String order_status; //状态编码
	private String order_status_info; //跟踪信息描述
	private String current_city_name;
	private String order_status_time;
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getRequest_time() {
		return request_time;
	}
	public void setRequest_time(String request_time) {
		this.request_time = request_time;
	}
	public String getCust_code() {
		return cust_code;
	}
	public void setCust_code(String cust_code) {
		this.cust_code = cust_code;
	}
	public String getCust_data_id() {
		return cust_data_id;
	}
	public void setCust_data_id(String cust_data_id) {
		this.cust_data_id = cust_data_id;
	}
	public String getOrder_sn() {
		return order_sn;
	}
	public void setOrder_sn(String order_sn) {
		this.order_sn = order_sn;
	}
	public String getBox_id() {
		return box_id;
	}
	public void setBox_id(String box_id) {
		this.box_id = box_id;
	}
	public String getOrder_status() {
		return order_status;
	}
	public void setOrder_status(String order_status) {
		this.order_status = order_status;
	}
	public String getOrder_status_info() {
		return order_status_info;
	}
	public void setOrder_status_info(String order_status_info) {
		this.order_status_info = order_status_info;
	}
	public String getCurrent_city_name() {
		return current_city_name;
	}
	public void setCurrent_city_name(String current_city_name) {
		this.current_city_name = current_city_name;
	}
	public String getOrder_status_time() {
		return order_status_time;
	}
	public void setOrder_status_time(String order_status_time) {
		this.order_status_time = order_status_time;
	}
	
}
