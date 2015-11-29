package cn.explink.b2c.feiniuwang;

import cn.explink.enumutil.FlowOrderTypeEnum;

public enum FNWFlowEnum {
	ARRIVAL("ARRIVAL",FlowOrderTypeEnum.RuKu.getValue(),"到件"),//入库
	DEPARTURE("DEPARTURE",FlowOrderTypeEnum.ChuKuSaoMiao.getValue(),"发件"),//站点出库
	SENT_SCAN("SENT_SCAN",FlowOrderTypeEnum.FenZhanLingHuo.getValue(),"派件"),//派件
	PROBLEM("PROBLEM",FlowOrderTypeEnum.JuShou.getValue(),"疑难件"),//拒签
	RETENTION("RETENTION",FlowOrderTypeEnum.FenZhanZhiLiu.getValue(),"留仓件"),//滞留
	SIGNED("SIGNED",FlowOrderTypeEnum.YiShenHe.getValue(),"签收"),
	OTHER1("OTHER",FlowOrderTypeEnum.TuiHuoChuZhan.getValue(),"退货入库"),//退货
	OTHER2("OTHER",FlowOrderTypeEnum.TuiHuoChuZhan.getValue(),"退货出站"),//退货
	;
	private String sign;
	private int key;
	private String text;
	
	private FNWFlowEnum(String sign,int key,String text){
		this.sign = sign;
		this.key = key;
		this.text = text;
	}
	
	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public static FNWFlowEnum getMsg(String sign){
		for(FNWFlowEnum f:FNWFlowEnum.values())
		{
			if(f.getSign().equals(sign)){
				return f;
				}
			
		}
		return null;
	}
}
