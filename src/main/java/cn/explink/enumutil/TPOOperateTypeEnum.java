package cn.explink.enumutil;

public enum TPOOperateTypeEnum {
	ADD(0, "新增"), CANCEL(-1, "取消"), UPDATE(1, "修改");
	private int value;
	private String text;

	private TPOOperateTypeEnum(int value, String text) {
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
