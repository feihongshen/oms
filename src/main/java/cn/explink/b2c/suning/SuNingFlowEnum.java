package cn.explink.b2c.suning;

import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;


/**
 * 【苏宁易购】反馈允许反馈的状态
 * 签收、拒收
 * @author 刘肖
 *
 */
public enum SuNingFlowEnum {
	
	RUKU("01",FlowOrderTypeEnum.RuKu.getValue(),"入库（苏宁易购入站）"), 
	CHUKU("02",FlowOrderTypeEnum.ChuKuSaoMiao.getValue(),"苏宁易购出站"), 
	DAOHUO("01",FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue(),"到货扫描"),
	LINGHUO("02",FlowOrderTypeEnum.FenZhanLingHuo.getValue(),"分站领货"),
	TMS_SIGN1("03",FlowOrderTypeEnum.YiShenHe.getValue(),"签收"),
	TMS_SIGN2("03",FlowOrderTypeEnum.YiShenHe.getValue(),"上门取件成功")
	;
	
	private String work_type; //需要传给【苏宁易购】的当前状态
	private int flowordertype; // 0 配送中， 1最终结果
	private String text;
	
	public String getWork_type() {
		return work_type;
	}
	public void setWork_type(String work_type) {
		this.work_type = work_type;
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

	private SuNingFlowEnum(String work_type,int flowordertype,String text){
		this.work_type = work_type;
		this.flowordertype = flowordertype;
		this.text = text;
	}
	
}
