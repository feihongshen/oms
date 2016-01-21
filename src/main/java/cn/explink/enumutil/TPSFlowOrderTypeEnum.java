package cn.explink.enumutil;

public enum TPSFlowOrderTypeEnum {
	registeScan(0, "揽件扫描"),
	inboundScan(1, "进站扫描")
	

;
	private int value;
	private String text;


	private TPSFlowOrderTypeEnum(int value, String text) {
		this.value = value;
		this.text = text;
	}

	public int getValue() {
		return value;
	}

	public String getText() {
		return text;
	}
}
