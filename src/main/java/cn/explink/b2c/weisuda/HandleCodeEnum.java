package cn.explink.b2c.weisuda;

public enum HandleCodeEnum {

	SUCCESS(1, "S00", "成功"), FAILE(2, "F00", "失败");

	private int value;
	private String name;
	private String text;

	private HandleCodeEnum(int value, String text, String name) {
		this.value = value;
		this.text = text;
		this.name = name;
	}

	public int getValue() {
		return this.value;
	}

	public String getName() {
		return this.name;
	}

	public String getText() {
		return this.text;
	}

}