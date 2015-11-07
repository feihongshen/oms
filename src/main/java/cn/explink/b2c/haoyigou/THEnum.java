package cn.explink.b2c.haoyigou;
//退货单在txt文件中存储位置
public enum THEnum {
	DELIVERYID(1,10),
	SHIPORDERNO(11,25),
	BLANK1(26,29),
	COMPANYID(30,40),
	BLANK2(41,163),
	EDIDATE(164,171),
	NUMBEROFCARTONS(172,179),
	BLANK3(180,348),
	STATUSUPDATEDATE(349,356),
	BLANK4(357,363),
	DELIVERYSTATUSDESCRIPTION(364,463),
	CLOSUREDATE(464,483),
	BLANK5(484,489),
	DELIVERYSTATUS(490,499),
	pickupperson(500,515),
	pickuppersonphone(516,539),
	longitude(540,589),
	dimensionvalue(590,639),
	returndeliveryorderid(640,679),
	undefinedone(680,709),
	undefinedtwo(710,759),
	BLANK6(760,767);
	
	private int startno;
	private int endno;
	public int getStartno() {
		return startno;
	}
	public void setStartno(int startno) {
		this.startno = startno;
	}
	public int getEndno() {
		return endno;
	}
	public void setEndno(int endno) {
		this.endno = endno;
	}
	
	private THEnum(int startno,int endno){
		this.startno = startno;
		this.endno = endno;
	}
	//获取每个字段在txt文件中的长度
	public int getLength(){
		return (endno - startno) ==0?1:(endno - startno)+1;
	}
	
}
