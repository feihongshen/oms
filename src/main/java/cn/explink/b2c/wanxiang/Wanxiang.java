package cn.explink.b2c.wanxiang;

/**
 * 
 * @author Administrator 联通查询接口设置
 */
public class Wanxiang {

	private String private_key; // 密钥
	private String customerid; // 在系统中的customerid
	private String url; // 反馈万象URL
	private int maxCount;
	private String branchname; // 站点名称
	private String user_name; // 用户名
	private String pass_word; // 密码
	private int version; // 版本
	private int shangmentuiSupport;// 0 默认支持， 1不支持 
	private int jushousendflag; //拒收是否推送， 0推送，  1不推送
	
	public int getJushousendflag() {
		return jushousendflag;
	}
	public void setJushousendflag(int jushousendflag) {
		this.jushousendflag = jushousendflag;
	}
	
	public int getShangmentuiSupport() {
		return shangmentuiSupport;
	}
	public void setShangmentuiSupport(int shangmentuiSupport) {
		this.shangmentuiSupport = shangmentuiSupport;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getUser_name() {
		return this.user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getPass_word() {
		return this.pass_word;
	}

	public void setPass_word(String pass_word) {
		this.pass_word = pass_word;
	}

	public String getBranchname() {
		return this.branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public int getMaxCount() {
		return this.maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	public String getPrivate_key() {
		return this.private_key;
	}

	public void setPrivate_key(String private_key) {
		this.private_key = private_key;
	}

	public String getCustomerid() {
		return this.customerid;
	}

	public void setCustomerid(String customerid) {
		this.customerid = customerid;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
