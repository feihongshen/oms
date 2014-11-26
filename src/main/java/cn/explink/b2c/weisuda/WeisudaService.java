package cn.explink.b2c.weisuda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

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
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.b2c.weisuda.xml.Errors;
import cn.explink.b2c.weisuda.xml.GetUnVerifyOrders_back_Item;
import cn.explink.b2c.weisuda.xml.GetUnVerifyOrders_back_Root;
import cn.explink.b2c.weisuda.xml.ObjectUnMarchal;
import cn.explink.b2c.weisuda.xml.UpdateOrders_root;
import cn.explink.dao.BranchDAO;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.WeisudaDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.Customer;
import cn.explink.domain.User;
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

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@PostConstruct
	public void init() {
		try {
			camelContext.addRoutes(new RouteBuilder() {
				@Override
				public void configure() throws Exception {

					from("jms:queue:VirtualTopicConsumers.omsToWeisudaSyn.savezhandian?concurrentConsumers=1").to("bean:weisudaService?method=siteUpdate").routeId("weisuda_更新站点");
					// from("jms:queue:VirtualTopicConsumers.omsToWeisudaSyn.delzhandian?concurrentConsumers=5").to("bean:weisudaService?method=siteDel").routeId("weisuda_撤销站点");
					from("jms:queue:VirtualTopicConsumers.omsToWeisudaSyn.courierUpdate?concurrentConsumers=1").to("bean:weisudaService?method=courierUpdate").routeId("weisuda_更新快递员");
					// from("jms:queue:VirtualTopicConsumers.omsToWeisudaSyn.carrierDel?concurrentConsumers=5").to("bean:weisudaService?method=carrierDel").routeId("weisuda_删除快递员");
				}
			});
		} catch (Exception e) {
			logger.error("camel context start fail", e);
		}
	}

	/**
	 * 插入唯速达订单数据
	 * 
	 * @param cwbOrderWithDeliveryState
	 * @param orderFlow
	 */
	public void insertWeisuda(CwbOrderWithDeliveryState cwbOrderWithDeliveryState, DmpOrderFlow orderFlow) {
		logger.info("唯速达_01进入唯速达对接cwb={}", orderFlow.getCwb());

		long customerid = cwbOrderWithDeliveryState.getCwbOrder().getCustomerid();
		Customer customer = getDmpDAO.getCustomer(customerid);
		if (customer.getB2cEnum().equals(getB2cEnumKeys(customer, "vipshop"))) {
			String orderTime = DateTimeUtil.formatDate(orderFlow.getCredate());
			DmpCwbOrder cwbOrder = cwbOrderWithDeliveryState.getCwbOrder();
			User deliverUser = getDmpDAO.getUserById(cwbOrder.getDeliverid());

			WeisudaCwb weisudaCwbold = weisudaDAO.getWeisudaCwb(orderFlow.getCwb(), orderTime);
			if (weisudaCwbold == null) {
				WeisudaCwb weisudaCwb = new WeisudaCwb();
				weisudaCwb.setCwb(orderFlow.getCwb());
				weisudaCwb.setCourier_code(deliverUser.getUsername());
				weisudaCwb.setBound_time(System.currentTimeMillis() / 1000 + "");
				weisudaCwb.setOperationTime(orderTime);
				weisudaDAO.insertWeisuda(weisudaCwb);
				logger.info("唯速达_01获取唯速达数据插入成功cwb={}", weisudaCwb.getCwb());
			}
		} else {
			logger.warn("未设置对接，customername=" + customer.getCustomername() + ",cwb=" + orderFlow.getCwb());
		}

	}

	/**
	 * 订单与快递员绑定关系同步接口
	 */

	public void selectWeisudaCwb() {
		if (!b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			logger.info("唯速达_01未开启[唯速达]接口");
			return;
		}
		try {
			Weisuda weisuda = getWeisuda(PosEnum.Weisuda.getKey());

			int i = 0;
			while (true) {
				List<WeisudaCwb> weisudaCwbs = weisudaDAO.getWeisudaCwb("0");
				i++;
				if (i > 100) {
					String warning = "查询0唯速达0状态反馈已经超过100次循环，可能存在程序未知异常,请及时查询并处理!";
					logger.warn(warning);
					return;
				}

				if (weisudaCwbs == null || weisudaCwbs.size() == 0) {
					logger.info("唯速达_01当前没有要推送0唯速达0的数据");
					return;
				}

				DealWithBuildXMLAndSending(weisudaCwbs, weisuda);

			}

		} catch (Exception e) {
			String errorinfo = "唯速达_01发送0唯速达0状态反馈遇到不可预知的异常";
			logger.error(errorinfo, e);
		}

	}

	/**
	 * APP包裹签收信息同步接口
	 */
	public void getUnVerifyOrders() {
		if (!b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			logger.info("唯速达_02未开启[唯速达]接口");
			return;
		}
		Weisuda weisuda = getWeisuda(PosEnum.Weisuda.getKey());
		String response = check(weisuda, "nums", weisuda.getNums(), WeisudsInterfaceEnum.getUnVerifyOrders.getValue());
		if (response != null) {
			if (response.contains("<error><code>")) {
				logger.info("唯速达_02包裹签收信息同步接口验证失败！userMessage={}", response);
				return;
			}
			if (response.contains("<root>") && response.contains("<courier_code>")) {
				try {
					GetUnVerifyOrders_back_Root back_Root = (GetUnVerifyOrders_back_Root) ObjectUnMarchal.XmltoPOJO(response, new GetUnVerifyOrders_back_Root());

					for (GetUnVerifyOrders_back_Item item : back_Root.getItem()) {
						WeisudaCwb weisudaCwbs = weisudaDAO.getWeisudaCwbIstuisong(item.getOrder_id());

						if (weisudaCwbs == null) {
							continue;
						}
						String json = buliderJson(item, weisudaCwbs);

						logger.info("唯速达_02请求dmp-json={}", json);

						String result = getDmpDAO.requestDMPOrderService_Weisuda(json);

						if ("SUCCESS".equals(result)) {
							weisudaDAO.updataWeisudaCwbIsqianshou(item.getOrder_id(), "1", "包裹信息通过_手机_签收成功");
							updateUnVerifyOrders(item.getOrder_id());
						} else if (result.contains("处理唯速达反馈请求异") && result.contains("已审核的订单不允许进行反馈")) {
							weisudaDAO.updataWeisudaCwbIsqianshou(item.getOrder_id(), "1", "包裹信息已经通过_其它方式_签收成功");
							updateUnVerifyOrders(item.getOrder_id());
						}
						/*
						 * if(item.getOrder_status().equals("0")) {
						 * weisudaDAO.updataWeisudaCwbIsqianshou
						 * (item.getOrder_id(),"0","订单状态已重置！");
						 * logger.info("订单状态已重置！cwb={}",item.getOrder_id()); }
						 */
						else {
							logger.info("唯速达_02请求dmp唯速达信息异常{},cwb={}", result, item.getOrder_id());
							weisudaDAO.updataWeisudaCwbIsqianshou(item.getOrder_id(), "2", result);
							continue;
						}
					}
				}

				catch (Exception e) {
					logger.error("唯速达_02请求dmp唯速达信息异常" + response, e);
				}
			}

			else {
				logger.info("唯速达_02返回订单失败！{}", response);
			}
		}
	}

	/**
	 * APP包裹签收信息同步结果反馈接口
	 * 
	 * @param 订单号码
	 */
	private void updateUnVerifyOrders(String orderid) {
		Weisuda weisuda = getWeisuda(PosEnum.Weisuda.getKey());
		String send = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<root>" + "<item>" + "<order_id>" + orderid + "</order_id>" + "</item>" + "</root>";
		check(weisuda, "data", send, WeisudsInterfaceEnum.updateUnVerifyOrders.getValue());
	}

	/**
	 * 包裹签收信息修改通知接口
	 */
	public void updateOrders(CwbOrderWithDeliveryState cwbOrderWithDeliveryState, DmpOrderFlow orderFlow) {
		if (!b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			logger.info("唯速达_04未开启[唯速达]接口");
			return;
		}
		Weisuda weisuda = getWeisuda(PosEnum.Weisuda.getKey());
		logger.info("唯速达_04进入唯速达对接cwb={}", orderFlow.getCwb());
		long customerid = cwbOrderWithDeliveryState.getCwbOrder().getCustomerid();
		Customer customer = getDmpDAO.getCustomer(customerid);
		if (customer.getB2cEnum().equals(getB2cEnumKeys(customer, "vipshop"))) {
			String cwb = cwbOrderWithDeliveryState.getCwbOrder().getCwb();
			try {
				WeisudaCwb weisudaCwb = weisudaDAO.getWeisudaCwbByOrder(cwb);
				if (weisudaCwb != null) {
					if (weisudaCwb.getIsqianshou().equals("0")) {
						DmpCwbOrder cwbOrder = cwbOrderWithDeliveryState.getCwbOrder();
						DmpDeliveryState deliveryState = cwbOrderWithDeliveryState.getDeliveryState();
						String order_id = cwbOrder.getCwb();
						String order_status = "";
						String pay_status = "2";
						String consignee = "";
						Long deliverytime = DateTimeUtil.StringToDate(deliveryState.getDeliverytime()).getTime();
						String opertime = "";
						opertime = (deliverytime == null ? "" : deliverytime / 1000 + "");
						String reason = "";
						String memo = "";
						String paymethod = "";
						if (deliveryState.getDeliverystate() == DeliveryStateEnum.FenZhanZhiLiu.getValue()) {
							return;
						} else if (deliveryState.getDeliverystate() == DeliveryStateEnum.QuanBuTuiHuo.getValue()) {
							order_status = "7";
							reason = cwbOrder.getBackreason();
							consignee = cwbOrder.getConsigneename();
						} else if (deliveryState.getDeliverystate() == DeliveryStateEnum.PeiSongChengGong.getValue()) {
							order_status = "9";
							consignee = deliveryState.getSign_man();
						}
						if (cwbOrder.getPaywayid() == PaytypeEnum.Xianjin.getValue()) {
							pay_status = "2";
						} else if (cwbOrder.getPaywayid() == PaytypeEnum.Pos.getValue()) {
							pay_status = "3";
						} else if (cwbOrder.getPaywayid() == PaytypeEnum.CodPos.getValue()) {
							pay_status = "4";
						}
						String backreason = reason == null ? "" : reason;
						String data = "<root>" + "<item>" + "<order_id>" + order_id + "</order_id>" + "<order_status>" + order_status + "</order_status>" + "<pay_status>" + pay_status
								+ "</pay_status>" + "<consignee>" + consignee + "</consignee>" + "<opertime>" + opertime + "</opertime>" + "<reason>" + backreason + "</reason>" + "<memo>" + memo
								+ "</memo>" + "<paymethod>" + paymethod + "</paymethod>" + "</item>" + "</root>";
						String response = check(weisuda, "data", data, WeisudsInterfaceEnum.updateOrders.getValue());
						if (response.contains("<error><code>")) {
							logger.info("唯速达_04包裹修改信息接口验证失败！userMessage={}", response);
							return;
						}

						UpdateOrders_root roots = (UpdateOrders_root) ObjectUnMarchal.XmltoPOJO(response, new UpdateOrders_root());
						List<String> order_ids = roots.getOrder_id();
						for (String orderid : order_ids) {
							weisudaDAO.updataWeisudaCwbIsqianshou(orderid, "1", "包裹签收信息修改_成功！");
						}
					}
				}
			} catch (Exception e) {
				weisudaDAO.updataWeisudaCwbIsqianshou(cwb, "2", "包裹签收信息修改_失败_异常！");
				logger.info("唯速达_04包裹签收信息修改_失败_异常！Message={}", e.getMessage());
			}
		}
	}

	/**
	 * 站点更新接口
	 */
	public void siteUpdate(@Header("branch") String branch) {
		if (!b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			logger.info("唯速达_05未开启[唯速达]接口");
			return;
		}
		try {
			Weisuda weisuda = getWeisuda(PosEnum.Weisuda.getKey());
			Branch bch = JsonUtil.readValue(branch, Branch.class);
			String data = "<root>" + "<item>" + "<code>" + bch.getBranchid() + "</code>" + "<old_code></old_code>" + "<name>" + bch.getBranchname() + "</name>" + "<province>"
					+ bch.getBranchprovince() + "</province>" + "<city>" + bch.getBranchcity() + "</city>" + "<zone>" + bch.getBrancharea() + "</zone>" + "<password></password>" + "</item>"
					+ "</root>";
			String response = check(weisuda, "data", data, WeisudsInterfaceEnum.siteUpdate.getValue());
			if (response.contains("<error><code>")) {
				logger.info("唯速达_05站点更新接口验证失败！userMessage={}", response);
				return;
			}
			if ("<root></root>".equals(response)) {
				logger.info("唯速达_05站点更新失败！branchid={}", bch.getBranchid());
			} else {

				logger.info("唯速达_05更新成功的站点！{}", response);
			}
		} catch (Exception e) {
			logger.error("唯速达_05更新站点信息出错{}", branch);
		}
	}

	/**
	 * 站点撤销接口
	 */
	/*
	 * public void siteDel(@Header("branchid")String branchid){ if
	 * (!b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
	 * logger.info("未开启[唯速达]接口"); return ; } try { Weisuda weisuda=
	 * getWeisuda(PosEnum.Weisuda.getKey()); String data="<root>" + "<item>" +
	 * "<del_code>"+branchid+"</del_code>" + "<rec_code></rec_code>" + "</item>"
	 * + "</root>"; String response=check(weisuda, "data",
	 * data,WeisudsInterfaceEnum.siteDel.getValue());
	 * if("<root></root>".equals(response)) {
	 * logger.info("站点撤销失败！branchid={}",branchid); } else{
	 * 
	 * logger.info("站点撤销成功！{}",response); } } catch (Exception e) {
	 * logger.error("站点撤销信息出错{}",branchid); } }
	 */
	/**
	 * 快递员信息更新接口
	 */
	public void courierUpdate(@Header("user") String userFlag, @Body() String jsonUser) {

		if (userFlag == null) {
			return;
		}

		if (userFlag.contains("del")) {
			carrierDel(jsonUser);
			return;
		}
		if (!userFlag.contains("update")) {
			return;
		}

		if (!b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			logger.info("唯速达_07未开启[唯速达]接口");
			return;
		}
		try {
			User user = JsonUtil.readValue(jsonUser, User.class);
			String userMessage = "快递员ID:" + user.getUsername() + " 快递员姓名:" + user.getRealname() + " 快递员站点ID" + user.getBranchid() + " 快递员手机号码:" + user.getUsermobile() + " 快递员登陆密码:"
					+ user.getPassword();
			Weisuda weisuda = getWeisuda(PosEnum.Weisuda.getKey());
			String data = "<root>" + "<item>" + "<code>" + user.getUsername() + "</code>" + "<old_code></old_code>" + "<name>" + user.getRealname() + "</name>" + "<site_code>" + user.getBranchid()
					+ "</site_code>" + "<mobile>" + user.getUsermobile() + "</mobile>" + "<password>" + user.getPassword() + "</password>" + "</item>" + "</root>";
			String response = check(weisuda, "data", data, WeisudsInterfaceEnum.courierUpdate.getValue());

			if (response.contains("<error><code>")) {
				logger.info("唯速达_07快递员更新接口验证失败！userMessage={}", response);
				return;
			}

			if ("<root></root>".equals(response)) {
				logger.info("唯速达_07快递员更新失败！userMessage={}", userMessage);
			} else {

				logger.info("唯速达_07快递员更新成功！userMessage={}", userMessage);
			}
		} catch (Exception e) {
			logger.error("唯速达_07快递员更新信息出错 ！jsonUser={}", jsonUser);
		}
	}

	/**
	 * 快递员删除接口
	 */
	public void carrierDel(String jsonUser) {
		if (!b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			logger.info("唯速达_08未开启[唯速达]接口");
			return;
		}
		try {
			User user = JsonUtil.readValue(jsonUser, User.class);
			String userMessage = "被删除的快递员ID:" + user.getUserid() + " 快递员姓名:" + user.getRealname() + " 快递员站点ID" + user.getBranchid() + " 快递员手机号码:" + user.getUsermobile() + " 快递员登陆密码:"
					+ user.getPassword();
			Weisuda weisuda = getWeisuda(PosEnum.Weisuda.getKey());
			String data = "<root>" + "<item>" + "<del_code>" + user.getUsername() + "</del_code>" + "</item>" + "</root>";
			String response = check(weisuda, "data", data, WeisudsInterfaceEnum.carrierDel.getValue());
			if (response.contains("<error><code>")) {
				logger.info("唯速达_08快递员删除接口验证失败！userMessage={}", response);
				return;
			}
			if ("<root></root>".equals(response)) {
				logger.info("唯速达_08快递员删除失败！userMessage={}", userMessage);
			} else {

				logger.info("唯速达_08快递员删除成功！userMessage={}", userMessage);
			}
		} catch (Exception e) {
			logger.error("唯速达_08快递员删除信息出错 ！jsonUser={}", jsonUser);
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
		for (WeisudaCwb data : weisudaCwbs) {
			try {
				String courier_code = data.getCourier_code();
				cwb = data.getCwb();
				String timestamp = System.currentTimeMillis() / 1000 + "";
				String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<root>" + "<item>" + "<courier_code>" + courier_code + "</courier_code>" + "<order_id>" + cwb + "</order_id>"
						+ "<bound_time>" + timestamp + "</bound_time>" + "</item>" + "</root>";
				response = check(weisuda, "data", xml, WeisudsInterfaceEnum.pushOrders.getValue());

				logger.info("唯速达_01快递员绑定接口返回：{},cwb={}", response, cwb);
				if (response.contains("<error><code>")) {
					logger.info("唯速达_01快递员绑定接口验证失败！userMessage={}", response);
					return;
				}
				if (response.contains(cwb)) {
					weisudaDAO.updateWeisuda(cwb, "1", "快递员绑定成功!");
					logger.info("唯速达_01快递员绑定接口成功!{}", response);
				} else {
					if (response.contains("<error>")) {
						Errors error = (Errors) ObjectUnMarchal.XmltoPOJO(response, new Errors());

						weisudaDAO.updateWeisuda(cwb, "2", error.getMsg());
						logger.info("唯速达_01快递员绑定失败!接口数据校验失败 {}", error.getMsg());

					} else {
						weisudaDAO.updateWeisuda(cwb, "2", response);
						logger.info("唯速达_01快递员绑定失败! cwb={},response={}", cwb, response);
					}

				}

			} catch (Exception e) {
				logger.warn("唯速达_01异常：" + e.getMessage() + "cwb=" + cwb + "返回：" + response);
			}
		}

	}

	// 获取配置信息
	public Weisuda getWeisuda(int key) {
		Weisuda et = null;
		String objectMethod = b2ctools.getObjectMethod(key).getJoint_property();
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
		String timestamp = System.currentTimeMillis() / 1000 + "";
		String code = weisuda.getCode();
		String secret = weisuda.getSecret();
		String v = weisuda.getV();
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> mapMd5 = new HashMap<String, String>();
		mapMd5.put(params, value);
		String md5 = createLinkString(mapMd5);
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
		default:
			break;
		}

		String str = RestHttpServiceHanlder.sendHttptoServer(map, url);
		return str;
	}

	public String buliderJson(GetUnVerifyOrders_back_Item item, WeisudaCwb weisudaCwbs) throws Exception {
		String json = "";
		OrderFlowDto dto = new OrderFlowDto();
		dto.setCustid(weisudaCwbs.getId());
		dto.setCwb(weisudaCwbs.getCwb());
		long deliverystate = 0;
		String status = item.getOrder_status();
		if ("9".equals(status)) {
			deliverystate = DeliveryStateEnum.PeiSongChengGong.getValue();

		} else if ("7".equals(status)) {
			deliverystate = DeliveryStateEnum.QuanBuTuiHuo.getValue();
		} else if ("0".equals(status)) {
			deliverystate = DeliveryStateEnum.FenZhanZhiLiu.getValue();
		}
		dto.setDeliverystate(deliverystate + "");
		dto.setExptmsg(item.getReason());
		dto.setFlowordertype(FlowOrderTypeEnum.YiFanKui.getValue() + "");
		dto.setOperatortime(DateTimeUtil.getNowTime());
		dto.setCwbremark(item.getMemo());
		int paytype = 0;
		if ("1".equals(item.getPaymethod())) {
			paytype = PaytypeEnum.Xianjin.getValue();
		} else if ("2".equals(item.getPaymethod())) {
			paytype = PaytypeEnum.Xianjin.getValue();
		} else if ("3".equals(item.getPaymethod())) {
			paytype = PaytypeEnum.Pos.getValue();
		} else if ("4".equals(item.getPaymethod())) {
			paytype = PaytypeEnum.CodPos.getValue();
		} else if ("5".equals(item.getPaymethod())) {
			paytype = PaytypeEnum.Qita.getValue();
		} else if ("6".equals(item.getPaymethod())) {
			paytype = PaytypeEnum.CodPos.getValue();
		}
		dto.setPaytype(paytype);
		dto.setConsignee(item.getConsignee());
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

}
