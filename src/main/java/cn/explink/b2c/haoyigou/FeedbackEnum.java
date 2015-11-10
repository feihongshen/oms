package cn.explink.b2c.haoyigou;

public enum FeedbackEnum {
	success("1","配送成功"),
	bufenSuccess("2","部分成功"),
	failure("5","客户拒收"),
	linghuo("7","派件员已领货,派件中"),
	//============================
	SMTsuccess("1","上门退货成功");//TODO ====待确认
	private String value;
	private String text;
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
	private FeedbackEnum(String value,String text){
		this.value = value;
		this.text = text;
	}
	
}
