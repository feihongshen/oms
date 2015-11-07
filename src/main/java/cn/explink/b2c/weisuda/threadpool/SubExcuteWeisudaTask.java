package cn.explink.b2c.weisuda.threadpool;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	
	private Logger logger = LoggerFactory.getLogger(WeisudaExcutorService.class);
	
	private WeisudaDAO weisudaDAO;
	private Weisuda weisuda;
	private GetDmpDAO getDmpDAO;
	
	private List<GetUnVerifyOrders_back_Item> tasklist;
	public SubExcuteWeisudaTask(List<GetUnVerifyOrders_back_Item> list,WeisudaDAO weisudaDAO,Weisuda weisuda,GetDmpDAO getDmpDAO){
		this.tasklist=list;
		this.weisudaDAO=weisudaDAO;
		this.weisuda=weisuda;
		this.getDmpDAO=getDmpDAO;
	}
	
	
	@Override
	public void run() {
		if(tasklist==null || tasklist.size() == 0){
			return ;
		}
		
		for (GetUnVerifyOrders_back_Item item : tasklist) {
			String cwb="";
			try {
				WeisudaCwb weisudaCwbs = this.weisudaDAO.getWeisudaCwbIstuisong(item.getOrder_id());
				if (weisudaCwbs == null) {
					this.updateUnVerifyOrders(item.getOrder_id(),weisuda);
					continue;
				}
				cwb=weisudaCwbs.getCwb();
				
				String json = this.buliderJson(item, weisudaCwbs);

				this.logger.info("唯速达_02请求dmp-json={}", json);

				String result = this.getDmpDAO.requestDMPOrderService_Weisuda(json);

				dealWithDmpFeedbackResult(item, result,weisuda);
			} catch (Exception e) {
				logger.error("唯速达签收结果处理单个数据异常"+cwb,e);
			}
			
			
		}
		
		
	}
	
	private void updateUnVerifyOrders(String orderid,Weisuda weisuda) {
		String send = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<root>" + "<item>" + "<order_id>" + orderid + "</order_id>" + "</item>" + "</root>";
		this.logger.info("唯速达_03APP包裹签收信息同步结果 发送报文,userMessage={}", send);
		String response = this.check(weisuda, "data", send, WeisudsInterfaceEnum.updateUnVerifyOrders.getValue());
		this.logger.info("唯速达_03APP包裹签收信息同步结果反馈接口返回报文,response={}", response);
	}
	
	public String buliderJson(Item item, WeisudaCwb weisudaCwbs) throws Exception {
		GetUnVerifyOrders_back_Item itemps = new GetUnVerifyOrders_back_Item();
		Getback_Item itemsmt = new Getback_Item();
		String json = "";
		OrderFlowDto dto = new OrderFlowDto();
		dto.setCustid(weisudaCwbs.getId());
		dto.setCwb(weisudaCwbs.getCwb());
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
	
}
