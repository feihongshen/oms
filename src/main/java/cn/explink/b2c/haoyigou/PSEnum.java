package cn.explink.b2c.haoyigou;
//配送单在txt文件中存储位置
public enum PSEnum {
	DISPATCHERID(1,16),
	BLANK1(17,24),
	CUSTOMERID(25,34),
	SHIPORDERNO(35,49),
	BLANK2(50,50),
	DELIVERYORDERNO(51,70),
	RECEIVERNAME(71,90),
	BLANK3(91,116),
	DELIVERYDATE(117,127),
	NUMBEROFCARTONS(128,130),
	DELIVERYSTATUSDESCRIPTION(131,228),
	BLANK4(229,250),
	DELIVERYSTATUS(251,254),
	BLANK5(255,272),
	deliveryperson(273,288),
	Deliverypersonphone(289,318),
	receipttime(319,348),
	Longitude(349,398),
	dimensionvalue(399,448),
	undefinedone(449,478),
	undefinedtwo(479,538),
	BLANK6(539,546);
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

	private PSEnum(int startno,int endno){
		this.startno = startno;
		this.endno = endno;
	}
	//获取每个字段在txt文件中的长度
	public int getLength(){
		return (endno - startno) ==0?1:(endno - startno)+1;
	}
}
