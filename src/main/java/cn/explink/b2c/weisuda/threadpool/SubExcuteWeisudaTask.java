package cn.explink.b2c.weisuda.threadpool;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.explink.b2c.explink.xmldto.OrderFlowDto;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.b2c.weisuda.Weisuda;
import cn.explink.b2c.weisuda.WeisudaCwb;
import cn.explink.b2c.weisuda.WeisudaService;
import cn.explink.b2c.weisuda.WeisudsInterfaceEnum;
import cn.explink.b2c.weisuda.xml.GetUnVerifyOrders_back_Item;
import cn.explink.b2c.weisuda.xml.Getback_Item;
import cn.explink.b2c.weisuda.xml.Goods;
import cn.explink.b2c.weisuda.xml.Item;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.WeisudaDAO;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.enumutil.PaytypeEnum;
import cn.explink.util.DateTimeUtil;
import cn.explink.util.RestHttpServiceHanlder;
import cn.explink.util.MD5.MD5Util;

/**
 *  显示
 * @author Administrator
 *
 */

public class SubExcuteWeisudaTask implements Runnable{
	
	
	private Logger logger = LoggerFactory.getLogger(SubExcuteWeisudaTask.class);
	
	private WeisudaDAO weisudaDAO;
	private Weisuda weisuda;
	private GetDmpDAO getDmpDAO;
	private CyclicBarrier  barrier;
	
	private List<GetUnVerifyOrders_back_Item> tasklist;
	public SubExcuteWeisudaTask(List<GetUnVerifyOrders_back_Item> list,WeisudaDAO weisudaDAO,Weisuda weisuda,GetDmpDAO getDmpDAO,CyclicBarrier  barrier){
		this.tasklist=list;
		this.weisudaDAO=weisudaDAO;
		this.weisuda=weisuda;
		this.getDmpDAO=getDmpDAO;
		this.barrier=barrier;
	}
	
	@Override
	public void run() {
		if(tasklist==null || tasklist.size() == 0){
			try {
				barrier.await();
			} catch (InterruptedException e) {  
			} catch (BrokenBarrierException e) { 
			}
			return ;
		}
		
		//Modified by leoliao at 2016-04-01 改为批量反馈给品骏达方式
		Map<String, String> mapResult = new HashMap<String, String>();
		
		for (GetUnVerifyOrders_back_Item item : tasklist) {
			String cwb=item.getOrder_id();
			try {
				WeisudaCwb weisudaCwbs = this.weisudaDAO.getWeisudaCwbIstuisong(cwb);
				if(weisudaCwbs == null){
					logger.info("唯速达_03APP包裹签收信息同步：接口表express_b2cdata_weisuda没有订单号为{}的订单", cwb);
					
					this.updateUnVerifyOrders(item.getOrder_id(),weisuda);
					continue;
				}
				
				String json = this.buliderJson(item, cwb);
				
				long timeA = System.currentTimeMillis();
				
				//请求DMP进行签收处理
				String result = sendDmpFlow(json);
				
				long timeB = System.currentTimeMillis();			
				this.logger.info("执行订单cwb="+cwb+"签收,签收处理结果:"+result+",信息同步耗时:{}秒，", ((timeB - timeA) / 1000));
				
				mapResult.put(cwb, result);
				//dealWithDmpFeedbackResult(item, result,weisuda);
			} catch (Exception e) {
				logger.error("唯速达签收结果处理单个数据异常"+cwb,e);
			}
		}
		
		try{
			//批量反馈签收处理结果给品骏达
			this.dealWithDmpFeedbackResult(mapResult, weisuda);			
		}catch(Exception ex){
			logger.error("批量反馈唯速达签收结果给品骏达异常", ex);
		}
		
		try {
			barrier.await();
		} catch (InterruptedException e) {  
		} catch (BrokenBarrierException e) { 
		}
		
	}




	private String sendDmpFlow(String json) {
		String result = this.getDmpDAO.requestDMPOrderService_Weisuda(json);
		this.logger.info("唯速达_02请求dmp-json={}", json);
		return result;
	}
	
	private void updateUnVerifyOrders(String orderid,Weisuda weisuda) {
		String send = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<root>" + "<item>" + "<order_id>" + orderid + "</order_id>" + "</item>" + "</root>";
		this.logger.info("唯速达_03APP包裹签收信息同步结果 发送报文,userMessage={}", send);
		String response = this.check(weisuda, "data", send, WeisudsInterfaceEnum.updateUnVerifyOrders.getValue());
		this.logger.info("唯速达_03APP包裹签收信息同步结果反馈接口返回报文,response={}", response);
	}
	
	public String buliderJson(Item item, String cwb) throws Exception {
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
			dto.setExchangetpstranscwb(itemps.getTransport_no());
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
		
		/**Added by leoliao at 2016-04-01  添加新的支付方式
		 * 品骏达现有支付方式：1-线上已支付，2-现金，3-POS支付，11-微信扫码，12-支付宝扫码，13-工行MPOS,14-快刷,
		                      15-pos快刷支付,16-通联支付，17-唯宝支付，18-支付宝主动扫码, 21-唯宝支付
                     后期计划添加支付方式： 20-微信主动扫码,19-银商支付
           DMP支付方式：Xianjin(1, "现金"), Pos(2, "POS刷卡"), Zhipiao(3, "支票"), Qita(4, "其他"), CodPos(5, "COD扫码支付");
		 */
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
		} else if ("17".equals(item.getPaymethod()) || "21".equals(item.getPaymethod())) {
			payremark = "唯宝支付";
			paytype = PaytypeEnum.CodPos.getValue();
		} else if ("18".equals(item.getPaymethod())) {
			payremark = "支付宝主动扫码";
			paytype = PaytypeEnum.CodPos.getValue();
		} else if ("19".equals(item.getPaymethod())) {
			payremark = "银商支付";
			paytype = PaytypeEnum.Pos.getValue();
		} else if ("20".equals(item.getPaymethod())) {
			payremark = "微信主动扫码";
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
	
	/**
	 * 处理dmp系统回传过来的结果
	 * @param item
	 * @param result
	 */
	private void dealWithDmpFeedbackResult(GetUnVerifyOrders_back_Item item,String result,Weisuda weisuda) {
		if ("SUCCESS".equals(result)) {
			this.weisudaDAO.updataWeisudaCwbIsqianshou(item.getOrder_id(), "1", "包裹信息通过_手机_签收成功");
			this.updateUnVerifyOrders(item.getOrder_id(),weisuda);
		} else if (result.contains("处理唯速达反馈请求异") && result.contains("不允许进行反馈")) {
			this.weisudaDAO.updataWeisudaCwbIsqianshou(item.getOrder_id(), "1", "包裹信息已经通过_其它方式_签收成功");
			this.updateUnVerifyOrders(item.getOrder_id(),weisuda);
		}else {
			this.logger.info("唯速达_02请求dmp唯速达信息异常{},cwb={}", result, item.getOrder_id());
			this.weisudaDAO.updataWeisudaCwbIsqianshou(item.getOrder_id(), "2", result);
			this.updateUnVerifyOrders(item.getOrder_id(),weisuda);
		}
	}
	
	
	/**
	 * 使用批量方式把签收信息同步结果反馈给品骏达
	 * @author leo01.liao
	 * @param mapResult key-订单号 value-DMP处理签收结果
	 * @param weisuda
	 */
	private void dealWithDmpFeedbackResult(Map<String, String> mapResult, Weisuda weisuda) {
		logger.info("使用批量方式把签收信息同步结果反馈给品骏达,mapResult={}", mapResult);
		
		if(mapResult == null || mapResult.isEmpty()){
			return;
		}
		
		//DMP签收成功或已经签收的订单号
		List<String> listOrderIdSuccess = new ArrayList<String>();
		
		Iterator<String> keys = mapResult.keySet().iterator();
		while(keys.hasNext()){
			String orderId = keys.next();
			String result  = mapResult.get(orderId);
			
			if ("SUCCESS".equals(result)) {
				this.weisudaDAO.updataWeisudaCwbIsqianshou(orderId, "1", "包裹信息通过_手机_签收成功");
				listOrderIdSuccess.add(orderId);
			} else if (result.contains("处理唯速达反馈请求异") && result.contains("不允许进行反馈")) {
				this.weisudaDAO.updataWeisudaCwbIsqianshou(orderId, "1", "包裹信息已经通过_其它方式_签收成功");
				listOrderIdSuccess.add(orderId);
			}else {
				this.logger.info("唯速达_02请求dmp唯速达信息异常{},cwb={}", result, orderId);
				this.weisudaDAO.updataWeisudaCwbIsqianshou(orderId, "2", result);
				
				//Added by leoliao at 2016-08-23 品骏达要求特别处理：上门退外单反馈同步成功信息
				if("处理唯速达反馈请求异常:上门退类型的订单,不允许反馈为配送成功".equals(result)){
					listOrderIdSuccess.add(orderId);
				}
				//Added end
			}
		}
		
		//DMP签收成功或已经签收的需要反馈给品骏达
		if(listOrderIdSuccess.size() > 0){
			long timeA = System.currentTimeMillis();
			
			this.updateUnVerifyOrders(listOrderIdSuccess, weisuda);
			
			long timeB = System.currentTimeMillis();			
			this.logger.info("执行唯速达_03APP包裹签收信息同步结果给品骏达耗时:{}秒，", ((timeB - timeA) / 1000));
		}
	}
	
	/**
	 * 使用批量方式把签收信息同步结果反馈给品骏达
	 * @author leo01.liao
	 * @param listOrderId 订单号列表
	 * @param weisuda
	 */
	private void updateUnVerifyOrders(List<String> listOrderId, Weisuda weisuda) {
		if(listOrderId == null || listOrderId.isEmpty()){
			return;
		}
		
		StringBuffer sbSend = new StringBuffer();
		sbSend.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sbSend.append("<root>");
		for(String orderId : listOrderId){
			sbSend.append("<item>");
			sbSend.append("<order_id>");
			sbSend.append(orderId);
			sbSend.append("</order_id>");			
			sbSend.append("</item>");
		}		
		sbSend.append("</root>");
		
		this.logger.info("[批量发送]唯速达_03APP包裹签收信息同步结果 发送报文,userMessage={}", sbSend);
		String response = this.check(weisuda, "data", sbSend.toString(), WeisudsInterfaceEnum.updateUnVerifyOrders.getValue());
		this.logger.info("[批量发送]唯速达_03APP包裹签收信息同步结果反馈接口返回报文,response={}", response);
	}
}
