package cn.explink.b2c.shenzhoushuma;

import cn.explink.enumutil.FlowOrderTypeEnum;

/**
 * 节点编码类型
 * 
 * @author yurong.liang
 */
public enum OpPointEnum {

	RuKu("10", FlowOrderTypeEnum.RuKu.getValue(), "提货回仓"), // 入库
	ChuKu("15", FlowOrderTypeEnum.ChuKuSaoMiao.getValue(), "发货派车"), // 出库
	DaoHuo("20", FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue(), "到目的仓"), // 分站到货
	LingHuo("25", FlowOrderTypeEnum.FenZhanLingHuo.getValue(), "末端配送"), // 分站领货
	QianShou("30", FlowOrderTypeEnum.YiShenHe.getValue(), "整单签收"),// 已审核
	JuShou("35", FlowOrderTypeEnum.YiShenHe.getValue(), "整单拒收");// 已审核

	private String opPointType;
	private int flowordertype;
	private String opPointText;

	private OpPointEnum(String opPointType, int flowordertype,
			String opPointText) {
		this.opPointType = opPointType;
		this.flowordertype = flowordertype;
		this.opPointText = opPointText;
	}

	// 根据flowordertype获得节点编码
	public static String getopPointTypeByFlowordertype(Integer flowordertype) {
		for (OpPointEnum opPointEnum : OpPointEnum.values()) {
			if (opPointEnum.getFlowordertype() == flowordertype)
				return opPointEnum.getOpPointType();
		}
		return null;
	}

	public String getOpPointType() {
		return opPointType;
	}

	public int getFlowordertype() {
		return flowordertype;
	}

	public String getOpPointText() {
		return opPointText;
	}

	public void setOpPointType(String opPointType) {
		this.opPointType = opPointType;
	}

	public void setFlowordertype(int flowordertype) {
		this.flowordertype = flowordertype;
	}

	public void setOpPointText(String opPointText) {
		this.opPointText = opPointText;
	}

}
