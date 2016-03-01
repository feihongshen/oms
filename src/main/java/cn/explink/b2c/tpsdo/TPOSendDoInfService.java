package cn.explink.b2c.tpsdo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.sf.json.JSONObject;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.CacheBaseListener;
import cn.explink.b2c.tpsdo.bean.TPOSendDoInf;
import cn.explink.b2c.tpsdo.bean.ThirdPartyOrder2DOCfg;
import cn.explink.b2c.tpsdo.bean.ThirdPartyOrder2DORequestVo;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.Customer;
import cn.explink.enumutil.CwbOrderTypeIdEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.enumutil.PaytypeEnum;
import cn.explink.jms.dto.CwbOrderWithDeliveryState;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.JsonUtil;
import cn.explink.util.StringUtil;

@Service
public class TPOSendDoInfService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private CacheBaseListener cacheBaseListener;
	@Autowired
	private GetDmpDAO getDmpDAO;
	@Autowired
	TPOSendDoInfDao tPOSendDoInfDao;
	@Autowired
	private B2cTools b2ctools;
	/**
	 *  把外单（第三方订单）数据 插入到 TPO_SEND_DO_INF表，待定时器推送给TPS 的DO服务
	 * @param cwbOrderWithDeliveryState
	 * @param orderFlow
	 */
	public void insert2_TPO_SEND_DO_INF(CwbOrderWithDeliveryState cwbOrderWithDeliveryState, DmpOrderFlow orderFlow){
		//外单数据 在 接单（导入数据环节）的时候就要推送给TPS 的DO服务
		if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.DaoRuShuJu.getValue()) {
			
			DmpCwbOrder cwbOrder = cwbOrderWithDeliveryState.getCwbOrder();
			long customerid = cwbOrderWithDeliveryState.getCwbOrder().getCustomerid();
			Customer customer = this.cacheBaseListener.getCustomer(customerid);
			if (customer == null) {
				this.logger.info("Customer对象在缓存中没有获取到，请求dmp..cwb={},customerid={}", orderFlow.getCwb(),customerid);
				customer = this.getDmpDAO.getCustomer(customerid);
			}
				
			//查询出唯速达所设置的客户
			ThirdPartyOrder2DOCfg pushCfg = this.getThirdPartyOrder2DOCfg();
			if(pushCfg == null || pushCfg.getOpenFlag() == 0){
				this.logger.info("未获取到外单推送DO服务对接配置信息，外单cwb={}", orderFlow.getCwb());
				return;
			}
			//是否是外单客户
			boolean isTPCust  = this.isThirdPartyCustomer(customerid);
			if(isTPCust){
				this.logger.info("外单cwb={}加入外单推DO接口表（TPO_SEND_DO_INF）", orderFlow.getCwb());
				ThirdPartyOrder2DORequestVo  thirdPartyOrder2DORequestVo = this.buildThirdPartyOrder2DORequestVo(cwbOrder, customer, pushCfg);
				TPOSendDoInf tPOSendDoInf = this.buildTPOSendDoInf(cwbOrder,customer);
				try {
					String reqObjJson =  JsonUtil.translateToJson(thirdPartyOrder2DORequestVo);
					tPOSendDoInf.setReqObjJson(reqObjJson);
				} catch (Exception e) {
					logger.error("请求参数对象转JSON异常,cwb="+tPOSendDoInf.getCwb(), e); 
				}
				
				this.tPOSendDoInfDao.saveTPOSendDoInf(tPOSendDoInf);
				
			}						
				
		}
	}

	//获取配置信息
	public ThirdPartyOrder2DOCfg getThirdPartyOrder2DOCfg() {
		ThirdPartyOrder2DOCfg cfg = null;
		String objectMethod = this.b2ctools.getObjectMethod(B2cEnum.ThirdPartyOrder_2_DO.getKey()).getJoint_property();
		if (objectMethod != null) {
			JSONObject jsonObj = JSONObject.fromObject(objectMethod);
			cfg = (ThirdPartyOrder2DOCfg) JSONObject.toBean(jsonObj, ThirdPartyOrder2DOCfg.class);
		} 
		return cfg;
	}
	
	/**
	 * 
	 * @param customerid
	 * @return
	 */
	public boolean isThirdPartyCustomer(long customerid){
		//查询出唯速达所设置的客户
		ThirdPartyOrder2DOCfg pushCfg = this.getThirdPartyOrder2DOCfg();
		if(pushCfg == null){
			return false;
		}
		String customeridsCfg = pushCfg.getCustomerids();
		String[] customerIdsCfgArray = customeridsCfg.split(",|，");
		//是否是外单客户
		boolean isTPCust  = false;
		for(String customeridStr : customerIdsCfgArray){
			try{
				if( Long.valueOf(customeridStr).longValue() == customerid){
					isTPCust = true;
					break;
				}
			}catch(Exception e){
				//do nothing
			}
		}
		return isTPCust;
	}
	
	
	/**
	 * 根据cwbOrder构建TPOSendDoInf对象
	 * @param cwbOrder
	 * @return
	 */
	private TPOSendDoInf buildTPOSendDoInf(DmpCwbOrder cwbOrder,Customer customer){
		TPOSendDoInf tPOSendDoInf = new TPOSendDoInf();
		tPOSendDoInf.setCreateTime(new Date());
		tPOSendDoInf.setCustcode(StringUtil.nullConvertToEmptyString(customer.getCustomercode()));
		tPOSendDoInf.setCwb(cwbOrder.getCwb());
		tPOSendDoInf.setUpdateTime(new Date());
		return tPOSendDoInf;
	}
	
	
	/**
	 * 根据cwbOrder构建ThirdPartyOrder2DORequestVo对象
	 * @param cwbOrder
	 * @param customer
	 * @param pushCfg
	 * @return
	 */
	private ThirdPartyOrder2DORequestVo  buildThirdPartyOrder2DORequestVo(DmpCwbOrder cwbOrder,Customer customer,ThirdPartyOrder2DOCfg pushCfg){
		ThirdPartyOrder2DORequestVo thirdPartyOrder2DORequestVo = new ThirdPartyOrder2DORequestVo();
		int cwbordertypeid = Integer.parseInt(cwbOrder.getCwbordertypeid());
		
		String posString = this.getDmpDAO.getNowCustomerPos(customer.getCustomerid());
		String nextBranceTpsbranchcode = null; //下一站
		if(cwbOrder.getNextbranchid() > 0){
			Branch branch = this.cacheBaseListener.getBranch(cwbOrder.getNextbranchid());
			if (branch == null) {
				this.logger.info("Branch对象在缓存中没有获取到，OMS外单业务请求dmp..cwb={},nextBranchid={}", cwbOrder.getCwb(),cwbOrder.getNextbranchid());
				branch = this.getDmpDAO.getNowBranch(cwbOrder.getNextbranchid());
			}
			nextBranceTpsbranchcode = branch.getTpsbranchcode();
		}
		String destOrg = null; //目的机构（派送站点）
		if(cwbOrder.getDeliverybranchid() > 0){
			Branch branch = this.cacheBaseListener.getBranch(cwbOrder.getDeliverybranchid());
			if (branch == null) {
				this.logger.info("Branch对象在缓存中没有获取到，OMS外单业务请求dmp..cwb={},deliverybranchid={}", cwbOrder.getCwb(),cwbOrder.getDeliverybranchid());
				branch = this.getDmpDAO.getNowBranch(cwbOrder.getDeliverybranchid());
			}
			destOrg = branch.getTpsbranchcode();
		}
		thirdPartyOrder2DORequestVo.setAcceptDept(nextBranceTpsbranchcode == null ? "" : nextBranceTpsbranchcode);//由原来的id改为编码 。zhouguoting 2016/02/25
		//thirdPartyOrder2DORequestVo.setAcceptDept(cwbOrder.getNextbranchid() == 0 ? "" : cwbOrder.getNextbranchid() + "");
		thirdPartyOrder2DORequestVo.setAcceptOperator("");
		thirdPartyOrder2DORequestVo.setAccountMark(posString);
		thirdPartyOrder2DORequestVo.setActualFee(cwbOrder.getInfactfare());
		int paywayid = (int)cwbOrder.getPaywayid();
		if(paywayid == PaytypeEnum.Xianjin.getValue()){
			thirdPartyOrder2DORequestVo.setActualPayType(0);
			thirdPartyOrder2DORequestVo.setPayment(0);
		}
		else if(paywayid == PaytypeEnum.Pos.getValue()){
			thirdPartyOrder2DORequestVo.setActualPayType(1);
			thirdPartyOrder2DORequestVo.setPayment(1);
		}
		else if(paywayid == PaytypeEnum.CodPos.getValue()){
			thirdPartyOrder2DORequestVo.setActualPayType(2);
			thirdPartyOrder2DORequestVo.setPayment(2);
		}
		else{
			thirdPartyOrder2DORequestVo.setActualPayType(-1);
			thirdPartyOrder2DORequestVo.setPayment(-1);
		}
		
		//thirdPartyOrder2DORequestVo.setAssuranceFee(BigDecimal.ZERO);
		//thirdPartyOrder2DORequestVo.setAssuranceValue(BigDecimal.ZERO);
		//thirdPartyOrder2DORequestVo.setCalculateWeight(BigDecimal.ZERO);
		thirdPartyOrder2DORequestVo.setCarriage(cwbOrder.getShouldfare());
		thirdPartyOrder2DORequestVo.setCneeAddr(StringUtil.nullConvertToEmptyString(cwbOrder.getConsigneeaddress()));
		thirdPartyOrder2DORequestVo.setCneeCity(StringUtil.nullConvertToEmptyString(cwbOrder.getCwbcity()));
		thirdPartyOrder2DORequestVo.setCneeProv(StringUtil.nullConvertToEmptyString(cwbOrder.getCwbprovince()));
		thirdPartyOrder2DORequestVo.setCneeRegion(StringUtil.nullConvertToEmptyString(cwbOrder.getCwbcounty()));
		thirdPartyOrder2DORequestVo.setCneeTown("");
		thirdPartyOrder2DORequestVo.setCneeCertificate("");
		thirdPartyOrder2DORequestVo.setCneeCorpName("");
		thirdPartyOrder2DORequestVo.setCneeMobile(StringUtil.nullConvertToEmptyString(cwbOrder.getConsigneemobile()));
		thirdPartyOrder2DORequestVo.setCneeName(StringUtil.nullConvertToEmptyString(cwbOrder.getConsigneename()));
		thirdPartyOrder2DORequestVo.setCneeNo("");
		
		//0：送货时间不限1：只工作日(双休日/节假日不用送)2：只双休日/节假日送货(工作日不用送)
		if(cwbOrder.getCustomercommand() != null){
			if(cwbOrder.getCustomercommand().contains("只工作日")){
				thirdPartyOrder2DORequestVo.setCneePeriod(1);
			}else if(cwbOrder.getCustomercommand().contains("工作日不用送")){
				thirdPartyOrder2DORequestVo.setCneePeriod(2);
			}else{
				thirdPartyOrder2DORequestVo.setCneePeriod(0);
			}
		}else{
			thirdPartyOrder2DORequestVo.setCneePeriod(0);

		}
		thirdPartyOrder2DORequestVo.setCneeRemark(StringUtil.nullConvertToEmptyString(cwbOrder.getPodsignremark()));
		thirdPartyOrder2DORequestVo.setCneeTel(StringUtil.nullConvertToEmptyString(cwbOrder.getConsigneephone()));
		thirdPartyOrder2DORequestVo.setCnorAddr("");
		thirdPartyOrder2DORequestVo.setCnorCity("");
		thirdPartyOrder2DORequestVo.setCnorProv("");
		thirdPartyOrder2DORequestVo.setCnorRegion("");
		thirdPartyOrder2DORequestVo.setCnorRemark("");
		thirdPartyOrder2DORequestVo.setCnorTel("");
		thirdPartyOrder2DORequestVo.setCnorTown("");
		thirdPartyOrder2DORequestVo.setCnorMobile("");
		thirdPartyOrder2DORequestVo.setCnorName("");
		 if(cwbOrder.getReceivablefee().intValue() > 0){
			 thirdPartyOrder2DORequestVo.setIsCod(1);//是否货到付款
			 thirdPartyOrder2DORequestVo.setCodAmount(cwbOrder.getReceivablefee());
         }else{
        	 thirdPartyOrder2DORequestVo.setIsCod(0);
         }
		thirdPartyOrder2DORequestVo.setCustCode(StringUtil.nullConvertToEmptyString(customer.getCustomercode()));
		thirdPartyOrder2DORequestVo.setCustName(StringUtil.nullConvertToEmptyString(customer.getCustomername()));
		thirdPartyOrder2DORequestVo.setCustOrderNo(cwbOrder.getCwb());
		//thirdPartyOrder2DORequestVo.setDestOrg(cwbOrder.getDeliverybranchid() == 0  ? null : cwbOrder.getDeliverybranchid() + "");
		thirdPartyOrder2DORequestVo.setDestOrg(destOrg);//由原来的传机构id改为传编码 。zhouguoting 2016/02/25
		thirdPartyOrder2DORequestVo.setDistributer(cwbOrder.getDeliverid() == 0 ? "" : cwbOrder.getDeliverid() + "");
		//thirdPartyOrder2DORequestVo.setJoinTime(null);
		if(cwbordertypeid == CwbOrderTypeIdEnum.Express.getValue()){
			thirdPartyOrder2DORequestVo.setOrderType(1);
		}else if(cwbordertypeid == CwbOrderTypeIdEnum.Peisong.getValue()){
			thirdPartyOrder2DORequestVo.setOrderType(2);
		} else if(cwbordertypeid == CwbOrderTypeIdEnum.Shangmenhuan.getValue()){
			thirdPartyOrder2DORequestVo.setOrderType(4);
		} else if(cwbordertypeid  == CwbOrderTypeIdEnum.Shangmentui.getValue()){
			thirdPartyOrder2DORequestVo.setOrderType(3);
		}
		
		if(cwbordertypeid == CwbOrderTypeIdEnum.Express.getValue()){
			thirdPartyOrder2DORequestVo.setOrderSource(3); //快递订单
		}else{
			thirdPartyOrder2DORequestVo.setOrderSource(4);//外部订单
		}		
		//thirdPartyOrder2DORequestVo.setPayType(payType);
		//thirdPartyOrder2DORequestVo.setPickerTime(pickerTime);
		if(cwbOrder.getPaybackfee().compareTo(BigDecimal.ZERO) != 0){
			thirdPartyOrder2DORequestVo.setReturnAmount(cwbOrder.getPaybackfee());
		}
		thirdPartyOrder2DORequestVo.setSendCarrierCode(pushCfg.getCarrierCode());
		//thirdPartyOrder2DORequestVo.setTotalBox(totalBox);
		thirdPartyOrder2DORequestVo.setTotalNum((int)cwbOrder.getSendcarnum());
		//thirdPartyOrder2DORequestVo.setTotalVolume(totalVolume);
		//thirdPartyOrder2DORequestVo.setTotalWeight(totalWeight);
		thirdPartyOrder2DORequestVo.setTransportNo("");
		thirdPartyOrder2DORequestVo.setWarehouse(StringUtil.nullConvertToEmptyString(cwbOrder.getCarwarehouse()));
		thirdPartyOrder2DORequestVo.setZipCode(StringUtil.nullConvertToEmptyString(cwbOrder.getConsigneepostcode()));
				
		if(StringUtils.isNotBlank(cwbOrder.getTranscwb())){
			List<ThirdPartyOrder2DORequestVo.Boxinfo> boxinfos = new ArrayList<ThirdPartyOrder2DORequestVo.Boxinfo>();
			String[] boxNos = cwbOrder.getTranscwb().split(",|，");
			thirdPartyOrder2DORequestVo.setTotalBox(boxNos.length);
			for(String boxNo : boxNos){
				ThirdPartyOrder2DORequestVo.Boxinfo boxInfo = new ThirdPartyOrder2DORequestVo.Boxinfo();
				boxInfo.setBoxNo(boxNo);
				boxinfos.add(boxInfo);
			}
			thirdPartyOrder2DORequestVo.setBoxinfos(boxinfos);
		}	
		return thirdPartyOrder2DORequestVo;
	}
	
	/**
	 * 更新推送状态 和绑定运单号
	 * @param cwb
	 * @param custcode
	 * @param transportNo
	 * @param isSent
	 * @param remark
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW,isolation=Isolation.READ_COMMITTED)
	public void updateTPOSendDoInf(String cwb, String custcode, String transportNo,int isSent,int trytime, String remark){
		this.tPOSendDoInfDao.updateTPOSendDoInf(cwb, custcode, transportNo, isSent, trytime, remark);
	}
	
}
