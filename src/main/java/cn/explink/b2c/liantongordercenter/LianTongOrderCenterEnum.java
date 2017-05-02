package cn.explink.b2c.liantongordercenter;

public enum LianTongOrderCenterEnum {
	FaHuo("00","标记发货：已下单"),LanJian("10","快递已揽件"),DaoHuo("11","已到派送点"),PaiSong("12","开始派送"),ZiTi("16","已到自提点"),
	QianShou("20","用户已签收"),JuShou("30","用户拒收"),QuXiao("40","订单取消");
	
	private String value;
	private String text;
	
	private LianTongOrderCenterEnum(String value, String text) {
		this.value = value;
		this.text = text;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}
