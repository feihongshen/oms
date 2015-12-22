package cn.explink.b2c.weisuda;

public class WeisudaCwb {
	private String id;
	private String cwb;
	private int cwbordertypeid;
	private String courier_code;
	private String bound_time;
	private String istuisong;
	private String isqianshou;
	private String remark;
	private String operationTime;
	private String waidanjson;
	private int ordertype;//0代表是唯品会订单    1 ：  品骏达外单
	
	

	public String getWaidanjson() {
		return waidanjson;
	}

	public void setWaidanjson(String waidanjson) {
		this.waidanjson = waidanjson;
	}

	public int getOrdertype() {
		return ordertype;
	}

	public void setOrdertype(int ordertype) {
		this.ordertype = ordertype;
	}

	public String getCwb() {
		return this.cwb;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setCwb(String cwb) {
		this.cwb = cwb;
	}

	/**
	 * @return the cwbordertypeid
	 */
	public int getCwbordertypeid() {
		return this.cwbordertypeid;
	}

	/**
	 * @param cwbordertypeid the cwbordertypeid to set
	 */
	public void setCwbordertypeid(int cwbordertypeid) {
		this.cwbordertypeid = cwbordertypeid;
	}

	public String getCourier_code() {
		return this.courier_code;
	}

	public void setCourier_code(String courier_code) {
		this.courier_code = courier_code;
	}

	public String getBound_time() {
		return this.bound_time;
	}

	public void setBound_time(String bound_time) {
		this.bound_time = bound_time;
	}

	public String getIstuisong() {
		return this.istuisong;
	}

	public void setIstuisong(String istuisong) {
		this.istuisong = istuisong;
	}

	public String getIsqianshou() {
		return this.isqianshou;
	}

	public void setIsqianshou(String isqianshou) {
		this.isqianshou = isqianshou;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getOperationTime() {
		return this.operationTime;
	}

	public void setOperationTime(String operationTime) {
		this.operationTime = operationTime;
	}
}
