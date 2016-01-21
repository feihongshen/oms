package cn.explink.b2c.tpsdo.bean;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 外单推DO服务请求 参数  封装类
 * @author gordon.zhou
 *
 */
public class ThirdPartyOrder2DORequestVo {
	private String transportNo; //TPS运单号
	private String custOrderNo;//客户订单号cwb
	private String custCode;// 发件客户编码
	private String warehouse; //所属仓库
	private String custName; //客户名称
	private String accountMark; //分账标示
	private int orderType; //订单类型1：快递单2：配送单3：上门退 4：上门换
	private String acceptDept; //揽件站点站点自建编码(配送单非必填)
	private String acceptOperator; //揽件员（配送单非必填）
	private String pickerTime; //揽件时间(yyyy-mm-dd hh:mm:ss 配送非必填)
	private int orderSource; //订单来源默认1。1：多渠道订单2：VIP订单3：快递订单4:外部订单
	private Date joinTime; // 出库时间
	private String cnorProv	;//发货省名称
	private String cnorCity	;//发货城市名称
	private String cnorRegion; //发货区/县名称
	private String cnorTown	; //发货街道名称
	private String cnorAddr	; //发货详细地址
	private String cnorName	; // 发货联系人
	private String cnorMobile; //发货人联系手机
	private String cnorTel; //发货人联系电话
	private String cnorRemark; //发货备注
	private String cneeProv; //收货省
	private String cneeCity; //收货城市
	private String cneeRegion;// 收货区/县
	private String cneeTown; //收货街道
	private String cneeAddr; //收货地址
	private String cneeName; //收货联系人
	private String cneeMobile;//收货联系人手机
	private String zipCode; //收货人邮编
	private String cneeTel; //收货联系人电话
	private int cneePeriod; //送货时间默认0.0：送货时间不限1：只工作日(双休日/节假日不用送)2：只双休日/节假日送货(工作日不用送)
	private String cneeRemark;//收货备注	
	private String cneeCertificate; //寄件人证件号
	private String cneeNo; //收货公司编码
	private String cneeCorpName; //收货公司名称
	private String destOrg;//目的站点
	private int isCod; //是否货到付款.默认00：否1：是
	private BigDecimal codAmount = BigDecimal.ZERO; //当isCod为1时，必填且大于0
	private BigDecimal carriage = BigDecimal.ZERO; //运费合计(没有传0.00)
	private BigDecimal returnAmount;//上门退订单才有(没有就是0)
	private int totalNum; //订单对应的商品数量(没有传0)
	private BigDecimal totalWeight = BigDecimal.ZERO; //实际重量没有传0.00
	private BigDecimal calculateWeight = BigDecimal.ZERO;// 计费重量没有传0.00
	private BigDecimal totalVolume = BigDecimal.ZERO; //合计体积没有传0.00
	private int totalBox; //合计箱数没有传0
	private BigDecimal assuranceValue = BigDecimal.ZERO; //保价金额默认0.00
	private BigDecimal assuranceFee = BigDecimal.ZERO;//保费默认0.00
	private int payType = -1; //支付方式。默认-1，0：月1：现付2：到付
	private int payment = -1; //付款方式。默认-1，0：现金1：pos 2：支付宝
	private BigDecimal actualFee = BigDecimal.ZERO;; //实收运费  	默认0.00
	private String distributer; //快件员id派件员
	private String sendCarrierCode; //派送承运商派送承运商, TMS 承运商编码
	private int actualPayType=-1; // 实际付款方式默认-1，0：现金1：pos 2：支付宝
	
	private List<OrderCargoInfo> details;
	private List<boxinfo> boinfos;
	
	public String getTransportNo() {
		return transportNo;
	}
	public void setTransportNo(String transportNo) {
		this.transportNo = transportNo;
	}
	public String getCustOrderNo() {
		return custOrderNo;
	}
	public void setCustOrderNo(String custOrderNo) {
		this.custOrderNo = custOrderNo;
	}
	public String getCustCode() {
		return custCode;
	}
	public void setCustCode(String custCode) {
		this.custCode = custCode;
	}
	public String getWarehouse() {
		return warehouse;
	}
	public void setWarehouse(String warehouse) {
		this.warehouse = warehouse;
	}
	public String getCustName() {
		return custName;
	}
	public void setCustName(String custName) {
		this.custName = custName;
	}
	public String getAccountMark() {
		return accountMark;
	}
	public void setAccountMark(String accountMark) {
		this.accountMark = accountMark;
	}
	public int getOrderType() {
		return orderType;
	}
	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}
	public String getAcceptDept() {
		return acceptDept;
	}
	public void setAcceptDept(String acceptDept) {
		this.acceptDept = acceptDept;
	}
	public String getAcceptOperator() {
		return acceptOperator;
	}
	public void setAcceptOperator(String acceptOperator) {
		this.acceptOperator = acceptOperator;
	}
	public String getPickerTime() {
		return pickerTime;
	}
	public void setPickerTime(String pickerTime) {
		this.pickerTime = pickerTime;
	}
	public int getOrderSource() {
		return orderSource;
	}
	public void setOrderSource(int orderSource) {
		this.orderSource = orderSource;
	}
	public Date getJoinTime() {
		return joinTime;
	}
	public void setJoinTime(Date joinTime) {
		this.joinTime = joinTime;
	}
	public String getCnorProv() {
		return cnorProv;
	}
	public void setCnorProv(String cnorProv) {
		this.cnorProv = cnorProv;
	}
	public String getCnorCity() {
		return cnorCity;
	}
	public void setCnorCity(String cnorCity) {
		this.cnorCity = cnorCity;
	}
	public String getCnorRegion() {
		return cnorRegion;
	}
	public void setCnorRegion(String cnorRegion) {
		this.cnorRegion = cnorRegion;
	}
	public String getCnorTown() {
		return cnorTown;
	}
	public void setCnorTown(String cnorTown) {
		this.cnorTown = cnorTown;
	}
	public String getCnorAddr() {
		return cnorAddr;
	}
	public void setCnorAddr(String cnorAddr) {
		this.cnorAddr = cnorAddr;
	}
	public String getCnorName() {
		return cnorName;
	}
	public void setCnorName(String cnorName) {
		this.cnorName = cnorName;
	}
	public String getCnorMobile() {
		return cnorMobile;
	}
	public void setCnorMobile(String cnorMobile) {
		this.cnorMobile = cnorMobile;
	}
	public String getCnorTel() {
		return cnorTel;
	}
	public void setCnorTel(String cnorTel) {
		this.cnorTel = cnorTel;
	}
	public String getCnorRemark() {
		return cnorRemark;
	}
	public void setCnorRemark(String cnorRemark) {
		this.cnorRemark = cnorRemark;
	}
	public String getCneeProv() {
		return cneeProv;
	}
	public void setCneeProv(String cneeProv) {
		this.cneeProv = cneeProv;
	}
	public String getCneeCity() {
		return cneeCity;
	}
	public void setCneeCity(String cneeCity) {
		this.cneeCity = cneeCity;
	}
	public String getCneeRegion() {
		return cneeRegion;
	}
	public void setCneeRegion(String cneeRegion) {
		this.cneeRegion = cneeRegion;
	}
	public String getCneeTown() {
		return cneeTown;
	}
	public void setCneeTown(String cneeTown) {
		this.cneeTown = cneeTown;
	}
	public String getCneeAddr() {
		return cneeAddr;
	}
	public void setCneeAddr(String cneeAddr) {
		this.cneeAddr = cneeAddr;
	}
	public String getDestOrg() {
		return destOrg;
	}
	public void setDestOrg(String destOrg) {
		this.destOrg = destOrg;
	}
	public String getCneeName() {
		return cneeName;
	}
	public void setCneeName(String cneeName) {
		this.cneeName = cneeName;
	}
	public String getCneeMobile() {
		return cneeMobile;
	}
	public void setCneeMobile(String cneeMobile) {
		this.cneeMobile = cneeMobile;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public String getCneeTel() {
		return cneeTel;
	}
	public void setCneeTel(String cneeTel) {
		this.cneeTel = cneeTel;
	}
	public int getCneePeriod() {
		return cneePeriod;
	}
	public void setCneePeriod(int cneePeriod) {
		this.cneePeriod = cneePeriod;
	}
	public String getCneeRemark() {
		return cneeRemark;
	}
	public void setCneeRemark(String cneeRemark) {
		this.cneeRemark = cneeRemark;
	}
	public String getCneeCertificate() {
		return cneeCertificate;
	}
	public void setCneeCertificate(String cneeCertificate) {
		this.cneeCertificate = cneeCertificate;
	}
	public String getCneeNo() {
		return cneeNo;
	}
	public void setCneeNo(String cneeNo) {
		this.cneeNo = cneeNo;
	}
	public String getCneeCorpName() {
		return cneeCorpName;
	}
	public void setCneeCorpName(String cneeCorpName) {
		this.cneeCorpName = cneeCorpName;
	}
	
	public int getIsCod() {
		return isCod;
	}
	public void setIsCod(int isCod) {
		this.isCod = isCod;
	}
	public BigDecimal getCodAmount() {
		return codAmount;
	}
	public void setCodAmount(BigDecimal codAmount) {
		this.codAmount = codAmount;
	}
	public BigDecimal getCarriage() {
		return carriage;
	}
	public void setCarriage(BigDecimal carriage) {
		this.carriage = carriage;
	}
	public BigDecimal getReturnAmount() {
		return returnAmount;
	}
	public void setReturnAmount(BigDecimal returnAmount) {
		this.returnAmount = returnAmount;
	}
	public int getTotalNum() {
		return totalNum;
	}
	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}
	public BigDecimal getTotalWeight() {
		return totalWeight;
	}
	public void setTotalWeight(BigDecimal totalWeight) {
		this.totalWeight = totalWeight;
	}
	public BigDecimal getCalculateWeight() {
		return calculateWeight;
	}
	public void setCalculateWeight(BigDecimal calculateWeight) {
		this.calculateWeight = calculateWeight;
	}
	public BigDecimal getTotalVolume() {
		return totalVolume;
	}
	public void setTotalVolume(BigDecimal totalVolume) {
		this.totalVolume = totalVolume;
	}
	public int getTotalBox() {
		return totalBox;
	}
	public void setTotalBox(int totalBox) {
		this.totalBox = totalBox;
	}
	public BigDecimal getAssuranceValue() {
		return assuranceValue;
	}
	public void setAssuranceValue(BigDecimal assuranceValue) {
		this.assuranceValue = assuranceValue;
	}
	public BigDecimal getAssuranceFee() {
		return assuranceFee;
	}
	public void setAssuranceFee(BigDecimal assuranceFee) {
		this.assuranceFee = assuranceFee;
	}
	public int getPayType() {
		return payType;
	}
	public void setPayType(int payType) {
		this.payType = payType;
	}
	public int getPayment() {
		return payment;
	}
	public void setPayment(int payment) {
		this.payment = payment;
	}
	public BigDecimal getActualFee() {
		return actualFee;
	}
	public void setActualFee(BigDecimal actualFee) {
		this.actualFee = actualFee;
	}
	public String getDistributer() {
		return distributer;
	}
	public void setDistributer(String distributer) {
		this.distributer = distributer;
	}
	public String getSendCarrierCode() {
		return sendCarrierCode;
	}
	public void setSendCarrierCode(String sendCarrierCode) {
		this.sendCarrierCode = sendCarrierCode;
	}
	public int getActualPayType() {
		return actualPayType;
	}
	public void setActualPayType(int actualPayType) {
		this.actualPayType = actualPayType;
	}
	public List<OrderCargoInfo> getDetails() {
		return details;
	}
	public void setDetails(List<OrderCargoInfo> details) {
		this.details = details;
	}
	public List<boxinfo> getBoinfos() {
		return boinfos;
	}
	public void setBoinfos(List<boxinfo> boinfos) {
		this.boinfos = boinfos;
	}
	
	
	
	
}

class OrderCargoInfo{
	private String cargoName;//货物名称
	private int count;//件数
	private BigDecimal cargoLength = BigDecimal.ZERO;	//长
	private BigDecimal cargoWidth = BigDecimal.ZERO;//宽
	private BigDecimal cargoHeight = BigDecimal.ZERO; //高
	private BigDecimal weight = BigDecimal.ZERO;//重量	
	private BigDecimal volume = BigDecimal.ZERO;//体积
	private String sizeSn;//商品条码
	private BigDecimal price = BigDecimal.ZERO;//商品价格
	private String unit;//单位
	public String getCargoName() {
		return cargoName;
	}
	public void setCargoName(String cargoName) {
		this.cargoName = cargoName;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public BigDecimal getCargoLength() {
		return cargoLength;
	}
	public void setCargoLength(BigDecimal cargoLength) {
		this.cargoLength = cargoLength;
	}
	public BigDecimal getCargoWidth() {
		return cargoWidth;
	}
	public void setCargoWidth(BigDecimal cargoWidth) {
		this.cargoWidth = cargoWidth;
	}
	public BigDecimal getCargoHeight() {
		return cargoHeight;
	}
	public void setCargoHeight(BigDecimal cargoHeight) {
		this.cargoHeight = cargoHeight;
	}
	public BigDecimal getWeight() {
		return weight;
	}
	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}
	public BigDecimal getVolume() {
		return volume;
	}
	public void setVolume(BigDecimal volume) {
		this.volume = volume;
	}
	public String getSizeSn() {
		return sizeSn;
	}
	public void setSizeSn(String sizeSn) {
		this.sizeSn = sizeSn;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}	
	
}

class boxinfo{
	private String boxNo;//箱号
	private BigDecimal volume;//体积
	private BigDecimal weight;//重量
	public String getBoxNo() {
		return boxNo;
	}
	public void setBoxNo(String boxNo) {
		this.boxNo = boxNo;
	}
	public BigDecimal getVolume() {
		return volume;
	}
	public void setVolume(BigDecimal volume) {
		this.volume = volume;
	}
	public BigDecimal getWeight() {
		return weight;
	}
	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}
	
	
}
