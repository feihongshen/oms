package cn.explink.b2c.tpsdo.bean;

import java.math.BigDecimal;
/**
 * 外单修改信息封装类
 * @author gordon.zhou
 *
 */
public class ThirdPartyOrderEditInfo {
	//修改类型：2.修改金额3.修改支付方式4.修改订单类型5.修改订单信息（收件人信息，配送站点）
	private int editType;
	private String cwb;
	private BigDecimal receivablefee;
	private BigDecimal paybackfee;
	private int newpaywayid;
	private int cwbordertypeid;
	
	private long branchid;
	private String consigneename;
	private String consigneemobile;
	private String customercommand;
	private String consigneeaddress;
	public String getCwb() {
		return cwb;
	}
	public void setCwb(String cwb) {
		this.cwb = cwb;
	}
	public BigDecimal getReceivablefee() {
		return receivablefee;
	}
	public void setReceivablefee(BigDecimal receivablefee) {
		this.receivablefee = receivablefee;
	}
	public BigDecimal getPaybackfee() {
		return paybackfee;
	}
	public void setPaybackfee(BigDecimal paybackfee) {
		this.paybackfee = paybackfee;
	}
	public int getNewpaywayid() {
		return newpaywayid;
	}
	public void setNewpaywayid(int newpaywayid) {
		this.newpaywayid = newpaywayid;
	}
	public int getCwbordertypeid() {
		return cwbordertypeid;
	}
	public void setCwbordertypeid(int cwbordertypeid) {
		this.cwbordertypeid = cwbordertypeid;
	}
	
	public long getBranchid() {
		return branchid;
	}
	public void setBranchid(long branchid) {
		this.branchid = branchid;
	}
	public String getConsigneename() {
		return consigneename;
	}
	public void setConsigneename(String consigneename) {
		this.consigneename = consigneename;
	}
	public String getConsigneemobile() {
		return consigneemobile;
	}
	public void setConsigneemobile(String consigneemobile) {
		this.consigneemobile = consigneemobile;
	}
	
	public String getConsigneeaddress() {
		return consigneeaddress;
	}
	public void setConsigneeaddress(String consigneeaddress) {
		this.consigneeaddress = consigneeaddress;
	}

	public int getEditType() {
		return editType;
	}
	public void setEditType(int editType) {
		this.editType = editType;
	}
	public String getCustomercommand() {
		return customercommand;
	}
	public void setCustomercommand(String customercommand) {
		this.customercommand = customercommand;
	}
	
	
	
}
