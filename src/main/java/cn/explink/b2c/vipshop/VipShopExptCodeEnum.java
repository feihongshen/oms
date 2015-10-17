package cn.explink.b2c.vipshop;


/**
 * vipshop推送配送异常码和揽退异常码的匹配关系
 * 
 * @author Administrator
 * 
 */
public enum VipShopExptCodeEnum {
	//滞留异常
	code401(0, "401", "暂时联系不上收货人","406","联系不上退货人"),
	code402(0, "402", "收货人更改送货时间","408","客户更改揽件时间"),
	code403(0, "403", "收货人更改收货信息","409","客户更改揽件信息"),
	
	defaultZhiLiu(0, "0", "默认","408","客户更改揽件时间"),
	
	//拒收&揽退失败
	code1(1, "1", "1_商品不合适","3605","商品不符合质检要求"),
	code2(1, "2", "1_商品质量问题","3605","商品不符合质检要求"),
	code3(1, "3", "1_错漏发","3607","无货揽退属于客户拒收退货"),
	
	code4(1, "4", "0_不在收货地址","3610","其它原因"),
	code5(1, "5", "0_破损","3605","商品不符合质检要求"),
	code6(1, "6", "0_唯品会通知退回","3603","唯品会通知取消"),	
	code7(1, "7", "0_重复订购","3610","其它原因"),
	code9(1, "9", "0_电话拒收","3601","客户表示自退"),
	code10(1, "10", "0_联系不上收货人","3610","其它原因"),
	code11(1, "11", "0_保留时效过长","3604","超出7天揽件时效"),
	code12(1, "12", "0_延误配送","3604","超出7天揽件时效"),
	code13(1, "13", "1_不在收货地址","3610","其他原因"),
	code14(1, "14", "1_延误配送","3604","超出7天揽件时效"),
	
	code15(1, "15", "1_重复订购","3610","其它原因"),
	code16(1, "16", "1_破损","3605","商品不符合质检要求"),
	
	detfaultJuShou(1, "0", "默认拒退","3610","其它原因"),
	
	
	;
	
	private int type; //0 是滞留 1是拒收
	private String peisongCode;
	private String peisongMsg;
	

	private String lantuiCode;
	private String lantuiMsg;

	

	private VipShopExptCodeEnum(int type,String  peisongCode, String peisongMsg, String lantuiCode,String lantuiMsg) {
		this.type=type;
		this.peisongCode=peisongCode;
		this.lantuiCode=lantuiCode;
		this.peisongMsg=peisongMsg;
		this.lantuiMsg=lantuiMsg;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	public String getPeisongCode() {
		return peisongCode;
	}

	public void setPeisongCode(String peisongCode) {
		this.peisongCode = peisongCode;
	}

	public String getPeisongMsg() {
		return peisongMsg;
	}

	public void setPeisongMsg(String peisongMsg) {
		this.peisongMsg = peisongMsg;
	}

	public String getLantuiCode() {
		return lantuiCode;
	}

	public void setLantuiCode(String lantuiCode) {
		this.lantuiCode = lantuiCode;
	}

	public String getLantuiMsg() {
		return lantuiMsg;
	}

	public void setLantuiMsg(String lantuiMsg) {
		this.lantuiMsg = lantuiMsg;
	}

}
