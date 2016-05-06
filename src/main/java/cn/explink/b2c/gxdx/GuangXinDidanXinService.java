/**
 * 
 */
package cn.explink.b2c.gxdx;

import java.io.IOException;
import java.util.List;

import net.sf.json.JSONObject;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.gxdx.xmldto.returnXml;
import cn.explink.b2c.gxdx.xmldto.util.BeanToXml;
import cn.explink.b2c.gxdx.xmldto.util.XmlToBean;
import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.b2c.tools.JointEntity;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.B2CData;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.RestHttpServiceHanlder;

/**
 * @ClassName: GuangXinDidanXinService
 * @Description: TODO
 * @Author: 王强
 * @Date: 2015年11月16日上午10:58:43
 */
@Service
public class GuangXinDidanXinService {
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	B2CDataDAO b2cDataDAO;
	@Autowired
	private GetDmpDAO getdmpDAO;
	private Logger logger = LoggerFactory.getLogger(GuangXinDidanXinService.class);

	
	public static String filterGuangXinDidanXinFlowEnum(long flowordertype, long deliverystate) {
		if (flowordertype != FlowOrderTypeEnum.YiShenHe.getValue()) {
			for (GxDxOrderTypeEnum TEnum : GxDxOrderTypeEnum.values()) {
				if(TEnum.getFlowordertype()!=FlowOrderTypeEnum.YiShenHe.getValue()){
					if (flowordertype == TEnum.getFlowordertype()) {
						return TEnum.getState();// 请求指令
					}
				}
			}
		}
		if(flowordertype == FlowOrderTypeEnum.YiShenHe.getValue()){
			if(deliverystate == GxDxOrderTypeEnum.SC03.getDeliveryState()){
				return GxDxOrderTypeEnum.SC03.getState();
			}
			if(deliverystate == GxDxOrderTypeEnum.SC04.getDeliveryState()){
				return GxDxOrderTypeEnum.SC04.getState();
			}
			if(deliverystate == GxDxOrderTypeEnum.SC05.getDeliveryState()){
				return GxDxOrderTypeEnum.SC05.getState();
			}
			if(deliverystate == GxDxOrderTypeEnum.SC06_1.getDeliveryState()
			||deliverystate == GxDxOrderTypeEnum.SC06_2.getDeliveryState()
				){
				return GxDxOrderTypeEnum.SC06_1.getState();
			}
			if(deliverystate == GxDxOrderTypeEnum.SC07.getDeliveryState()){
				return GxDxOrderTypeEnum.SC07.getState();
			}
			if(deliverystate == GxDxOrderTypeEnum.SC08.getDeliveryState()){
				return GxDxOrderTypeEnum.SC08.getState();
			}
			if(deliverystate == GxDxOrderTypeEnum.SC09.getDeliveryState()){
				return GxDxOrderTypeEnum.SC09.getState();
			}
			if(deliverystate == GxDxOrderTypeEnum.SC10.getDeliveryState()){
				return GxDxOrderTypeEnum.SC10.getState();
			}
		}
		return null;
	}

	public GxDx getGxDx(int key) {
		GxDx gxDx = null;
		if (this.getObjectMethod(key) != null) {
			JSONObject jsonObj = JSONObject.fromObject(this.getObjectMethod(key));
			gxDx = (GxDx) JSONObject.toBean(jsonObj, GxDx.class);
		} else {
			gxDx = new GxDx();
		}
		return gxDx;
	}

	/**
	 * 获取广州通路配置信息的接口
	 *
	 * @param key
	 * @return
	 */
	private String getObjectMethod(int key) {
		try {
			JointEntity obj = this.getdmpDAO.getJointEntity(key);
			return obj.getJoint_property();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 将订单状态返回给广信电信
	 */
	public void feedback_status() {

		GxDx gxDx = this.getGxDx(B2cEnum.GuangXinDianXin.getKey());
		if (!this.b2ctools.isB2cOpen(B2cEnum.GuangXinDianXin.getKey())) {
			this.logger.info("未开0广信电信0的对接!");
			return;
		}
		this.sendCwbStatus_To_gxDx(gxDx, FlowOrderTypeEnum.RuKu.getValue());// 入库
		this.sendCwbStatus_To_gxDx(gxDx, FlowOrderTypeEnum.ChuKuSaoMiao.getValue());//出库
		this.sendCwbStatus_To_gxDx(gxDx, FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue());//分站到货
		this.sendCwbStatus_To_gxDx(gxDx, FlowOrderTypeEnum.FenZhanLingHuo.getValue());// 配送中
		this.sendCwbStatus_To_gxDx(gxDx, FlowOrderTypeEnum.YiShenHe.getValue());
		
	}

	/**
	 * 状态反馈接口开始(广信电信)
	 *
	 * @param smile
	 */
	private void sendCwbStatus_To_gxDx(GxDx gxDx, long flowordertype) {
		try {

			int i = 0;
			while (true) {
				//查询出需要推送给广信电信的订单
				List<B2CData> gxDxDataList = this.b2cDataDAO.getDataListByFlowStatus(flowordertype, gxDx.getCustomerid(), gxDx.getMaxCount());
				i++;
				if (i > 100) {
					this.logger.warn("查询0广信电信0状态反馈已经超过100次循环，可能存在程序未知异常,请及时查询并处理!");
					return;
				}

				if ((gxDxDataList == null) || (gxDxDataList.size() == 0)) {
					this.logger.info("当前没有要推送0广信电信0的数据");
					return;
				}
				this.DealWithBuildXMLAndSending(gxDx, gxDxDataList);
			}

		} catch (Exception e) {
			String errorinfo = "发送0广信电信0状态反馈遇到不可预知的异常";
			this.logger.error(errorinfo, e);
		}

	}

	/**
	 * @param 发送xml信息
	 * @param gxDxDataList
	 * @throws Exception
	 */
	private void DealWithBuildXMLAndSending(GxDx gxDx, List<B2CData> gxDxDataList) {
		try {
			for (B2CData data : gxDxDataList) {
				GoodsState goodsState = new GoodsState();
				OrderStatesList orderStates = new OrderStatesList();
				goodsState.setLogisticProviderID(gxDx.getLogisticProviderID());
				//将json 转换为GoodsState 对象
				String jsoncontent = data.getJsoncontent();
				 orderStates = getGuangXinDianXinMethod(jsoncontent);
				
				goodsState.setOrderStates(orderStates);
				
				String requestXml = BeanToXml.toXml(goodsState);
				logger.info("请求广信电信参数为:{}", requestXml);
				
				String response_xml = RestHttpServiceHanlder.sendHttptoServer(requestXml, gxDx.getRequestUrl());
				logger.info("状态反馈广信电信[返回信息]-XML={}", response_xml);
				//将返回的xml 转换成实体
				returnXml returnxml = (returnXml) XmlToBean.toBean(response_xml, returnXml.class);
				
				int send_b2c_flag=1; 
				if(returnxml.getSuccess().equals("True")){
					send_b2c_flag=1; 
				}else{
					send_b2c_flag=2;
				}
				b2cDataDAO.updateB2cIdSQLResponseStatus(data.getB2cid(), send_b2c_flag,returnxml.getRemark());
			}
		}catch (Exception e) {
			logger.error("调用0广信电信0服务器异常"+e.getMessage(),e);
		}
	}

	public OrderStatesList getGuangXinDianXinMethod(String jsoncontent) throws JsonParseException, JsonMappingException, IOException {
		return JacksonMapper.getInstance().readValue(jsoncontent, OrderStatesList.class);
	}


}
