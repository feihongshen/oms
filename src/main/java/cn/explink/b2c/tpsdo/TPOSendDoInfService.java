package cn.explink.b2c.tpsdo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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
import cn.explink.b2c.tpsdo.bean.ThirdPartyOrderEditInfo;
import cn.explink.dao.CwbDAO;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.Customer;
import cn.explink.domain.CwbOrder;
import cn.explink.domain.User;
import cn.explink.enumutil.CwbOrderTypeIdEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.enumutil.PaytypeEnum;
import cn.explink.enumutil.TPOOperateTypeEnum;
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
	@Autowired
	private CwbDAO cwbDAO;
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
				TPOSendDoInf history = this.tPOSendDoInfDao.getTPOSendDoInfByCwbAndOpertype(orderFlow.getCwb(), TPOOperateTypeEnum.ADD);
				if(history != null && StringUtils.isNotEmpty(history.getCwb())){
					this.logger.info("外单cwb={}已存在外单推DO接口表（TPO_SEND_DO_INF）中", orderFlow.getCwb());
					return;
				}
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
		
		String deliveryUsername="";
		User deliveryUser=null;
		if(cwbOrder.getDeliverid() > 0){
			deliveryUser=getDmpDAO.getUserById(cwbOrder.getDeliverid());
			if(deliveryUser!=null&&deliveryUser.getUsername()!=null){
				deliveryUsername=deliveryUser.getUsername().toUpperCase();
			}
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
			thirdPartyOrder2DORequestVo.setPayment(String.valueOf(0));
		}
		else if(paywayid == PaytypeEnum.Pos.getValue()){
			thirdPartyOrder2DORequestVo.setActualPayType(1);
			thirdPartyOrder2DORequestVo.setPayment(String.valueOf(1));
		}
		else if(paywayid == PaytypeEnum.CodPos.getValue()){
			thirdPartyOrder2DORequestVo.setActualPayType(2);
			thirdPartyOrder2DORequestVo.setPayment(String.valueOf(2));
		}
		else if(paywayid == PaytypeEnum.Zhipiao.getValue()){
			thirdPartyOrder2DORequestVo.setActualPayType(13);
			thirdPartyOrder2DORequestVo.setPayment(String.valueOf(13));
		}
		else if(paywayid == PaytypeEnum.Qita.getValue()){
			thirdPartyOrder2DORequestVo.setActualPayType(14);
			thirdPartyOrder2DORequestVo.setPayment(String.valueOf(14));
		}
		else{
			thirdPartyOrder2DORequestVo.setActualPayType(null);
			thirdPartyOrder2DORequestVo.setPayment("");
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
		thirdPartyOrder2DORequestVo.setCneeMobile(StringUtils.isBlank(cwbOrder.getConsigneemobile()) ? "**" : cwbOrder.getConsigneemobile());
		thirdPartyOrder2DORequestVo.setCneeName(StringUtils.isBlank(cwbOrder.getConsigneename()) ? "**" : cwbOrder.getConsigneename());
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
		thirdPartyOrder2DORequestVo.setCneeTel(StringUtils.isBlank(cwbOrder.getConsigneephone()) ? "**" : cwbOrder.getConsigneephone());
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
        	 //如果非代收货款，支付方式设为空
        	 thirdPartyOrder2DORequestVo.setActualPayType(null);
 			 thirdPartyOrder2DORequestVo.setPayment("");
         }
		thirdPartyOrder2DORequestVo.setCustCode(StringUtil.nullConvertToEmptyString(customer.getCustomercode()));
		thirdPartyOrder2DORequestVo.setCustName(StringUtil.nullConvertToEmptyString(customer.getCustomername()));
		thirdPartyOrder2DORequestVo.setCustOrderNo(cwbOrder.getCwb());
		//thirdPartyOrder2DORequestVo.setDestOrg(cwbOrder.getDeliverybranchid() == 0  ? null : cwbOrder.getDeliverybranchid() + "");
		thirdPartyOrder2DORequestVo.setDestOrg(destOrg);//由原来的传机构id改为传编码 。zhouguoting 2016/02/25
		thirdPartyOrder2DORequestVo.setDistributer(deliveryUsername);
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
		thirdPartyOrder2DORequestVo.setOperateType(new Integer(TPOOperateTypeEnum.ADD.getValue()));
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
				//Added by leoliao at 2016-06-08 解决箱号为空的问题
				if(boxNo == null || boxNo.trim().equals("")){
					continue;
				}
				//Added end
				
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
	public void updateTPOSendDoInf(long id, String transportNo,int isSent,int trytime, String remark){
		this.tPOSendDoInfDao.updateTPOSendDoInf(id, transportNo, isSent, trytime, remark);
	}
	
	
	public String genExpThirdPartyInfRec(String cwbs,String acceptDept){
		List<Branch> branches = this.getDmpDAO.getAllBranchs();
		boolean isRightAcceptDept = false;
		if(branches == null){
			return "无法获取dmp机构信息";
		}
		for(Branch branch : branches){
			if(branch.getTpsbranchcode() != null && branch.getTpsbranchcode().equals(acceptDept)){
				isRightAcceptDept = true;
				break;
			}
		}
		if(!isRightAcceptDept){
			return "非法的接单机构tpsbranchcode";
		}
		String[] cwbArray = cwbs.split(",|，|\r|\n");
		if(cwbArray.length > 3000){
			return "建议一次刷数不要操作3000单";
		}
		StringBuilder sbd = new StringBuilder();
		for(String cwbStr : cwbArray){
			sbd.append("'"+cwbStr + "',");
		}
		List<CwbOrder> cwbOrders = this.cwbDAO.getCwbOrderByCwb(sbd.deleteCharAt(sbd.length() -1).toString());
		StringBuilder returnCwbs = new StringBuilder();
		if(CollectionUtils.isEmpty(cwbOrders)){
			return cwbs;
		}
		for(CwbOrder cwbOrder : cwbOrders){
			try{
				String result = this.insert2_TPO_SEND_DO_INF(cwbOrder,acceptDept);
				if(StringUtils.isNotBlank(result)){
					returnCwbs.append(result + "\n");
				}
			}catch(Exception e){
				e.printStackTrace();
				returnCwbs.append(cwbOrder.getCwb() + "\n");
			}
		}
		return returnCwbs.toString();
		
	}
	
	
	/**
	 * 把外单（第三方订单）数据 插入到 TPO_SEND_DO_INF表，待定时器推送给TPS 的DO服务
	 * @param cwbOrder
	 * @param acceptDept
	 * @return
	 */
	public String insert2_TPO_SEND_DO_INF(CwbOrder cwbOrder,String acceptDept){
		//外单数据 在 接单（导入数据环节）的时候就要推送给TPS 的DO服务
			long customerid = cwbOrder.getCustomerid();
			Customer customer = this.cacheBaseListener.getCustomer(customerid);
			if (customer == null) {
				this.logger.info("Customer对象在缓存中没有获取到，请求dmp..cwb={},customerid={}", cwbOrder.getCwb(),customerid);
				customer = this.getDmpDAO.getCustomer(customerid);
			}
				
			//查询出唯速达所设置的客户
			ThirdPartyOrder2DOCfg pushCfg = this.getThirdPartyOrder2DOCfg();
			if(pushCfg == null || pushCfg.getOpenFlag() == 0){
				this.logger.info("未获取到外单推送DO服务对接配置信息，外单cwb={}", cwbOrder.getCwb());
				return cwbOrder.getCwb();
			}
			//是否是外单客户
			boolean isTPCust  = this.isThirdPartyCustomer(customerid);
			if(!isTPCust){
				return "非外单客户订单："+cwbOrder.getCwb();
			}
			TPOSendDoInf history = this.tPOSendDoInfDao.getTPOSendDoInfByCwbAndOpertype(cwbOrder.getCwb(), TPOOperateTypeEnum.ADD);
			if(history != null && StringUtils.isNotEmpty(history.getCwb())){
				return cwbOrder.getCwb();
			}
			this.logger.info("外单cwb={}加入外单推DO接口表（TPO_SEND_DO_INF）", cwbOrder.getCwb());
			ThirdPartyOrder2DORequestVo  thirdPartyOrder2DORequestVo = this.buildThirdPartyOrder2DORequestVo(cwbOrder, customer, pushCfg,acceptDept);
			TPOSendDoInf tPOSendDoInf = this.buildTPOSendDoInf(cwbOrder,customer);
			try {
				String reqObjJson =  JsonUtil.translateToJson(thirdPartyOrder2DORequestVo);
				tPOSendDoInf.setReqObjJson(reqObjJson);
			} catch (Exception e) {
				logger.error("请求参数对象转JSON异常,cwb="+tPOSendDoInf.getCwb(), e); 
			}
			
			this.tPOSendDoInfDao.saveTPOSendDoInf(tPOSendDoInf);
									
			return null;
		
	}

	
	
	
	/**
	 * 根据cwbOrder构建TPOSendDoInf对象
	 * @param cwbOrder
	 * @return
	 */
	private TPOSendDoInf buildTPOSendDoInf(CwbOrder cwbOrder,Customer customer){
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
	private ThirdPartyOrder2DORequestVo  buildThirdPartyOrder2DORequestVo(CwbOrder cwbOrder,Customer customer,ThirdPartyOrder2DOCfg pushCfg,String acceptDept){
		ThirdPartyOrder2DORequestVo thirdPartyOrder2DORequestVo = new ThirdPartyOrder2DORequestVo();
		int cwbordertypeid = Integer.parseInt(cwbOrder.getCwbordertypeid());
		
		String posString = this.getDmpDAO.getNowCustomerPos(customer.getCustomerid());
		String destOrg = null; //目的机构（派送站点）
		if(cwbOrder.getDeliverybranchid() > 0){
			Branch branch = this.cacheBaseListener.getBranch(cwbOrder.getDeliverybranchid());
			if (branch == null) {
				this.logger.info("Branch对象在缓存中没有获取到，OMS外单业务请求dmp..cwb={},deliverybranchid={}", cwbOrder.getCwb(),cwbOrder.getDeliverybranchid());
				branch = this.getDmpDAO.getNowBranch(cwbOrder.getDeliverybranchid());
			}
			destOrg = branch.getTpsbranchcode();
		}
		
		String deliveryUsername="";
		User deliveryUser=null;
		if(cwbOrder.getDeliverid() > 0){
			deliveryUser=getDmpDAO.getUserById(cwbOrder.getDeliverid());
			if(deliveryUser!=null&&deliveryUser.getUsername()!=null){
				deliveryUsername=deliveryUser.getUsername().toUpperCase();
			}
		}
		
		thirdPartyOrder2DORequestVo.setAcceptDept(acceptDept == null ? "" : acceptDept);//由原来的id改为编码 。zhouguoting 2016/02/25
		//thirdPartyOrder2DORequestVo.setAcceptDept(cwbOrder.getNextbranchid() == 0 ? "" : cwbOrder.getNextbranchid() + "");
		thirdPartyOrder2DORequestVo.setAcceptOperator("");
		thirdPartyOrder2DORequestVo.setAccountMark(posString);
		thirdPartyOrder2DORequestVo.setActualFee(cwbOrder.getInfactfare());
		int paywayid = StringUtils.isNotEmpty(cwbOrder.getPaytype()) ? Integer.valueOf(cwbOrder.getPaytype()) : PaytypeEnum.Qita.getValue() ;
		if(paywayid == PaytypeEnum.Xianjin.getValue()){
			thirdPartyOrder2DORequestVo.setActualPayType(0);
			thirdPartyOrder2DORequestVo.setPayment(String.valueOf(0));
		}
		else if(paywayid == PaytypeEnum.Pos.getValue()){
			thirdPartyOrder2DORequestVo.setActualPayType(1);
			thirdPartyOrder2DORequestVo.setPayment(String.valueOf(1));
		}
		else if(paywayid == PaytypeEnum.CodPos.getValue()){
			thirdPartyOrder2DORequestVo.setActualPayType(2);
			thirdPartyOrder2DORequestVo.setPayment(String.valueOf(2));
		}
		else if(paywayid == PaytypeEnum.Zhipiao.getValue()){
			thirdPartyOrder2DORequestVo.setActualPayType(13);
			thirdPartyOrder2DORequestVo.setPayment(String.valueOf(13));
		}
		else if(paywayid == PaytypeEnum.Qita.getValue()){
			thirdPartyOrder2DORequestVo.setActualPayType(14);
			thirdPartyOrder2DORequestVo.setPayment(String.valueOf(14));
		}
		else{
			thirdPartyOrder2DORequestVo.setActualPayType(null);
			thirdPartyOrder2DORequestVo.setPayment("");
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
		thirdPartyOrder2DORequestVo.setCneeMobile(StringUtils.isBlank(cwbOrder.getConsigneemobile()) ? "**" : cwbOrder.getConsigneemobile());
		thirdPartyOrder2DORequestVo.setCneeName(StringUtils.isBlank(cwbOrder.getConsigneename()) ? "**" : cwbOrder.getConsigneename());
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
		thirdPartyOrder2DORequestVo.setCneeRemark("");
		thirdPartyOrder2DORequestVo.setCneeTel(StringUtils.isBlank(cwbOrder.getConsigneephone()) ? "**" : cwbOrder.getConsigneephone());
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
        	 //如果非代收货款，支付方式设为空
        	 thirdPartyOrder2DORequestVo.setActualPayType(null);
 			 thirdPartyOrder2DORequestVo.setPayment("");
         }
		thirdPartyOrder2DORequestVo.setCustCode(StringUtil.nullConvertToEmptyString(customer.getCustomercode()));
		thirdPartyOrder2DORequestVo.setCustName(StringUtil.nullConvertToEmptyString(customer.getCustomername()));
		thirdPartyOrder2DORequestVo.setCustOrderNo(cwbOrder.getCwb());
		//thirdPartyOrder2DORequestVo.setDestOrg(cwbOrder.getDeliverybranchid() == 0  ? null : cwbOrder.getDeliverybranchid() + "");
		thirdPartyOrder2DORequestVo.setDestOrg(destOrg);//由原来的传机构id改为传编码 。zhouguoting 2016/02/25
		thirdPartyOrder2DORequestVo.setDistributer(deliveryUsername);
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
		thirdPartyOrder2DORequestVo.setOperateType(new Integer(TPOOperateTypeEnum.ADD.getValue()));
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
	 * 根据订单号插入一条取消类型的 外单接口表记录
	 * @param cwb
	 */
	public void addCancelTypeTPOSendDoInf(String cwb){
		TPOSendDoInf tPOSendDoInf = this.tPOSendDoInfDao.getTPOSendDoInfByCwbAndOpertype(cwb, TPOOperateTypeEnum.ADD);
		
		if(tPOSendDoInf == null || StringUtils.isEmpty(tPOSendDoInf.getCwb())){
			this.logger.info("未在外单接口表中找到 cwb={}的记录，认为不是外单，订单失效操作不进外单接口表",cwb);
			return;
		}
			
		tPOSendDoInf.setOperateType(TPOOperateTypeEnum.CANCEL.getValue());
		tPOSendDoInf.setIsSent(0);
		tPOSendDoInf.setTrytime(0);
		ThirdPartyOrder2DORequestVo requestVo  = null;
		try {
			requestVo = JsonUtil.readValue(tPOSendDoInf.getReqObjJson(),ThirdPartyOrder2DORequestVo.class);
			requestVo.setOperateType(new Integer(TPOOperateTypeEnum.CANCEL.getValue()));
			requestVo.setTransportNo(tPOSendDoInf.getTransportno() == null ? "" : tPOSendDoInf.getTransportno());
			String reqObjJson =  JsonUtil.translateToJson(requestVo);
			tPOSendDoInf.setReqObjJson(reqObjJson);
		} catch (Exception e) {
			this.logger.error("取消操作类型外单推送DO请求参数json串，对象互转失败，cwb="+tPOSendDoInf.getCwb(), e);
			return;
		}
		this.tPOSendDoInfDao.saveTPOSendDoInf(tPOSendDoInf);
		//把操作类型是新增的那条记录 的state设为0
		this.tPOSendDoInfDao.invalidateTPOSendDoInfById(tPOSendDoInf.getId());
	}
	
	/**
	 * 根据订单修改信息 插入一条修改类型的 外单接口表记录
	 * @param editInfo
	 */
	public void addUpdateTypeTPOSendDoInf(ThirdPartyOrderEditInfo editInfo){
		TPOSendDoInf tPOSendDoInf = this.tPOSendDoInfDao.getTPOSendDoInfByCwbAndOpertype(editInfo.getCwb(), TPOOperateTypeEnum.ADD);
		if(tPOSendDoInf == null || StringUtils.isEmpty(tPOSendDoInf.getCwb())){
			this.logger.info("未在外单接口表中找到 cwb={}的记录，认为不是外单，订单修改操作不进外单接口表",editInfo.getCwb());
			return;
		}
		
		tPOSendDoInf.setOperateType(TPOOperateTypeEnum.UPDATE.getValue());
		tPOSendDoInf.setIsSent(0);
		tPOSendDoInf.setTrytime(0);
		ThirdPartyOrder2DORequestVo requestVo  = null;
		try {
			requestVo = JsonUtil.readValue(tPOSendDoInf.getReqObjJson(),ThirdPartyOrder2DORequestVo.class);
			requestVo.setOperateType(new Integer(TPOOperateTypeEnum.UPDATE.getValue()));
			requestVo.setTransportNo(tPOSendDoInf.getTransportno() == null ? "" : tPOSendDoInf.getTransportno());
		} catch (Exception e) {
			this.logger.error("修改操作类型外单推送DO请求参数json串转对象失败，cwb="+tPOSendDoInf.getCwb(), e);
			return;
		}
		
		//修改类型：2.修改金额3.修改支付方式4.修改订单类型5.修改订单信息（收件人信息，配送站点）
		int editType = editInfo.getEditType();
		switch (editType) {
			case 2://修改金额
				requestVo.setCodAmount(editInfo.getReceivablefee());
				requestVo.setReturnAmount(editInfo.getPaybackfee());
				if(editInfo.getReceivablefee().intValue() > 0){
					 requestVo.setIsCod(1);//是否货到付款
		        }else{
		        	 requestVo.setIsCod(0);
		        	 //如果非代收货款，支付方式设为空
		        	 requestVo.setActualPayType(null);
		        	 requestVo.setPayment("");
		        }
				break;
				
			case 3: //修改支付方式
				int paywayid = editInfo.getNewpaywayid();
				if(requestVo.getIsCod() == 0){
					//非代收货款的，对于修改支付类型，不需要做任何事情
				}
				else if(paywayid == PaytypeEnum.Xianjin.getValue()){
					requestVo.setActualPayType(0);
					requestVo.setPayment(String.valueOf(0));
				}
				else if(paywayid == PaytypeEnum.Pos.getValue()){
					requestVo.setActualPayType(1);
					requestVo.setPayment(String.valueOf(1));
				}
				else if(paywayid == PaytypeEnum.CodPos.getValue()){
					requestVo.setActualPayType(2);
					requestVo.setPayment(String.valueOf(2));
				}
				else if(paywayid == PaytypeEnum.Zhipiao.getValue()){
					requestVo.setActualPayType(13);
					requestVo.setPayment(String.valueOf(13));
				}
				else if(paywayid == PaytypeEnum.Qita.getValue()){
					requestVo.setActualPayType(14);
					requestVo.setPayment(String.valueOf(14));
				}
				else{
					requestVo.setActualPayType(null);
					requestVo.setPayment("");
				}
				break;
				
			case 4://修改订单类型		
				int cwbordertypeid = editInfo.getCwbordertypeid();	
				if(cwbordertypeid == CwbOrderTypeIdEnum.Express.getValue()){
					requestVo.setOrderType(1);
				}else if(cwbordertypeid == CwbOrderTypeIdEnum.Peisong.getValue()){
					requestVo.setOrderType(2);
				} else if(cwbordertypeid == CwbOrderTypeIdEnum.Shangmenhuan.getValue()){
					requestVo.setOrderType(4);
				} else if(cwbordertypeid  == CwbOrderTypeIdEnum.Shangmentui.getValue()){
					requestVo.setOrderType(3);
				}
				break;
				
			case 5://修改订单信息
				requestVo.setCneeMobile(StringUtils.isBlank(editInfo.getConsigneemobile()) ? "**" : editInfo.getConsigneemobile());
				requestVo.setCneeName(StringUtils.isBlank(editInfo.getConsigneename()) ? "**" : editInfo.getConsigneename());
				requestVo.setCneeAddr(StringUtil.nullConvertToEmptyString(editInfo.getConsigneeaddress()));
			
				if(editInfo.getBranchid() > 0){
					Branch branch = this.cacheBaseListener.getBranch(editInfo.getBranchid());
					if (branch == null) {
						this.logger.info("Branch对象在缓存中没有获取到，OMS外单业务请求dmp..cwb={},deliverybranchid={}", editInfo.getCwb(),editInfo.getBranchid());
						branch = this.getDmpDAO.getNowBranch(editInfo.getBranchid());
					}
					requestVo.setDestOrg(branch.getTpsbranchcode());
				}
				//0：送货时间不限1：只工作日(双休日/节假日不用送)2：只双休日/节假日送货(工作日不用送)
				if(editInfo.getCustomercommand() != null){
					if(editInfo.getCustomercommand().contains("只工作日")){
						requestVo.setCneePeriod(1);
					}else if(editInfo.getCustomercommand().contains("工作日不用送")){
						requestVo.setCneePeriod(2);
					}else{
						requestVo.setCneePeriod(0);
					}
				}else{
					requestVo.setCneePeriod(0);
				}
				break;
				
			default:
				break;
		}
	
		try {
			String reqObjJson =  JsonUtil.translateToJson(requestVo);
			tPOSendDoInf.setReqObjJson(reqObjJson);
		} catch (Exception e) {
			this.logger.error("修改操作类型外单推送DO请求对象转json串失败，cwb="+tPOSendDoInf.getCwb(), e);
			return;
		}
		this.tPOSendDoInfDao.saveTPOSendDoInf(tPOSendDoInf);
	}
	

}
