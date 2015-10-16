package cn.explink.b2c.zhemeng;

import cn.explink.enumutil.FlowOrderTypeEnum;

/**
 * tmall推送状态反馈的枚举
 * 
 * @author Administrator
 *
 */
public enum ZhemengFlowEnum {
	PU("PU", FlowOrderTypeEnum.RuKu.getValue(), "已揽收"),
	OD("OD",FlowOrderTypeEnum.FenZhanLingHuo.getValue(), "派件中"),
	OK("OK",FlowOrderTypeEnum.YiShenHe.getValue(), "签收成功"),
	JS("JS",FlowOrderTypeEnum.YiShenHe.getValue(), "签收失败/拒签"),
	YC("YC",FlowOrderTypeEnum.YiShenHe.getValue(), "丢失/滞留"),
	AF("AF",FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue(), "到件(订单到达XX站点)"),
	DF("DF",FlowOrderTypeEnum.ChuKuSaoMiao.getValue(), "货物发往XX站点"),
	
	//其他类型状态暂时不做回传

	;

	private String state;
	private int flowordertype;
	private String text;

	private ZhemengFlowEnum(String tmall_state, int flowordertype, String text) {
		this.state = tmall_state;
		this.flowordertype = flowordertype;
		this.text = text;
	}

	public String getTmall_state() {
		return state;
	}

	public void setTmall_state(String tmall_state) {
		this.state = tmall_state;
	}

	public int getFlowordertype() {
		return flowordertype;
	}

	public void setFlowordertype(int flowordertype) {
		this.flowordertype = flowordertype;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
