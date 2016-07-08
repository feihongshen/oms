package cn.explink.b2c.telecomsc;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.cwbsearch.B2cDatasearchService;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.BuildB2cDataMaster;
import cn.explink.b2c.tools.CacheBaseListener;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.Customer;
import cn.explink.enumutil.CwbOrderTypeIdEnum;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.jms.dto.CwbOrderWithDeliveryState;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpOrderFlow;

@Service
public class TelecomJsonService {
	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;
	@Autowired
	BuildB2cDataMaster buildB2cDataMaster;
	@Autowired
	B2cDatasearchService b2cDatasearchService;
	@Autowired
	CacheBaseListener cacheBaseListener;
	private Logger logger = LoggerFactory.getLogger(TelecomJsonService.class);
	private ObjectMapper objectMapper = JacksonMapper.getInstance();

	/**
	 * 封装为Json格式的字符串，用于B2C对接。
	 * 
	 * @param cwbOrder
	 * @param flowOrdertype
	 * @return
	 */
	public String orderToJson(CwbOrderWithDeliveryState cwbOrderWothDeliverystate, DmpOrderFlow orderFlow, long flowOrdertype) throws Exception {

		logger.info("telecomToJson-执行了B2C对接状态的封装，cwb={},flowordertype={}", orderFlow.getCwb(), flowOrdertype);

		long delivery_state = cwbOrderWothDeliverystate.getDeliveryState() == null ? 0 : cwbOrderWothDeliverystate.getDeliveryState().getDeliverystate(); // 反馈状态
		// ObjectMapper objectMapper=JacksonMapper.getInstance();

		Customer customer = cacheBaseListener.getCustomer(cwbOrderWothDeliverystate.getCwbOrder().getCustomerid());

		if (customer == null) {
			logger.error("获取customer对象为空，return，当前订单号=" + orderFlow.getCwb() + ",customerid=" + cwbOrderWothDeliverystate.getCwbOrder().getCustomerid() + ",flowOrdertype=" + flowOrdertype);
			return null;
		}
		if (customer != null && customer.getB2cEnum() == null) {
			logger.warn("未设置对接，customername=" + customer.getCustomername());
			return null;
		}

		if (customer.getB2cEnum().equals(String.valueOf(B2cEnum.Telecomshop.getKey()))) {
			buildB2cDataMaster.getBuildTelecomshopB2cData().BuildWanXiangMethod(orderFlow, flowOrdertype, cwbOrderWothDeliverystate.getCwbOrder(), cwbOrderWothDeliverystate.getDeliveryState(),
					delivery_state, objectMapper);
		}

		if (customer.getB2cEnum().equals(String.valueOf(B2cEnum.Wanxiang.getKey()))) {
			return buildB2cDataMaster.getBuildWanxiangB2cData().BuildWanXiangMethod(orderFlow, flowOrdertype, cwbOrderWothDeliverystate.getCwbOrder(), cwbOrderWothDeliverystate.getDeliveryState(),
					delivery_state, objectMapper);
		}
		if(customer.getB2cEnum().equals(String.valueOf(B2cEnum.meilinkai.getKey()))){
			return this.buildB2cDataMaster.getBuildMLKB2cData().buildMLKMethod(orderFlow, flowOrdertype, cwbOrderWothDeliverystate.getCwbOrder(), cwbOrderWothDeliverystate.getDeliveryState(), delivery_state, objectMapper);
		}
		
		//唯品会上门退订单反馈上门退成功后即把轨迹推送给TMS（不需要在归班审核时才推给TMS）
		DmpCwbOrder dmpCwbOrder = cwbOrderWothDeliverystate.getCwbOrder();
		String cwbordertypeid = (dmpCwbOrder==null?"-1" : dmpCwbOrder.getCwbordertypeid());
		String dmpCwb         = (dmpCwbOrder==null?"":dmpCwbOrder.getCwb());
		
		try{
			if (String.valueOf(CwbOrderTypeIdEnum.Shangmentui.getValue()).equals(cwbordertypeid) && 
				delivery_state ==  DeliveryStateEnum.ShangMenTuiChengGong.getValue() &&
				flowOrdertype == FlowOrderTypeEnum.YiFanKui.getValue() &&
				customer.getB2cEnum().equals(this.getB2cEnumKeys(customer, "vipshop"))) {
				
				logger.info("唯品会上门退订单(cwb={})反馈上门退成功后存到OMS express_send_b2c_data表:准备数据", dmpCwb);
				
				return this.buildB2cDataMaster.getBulidVipShopB2cData().BuildVipShopMethod(customer.getB2cEnum(), orderFlow, flowOrdertype, cwbOrderWothDeliverystate.getCwbOrder(),
							cwbOrderWothDeliverystate.getDeliveryState(), delivery_state, this.objectMapper);
			}
		}catch(Exception ex){
			logger.error("唯品会上门退订单(cwb="+dmpCwb+")反馈上门退成功后存到OMS express_send_b2c_data表出错", ex);
		}
		
		return null;
	}

	/**
	 * 获取对接电商 的配置 支持同一类客户不同枚举的设置 如：唯品会，天猫，一号店等。
	 * 
	 * @param customer
	 * @return
	 */
	public String getB2cEnumKeys(Customer customer, String constainsStr) {
		for (B2cEnum enums : B2cEnum.values()) { // 遍历唯品会enum，可能有多个枚举
			if (enums.getMethod().contains(constainsStr)) {
				if (customer.getB2cEnum().equals(String.valueOf(enums.getKey()))) {
					return String.valueOf(enums.getKey());
				}
			}
		}
		return null;
	}

}
