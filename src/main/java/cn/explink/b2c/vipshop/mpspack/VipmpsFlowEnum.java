package cn.explink.b2c.vipshop.mpspack;

import cn.explink.enumutil.FlowOrderTypeEnum;

/**
 * vipshop 集包 推送状态反馈的枚举
 * 
 * @author Administrator
 * 
 */
public enum VipmpsFlowEnum {
	Ruku(411, FlowOrderTypeEnum.RuKu.getValue(), "到达分拨中心[正常订单]"), 
	ChuKuSaoMiao(412, FlowOrderTypeEnum.ChuKuSaoMiao.getValue(), "分拨下站[正常订单]"), 
	FenZhanDaoHuo(413, FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue(),"到达配送站点[正常订单]"), 
	
	FenZhanLingHuo(4, FlowOrderTypeEnum.FenZhanLingHuo.getValue(), "分站领货即将派送"), 
	ZhongZhuanZhanRuKu(4, FlowOrderTypeEnum.ZhongZhuanZhanRuKu.getValue(), "中转站入库"),
	ZhongZhuanZhanChuKu(4, FlowOrderTypeEnum.ZhongZhuanZhanChuKuSaoMiao.getValue(), "中转站入库"),
	
	FenZhanZhiLiu(-1, FlowOrderTypeEnum.YiShenHe.getValue(), "分站滞留");

	public int getVipshop_state() {
		return this.vipshop_state;
	}

	public void setVipshop_state(int vipshop_state) {
		this.vipshop_state = vipshop_state;
	}

	public int getFlowordertype() {
		return this.flowordertype;
	}

	public void setFlowordertype(int flowordertype) {
		this.flowordertype = flowordertype;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	private int vipshop_state;
	private int flowordertype;
	private String text;

	private VipmpsFlowEnum(int vipshop_state, int flowordertype, String text) {
		this.vipshop_state = vipshop_state;
		this.flowordertype = flowordertype;
		this.text = text;
	}

}
