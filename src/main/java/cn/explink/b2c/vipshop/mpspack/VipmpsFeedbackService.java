package cn.explink.b2c.vipshop.mpspack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.TransflowdataDAO;
import cn.explink.b2c.vipshop.ReaderXMLHandler;
import cn.explink.b2c.vipshop.SOAPHandler;
import cn.explink.b2c.vipshop.VipShop;
import cn.explink.b2c.vipshop.VipShopConfig;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.Transflowdata;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.DateTimeUtil;
import cn.explink.util.JsonUtil;
import cn.explink.util.StringUtil;
import cn.explink.util.MD5.MD5Util;

@Service
public class VipmpsFeedbackService {

	@Autowired
	private B2cTools b2ctools;
	@Autowired
	private B2CDataDAO b2cDataDAO;
	@Autowired
	SOAPHandler soapHandler;
	@Autowired
	ReaderXMLHandler readXMLHandler;

	@Autowired
	TransflowdataDAO transflowdataDAO;
	
	@Autowired
	GetDmpDAO getDmpdao;
	public static List<FlowOrderTypeEnum> floworderList=new ArrayList<FlowOrderTypeEnum>();
	static{
		for(VipmpsFlowEnum e:VipmpsFlowEnum.values()){
			floworderList.add(FlowOrderTypeEnum.getText(e.getFlowordertype()));
		}
	}

	private Logger logger = LoggerFactory.getLogger(VipmpsFeedbackService.class);

	// 获取VipShop配置信息
	public VipShop getVipshop(int key) {
		return this.b2ctools.getObject(key, VipShop.class);
	}

	// 获取vipshop XML Note
	public VipmpsNote getVipShopNote(String jsoncontent) {
		try {
			return JsonUtil.readValue(jsoncontent, VipmpsNote.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	}

	/**
	 * 反馈唯品会订单信息
	 */
	public void feedback_status(int key) {

		VipShop vipshop = this.getVipshop(key); // 获取配置信息
		if (!this.b2ctools.isB2cOpen(key)) {
			this.logger.info("未开vipshop的对接!key={}", key);
			return;
		}
		if(vipshop.getOpenmpspackageflag()==0){
			logger.info("未开启集包模式运单推送接口,key={}",key);
			return;
		}
			
		for(FlowOrderTypeEnum flowEnum:floworderList){
			this.sendTransCwbStatus(vipshop,flowEnum.getValue()); 
		}

	
	}

	/**
	 * 反馈的处理方法
	 * 
	 * @param vipshop
	 * @param vip_code
	 * @param flowordertype
	 */
	private void sendTransCwbStatus(VipShop vipshop, int flowordertype) {
			long calcCount = 0;
			while (true) {
				calcCount++;
				if(calcCount>200){
					logger.warn("当前请求唯品会集包回传接口循环次数已超过200，跳出");
					return;
				}
				String requestXML=null;
				try {
					
					List<Transflowdata> vipshoplist = this.transflowdataDAO.getDataListByFlowStatus(flowordertype,getCustomerIds(vipshop) , vipshop.getSendMaxCount());
					if ((vipshoplist == null) || (vipshoplist.size() == 0)) {
						this.logger.info("当前没有要推送vipshop-集包的数据,flowordertype={}", flowordertype);
						return ;
					}
					String request_time = DateTimeUtil.getNowTime();
					requestXML = this.appendXMLString(vipshop, vipshoplist, flowordertype, request_time);
					if (requestXML == null) {
						return ;
					}
					

					String sign = MD5Util.md5(createMd5LinkString(request_time, vipshop, requestXML)).toLowerCase();
					
					String response_XML = this.soapHandler.httpInvokeWs(vipshop.getTransflowUrl(), VipShopConfig.nameSpace, VipShopConfig.statusBackMethodNameNew, requestXML, sign,VipShopConfig.JIBAO_TYPE);
					
					this.logger.info("vipshop集包返回XML={},flowordertype={}", readXMLHandler.subStringSOAP(ReaderXMLHandler.parseBack(response_XML)), flowordertype);
					
					Map<String, Object> parseMap = this.readXMLHandler.parseGetCwbInfo_VipShopXml(response_XML);
					
					String sys_response_code = StringUtil.nullConvertToEmptyString(parseMap.get("sys_response_code"));
					String sys_response_msg =  StringUtil.nullConvertToEmptyString(parseMap.get("sys_respnose_msg"));


					if (!"S00".equals(sys_response_code)) {
						logger.error("唯品会集包运单返回信息失败，请及时解决!");
						return ;
					}

					this.updateTransflowState(vipshop, flowordertype, parseMap, sys_response_msg);

				} catch (Exception e) {
					this.logger.error("唯品会集包状态反馈发送不可预知的异常:"+requestXML, e);
				}

			}
		

	}

	private String getCustomerIds(VipShop vipshop) {
		String lefengCustomerid=vipshop.getLefengCustomerid()==null||vipshop.getLefengCustomerid().isEmpty()?vipshop.getCustomerids():vipshop.getLefengCustomerid();
		String customerids=vipshop.getCustomerids()+","+lefengCustomerid;
		return customerids;
	}


	private void updateTransflowState(VipShop vipshop, int flowordertype, Map<String, Object> parseMap, String sys_response_msg) {

		List<Map<String, String>> orderlist = (List<Map<String, String>>) parseMap.get("orderlist");
		for (Map<String, String> orderMap : orderlist) {
			String biz_response_code =StringUtil.nullConvertToEmptyString(orderMap.get("biz_response_code"));
			String biz_response_msg =StringUtil.nullConvertToEmptyString(orderMap.get("biz_response_msg"));
			
			int send_b2c_flag = 1; // 默认全部成功
			if(!biz_response_code.equals("B00")){
				send_b2c_flag=2;
			}
			
			String b2cid = parseStrCut(orderMap.get("cust_data_id"));

			this.transflowdataDAO.updateTransflowStatus(Long.valueOf(b2cid), send_b2c_flag, biz_response_msg);
				
		}
	}

	/**
	 * VipShop生成签名字符串
	 */
	public String createMd5LinkString(String request_time, VipShop vipshop, String requestXML) {
		String linkString = vipshop.getPrivate_key() + VipShopConfig.versionNew + request_time + vipshop.getShipper_no();
		StringBuffer sub = new StringBuffer(linkString);
		sub.append(this.getReplace(requestXML));
		return sub.toString();
	}

	// 拼接XML
	private String appendXMLString(VipShop vipshop, List<Transflowdata>  vipshopDataList, int flowordertype, String request_time) {
		StringBuffer sub = new StringBuffer();
		sub.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sub.append("<request>");
		sub.append("<head>");
		sub.append("<version>" + VipShopConfig.versionNew + "</version>");
		sub.append("<request_time>" + request_time + "</request_time>");
		sub.append("<cust_code>" + vipshop.getShipper_no() + "</cust_code>");
		sub.append("</head>");
		sub.append("<traces>");
		for (Transflowdata transflowdata : vipshopDataList) {
			String jsoncontent = transflowdata.getJsoncontent();
			VipmpsNote note = this.getVipShopNote(jsoncontent);
			if(note== null){
				continue;
			}
			
			sub.append("<trace>");
			sub.append("<cust_data_id>" + parseStrAdd(String.valueOf(transflowdata.getId())) + "</cust_data_id>");
			sub.append("<order_sn>" + note.getOrder_sn() + "</order_sn>");
			sub.append("<box_id>" + note.getBox_id() + "</box_id>");
			sub.append("<order_status>" + note.getOrder_status() + "</order_status>");
			sub.append("<order_status_info>" + note.getOrder_status_info() + "</order_status_info>");
			sub.append("<current_city_name>" + note.getCurrent_city_name() + "</current_city_name>");
			sub.append("<order_status_time>" + note.getOrder_status_time() + "</order_status_time>");
			sub.append("</trace>");

			
		}
		sub.append("</traces>");
		sub.append("</request>");
		String returnStr = sub.toString();
		if (returnStr.contains("<traces></traces>")) {
			return null;
		}

		this.logger.info("请求vipshop集包:flowordertype={},推送vipshop-XML：{}",flowordertype, returnStr);
		return returnStr;
	}
	
	
	
	
	
		
		
		

		


	/**
	 * 替换掉json格式的节点
	 * 
	 * @return
	 */
	private String getReplace(String xml) {

		xml = xml.substring(xml.indexOf("<traces>") + 8, xml.indexOf("</traces>"));
		xml = xml.replaceAll("<trace>", "").replaceAll("</trace>", "");
		xml = xml.replaceAll("<cust_data_id>", "").replaceAll("</cust_data_id>", "")
				.replaceAll("<order_sn>", "").replaceAll("</order_sn>", "")
				.replaceAll("<box_id>", "").replaceAll("</box_id>", "")
				.replaceAll("<order_status>", "").replaceAll("</order_status>", "")
				.replaceAll("<order_status_info>", "").replaceAll("</order_status_info>", "")
				.replaceAll("<current_city_name>", "").replaceAll("</current_city_name>", "")
				.replaceAll("<order_status_time>", "").replaceAll("</order_status_time>", "")
				.replaceAll("<is_unpacked>0</is_unpacked>", "");
		
		return xml;
	}

	

	private static String parseStrAdd(String str) {
		if (str.contains("_temp")) {
			return str;
		}
		return str + "_8";
	}

	private static String parseStrCut(String str) {
		if (str.contains("_8")) {
			return str.substring(0, str.indexOf("_8"));
		}
		if (str.contains("_1")) {
			return str.substring(0, str.indexOf("_1"));
		}
		if (str.contains("_temp")) {
			return str.substring(0, str.indexOf("_temp"));
		}
		return str;
	}

	


}
