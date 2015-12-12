package cn.explink.b2c.meilinkai;

public enum HDtoLipsEnum {
	REASON_1(-1,"User Not Exist","Transaction DateTime Is Not Exist"),
	REASON_2(-2,"Password Invalid","Fail in insert into Database"),
	REASON_3(-3,"Data security Invalid","User security Invalid"),
	REASON1(1,"User verified","Insert Successfully");
	
	private int result;
	private String checkUsertext;
	private String hdtolipsresult;
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public String getCheckUsertext() {
		return checkUsertext;
	}
	public void setCheckUsertext(String checkUsertext) {
		this.checkUsertext = checkUsertext;
	}
	public String getHdtolipsresult() {
		return hdtolipsresult;
	}
	public void setHdtolipsresult(String hdtolipsresult) {
		this.hdtolipsresult = hdtolipsresult;
	}
	
	private HDtoLipsEnum(int result,String checkUsertext,String hdtolipsresult){
		this.result = result;
		this.checkUsertext = checkUsertext;
		this.hdtolipsresult = hdtolipsresult;
	}
}
