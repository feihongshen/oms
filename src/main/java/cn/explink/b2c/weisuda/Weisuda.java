package cn.explink.b2c.weisuda;

public class Weisuda {
	private String code;
	private String v;
	private String secret;
	private String pushOrders_URL;
	private String UnVerifyOrders_URL;
	private String updateUnVerifyOrders_URL;
	private String updateOrders_URL;
	private String siteUpdate_URL;
	private String siteDel_URL;
	private String courierUpdate_URL;
	private String carrierDel_URL;
	private String nums;
	private String count;

	public String getSecret() {
		return this.secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getV() {
		return this.v;
	}

	public void setV(String v) {
		this.v = v;
	}

	public String getPushOrders_URL() {
		return this.pushOrders_URL;
	}

	public String getUnVerifyOrders_URL() {
		return this.UnVerifyOrders_URL;
	}

	public String getUpdateUnVerifyOrders_URL() {
		return this.updateUnVerifyOrders_URL;
	}

	public void setPushOrders_URL(String pushOrders_URL) {
		this.pushOrders_URL = pushOrders_URL;
	}

	public void setUnVerifyOrders_URL(String unVerifyOrders_URL) {
		this.UnVerifyOrders_URL = unVerifyOrders_URL;
	}

	public void setUpdateUnVerifyOrders_URL(String updateUnVerifyOrders_URL) {
		this.updateUnVerifyOrders_URL = updateUnVerifyOrders_URL;
	}

	public String getUpdateOrders_URL() {
		return this.updateOrders_URL;
	}

	public void setUpdateOrders_URL(String updateOrders_URL) {
		this.updateOrders_URL = updateOrders_URL;
	}

	public String getSiteUpdate_URL() {
		return this.siteUpdate_URL;
	}

	public void setSiteUpdate_URL(String siteUpdate_URL) {
		this.siteUpdate_URL = siteUpdate_URL;
	}

	public String getSiteDel_URL() {
		return this.siteDel_URL;
	}

	public void setSiteDel_URL(String siteDel_URL) {
		this.siteDel_URL = siteDel_URL;
	}

	public String getCourierUpdate_URL() {
		return this.courierUpdate_URL;
	}

	public void setCourierUpdate_URL(String courierUpdate_URL) {
		this.courierUpdate_URL = courierUpdate_URL;
	}

	public String getCarrierDel_URL() {
		return this.carrierDel_URL;
	}

	public void setCarrierDel_URL(String carrierDel_URL) {
		this.carrierDel_URL = carrierDel_URL;
	}

	public String getNums() {
		return this.nums;
	}

	public void setNums(String nums) {
		this.nums = nums;
	}

	public String getCount() {
		return this.count;
	}

	public void setCount(String count) {
		this.count = count;
	}

}
