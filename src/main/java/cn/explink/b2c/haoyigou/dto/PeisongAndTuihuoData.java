package cn.explink.b2c.haoyigou.dto;

public class PeisongAndTuihuoData {
	//配送属性
	private String dispatchid;
	private String customerid;
	private String shiporderno;
	private String deliveryorderno;
	private String receivername;
	private String deliverydate;
	private String numberofcartons;
	private String deliverystatusdescription;
	
	private String deliveryperson;
	private String deliverypersonphone;
	private String receipttime;
	private String longitude;
	private String dimensionvalue;
	private String undefinedone;
	private String undefinedtwo;
	//退货属性(单独拥有)
	private String deliveryid;
	private String companyid;
	private String edidate;
	private String statusupdatedate;
	private String closuredate;
	private String deliverystatus;
	private String pickupperson;
	private String pickuppersonphone;
	private String returndeliveryorderid;
	//公共属性
	private String blank1;
	private String blank2;
	private String blank3;
	private String blank4;
	private String blank5;
	private String blank6;
	//区分字段(1.配送 2.退货)
	private int whichone;
	
	public int getWhichone() {
		return whichone;
	}
	public void setWhichone(int whichone) {
		this.whichone = whichone;
	}
	public String getDispatchid() {
		return dispatchid;
	}
	public void setDispatchid(String dispatchid) {
		this.dispatchid = dispatchid;
	}
	public String getBlank1() {
		return blank1;
	}
	public void setBlank(String blank1) {
		this.blank1 = blank1;
	}
	public String getCustomerid() {
		return customerid;
	}
	public void setCustomerid(String customerid) {
		this.customerid = customerid;
	}
	public String getShiporderno() {
		return shiporderno;
	}
	public void setShiporderno(String shiporderno) {
		this.shiporderno = shiporderno;
	}
	public String getBlank2() {
		return blank2;
	}
	public void setBlank2(String blank2) {
		this.blank2 = blank2;
	}
	public String getDeliveryorderno() {
		return deliveryorderno;
	}
	public void setDeliveryorderno(String deliveryorderno) {
		this.deliveryorderno = deliveryorderno;
	}
	public String getReceivername() {
		return receivername;
	}
	public void setReceivername(String receivername) {
		this.receivername = receivername;
	}
	public String getBlank3() {
		return blank3;
	}
	public void setBlank3(String blank3) {
		this.blank3 = blank3;
	}
	public String getDeliverydate() {
		return deliverydate;
	}
	public void setDeliverydate(String deliverydate) {
		this.deliverydate = deliverydate;
	}
	public String getNumberofcartons() {
		return numberofcartons;
	}
	public void setNumberofcartons(String numberofcartons) {
		this.numberofcartons = numberofcartons;
	}
	public String getDeliverystatusdescription() {
		return deliverystatusdescription;
	}
	public void setDeliverystatusdescription(String deliverystatusdescription) {
		this.deliverystatusdescription = deliverystatusdescription;
	}
	public String getBlank4() {
		return blank4;
	}
	public void setBlank4(String blank4) {
		this.blank4 = blank4;
	}
	
	public String getBlank5() {
		return blank5;
	}
	public void setBlank5(String blank5) {
		this.blank5 = blank5;
	}
	public String getDeliveryperson() {
		return deliveryperson;
	}
	public void setDeliveryperson(String deliveryperson) {
		this.deliveryperson = deliveryperson;
	}
	public String getDeliverypersonphone() {
		return deliverypersonphone;
	}
	public void setDeliverypersonphone(String deliverypersonphone) {
		this.deliverypersonphone = deliverypersonphone;
	}
	public String getReceipttime() {
		return receipttime;
	}
	public void setReceipttime(String receipttime) {
		this.receipttime = receipttime;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getDimensionvalue() {
		return dimensionvalue;
	}
	public void setDimensionvalue(String dimensionvalue) {
		this.dimensionvalue = dimensionvalue;
	}
	public String getUndefinedone() {
		return undefinedone;
	}
	public void setUndefinedone(String undefinedone) {
		this.undefinedone = undefinedone;
	}
	public String getUndefinedtwo() {
		return undefinedtwo;
	}
	public void setUndefinedtwo(String undefinedtwo) {
		this.undefinedtwo = undefinedtwo;
	}
	public String getBlank6() {
		return blank6;
	}
	public void setBlank6(String blank6) {
		this.blank6 = blank6;
	}
	public String getDeliveryid() {
		return deliveryid;
	}
	public void setDeliveryid(String deliveryid) {
		this.deliveryid = deliveryid;
	}
	public String getCompanyid() {
		return companyid;
	}
	public void setCompanyid(String companyid) {
		this.companyid = companyid;
	}
	public String getEdidate() {
		return edidate;
	}
	public void setEdidate(String edidate) {
		this.edidate = edidate;
	}
	public String getStatusupdatedate() {
		return statusupdatedate;
	}
	public void setStatusupdatedate(String statusupdatedate) {
		this.statusupdatedate = statusupdatedate;
	}
	public String getClosuredate() {
		return closuredate;
	}
	public void setClosuredate(String closuredate) {
		this.closuredate = closuredate;
	}
	public String getDeliverystatus() {
		return deliverystatus;
	}
	public void setDeliverystatus(String deliverystatus) {
		this.deliverystatus = deliverystatus;
	}
	public String getPickupperson() {
		return pickupperson;
	}
	public void setPickupperson(String pickupperson) {
		this.pickupperson = pickupperson;
	}
	public String getPickuppersonphone() {
		return pickuppersonphone;
	}
	public void setPickuppersonphone(String pickuppersonphone) {
		this.pickuppersonphone = pickuppersonphone;
	}
	public String getReturndeliveryorderid() {
		return returndeliveryorderid;
	}
	public void setReturndeliveryorderid(String returndeliveryorderid) {
		this.returndeliveryorderid = returndeliveryorderid;
	}
	public void setBlank1(String blank1) {
		this.blank1 = blank1;
	}
	
}
