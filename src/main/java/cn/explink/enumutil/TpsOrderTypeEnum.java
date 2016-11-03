package cn.explink.enumutil;

/**
 * tps的订单类型
 * @author jian.xie
 *
 */
public enum TpsOrderTypeEnum {
	Express(1,"快递"),
	Peisong(2, "配送单"),
	Shangmentui(3,"上门退"),
	Shangmenhuan(4,"上门换"),
	;
	
	private int value;
	private String text;
	
	private TpsOrderTypeEnum(int value, String text){
		this.value = value;
		this.text = text;
	}
	
	public int getValue(){
		return value;
	}
	
	public String getText(){
		return text;
	}
}
