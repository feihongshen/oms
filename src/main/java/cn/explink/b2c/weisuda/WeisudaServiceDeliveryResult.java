package cn.explink.b2c.weisuda;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.CacheBaseListener;
import cn.explink.b2c.weisuda.threadpool.WeisudaExcutorService;
import cn.explink.b2c.weisuda.xml.ObjectUnMarchal;
import cn.explink.b2c.weisuda.xml.RootPS;
import cn.explink.dao.BranchDAO;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.WeisudaDAO;
import cn.explink.enumutil.pos.PosEnum;
import cn.explink.util.RestHttpServiceHanlder;
import cn.explink.util.MD5.MD5Util;

@Service
public class WeisudaServiceDeliveryResult {
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
	WeisudaExcutorService weisudaExcutorService;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());


	/**
	 * APP包裹签收信息同步接口
	 */
	
	
	public void getDeliveryResult() {
		if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			this.logger.info("唯速达_02未开启[唯速达]接口");
			return;
		}
		Weisuda weisuda = this.getWeisuda(PosEnum.Weisuda.getKey());
		
		/**Commented by leoliao at 2016-04-01 全部统一使用批量同步方式
		if(weisuda.getOpenbatchflag()==0){ //关闭批量之后退出
			return;
		}*/
		
		int count = weisuda.getCount().length() > 0 ? Integer.parseInt(weisuda.getCount()) : 1;
		for (int i = 0; i < count; i++) {
			excuteDeliveryResult(weisuda);
		}
		
		
	}


	private void excuteDeliveryResult(Weisuda weisuda) {
		String response = this.check(weisuda, "nums", weisuda.getNums(), WeisudsInterfaceEnum.getUnVerifyOrders.getValue());
		
		this.logger.info("唯速达_02_APP包裹签收信息同步接口下载数据,{}", response);
		
		if(response==null){
			this.logger.info("唯速达_02_APP包裹签收信息同步接口下载数据为空");
			return;
		}
		
		if (response.contains("<error><code>")) {
			this.logger.info("唯速达_02_APP包裹签收信息同步接口下载数据异常，<error><code>");
			return;
		}
		if (!(response.contains("<root>") && response.contains("<courier_code>"))) {
			this.logger.info("唯速达_02返回订单失败！{}", response);
			return;
		}
		
		try {
			
			RootPS back_Root = (RootPS) ObjectUnMarchal.XmltoPOJO(response, new RootPS());
			excuteDeliveryResult(back_Root);
		
		}catch (Exception e) {
			this.logger.error("唯速达_02请求dmp唯速达信息异常" + response, e);
		}
	}


	private void excuteDeliveryResult(RootPS back_Root) throws Exception {
		
		if(back_Root==null||back_Root.getItem().size()==0){
			return ;
		}
		weisudaExcutorService.excuteRequestDmp(back_Root.getItem(),weisudaDAO,this.getWeisuda(PosEnum.Weisuda.getKey()),this.getDmpDAO);

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
		
		

}
