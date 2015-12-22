package cn.explink.b2c.meilinkai;

public enum TrackEnum {
	Blank("","入库"),
	SHARRIVAL("SHARRIVAL","妥投"),
	SHSHIP("SHSHIP","妥投(此时为反馈状态,针对快递配送成功)");
	private String sign;
	private String text;
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	private TrackEnum(String sign,String text){
		this.sign = sign;
		this.text = text;
	}
}
