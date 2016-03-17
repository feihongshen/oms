package cn.explink.b2c.weisuda;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.explink.xmldto.OrderFlowDto;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.CacheBaseListener;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.b2c.tpsdo.TPOSendDoInfService;
import cn.explink.b2c.tpsdo.bean.ThirdPartyOrder2DOCfg;
import cn.explink.b2c.weisuda.xml.GetUnVerifyOrders_back_Item;
import cn.explink.b2c.weisuda.xml.Getback_Item;
import cn.explink.b2c.weisuda.xml.Goods;
import cn.explink.b2c.weisuda.xml.Item;
import cn.explink.b2c.weisuda.xml.ObjectUnMarchal;
import cn.explink.b2c.weisuda.xml.Order;
import cn.explink.b2c.weisuda.xml.OrderGoods;
import cn.explink.b2c.weisuda.xml.RootOrder;
import cn.explink.b2c.weisuda.xml.RootPS;
import cn.explink.b2c.weisuda.xml.RootSMT;
import cn.explink.dao.BranchDAO;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.WeisudaDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.BranchWithOld;
import cn.explink.domain.Customer;
import cn.explink.domain.SystemInstall;
import cn.explink.domain.User;
import cn.explink.enumutil.BranchEnum;
import cn.explink.enumutil.CwbOrderTypeIdEnum;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.enumutil.PaytypeEnum;
import cn.explink.enumutil.pos.PosEnum;
import cn.explink.jms.dto.CwbOrderWithDeliveryState;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;
import cn.explink.util.JsonUtil;
import cn.explink.util.RestHttpServiceHanlder;
import cn.explink.util.MD5.MD5Util;

@Service
public class WeisudaService {
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	GetDmpDAO getDmpDAO;
	@Autowired
	WeisudaDAO weisudaDAO;
	@Autowired
	private CamelContext camelContext;
	@Autowired
	private BranchDAO branchDAO;
	@Autowired
	CacheBaseListener cacheBaseListener;
	@Autowired
	B2cTools b2cTools2;
	@Autowired
	WeiSuDaWaiDanService weiSuDaWaiDanService;
	@Autowired
	TPOSendDoInfService tPOSendDoInfService;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@PostConstruct
	public void init() {
		try {
			this.camelContext.addRoutes(new RouteBuilder() {
				@Override
				public void configure() throws Exception {

					this.from("jms:queue:VirtualTopicConsumers.omsToWeisudaSyn.savezhandian?concurrentConsumers=1").to("bean:weisudaService?method=siteUpdate").routeId("weisuda_更新站点");
					this.from("jms:queue:VirtualTopicConsumers.omsToWeisudaSyn.delzhandian?concurrentConsumers=5").to("bean:weisudaService?method=siteDel").routeId("weisuda_撤销站点");
					this.from("jms:queue:VirtualTopicConsumers.omsToWeisudaSyn.courierUpdate?concurrentConsumers=1").to("bean:weisudaService?method=courierUpdate").routeId("weisuda_更新快递员");
					this.from("jms:queue:VirtualTopicConsumers.omsToWeisudaSyn.carrierDel?concurrentConsumers=5").to("bean:weisudaService?method=carrierDel").routeId("weisuda_删除快递员");
				}
			});
		} catch (Exception e) {
			this.logger.error("唯速达_camel context start fail", e);
		}
	}

	
	
	/**
	 * 插入唯速达订单数据
	 *
	 * @param cwbOrderWithDeliveryState
	 * @param orderFlow
	 */
	public void insertWeisuda(CwbOrderWithDeliveryState cwbOrderWithDeliveryState, DmpOrderFlow orderFlow)  {
		this.logger.info("唯速达_01进入唯速达对接cwb={}", orderFlow.getCwb());
		DmpCwbOrder cwbOrder = cwbOrderWithDeliveryState.getCwbOrder();
		int cwbordertypeid = Integer.parseInt(cwbOrder.getCwbordertypeid());
		if ((cwbordertypeid == CwbOrderTypeIdEnum.Peisong.getValue()) || (cwbordertypeid == CwbOrderTypeIdEnum.Shangmentui.getValue())|| (cwbordertypeid == CwbOrderTypeIdEnum.OXO.getValue())) {
			long customerid = cwbOrderWithDeliveryState.getCwbOrder().getCustomerid();
			
			Customer customer = this.cacheBaseListener.getCustomer(customerid);
			if (customer == null) {
				this.logger.info("Customer对象在缓存中没有获取到，唯速达请求dmp..cwb={}", orderFlow.getCwb());
				customer = this.getDmpDAO.getCustomer(customerid);
			}
			
			if (customer.getB2cEnum().equals(this.getB2cEnumKeys(customer, "vipshop"))) {
				this.weisudaDAO.deleteWeisudaCwbNotuisong(orderFlow.getCwb(), "0");
				String orderTime = DateTimeUtil.formatDate(orderFlow.getCredate());
				User deliverUser = this.getDmpDAO.getUserById(cwbOrder.getDeliverid());
				WeisudaCwb weisudaCwbold = this.weisudaDAO.getWeisudaCwb(orderFlow.getCwb(), orderTime,0);
				if (weisudaCwbold == null) {
					WeisudaCwb weisudaCwb = new WeisudaCwb();
					weisudaCwb.setCwb(orderFlow.getCwb());
					weisudaCwb.setCwbordertypeid(cwbordertypeid);
					weisudaCwb.setCourier_code(deliverUser.getUsername());
					weisudaCwb.setOperationTime(orderTime);
					this.weisudaDAO.insertWeisuda(weisudaCwb,0);
					this.logger.info("唯速达_01获取唯速达数据插入成功cwb={}", weisudaCwb.getCwb());
				}
				return;
			} 
				
			/**
			 * 旧外单接口逻辑已废弃 deleted by zhouguoting 2016/03/15
			 */
			//查询出唯速达所设置的客户
			/*Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
			if(weisuda.getIsSend() == 0){
				return;
			}
			
			boolean filterCustomerflag = filterWandanCustomerId(customerid, weisuda);
			
			if(filterCustomerflag){
				//this.weiSuDaWaiDanService.sendCwb(cwbOrder,weisuda,customer);
				this.weiSuDaWaiDanService.saveWeiSuDa(cwbOrder,weisuda,customer);
			} else {
				this.logger.info("唯速达_01未设置对接，customername={},cwb={}", customer.getCustomername(), orderFlow.getCwb());
			}*/
			
			/**
			 * 修复外单无法同步品骏达签收信息给DMP问题
			 */
			ThirdPartyOrder2DOCfg pushCfg = tPOSendDoInfService.getThirdPartyOrder2DOCfg();
			if(pushCfg == null || pushCfg.getOpenFlag() != 1){
				logger.info("未配置外单推送DO服务配置信息!无法保存外单数据到express_b2cdata_weisuda表");
				return;
			}
			boolean filterCustomerflag = tPOSendDoInfService.isThirdPartyCustomer(customerid);
			if(filterCustomerflag){
				this.weisudaDAO.deleteWeisudaCwbNotuisong(orderFlow.getCwb(), "0");
				String orderTime = DateTimeUtil.formatDate(orderFlow.getCredate());
				User deliverUser = this.getDmpDAO.getUserById(cwbOrder.getDeliverid());
				WeisudaCwb weisudaCwbold = this.weisudaDAO.getWeisudaCwb(orderFlow.getCwb(), orderTime, 1);
				if (weisudaCwbold == null) {
					WeisudaCwb weisudaCwb = new WeisudaCwb();
					weisudaCwb.setCwb(orderFlow.getCwb());
					weisudaCwb.setCwbordertypeid(cwbordertypeid);
					weisudaCwb.setCourier_code(deliverUser.getUsername());
					weisudaCwb.setOperationTime(orderTime);
					this.weisudaDAO.insertWeisuda(weisudaCwb,1);
					this.logger.info("唯速达_01获取唯速达数据插入成功cwb={}", weisudaCwb.getCwb());
				}
			} else {
				this.logger.info("外单客户未设置对接，customername={},cwb={}", customer.getCustomername(), orderFlow.getCwb());
			}
			
		} else {
			this.logger.info("唯速达_01不是所需要的订单类型，cwb={}", orderFlow.getCwb());
		}
	}



	private boolean filterWandanCustomerId(long customerid, Weisuda weisuda) {
		String customerids = weisuda.getCustomers();
		if(customerids==null||customerids.isEmpty()){
			return false;
		}
		String[] ids = customerids.split(",|，");
		boolean flag = false;
		//查看当前订单的客户是不是唯速达的外单客户
		for(int i=0; i<ids.length;i++){
			if(customerid == Long.parseLong(ids[i])){
				flag = true;
			}
		}
	
		return flag;
	}

	/**
	 * 订单与快递员绑定关系同步接口
	 */

	public void boundDeliveryToApp() {
		if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			this.logger.info("唯速达_01未开启[唯速达]接口");
			return;
		}
		
		Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
		if(weisuda.getOpenbatchflag()==1){ //开启批量之后退出
			return;
		}
		
		try {
			//Added by leoliao at 2016-03-08 改为一次获取需要发送的订单，然后分批发送。
			int maxBounds = weisuda.getMaxBoundCount()==0?100:weisuda.getMaxBoundCount();
			int cntLoop   = 10;
			
			List<WeisudaCwb> weisudaCwbs = this.weisudaDAO.getWeisudaCwb("0", 0, (cntLoop * maxBounds));
			if ((weisudaCwbs == null) || (weisudaCwbs.size() == 0)) {
				this.logger.info("唯速达_01当前没有要推送0唯速达0的数据");
				return;
			}
			
			int total = weisudaCwbs.size();
			int k     = 1;
			int batch = maxBounds; //每次发送数量
			while(true){
				int fromIdx = (k - 1) * batch;
				if (fromIdx >= total) {
					break;
				}
				
				int toIdx = k * batch;
				if (toIdx > total) {
					toIdx = total;
				}
				
				List<WeisudaCwb> subList = weisudaCwbs.subList(fromIdx, toIdx);
				this.DealWithBuildXMLAndSending(subList, weisuda);
				
				k++;
			}
			//Added end
			
			/**Commented by leoliao at 2016-03-08
			int i = 0;
			while (true) {
				List<WeisudaCwb> weisudaCwbs = this.weisudaDAO.getWeisudaCwb("0",0);
				i++;
				if (i > 100) {
					String warning = "查询0唯速达0状态反馈已经超过100次循环，可能存在程序未知异常,请及时查询并处理!";
					this.logger.warn(warning);
					return;
				}

				if ((weisudaCwbs == null) || (weisudaCwbs.size() == 0)) {
					this.logger.info("唯速达_01当前没有要推送0唯速达0的数据");
					return;
				}

				this.DealWithBuildXMLAndSending(weisudaCwbs, weisuda);

			}
			*/
		} catch (Exception e) {
			String errorinfo = "唯速达_01发送0唯速达0状态反馈遇到不可预知的异常";
			this.logger.error(errorinfo, e);
		}

	}

	/**
	 * APP包裹签收信息同步接口
	 */
	public void getUnVerifyOrders() {
		if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			this.logger.info("唯速达_02未开启[唯速达]接口");
			return;
		}
		Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
		String response = this.check(weisuda, "nums", weisuda.getNums(), WeisudsInterfaceEnum.getUnVerifyOrders.getValue());
		this.logger.info("唯速达_02_APP包裹签收信息同步接口下载数据,{}", response);
		if(response==null){
			this.logger.info("唯速达_02_APP包裹签收信息同步接口下载数据为空");
			return;
		}
		
		if (response.contains("<error><code>")) {
			return;
		}
		if (response.contains("<root>") && response.contains("<courier_code>")) {
			try {
				RootPS back_Root = (RootPS) ObjectUnMarchal.XmltoPOJO(response, new RootPS());

				for (GetUnVerifyOrders_back_Item item : back_Root.getItem()) {
					String cwb=item.getOrder_id();
					WeisudaCwb weisudaCwbs = this.weisudaDAO.getWeisudaCwbIstuisong(cwb);
					if (weisudaCwbs == null) {
						String json = this.buliderJson(item, cwb);
						this.logger.info("唯速达_02请求dmp-json={}", json);
						String result = this.getDmpDAO.requestDMPOrderService_Weisuda(json);
						this.logger.info("品骏达外单签收结果={}", result);
						this.updateUnVerifyOrders(item.getOrder_id());
						continue;
					}
					String json = this.buliderJson(item, cwb);
					this.logger.info("唯速达_02请求dmp-json={}", json);
					String result = this.getDmpDAO.requestDMPOrderService_Weisuda(json);

					updateWeisudaDeliveryState(item, result);
				}
			}

			catch (Exception e) {
				this.logger.error("唯速达_02请求dmp唯速达信息异常" + response, e);
			}
		}

		else {
			this.logger.info("唯速达_02返回订单失败！{}", response);
		}
	}



	private void updateWeisudaDeliveryState(GetUnVerifyOrders_back_Item item, String result) {
		if ("SUCCESS".equals(result)) {
			this.weisudaDAO.updataWeisudaCwbIsqianshou(item.getOrder_id(), "1", "包裹信息通过_手机_签收成功");
			this.updateUnVerifyOrders(item.getOrder_id());
		} else if (result.contains("处理唯速达反馈请求异") && result.contains("不允许进行反馈")) {
			this.weisudaDAO.updataWeisudaCwbIsqianshou(item.getOrder_id(), "1", "包裹信息已经通过_其它方式_签收成功");
			this.updateUnVerifyOrders(item.getOrder_id());
		}else {
			this.logger.info("唯速达_02请求dmp唯速达信息异常{},cwb={}", result, item.getOrder_id());
			this.weisudaDAO.updataWeisudaCwbIsqianshou(item.getOrder_id(), "2", result);
			this.updateUnVerifyOrders(item.getOrder_id());
			return;
		}
	}

	/**
	 * APP包裹签收信息同步结果反馈接口
	 *
	 * @param 订单号码
	 */
	private void updateUnVerifyOrders(String orderid) {
		Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
		String send = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<root>" + "<item>" + "<order_id>" + orderid + "</order_id>" + "</item>" + "</root>";
		this.logger.info("唯速达_03APP包裹签收信息同步结果 发送报文,userMessage={}", send);
		String response = this.check(weisuda, "data", send, WeisudsInterfaceEnum.updateUnVerifyOrders.getValue());
		this.logger.info("唯速达_03APP包裹签收信息同步结果反馈接口返回报文,response={}", response);
		
		
	}

	/**
	 * 包裹签收信息修改通知接口
	 */
	public void updateOrders(CwbOrderWithDeliveryState cwbOrderWithDeliveryState, DmpOrderFlow orderFlow) {
		if (CwbOrderTypeIdEnum.Peisong.getValue() == Integer.parseInt(cwbOrderWithDeliveryState.getCwbOrder().getCwbordertypeid())) {
			if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
				this.logger.info("唯速达_04未开启[唯速达]接口");
				return;
			}
			Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
			this.logger.info("唯速达_04进入唯速达对接cwb={}", orderFlow.getCwb());
			long customerid = cwbOrderWithDeliveryState.getCwbOrder().getCustomerid();
			Customer customer = this.cacheBaseListener.getCustomer(customerid);
			if (customer == null) {
				this.logger.info("Customer对象在缓存中没有获取到，唯速达请求dmp..cwb={}", orderFlow.getCwb());
				customer = this.getDmpDAO.getCustomer(customerid);
			}
			String cwb = cwbOrderWithDeliveryState.getCwbOrder().getCwb();
			//boolean filterCustomerflag = filterWandanCustomerId(customerid, weisuda);
			if (customer.getB2cEnum().equals(this.getB2cEnumKeys(customer, "vipshop"))) {
				updateOrdersMethod(cwbOrderWithDeliveryState, weisuda, cwb);
				return; //如果是唯品会订单，签收信息修改通知品骏达后就不需要执行后面的代码了 added by zhouguoting 2016/03/16
			} else {
				this.logger.info("唯速达_04未设置对接，customername={},cwb={}", customer.getCustomername(), orderFlow.getCwb());
			}
			/**
			 * 品骏达外单签收通知  2016/03/16
			 */
			ThirdPartyOrder2DOCfg pushCfg = tPOSendDoInfService.getThirdPartyOrder2DOCfg();
			if(pushCfg == null || pushCfg.getOpenFlag() != 1){
				logger.info("唯速达_04未配置外单推送DO服务配置信息!无法通知品骏达修改签收信息，customername={},cwb={}",customer.getCustomername(), orderFlow.getCwb());
				return;
			}
			boolean filterCustomerflag = tPOSendDoInfService.isThirdPartyCustomer(customerid);
			if(filterCustomerflag){
				updateOrdersMethod(cwbOrderWithDeliveryState, weisuda, cwb);
			}else{
				this.logger.info("唯速达_04当前客户未设置为外单客户，customername={},cwb={}", customer.getCustomername(), orderFlow.getCwb());
			}
			/*//品骏达外单签收通知  deleted by zhouguoting
			if(weisuda.getIsSend() == 1 ){
				if(filterCustomerflag){
					updateOrdersMethod(cwbOrderWithDeliveryState, weisuda, cwb);
				}else {
					this.logger.info("唯速达_04当前客户未设置为外单客户，customername={},cwb={}", customer.getCustomername(), orderFlow.getCwb());
				}
			}else{
				this.logger.info("唯速达_04未开启外单对接，customername={},cwb={}", customer.getCustomername(), orderFlow.getCwb());
			}*/
		} else {
			this.logger.info("唯速达_04不是所需要的订单类型，cwb={}", orderFlow.getCwb());
		}
	}



	private void updateOrdersMethod(CwbOrderWithDeliveryState cwbOrderWithDeliveryState, Weisuda weisuda, String cwb) {
		try {
			WeisudaCwb weisudaCwb = this.weisudaDAO.getWeisudaCwbByOrder(cwb);
			if (weisudaCwb != null) {
				if (weisudaCwb.getIsqianshou().equals("0")) {
					DmpCwbOrder cwbOrder = cwbOrderWithDeliveryState.getCwbOrder();
					DmpDeliveryState deliveryState = cwbOrderWithDeliveryState.getDeliveryState();
					String order_id = cwbOrder.getCwb();
					String order_status = "";
					String pay_status = "1";
					String consignee = "";
					Long deliverytime = DateTimeUtil.StringToDate(deliveryState.getDeliverytime()).getTime();
					String opertime = "";
					opertime = (deliverytime == null ? "" : (deliverytime / 1000) + "");
					String reason = "";
					String delay_reason = "";
					String memo = "";
					String paymethod = "2";
					if (deliveryState.getDeliverystate() == DeliveryStateEnum.FenZhanZhiLiu.getValue()) {
						order_status = "4";
						delay_reason = cwbOrder.getLeavedreason();
						pay_status = "0";
					} else if (deliveryState.getDeliverystate() == DeliveryStateEnum.QuanBuTuiHuo.getValue()) {
						order_status = "7";
						reason = cwbOrder.getBackreason();
						consignee = cwbOrder.getConsigneename();
						pay_status = "0";
					} else if (deliveryState.getDeliverystate() == DeliveryStateEnum.PeiSongChengGong.getValue()) {
						order_status = "9";
						consignee = deliveryState.getSign_man();
						pay_status = "1";
					}
					if (Long.valueOf(cwbOrder.getNewpaywayid()) == PaytypeEnum.Xianjin.getValue()) {
						paymethod = "2";
					} else if (Long.valueOf(cwbOrder.getNewpaywayid()) == PaytypeEnum.Pos.getValue()) {
						paymethod = "3";
					}
					Long bound_times = null;
					Date date = DateTimeUtil.StringToDate(weisudaCwb.getBound_time());
					if (date == null) {
						bound_times = DateTimeUtil.StringToDate(weisudaCwb.getOperationTime()).getTime();
					} else {
						bound_times = date.getTime();
					}
					String bound_time = "";
					bound_time = (bound_times == null ? "" : (bound_times / 1000) + "");
					String backreason = reason == null ? "" : reason;
					delay_reason = delay_reason == null ? "" : delay_reason;
					String data = "<root>" + "<item>" + "<order_id>" + order_id + "</order_id>" + "<order_status>" + order_status + "</order_status>" + "<carrier_code>"
							+ weisudaCwb.getCourier_code().toUpperCase() + "</carrier_code>" + "<bound_time>" + bound_time + "</bound_time>" + "<consignee>" + consignee + "</consignee>"
							+ "<opertime>" + opertime + "</opertime>" + "<reason>" + backreason + "</reason>" + "<delay_reason>" + delay_reason + "</delay_reason>" + "<memo>" + memo
							+ "</memo>" + "<paymethod>" + paymethod + "</paymethod>" + "<pay_status>" + pay_status + "</pay_status>" + "</item>" + "</root>";
					this.logger.info("唯速达_04包裹修改信息接口修改发送数据！data={}", data);
					String response = this.check(weisuda, "data", data, WeisudsInterfaceEnum.updateOrders.getValue());
					this.logger.info("唯速达_04包裹修改信息接口修改返回数据！response={}", response);
					int version = this.GetWeisuda_Version();
					if (version == 1) {
						if (response.contains("<error><code>")) {
							this.logger.info("唯速达_04包裹修改信息接口验证失败！userMessage={}", response);
							String remark=response.length()>80?response.substring(0, 80):response;
							this.weisudaDAO.updataWeisudaCwbIsqianshou(cwb, "2", "包裹签收信息修改_失败！" +remark);
						}

						else if (response.contains(cwb)) {
							this.weisudaDAO.updataWeisudaCwbIsqianshou(cwb, "1", "包裹签收信息修改_成功！");
						}
					} else if (version == 2) {

						RootOrder root = (RootOrder) ObjectUnMarchal.XmltoPOJO(response, new RootOrder());
						if ((root != null) && (root.getOrders() != null)) {

							for (Order order : root.getOrders().getOrder()) {
								if (order.getOrder_id().equals(cwb)) {
									if (order.getHandleCode().equals(HandleCodeEnum.SUCCESS.getText())) {
										this.weisudaDAO.updataWeisudaCwbIsqianshou(cwb, "1", "包裹签收信息修改_成功！");
										this.logger.info("唯速达_04包裹签收信息修改_成功！,response={}", response);
									} else {
										this.weisudaDAO.updataWeisudaCwbIsqianshou(cwb, "2", "包裹签收信息修改_失败！" + order.getMsg());
										this.logger.info("唯速达_04包裹签收信息修改_失败！,response={}", response);
									}
								}
							}

						}

					}
				}
				else{
					this.logger.info("唯速达_04包裹修改信息接口修改已取消，原因：下发品骏达接口表标示已签收！cwb={}", cwb);
				}
			}
		} catch (Exception e) {
			this.weisudaDAO.updataWeisudaCwbIsqianshou(cwb, "2", "包裹签收信息修改_失败_异常！");
			this.logger.error("唯速达_04包裹签收信息修改_失败_异常！", e);
		}
	}

	/**
	 * 站点更新接口
	 */
	public void siteUpdate(@Header("branch") String branch) {
		if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			this.logger.info("唯速达_05未开启[唯速达]接口");
			return;
		}
		try {
			
			Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
			BranchWithOld bch = JsonUtil.readValue(branch, BranchWithOld.class);
			
			String province=bch.getBranchprovince()==null||bch.getBranchprovince().isEmpty()?"***":bch.getBranchprovince();
			String city=bch.getBranchcity()==null||bch.getBranchcity().isEmpty()?"***":bch.getBranchcity();
			String zone=bch.getBrancharea()==null||bch.getBrancharea().isEmpty()?"***":bch.getBrancharea();
			String newCode=bch.getTpsbranchcode()==null?"":bch.getTpsbranchcode();
			String oldCode="";
			if(weisuda.getChangeBranchcode()==1){
				oldCode=""+bch.getBranchid();
			}else{
				oldCode=bch.getOldtpsbranchcode()==null?"":bch.getOldtpsbranchcode();
				oldCode=oldCode.equals(newCode)?"":oldCode;//???
			}
			
			String data = "<root>" 
							+ "<item>" 
								+ "<code>" + newCode+ "</code>" 
								+ "<old_code>"+oldCode+"</old_code>" 
								+ "<name>" + bch.getBranchname() + "</name>" 
								+ "<province>"+ province + "</province>" 
								+ "<city>" + city + "</city>" 
								+ "<zone>" + zone + "</zone>" 
								+ "<password></password>" 
							+ "</item>"
					+ "</root>";
			this.logger.info("唯速达_05站点更新接口发送报文,userMessage={}", data);
			String response = this.check(weisuda, "data", data, WeisudsInterfaceEnum.siteUpdate.getValue());
			this.logger.info("唯速达_05站点更新返回response={}", response);
			
		} catch (Exception e) {
			this.logger.error("唯速达_05更新站点信息出错" + branch, e);
		}
	}

	/**
	 * 站点撤销接口
	 */

	public void siteDel(@Header("branchid") String branchid) {
		if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			this.logger.info("未开启[唯速达]接口");
			return;
		}
		try {
			long bid=Long.parseLong(branchid);
			Branch branch = this.getDmpDAO.getNowBranch(bid);
			Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
			String data = "<root>" 
							+ "<item>" 
								+ "<del_code>" + branch.getTpsbranchcode() + "</del_code>" 
								+ "<rec_code></rec_code>" 
							+ "</item>"
						+ "</root>";
			this.logger.info("唯速达_06站点撤销接口发送报文,userMessage={}", data);
			String response = this.check(weisuda, "data", data, WeisudsInterfaceEnum.siteDel.getValue());
			this.logger.info("唯速达_06站点撤销返回response={}", response);
			
		} catch (Exception e) {
			this.logger.error("站点撤销信息出错" + branchid, e);
		}
	}

	/**
	 * 快递员信息更新接口
	 */
	public void courierUpdate(@Header("user") String userFlag, @Body() String jsonUser) {

		if (userFlag == null) {
			return;
		}

		if (userFlag.contains("del")) {
			this.carrierDel(jsonUser);
			return;
		}
		if (!userFlag.contains("update")) {
			return;
		}

		if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			this.logger.info("唯速达_07未开启[唯速达]接口");
			return;
		}
		try {
			User user = JsonUtil.readValue(jsonUser, User.class);
			String oldusername = user.getOldusername() == null ? "" : user.getOldusername();
			if (user.getUsername().equals(oldusername)) {
				oldusername = "";
			}
			/*
			 * String userMessage = "快递员OLDID=" + oldusername + " 快递员ID:" +
			 * user.getUsername() + " 快递员姓名:" + user.getRealname() + " 快递员站点ID"
			 * + user.getBranchid() + " 快递员手机号码:" + user.getUsermobile() +
			 * " 快递员登陆密码:" + user.getPassword();
			 */
			Branch branch = this.getDmpDAO.getNowBranch(user.getBranchid());
			Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
			String data = "<root>" 
							+ "<item>" 
								+ "<code>" + user.getUsername().toUpperCase() + "</code>" 
								+ "<old_code>" + oldusername.toUpperCase() + "</old_code>" 
								+ "<name>" + user.getRealname()+ "</name>" 
								+ "<site_code>" + branch.getTpsbranchcode() + "</site_code>" 
								+ "<mobile>" + user.getUsermobile() + "</mobile>"
								+ "<password>" + user.getPassword() + "</password>"
							+ "</item>"
					+ "</root>";
			this.logger.info("唯速达_07快递员更新接口发送报文,userMessage={}", data);
			String response = this.check(weisuda, "data", data, WeisudsInterfaceEnum.courierUpdate.getValue());
			
			this.logger.info("唯速达_07快递员更新接口返回response={}", response);
			
		} catch (Exception e) {
			this.logger.error("唯速达_07快递员更新信息出错 ！jsonUser=" + jsonUser, e);
		}
	}

	/**
	 * 快递员删除接口
	 */
	public void carrierDel(String jsonUser) {
		if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			this.logger.info("唯速达_08未开启[唯速达]接口");
			return;
		}
		try {
			User user = JsonUtil.readValue(jsonUser, User.class);
			/*
			 * String userMessage = "被删除的快递员ID:" + user.getUserid() + " 快递员姓名:"
			 * + user.getRealname() + " 快递员站点ID" + user.getBranchid() +
			 * " 快递员手机号码:" + user.getUsermobile() + " 快递员登陆密码:" +
			 * user.getPassword();
			 */
			Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
			String data = "<root>" 
								+ "<item>" 
									+ "<del_code>" + user.getUsername().toUpperCase() + "</del_code>" 
								+ "</item>" 
							+ "</root>";
			this.logger.info("唯速达_08快递员删除接口发送报文,userMessage={}", data);
			String response = this.check(weisuda, "data", data, WeisudsInterfaceEnum.carrierDel.getValue());
			
			this.logger.info("唯速达_08快递员删除返回response={}", response);
			
		} catch (Exception e) {
			this.logger.error("唯速达_08快递员删除信息出错 ！jsonUser=" + jsonUser, e);
		}
	}

	public void unboundOrders(CwbOrderWithDeliveryState cwbOrderWithDeliveryState, DmpOrderFlow orderFlow) {
		DmpCwbOrder cwbOrder = cwbOrderWithDeliveryState.getCwbOrder();
		int cwbordertypeid = Integer.parseInt(cwbOrder.getCwbordertypeid());
		if ((cwbordertypeid == CwbOrderTypeIdEnum.Peisong.getValue()) || (cwbordertypeid == CwbOrderTypeIdEnum.Shangmentui.getValue())) {

			if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
				this.logger.info("唯速达_09未开启[唯速达]接口");
				return;
			}
			Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
			this.logger.info("唯速达_09进入唯速达对接cwb={}", orderFlow.getCwb());
			long customerid = cwbOrderWithDeliveryState.getCwbOrder().getCustomerid();
			Customer customer = this.getDmpDAO.getCustomer(customerid);
			if (customer.getB2cEnum().equals(this.getB2cEnumKeys(customer, "vipshop"))) {
				String cwb = cwbOrderWithDeliveryState.getCwbOrder().getCwb();
				try {
					WeisudaCwb weisudaCwb = this.weisudaDAO.getWeisudaCwbByOrderAndIsTuisong(cwb, 1);
					if (weisudaCwb != null) {
						String data = "<root><item><order_id>" + cwb + "</order_id></item></root>";
						this.logger.info("唯速达_09快递员解绑接口发送报文,userMessage={}", data);
						String response = this.check(weisuda, "data", data, WeisudsInterfaceEnum.unboundOrders.getValue());
						this.logger.info("唯速达_09快递员解绑接返回,response={}", response);
						int version = this.GetWeisuda_Version();
						if (version == 1) {
							if (response.contains(cwb)) {
								this.weisudaDAO.updateWeisuda(cwb, "3", "快递员解绑成功！");
							} else {
								this.weisudaDAO.updateWeisuda(cwb, "2", "快递员解绑失败！");
							}
						} else if (version == 2) {
							RootOrder root = (RootOrder) ObjectUnMarchal.XmltoPOJO(response, new RootOrder());
							if ((root != null) && (root.getOrders() != null)) {

								for (Order order : root.getOrders().getOrder()) {
									if (order.getOrder_id().equals(cwb)) {
										if (order.getHandleCode().equals(HandleCodeEnum.SUCCESS.getText())) {
											this.weisudaDAO.updateWeisuda(cwb, "3", "快递员解绑成功！");
										} else {
											this.weisudaDAO.updateWeisuda(cwb, "2", "快递员解绑失败！");
										}
									}
								}

							}

						}
					}

				} catch (Exception e) {
					this.logger.error("唯速达_09快递员解绑接出错 ！", e);
				}
			} else {
				this.logger.info("唯速达_09未设置对接，customername={},cwb={}", customer.getCustomername(), orderFlow.getCwb());
			}
		} else {
			this.logger.info("唯速达_09不是所需要的订单类型，cwb={}", orderFlow.getCwb());
		}

	}

	/**
	 * APP上门退签收信息同步接口
	 */
	public void getback_getAppOrders() {
		if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			this.logger.info("唯速达_11未开启[唯速达]接口");
			return;
		}
		Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
		String response = this.check(weisuda, "nums", weisuda.getNums(), WeisudsInterfaceEnum.getback_getAppOrders.getValue());
		this.logger.info("唯速达_11_APP上门退签收信息同步接口下载数据,{}", response);
		if (response != null) {
			if (response.contains("<error><code>")) {
				this.logger.info("唯速达_11上门退签收信息同步接口验证失败！userMessage={}", response);
				return;
			}
			if (response.contains("<root>") && response.contains("<courier_code>")) {
				try {
					RootSMT back_Root = (RootSMT) ObjectUnMarchal.XmltoPOJO(response, new RootSMT());

					for (Getback_Item item : back_Root.getItem()) {
						String cwb=item.getOrder_id();
						WeisudaCwb weisudaCwbs = this.weisudaDAO.getWeisudaCwbIstuisong(cwb);

						if (weisudaCwbs == null) {
							this.getback_confirmAppOrders(item.getOrder_id());
							continue;
						}
						String json = this.buliderJson(item, cwb);

						this.logger.info("唯速达_11请求dmp-json={}", json);

						String result = this.getDmpDAO.requestDMPOrderService_Weisuda(json);

						if ("SUCCESS".equals(result)) {
							this.weisudaDAO.updataWeisudaCwbIsqianshou(item.getOrder_id(), "1", "上门退订单信息通过_手机_签收成功");
							this.getback_confirmAppOrders(item.getOrder_id());
						} else if (result.contains("处理唯速达反馈请求异") && result.contains("已审核的订单不允许进行反馈")) {
							this.weisudaDAO.updataWeisudaCwbIsqianshou(item.getOrder_id(), "1", "上门退订单信息已经通过_其它方式_签收成功");
							this.getback_confirmAppOrders(item.getOrder_id());
						}else {
							this.logger.info("唯速达_11请求dmp唯速达信息异常{},cwb={}", result, item.getOrder_id());
							String remark=result!=null&&result.length()>80?result.substring(0, 80):result;
							this.weisudaDAO.updataWeisudaCwbIsqianshou(item.getOrder_id(), "2", remark);
							this.getback_confirmAppOrders(item.getOrder_id());
							continue;
						}
					}
				}catch (Exception e) {
					this.logger.error("唯速达_11请求dmp唯速达信息异常" + response, e);
				}
			}

			else {
				this.logger.info("唯速达_11返回订单失败！{}", response);
			}
		}
	}

	/**
	 * APP包裹签收信息同步结果反馈接口
	 *
	 * @param 订单号码
	 */
	private void getback_confirmAppOrders(String orderid) {
		Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
		String send = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<root>" + "<item>" + "<order_id>" + orderid + "</order_id>" + "</item>" + "</root>";
		this.logger.info("唯速达_12APP上门退签收信息同步结果反馈接口 发送报文,userMessage={}", send);
		String response = this.check(weisuda, "data", send, WeisudsInterfaceEnum.getback_confirmAppOrders.getValue());
		this.logger.info("唯速达_12APP上门退签收信息同步结果反馈接口 返回response={}", response);
		
	}

	/**
	 * 包裹签收信息修改通知接口
	 */
	public void getback_updateOrders(CwbOrderWithDeliveryState cwbOrderWithDeliveryState, DmpOrderFlow orderFlow) {
		if (CwbOrderTypeIdEnum.Shangmentui.getValue() == Integer.parseInt(cwbOrderWithDeliveryState.getCwbOrder().getCwbordertypeid())) {

			if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
				this.logger.info("唯速达_13未开启[唯速达]接口");
				return;
			}
			Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
			this.logger.info("唯速达_13进入唯速达对接cwb={}", orderFlow.getCwb());
			long customerid = cwbOrderWithDeliveryState.getCwbOrder().getCustomerid();
			Customer customer = this.getDmpDAO.getCustomer(customerid);
			if (customer.getB2cEnum().equals(this.getB2cEnumKeys(customer, "vipshop"))) {
				String cwb = cwbOrderWithDeliveryState.getCwbOrder().getCwb();
				try {
					WeisudaCwb weisudaCwb = this.weisudaDAO.getWeisudaCwbByOrder(cwb);
					if (weisudaCwb != null) {
						if (weisudaCwb.getIsqianshou().equals("0")) {
							DmpCwbOrder cwbOrder = cwbOrderWithDeliveryState.getCwbOrder();
							DmpDeliveryState deliveryState = cwbOrderWithDeliveryState.getDeliveryState();
							String order_id = cwbOrder.getCwb();
							String order_status = "0";
							String pay_status = "1";
							Long deliverytime = DateTimeUtil.StringToDate(deliveryState.getDeliverytime()).getTime();
							Long bound_times = null;
							Date date = DateTimeUtil.StringToDate(weisudaCwb.getBound_time());
							if (date == null) {
								bound_times = DateTimeUtil.StringToDate(weisudaCwb.getOperationTime()).getTime();
							} else {
								bound_times = date.getTime();
							}
							String opertime = "";
							opertime = (deliverytime == null ? "" : (deliverytime / 1000) + "");
							String bound_time = "";
							bound_time = (bound_times == null ? "" : (bound_times / 1000) + "");
							String reason = "";
							String delay_reason = "";
							String memo = "";
							String paymethod = "2";
							if ((deliveryState.getDeliverystate() == DeliveryStateEnum.ShangMenTuiChengGong.getValue())) {
								order_status = "9";
								pay_status = "1";
							} else if (deliveryState.getDeliverystate() == DeliveryStateEnum.ShangMenJuTui.getValue()) {
								order_status = "7";
								reason = deliveryState.getBackreason();
								pay_status = "0";
							} else if (deliveryState.getDeliverystate() == DeliveryStateEnum.FenZhanZhiLiu.getValue()) {
								order_status = "0";
								reason = deliveryState.getLeavedreason();
								pay_status = "0";
							}
							String goodsString = this.getDmpDAO.getOrderGoods(weisudaCwb.getCwb());
							JSONArray jsonarray = JSONArray.fromObject(goodsString);
							List<OrderGoods> list = (List<OrderGoods>) JSONArray.toCollection(jsonarray, OrderGoods.class);
							String goodsxml = "<goods>";
							for (OrderGoods good : list) {
								goodsxml += "<good>" + "<code>" + good.getGoods_code() + "</code>" + "<fetch_num>" + good.getShituicount() + "</fetch_num>" + "<special_num>" + good.getTepituicount()
										+ "</special_num>" + "<remark>" + good.getReturn_reason() + "</remark>" + "</good>";
							}
							goodsxml += "</goods>";
							String backreason = reason == null ? "" : reason;
							delay_reason = delay_reason == null ? "" : delay_reason;
							String data = "<root>" + "<item>" + "<order_id>" + order_id + "</order_id>" + "<order_status>" + order_status + "</order_status>" + "<carrier_code>"
									+ weisudaCwb.getCourier_code().toUpperCase() + "</carrier_code>" + "<bound_time>" + bound_time + "</bound_time>" + "<opertime>" + opertime + "</opertime>"
									+ "<reason>" + backreason + "</reason>" + "<memo>" + memo + "</memo>" + "<paymethod>" + paymethod + "</paymethod>" + "<pay_status>" + pay_status + "</pay_status>"
									+ goodsxml + "</item>" + "</root>";
							this.logger.info("唯速达_13上门退修改信息接口修改发送数据！data={}", data);
							String response = this.check(weisuda, "data", data, WeisudsInterfaceEnum.getback_updateOrders.getValue());
							this.logger.info("唯速达_13上门退修改信息返回数据！response={},cwb={}", response, order_id);
							int version = this.GetWeisuda_Version();
							if (version == 1) {
								if (response.contains("<error><code>")) {
									this.logger.info("唯速达_13上门退修改信息接口验证失败！userMessage={}", response);
									return;
								}

								else if (response.contains(cwb)) {
									this.weisudaDAO.updataWeisudaCwbIsqianshou(cwb, "1", "上门退签收信息修改_成功！");
								}
							} else if (version == 2) {

								RootOrder root = (RootOrder) ObjectUnMarchal.XmltoPOJO(response, new RootOrder());
								if ((root != null) && (root.getOrders() != null)) {

									for (Order order : root.getOrders().getOrder()) {
										if (order.getOrder_id().equals(cwb)) {
											if (order.getHandleCode().equals(HandleCodeEnum.SUCCESS.getText())) {
												this.weisudaDAO.updataWeisudaCwbIsqianshou(cwb, "1", "包裹签收信息修改_成功！");
												this.logger.info("唯速达_13上门退修改信息接口验证成功！,response={}", response);
											} else {
												this.weisudaDAO.updataWeisudaCwbIsqianshou(cwb, "2", "包裹签收信息修改_失败！" + order.getMsg());
												this.logger.info("唯速达_13上门退修改信息接口验证失败！userMessage={}", response);
											}
										}
									}

								}

							}
						}
					}
				} catch (Exception e) {
					this.weisudaDAO.updataWeisudaCwbIsqianshou(cwb, "2", "上门退签收信息修改_失败_异常！");
					this.logger.error("唯速达_13上门退签收信息修改_失败_异常！", e);
				}
			} else {
				this.logger.info("唯速达_13未设置对接，customername={},cwb={}", customer.getCustomername(), orderFlow.getCwb());
			}
		} else {
			this.logger.info("唯速达_13不是所需要的订单类型，cwb={}", orderFlow.getCwb());
		}
	}

	/**
	 * 获取对接电商 的配置 支持同一类客户不同枚举的设置 如：唯品会，天猫，一号店等。
	 *
	 * @param customer
	 * @return
	 */
	public String getB2cEnumKeys(Customer customer, String constainsStr) {
		for (B2cEnum enums : B2cEnum.values()) {
			if (enums.getMethod().contains(constainsStr)) {
				if (customer.getB2cEnum().equals(String.valueOf(enums.getKey()))) {
					return String.valueOf(enums.getKey());
				}
			}
		}
		return null;
	}

	private void DealWithBuildXMLAndSending(List<WeisudaCwb> weisudaCwbs, Weisuda weisuda) {
		String cwb = "";
		String response = "";
		int version = this.GetWeisuda_Version();
		String upflagString="";
		for (WeisudaCwb data : weisudaCwbs) {
			try {
				String courier_code = data.getCourier_code();
				cwb = data.getCwb();
				Date datetime = DateTimeUtil.formatToDate(data.getOperationTime());
				String timestamp = (datetime.getTime() / 1000) + "";

				upflagString = "唯速达_01";
				int url = WeisudsInterfaceEnum.pushOrders.getValue();
				if (data.getCwbordertypeid() == CwbOrderTypeIdEnum.Shangmentui.getValue()) {
					upflagString = "唯速达_10上门退订单";
					url = WeisudsInterfaceEnum.getback_boundOrders.getValue();
				}
				String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" 
							+ "<root>" 
								+ "<item>" 
									+ "<courier_code>" + courier_code.toUpperCase() + "</courier_code>" 
									+ "<order_id>" + cwb+ "</order_id>" 
									+ "<bound_time>" + timestamp + "</bound_time>" 
								+ "</item>" 
							+ "</root>";
				this.logger.info(upflagString + "快递员绑定接口接口发送报文,userMessage={}", xml);
				response = this.check(weisuda, "data", xml, url);
				this.logger.info(upflagString + "快递员绑定接口返回：{},cwb={}", response, cwb);
				
				updateDeliveryUserBound(cwb, response, upflagString, version);

			} catch (Exception e) {
				this.logger.error(upflagString + "异常：" + e.getMessage() + "cwb=" + cwb + "返回：" + response, e);
			}
		}

	}

	private void updateDeliveryUserBound(String cwb, String response,
			String upflagString, int version) throws JAXBException,
			UnsupportedEncodingException {
		if (version == 1) {
			if (response.contains(cwb)) {
				this.weisudaDAO.updateWeisuda(cwb, "1", "快递员绑定成功!");
				this.logger.info(upflagString + "快递员绑定接口成功!response={},cwb={}", response, cwb);
			} else {
				this.weisudaDAO.updateWeisuda(cwb, "2", response);
				this.logger.info(upflagString + "快递员绑定失败! response={},cwb={}", response, cwb);
			}
		} else if (version == 2) {
			RootOrder root = (RootOrder) ObjectUnMarchal.XmltoPOJO(response, new RootOrder());
			if ((root != null) && (root.getOrders() != null)) {
				for (Order order : root.getOrders().getOrder()) {
					if (order.getOrder_id().equals(cwb)) {
						if (order.getHandleCode().equals(HandleCodeEnum.SUCCESS.getText())) {
							this.weisudaDAO.updateWeisuda(cwb, "1", "快递员绑定成功!");
							this.logger.info(upflagString + "快递员绑定接口成功!response={},cwb={}", response, cwb);
						} else {
							this.weisudaDAO.updateWeisuda(cwb, "2", order.getMsg());
							this.logger.info(upflagString + "快递员绑定失败! response={},cwb={}", response, cwb);
						}
					}
				}
			}
		}
	}

	// 获取配置信息
	public Weisuda getWeisuda(int key) {
		Weisuda et = null;
		String objectMethod = this.b2ctools.getObjectMethod(key).getJoint_property();
		if (objectMethod != null) {
			JSONObject jsonObj = JSONObject.fromObject(objectMethod);
			et = (Weisuda) JSONObject.toBean(jsonObj, Weisuda.class);
		} else {
			et = new Weisuda();
		}
		return et;
	}

	public static String createLinkString(Map<String, String> params) {

		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);

		String prestr = "";

		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);

			prestr = prestr + key + value;

		}

		return prestr;
	}

	private String check(Weisuda weisuda, String params, String value, int type) {
		String timestamp = (System.currentTimeMillis() / 1000) + "";
		String code = weisuda.getCode();
		String secret = weisuda.getSecret();
		String v = weisuda.getV();
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> mapMd5 = new HashMap<String, String>();
		mapMd5.put(params, value);
		String md5 = WeisudaService.createLinkString(mapMd5);
		String sign = MD5Util.md5(secret + md5 + secret);

		String access_token = MD5Util.md5(timestamp + "_" + secret + "_" + sign);
		map.put("timestamp", timestamp);
		map.put("code", code);
		map.put("v", v);
		map.put(params, value);
		map.put("sign_method", "fullmd5");
		map.put("sign", sign);
		map.put("access_token", access_token);
		String url = "";
		switch (type) {
		case 1:
			url = weisuda.getPushOrders_URL();
			break;
		case 2:
			url = weisuda.getUnVerifyOrders_URL();
			break;
		case 3:
			url = weisuda.getUpdateUnVerifyOrders_URL();
			break;
		case 4:
			url = weisuda.getUpdateOrders_URL();
			break;
		case 5:
			url = weisuda.getSiteUpdate_URL();
			break;
		case 6:
			url = weisuda.getSiteDel_URL();
			break;
		case 7:
			url = weisuda.getCourierUpdate_URL();
			break;
		case 8:
			url = weisuda.getCarrierDel_URL();
			break;
		case 9:
			url = weisuda.getUnboundOrders_URL();
			break;
		case 10:
			url = weisuda.getGetback_boundOrders_URL();
			break;
		case 11:
			url = weisuda.getGetback_getAppOrders_URL();
			break;
		case 12:
			url = weisuda.getGetback_confirmAppOrders_URL();
			break;
		case 13:
			url = weisuda.getGetback_updateOrders_URL();
			break;
		default:
			break;
		}

		String str = RestHttpServiceHanlder.sendHttptoServer(map, url);
		return str;
	}

	public String buliderJson(Item item, String  cwb) throws Exception {
		GetUnVerifyOrders_back_Item itemps = new GetUnVerifyOrders_back_Item();
		Getback_Item itemsmt = new Getback_Item();
		String json = "";
		OrderFlowDto dto = new OrderFlowDto();
		dto.setCustid("-1");
		dto.setCwb(cwb);
		dto.setStrandedrReason("");
		long deliverystate = 0;
		String status = item.getOrder_status();
		if (item.getClass().isInstance(itemps)) {// 如果是配送的订单
			itemps = (GetUnVerifyOrders_back_Item) item;
			if ("9".equals(status)) {
				deliverystate = DeliveryStateEnum.PeiSongChengGong.getValue();

			} else if ("7".equals(status)) {
				deliverystate = DeliveryStateEnum.QuanBuTuiHuo.getValue();
				dto.setExptmsg(item.getReason());
			} else if ("4".equals(status)) {
				deliverystate = DeliveryStateEnum.FenZhanZhiLiu.getValue();
				dto.setStrandedrReason(itemps.getDelay_reason());
			}
			dto.setConsignee(itemps.getConsignee());
		} else if (item.getClass().isInstance(itemsmt)) {// 如果是上门退订单
			itemsmt = (Getback_Item) item;
			if ("9".equals(status)) {
				deliverystate = DeliveryStateEnum.ShangMenTuiChengGong.getValue();

			} else if ("7".equals(status)) {
				deliverystate = DeliveryStateEnum.ShangMenJuTui.getValue();
			}
			Goods goods = itemsmt.getGoods();
			String goodString = JacksonMapper.getInstance().writeValueAsString(goods);
			dto.setReamrk1(goodString);
		}
		dto.setDeliverystate(deliverystate + "");
		dto.setExptmsg(item.getReason());
		dto.setFlowordertype(FlowOrderTypeEnum.YiFanKui.getValue() + "");
		dto.setOperatortime(DateTimeUtil.getNowTime());
		dto.setDeliveryname(item.getCourier_code());
		String payremark = "_";
		int paytype = 0;
		if ("1".equals(item.getPaymethod())) {
			paytype = PaytypeEnum.Xianjin.getValue();
			// paytype = PaytypeEnum.Qita.getValue();
			payremark = "线上已支付";
		} else if ("2".equals(item.getPaymethod())) {
			payremark = "现金";
			paytype = PaytypeEnum.Xianjin.getValue();
		} else if ("3".equals(item.getPaymethod())) {
			payremark = "POS";
			paytype = PaytypeEnum.Pos.getValue();
		} else if ("4".equals(item.getPaymethod())) {
			payremark = "支付宝COD";
			paytype = PaytypeEnum.CodPos.getValue();
		} else if ("5".equals(item.getPaymethod())) {
			paytype = PaytypeEnum.Qita.getValue();
		} else if ("6".equals(item.getPaymethod())) {
			paytype = PaytypeEnum.CodPos.getValue();
		} else if ("11".equals(item.getPaymethod())) {
			payremark = "微信APP支付";
			paytype = PaytypeEnum.CodPos.getValue();
		} else if ("12".equals(item.getPaymethod())) {
			payremark = "支付宝APP支付";
			paytype = PaytypeEnum.CodPos.getValue();
		} else if ("13".equals(item.getPaymethod())) {
			payremark = "工行MPOS";
			paytype = PaytypeEnum.Pos.getValue();
		} else if ("14".equals(item.getPaymethod())) {
			payremark = "快刷MPOS";
			paytype = PaytypeEnum.Pos.getValue();
		} else if ("15".equals(item.getPaymethod())) {
			payremark = "快钱pos支付";
			paytype = PaytypeEnum.Pos.getValue();
		} else if ("16".equals(item.getPaymethod())) {
			payremark = "通联pos支付";
			paytype = PaytypeEnum.Pos.getValue();
		} else if ("17".equals(item.getPaymethod())) {
			payremark = "唯宝支付";
			paytype = PaytypeEnum.CodPos.getValue();
		}
		dto.setCwbremark(item.getMemo());
		dto.setPayremark(payremark);
		dto.setPaytype(paytype);

		if (item.getOpertime() == null) {
			dto.setRequestTime(DateTimeUtil.getNowTime());
		} else {
			try {
				Date opertime = new Date(Long.parseLong(item.getOpertime()) * 1000L);
				dto.setRequestTime(DateTimeUtil.formatDate(opertime, "yyyy-MM-dd HH:mm:ss"));
			} catch (Exception e) {
				dto.setRequestTime(DateTimeUtil.getNowTime());
			}
		}
		json = JacksonMapper.getInstance().writeValueAsString(dto);
		return json;
	}

	/**
	 * APP包裹签收信息同步接口 采用多次循环
	 */
	public void getUnVerifyOrdersOfCount() {
		Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
		if(weisuda.getOpenbatchflag()==1){ //开启批量之后退出
			return;
		}

		int count = weisuda.getCount().length() > 0 ? Integer.parseInt(weisuda.getCount()) : 1;
		for (int i = 0; i < count; i++) {
			this.getUnVerifyOrders();
		}
	}

	/**
	 * APP上门退包裹签收信息同步接口 采用多次循环
	 */
	public void getback_getAppOrdersCounts() {
		Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());

		int count = weisuda.getCount().length() > 0 ? Integer.parseInt(weisuda.getCount()) : 1;
		for (int i = 0; i < count; i++) {
			this.getback_getAppOrders();
		}
	}

	public int deleteData() {
		String lastMonth = DateTimeUtil.getMonthBefore();
		int nums = this.weisudaDAO.deleteData(lastMonth);
		this.logger.info("唯速达_删除过期订单信息,删除operationTime={}之前的订单,执行条数nums={}", lastMonth, nums);
		return nums;
	}

	public String updataAllBranch() {
		List<Branch> branchs = this.getDmpDAO.getAllBranchs();
		Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
		String data = "<root>";
		for (Branch bch : branchs) {
			if (bch.getSitetype() == BranchEnum.ZhanDian.getValue()) {
				String branchProvince = bch.getBranchprovince()==null||bch.getBranchprovince().isEmpty()?"***":bch.getBranchprovince();
				String branchCity = bch.getBranchcity()==null||bch.getBranchcity().isEmpty()?"***":bch.getBranchcity();
				String branchArea = bch.getBrancharea()==null||bch.getBrancharea().isEmpty()?"***":bch.getBrancharea();
				String oldCode=weisuda.getChangeBranchcode()==1?(""+bch.getBranchid()):"";
				data += "<item>" 
							+ "<code>" + bch.getTpsbranchcode() + "</code>" 
							+ "<old_code>"+oldCode+"</old_code>" 
							+ "<name>" + bch.getBranchname() + "</name>" 
							+ "<province>"+ (branchProvince) + "</province>" 
							+ "<city>" + (branchCity) + "</city>"
							+ "<zone>"+ (branchArea) + "</zone>" 
							+ "<password></password>" 
						+ "</item>";

			}
		}
		data += "</root>";
		this.logger.info("唯速达_同步所有站点发送报文,data={}", data);
		
		String response = this.check(weisuda, "data", data, WeisudsInterfaceEnum.siteUpdate.getValue());

		return response;
	}

	public String updataAlluser() {
		String responStrAll="";
		List<Branch> branchs = this.getDmpDAO.getAllBranchs();
		for(Branch b:branchs){
			if (b.getSitetype() == BranchEnum.ZhanDian.getValue()) {
				List<User> users = this.getDmpDAO.getAllUserbybranchid(b.getBranchid());
				String responStr = synUserInfoToApp(users,b.getBranchname());
				responStrAll+=responStr+"  \n";
			}
		}

		return "同步员工信息成功:\n"+responStrAll;
	}
	
	public String updataAlluserById(long branchid) {
		String responStrAll="";
		List<User> users = this.getDmpDAO.getAllUserbybranchid(branchid);
		String responStr = synUserInfoToApp(users,branchid+"");
		responStrAll+=responStr+"  \n";

		return "同步员工信息成功:\n"+responStrAll;
	}

	private String synUserInfoToApp(List<User> users,String branchname) {
		boolean isHave=false; //是否存在
		String data = "<root>";
		for (User user : users) {
			if ((user.getRoleid() == 4) || (user.getRoleid() == 2)) {
				isHave=true;
				long branchid = user.getBranchid();
				Branch branch = this.getDmpDAO.getNowBranch(branchid);
				data += "<item>" 
							 + "<code>" + user.getUsername().toUpperCase() + "</code>" 
							 + "<old_code></old_code>" 
							 + "<name>" + user.getRealname() + "</name>" 
							 + "<site_code>"+ branch.getTpsbranchcode() + "</site_code>" 
							 + "<mobile>" + user.getUsermobile() + "</mobile>" 
							 + "<password>" + (user.getPassword() == null ? "" : user.getPassword())+ "</password>" 
						 + "</item>";
			}
		}
		if(!isHave){
			return "不存在待同步员工";
		}
		data += "</root>";
		this.logger.info("唯速达_同步所有快递员发送报文,branchname={},data={}",branchname, data);
		Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
		String response = this.check(weisuda, "data", data, WeisudsInterfaceEnum.courierUpdate.getValue());
		this.logger.info("唯速达_同步所有快递员返回报文,branchname={},data={}",branchname, response);
		return response;
	}

	/**
	 *
	 */
	private int GetWeisuda_Version() {
		int version = 1;
		SystemInstall systemInstall = this.getDmpDAO.getSystemInstallByName("weisuda_Version");

		if (systemInstall != null) {
			version = Integer.valueOf(systemInstall.getValue());
		}
		return version;
	}
	
}
