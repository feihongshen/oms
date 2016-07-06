package cn.explink.b2c.vipshop;

/**
 * 唯品会对接设置配置属性
 * 
 * @author Administrator
 *
 */
public class VipShop {
	private String shipper_no; // 承运商编码
	private String private_key; // 加密秘钥
	private int getMaxCount; // 每次获取订单最大数量
	private int sendMaxCount; // 每次推送最大数量
	private String getCwb_URL; // 获取订单的URl
	private String sendCwb_URL; // 反馈URL
	private String customerids; // 在系统中的id
	private long vipshop_seq; // 每次获取的seq，之后不断累加
	private long warehouseid; // 订单导入库房ID
	private int isopendownload; // 是否开启订单下载接口
	private int forward_hours; // 提前N个小时发送，可动态配置的。
	private int isTuoYunDanFlag; // 是否开启托运单 模式，生成多个批次 0 不开启
	private int cancelOrIntercept; // 0 开启取消， 1拦截
	private int isOpenLefengflag; //下载配送单 是否只开启乐蜂下载 0关闭， 1开启    
	private int resuseReasonFlag;  //拒收原因是否回传默认 0，  0回传， 1不回传
	private int isCreateTimeToEmaildateFlag; //是否唯品会订单出仓时间作为 邮件批次时间，emaildate 默认0 ，开启：1
	private String lefengCustomerid; //乐蜂customerid 
	private int daysno;//干线回单重发天数
	private long selb2cnum;//货态重发次数
	private String oxoState_URL;//获取OXO订单揽收状态URL
	
	private int isGetPeisongFlag;//是否接收正常配送的订单数据
	private int isGetShangmentuiFlag;//是否接收上门退的订单数据
	private int isGetShangmenhuanFlag;//是否接收上门换的订单数据
	private int isGetOXOFlag;//是否接收OXO的订单数据
	private int isGetExpressFlag;//是否接收快递单数据
	private int isAutoInterface;//是否自动化接口
	
	private int isTpsSendFlag;//是否订单下发接口
	public int getDaysno() {
		return daysno;
	}

	public void setDaysno(int daysno) {
		this.daysno = daysno;
	}

	public long getSelb2cnum() {
		return selb2cnum;
	}

	public void setSelb2cnum(long selb2cnum) {
		this.selb2cnum = selb2cnum;
	}

	private int openmpspackageflag; // 开启集包:0 默认  1开启，a.用于接口回传判断是否存储和推送  b.用于订单下载控制是否处理集包订单 
	private String transflowUrl; //运单推送集包URL

	public String getTransflowUrl() {
		return transflowUrl;
	}

	public void setTransflowUrl(String transflowUrl) {
		this.transflowUrl = transflowUrl;
	}

	public int getOpenmpspackageflag() {
		return openmpspackageflag;
	}

	public void setOpenmpspackageflag(int openmpspackageflag) {
		this.openmpspackageflag = openmpspackageflag;
	}

	public String getLefengCustomerid() {
		return lefengCustomerid;
	}

	public int getIsCreateTimeToEmaildateFlag() {
		return isCreateTimeToEmaildateFlag;
	}

	public void setIsCreateTimeToEmaildateFlag(int isCreateTimeToEmaildateFlag) {
		this.isCreateTimeToEmaildateFlag = isCreateTimeToEmaildateFlag;
	}

	public void setLefengCustomerid(String lefengCustomerid) {
		this.lefengCustomerid = lefengCustomerid;
	}

	public int getResuseReasonFlag() {
		return resuseReasonFlag;
	}

	public void setResuseReasonFlag(int resuseReasonFlag) {
		this.resuseReasonFlag = resuseReasonFlag;
	}

	public int getIsOpenLefengflag() {
		return isOpenLefengflag;
	}

	public void setIsOpenLefengflag(int isOpenLefengflag) {
		this.isOpenLefengflag = isOpenLefengflag;
	}

	public int getCancelOrIntercept() {
		return cancelOrIntercept;
	}

	public void setCancelOrIntercept(int cancelOrIntercept) {
		this.cancelOrIntercept = cancelOrIntercept;
	}

	private int isShangmentuiFlag; // 是否开启上门退业务 0 关闭，1开启

	public int getIsShangmentuiFlag() {
		return isShangmentuiFlag;
	}

	public void setIsShangmentuiFlag(int isShangmentuiFlag) {
		this.isShangmentuiFlag = isShangmentuiFlag;
	}

	public int getIsTuoYunDanFlag() {
		return isTuoYunDanFlag;
	}

	public void setIsTuoYunDanFlag(int isTuoYunDanFlag) {
		this.isTuoYunDanFlag = isTuoYunDanFlag;
	}

	public int getForward_hours() {
		return forward_hours;
	}

	public void setForward_hours(int forward_hours) {
		this.forward_hours = forward_hours;
	}

	public int getIsopendownload() {
		return isopendownload;
	}

	public void setIsopendownload(int isopendownload) {
		this.isopendownload = isopendownload;
	}

	public long getWarehouseid() {
		return warehouseid;
	}

	public void setWarehouseid(long warehouseid) {
		this.warehouseid = warehouseid;
	}

	public long getVipshop_seq() {
		return vipshop_seq;
	}

	public void setVipshop_seq(long vipshop_seq) {
		this.vipshop_seq = vipshop_seq;
	}

	public String getShipper_no() {
		return shipper_no;
	}

	public void setShipper_no(String shipper_no) {
		this.shipper_no = shipper_no;
	}

	public String getPrivate_key() {
		return private_key;
	}

	public void setPrivate_key(String private_key) {
		this.private_key = private_key;
	}

	public int getGetMaxCount() {
		return getMaxCount;
	}

	public void setGetMaxCount(int getMaxCount) {
		this.getMaxCount = getMaxCount;
	}

	public int getSendMaxCount() {
		return sendMaxCount;
	}

	public void setSendMaxCount(int sendMaxCount) {
		this.sendMaxCount = sendMaxCount;
	}

	public String getGetCwb_URL() {
		return getCwb_URL;
	}

	public void setGetCwb_URL(String getCwb_URL) {
		this.getCwb_URL = getCwb_URL;
	}

	public String getSendCwb_URL() {
		return sendCwb_URL;
	}

	public void setSendCwb_URL(String sendCwb_URL) {
		this.sendCwb_URL = sendCwb_URL;
	}

	public String getCustomerids() {
		return customerids;
	}

	public void setCustomerids(String customerids) {
		this.customerids = customerids;
	}

	public String getOxoState_URL() {
		return oxoState_URL;
	}

	public void setOxoState_URL(String oxoState_URL) {
		this.oxoState_URL = oxoState_URL;
	}

	public int getIsTpsSendFlag() {
		return isTpsSendFlag;
	}

	public void setIsTpsSendFlag(int isTpsSendFlag) {
		this.isTpsSendFlag = isTpsSendFlag;
	}

	public int getIsGetPeisongFlag() {
		return isGetPeisongFlag;
	}

	public void setIsGetPeisongFlag(int isGetPeisongFlag) {
		this.isGetPeisongFlag = isGetPeisongFlag;
	}

	public int getIsGetShangmentuiFlag() {
		return isGetShangmentuiFlag;
	}

	public void setIsGetShangmentuiFlag(int isGetShangmentuiFlag) {
		this.isGetShangmentuiFlag = isGetShangmentuiFlag;
	}

	public int getIsGetShangmenhuanFlag() {
		return isGetShangmenhuanFlag;
	}

	public void setIsGetShangmenhuanFlag(int isGetShangmenhuanFlag) {
		this.isGetShangmenhuanFlag = isGetShangmenhuanFlag;
	}

	public int getIsGetOXOFlag() {
		return isGetOXOFlag;
	}

	public void setIsGetOXOFlag(int isGetOXOFlag) {
		this.isGetOXOFlag = isGetOXOFlag;
	}

	public int getIsGetExpressFlag() {
		return isGetExpressFlag;
	}

	public void setIsGetExpressFlag(int isGetExpressFlag) {
		this.isGetExpressFlag = isGetExpressFlag;
	}

	public int getIsAutoInterface() {
		return isAutoInterface;
	}

	public void setIsAutoInterface(int isAutoInterface) {
		this.isAutoInterface = isAutoInterface;
	}

}
