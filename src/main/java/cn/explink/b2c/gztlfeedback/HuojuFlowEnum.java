package cn.explink.b2c.gztlfeedback;

public enum HuojuFlowEnum {
	BaoGuoDiuShi("包裹丢失","SC07"),
	KeHuYanQi("客户延期","SC04"),
	DaoJianQueRen("到件","SC01"),
	PeiSongChengGong("配送成功","SC13"),
	TuiGongYingShang("退供应商","SC08"),
	PeiSongChengGongH("配送成功","SC03"),
	PeiSongYanChi("配送延迟","SC04"),
	PeiSongShiBai("配送失败","SC05"),
	DaoJianQueRenH("到件确认","SC09"),
	PaiJian("派件","SC02"),
	DaoJian("到件","SC06"),
	FaJian("发件","SC11"),
	BuFenJuShou("部分拒收","SC14")
	;
	private String describe;
	private String statusCode;

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	private HuojuFlowEnum(String describe, String statusCode) {
		this.describe = describe;
		this.statusCode = statusCode;
	}

	public static HuojuFlowEnum getHuoJuFlowEnum(String statusCodehh){
		for (HuojuFlowEnum huojuflowenum : HuojuFlowEnum.values()) {
			if(statusCodehh.equals(huojuflowenum.getStatusCode())){
				return huojuflowenum;
			}
		}
		return null;
	}
	
}
