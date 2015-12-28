package cn.explink.b2c.huanqiugou;

import cn.explink.enumutil.FlowOrderTypeEnum;


/**
 * 订单流转信息推送
 * @author Administrator
 *
 */
public enum HuanqiugouFlowEnum {
	Ruku(FlowOrderTypeEnum.RuKu.getValue(),"2","2-订单送达分拨点"),
	ChuKuSaoMiao(FlowOrderTypeEnum.ChuKuSaoMiao.getValue(),"3","3-订单出分拨点"),
	QianShou(FlowOrderTypeEnum.YiShenHe.getValue(),"4","4-派送成功"),
	JuShou(FlowOrderTypeEnum.YiShenHe.getValue(),"5","订单拒收"),  
	PaisongYiChang(FlowOrderTypeEnum.YiShenHe.getValue(),"6","6-订单派送异常"), 
	DiuShi(FlowOrderTypeEnum.YiShenHe.getValue(),"7","7-订单丢失"),  
	TuiHuoRuFenBoDian(FlowOrderTypeEnum.TuiHuoZhanRuKu.getValue(),"8","8-退货入分拨站点"), 
	TuiHuoChuFenBoDian(FlowOrderTypeEnum.TuiGongYingShangChuKu.getValue(),"9","9-退货出分拨站点"),
	
	QuJianShiBai(FlowOrderTypeEnum.YiShenHe.getValue(),"10","10-取件失败"),
	QuJianYiChang(FlowOrderTypeEnum.YiShenHe.getValue(),"11","11-取件异常"),
	TuiHuoQuJianZhong(FlowOrderTypeEnum.FenZhanLingHuo.getValue(),"13","13-退货取件中"),
	TuiHuoDingDanShengCheng(FlowOrderTypeEnum.YiShenHe.getValue(),"12","12-退货订单生成"),
	
	;
	
	private long state;
	private String dms_code;
	private String text;
	private HuanqiugouFlowEnum(long state,String dms_code,String text){
		this.state=state;
		this.dms_code=dms_code;
		this.text=text;
	}
	public String getDms_code() {
		return dms_code;
	}
	public void setDms_code(String dms_code) {
		this.dms_code = dms_code;
	}
	public long getState() {
		return state;
	}
	public void setState(long state) {
		this.state = state;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}
