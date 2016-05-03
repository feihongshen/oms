package cn.explink.b2c.weisuda;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import net.sf.json.JSONObject;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.CacheBaseListener;
import cn.explink.b2c.weisuda.xml.ObjectUnMarchal;
import cn.explink.b2c.weisuda.xml.bound.RespRootOrder;
import cn.explink.b2c.weisuda.xml.bound.RespRootOrder.ErrorItem;
import cn.explink.dao.BranchDAO;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.WeisudaDAO;
import cn.explink.domain.SystemInstall;
import cn.explink.enumutil.CwbOrderTypeIdEnum;
import cn.explink.enumutil.pos.PosEnum;
import cn.explink.util.DateTimeUtil;
import cn.explink.util.RestHttpServiceHanlder;
import cn.explink.util.MD5.MD5Util;

@Service
public class WeisudaServiceExtends {
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

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	

	

	/**
	 * 订单与快递员绑定关系同步接口
	 * 批量推送
	 */

	public void boundsDeliveryToApp(boolean isRepeat) {
		if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			this.logger.info("唯速达_01未开启[唯速达]接口");
			return;
		}
		
		boundDeliveryAppMethod(CwbOrderTypeIdEnum.Peisong.getValue(),WeisudsInterfaceEnum.pushOrders.getValue(),"唯速达_01",isRepeat);
		boundDeliveryAppMethod(CwbOrderTypeIdEnum.Shangmentui.getValue(),WeisudsInterfaceEnum.getback_boundOrders.getValue(),"唯速达_10",isRepeat);
		boundDeliveryAppMethod(CwbOrderTypeIdEnum.OXO.getValue(),WeisudsInterfaceEnum.pushOrders.getValue(),"唯速达_10",isRepeat);

	}

	private void boundDeliveryAppMethod(long cwbordertypeid,int urlFlag,String upflagString, boolean isRepeat ) {
		try {
			Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());

			int maxBounds = weisuda.getMaxBoundCount()==0?100:weisuda.getMaxBoundCount();
			
			//Added by leoliao at 2016-03-08 改为一次获取需要发送的订单，然后分批发送。
			int cntLoop = 30;
			
			List<WeisudaCwb> boundList = null;
			if (!isRepeat) {
				//Added by leoliao at 2016-05-04 加上过滤已签收的条件,即已签收的不再发小件员绑定关系给品骏达
				boundList = this.weisudaDAO.getBoundWeisudaCwbs("0", cwbordertypeid, (cntLoop * maxBounds), 0, "0");
				//boundList = this.weisudaDAO.getBoundWeisudaCwbs("0", cwbordertypeid, (cntLoop * maxBounds), 0);
				if ((boundList == null) || (boundList.size() == 0)) {
					this.logger.info("唯速达_01当前没有要推送0唯速达0的数据");
					return;
				}
			} else {
				boundList = this.weisudaDAO.getBoundWeisudaCwbsRepeat("2", cwbordertypeid, (cntLoop * maxBounds), 0);
				if ((boundList == null) || (boundList.size() == 0)) {
					this.logger.info("重推唯速达_01当前没有要推送0唯速达0的数据");
					return;
				}
			}

			int total = boundList.size();
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
				
				List<WeisudaCwb> subList = boundList.subList(fromIdx, toIdx);				
				this.DealWithBuildXMLAndSending(subList, weisuda, urlFlag, upflagString, isRepeat);
				
				k++;
			}
			//Added end
			
			/**Commented by leoliao at 2016-03-08
			int i = 0;
			while (true) {
				List<WeisudaCwb> boundList = this.weisudaDAO.getBoundWeisudaCwbs("0",cwbordertypeid,maxBounds,0);
				i++;
				if (i > 100) {
					String warning = "查询0唯速达0状态反馈已经超过100次循环，可能存在程序未知异常,请及时查询并处理!";
					this.logger.warn(warning);
					return;
				}

				if ((boundList == null) || (boundList.size() == 0)) {
					this.logger.info("唯速达_01当前没有要推送0唯速达0的数据");
					return;
				}
				
				this.DealWithBuildXMLAndSending(boundList, weisuda,urlFlag,upflagString);

			}
			*/

		} catch (Exception e) {
			String errorinfo = "唯速达_01发送0唯速达0状态反馈遇到不可预知的异常";
			this.logger.error(errorinfo, e);
		}
	}
	
	private void DealWithBuildXMLAndSending(List<WeisudaCwb> boundList, Weisuda weisuda,int urlFlag,String upflagString, boolean isRepeat) {
		try {
		
			String response = "";
			int version = this.GetWeisuda_Version();
			
			StringBuffer sub=new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			sub.append("<root>");
			for (WeisudaCwb data : boundList) {
				buildBoundXml(sub, data);
			}
			sub.append("</root>");
			
			this.logger.info(upflagString + "快递员批量绑定接口接口发送报文,userMessage={}", sub.toString());
			response = this.check(weisuda, "data", sub.toString(), urlFlag);
			this.logger.info(upflagString + "快递员批量绑定接口返回：{}", response);
			updateDeliveryUserBound(response, upflagString, version,boundList, isRepeat );
		} catch (Exception e) {
			logger.error("唯速达批量绑定接口发生异常",e);
		} 

	}

	private void buildBoundXml(StringBuffer sub, WeisudaCwb data) {
		String courier_code = data.getCourier_code();
		String cwb = data.getCwb();
		//Date datetime = DateTimeUtil.formatToDate(data.getOperationTime());
		//Added by leoliao at 2016-04-22 改为24小时制
		Date datetime = DateTimeUtil.formatToDate(data.getOperationTime(), "yyyy-MM-dd HH:mm:ss"); 
		String timestamp = (datetime.getTime() / 1000L) + "";

		sub.append("<item>" 
					+ "<courier_code>" + courier_code.toUpperCase() + "</courier_code>" 
					+ "<order_id>" + cwb+ "</order_id>" 
					+ "<bound_time>" + timestamp + "</bound_time>" 
				+ "</item>");
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
	
	private void updateDeliveryUserBound(String response,
			String upflagString, int version,List<WeisudaCwb> boundList, boolean isRepeat ) throws JAXBException,
			UnsupportedEncodingException {
		
		RespRootOrder respOrders=(RespRootOrder) ObjectUnMarchal.XmltoPOJO(response, new RespRootOrder());
		List<String> orderIds = respOrders.getOrder_id();
		String cwbs = "";
		if (respOrders != null && orderIds != null) {
			for (String orderId : orderIds) {
				cwbs += "'" + orderId + "',";
			}
		}
		
		if (cwbs.length() > 0) {
			cwbs = cwbs.substring(0, cwbs.length() - 1);
		}
		
	    // 更新成功的
		if (cwbs.length() > 0) {
			this.weisudaDAO.updateBoundState(cwbs, "1", "成功");
		}
		// 更新失败的
		List<ErrorItem> errorList = respOrders.getErrors();
		if (!CollectionUtils.isEmpty(errorList)) {
			String failCwbs = null;
			for (ErrorItem errorItem : errorList) {
				failCwbs = "'" +errorItem.getOrderId()+ "'";
				if (!isRepeat) {
					this.weisudaDAO.updateBoundState(failCwbs, "2", errorItem.getErrorMsg());
				} else {
					this.weisudaDAO.updateBoundStateRepeat(failCwbs, "2", errorItem.getErrorMsg());
				}
			}
		}
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

}
