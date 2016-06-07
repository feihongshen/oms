/**
 * 
 */
package cn.explink.b2c.gxdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.ExptReason;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.User;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;

/**
 * @ClassName: BuildGxDxsenddata
 * @Description: TODO
 * @Author: 王强
 * @Date: 2015年11月16日下午3:21:04
 */
@Service
public class BuildGxDxsenddata {
	private Logger logger = LoggerFactory.getLogger(BuildGxDxsenddata.class);
	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	B2cTools b2ctools;

	public String buildGxDxsenddata(DmpOrderFlow orderFlow, long flowOrdertype, DmpCwbOrder cwbOrder, DmpDeliveryState deliveryState, long delivery_state, ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException {
		//返回订单状态在推送时所需要的请求指令
		String receivedStatus = GuangXinDidanXinService.filterGuangXinDidanXinFlowEnum(flowOrdertype, delivery_state);
		if (receivedStatus == null) {
			logger.info("订单号：{} 不属于0广信电信0所需要的json---,状态{},return", cwbOrder.getCwb(), flowOrdertype);
			return null;
		}
		//操作人
		User user = getDmpdao.getUserById(orderFlow.getUserid());
		//小件员
		User delivery = JacksonMapper.getInstance().readValue(getDmpdao.getDeliverById(cwbOrder.getDeliverid()),User.class);
		OrderStatesList orderStates = new OrderStatesList();
		List<OrderState> orderStatelist = new ArrayList<OrderState>();
		OrderState orderState = new OrderState();
		String time = orderFlow.getCredate().toString();
		time = time.substring(0, time.length()-2);
		//滞留状态的订单 设置滞留原因
		if(delivery_state == GxDxOrderTypeEnum.SC04.getDeliveryState()){
			ExptReason exptReason = this.b2ctools.getExptReasonByB2c(cwbOrder.getLeavedreasonid(), 0, String.valueOf(cwbOrder.getCustomerid()), delivery_state);
			orderState.setRemark(exptReason.getExpt_code());
		}
		//拒收订单原因
		if(delivery_state == GxDxOrderTypeEnum.SC05.getDeliveryState()){
			ExptReason exptReason = this.b2ctools.getExptReasonByB2c(0, cwbOrder.getBackreasonid(), String.valueOf(cwbOrder.getCustomerid()), delivery_state);
			orderState.setRemark(exptReason.getExpt_code());
		}
		orderState.setState(receivedStatus);
		orderState.setStateTime(time);
		orderState.setWaybillNo(cwbOrder.getCwb());
		orderState.setOperName(user.getRealname());
		//操作时间
		orderState.setOperateTime(time);
		orderState.setDeliveryMan(delivery.getRealname());
		orderStatelist.add(orderState);
		orderStates.setOrderState(orderStatelist);
		
		logger.info("订单号：{}封装成0广信电信0所需要的json----状态：{}", cwbOrder.getCwb(), flowOrdertype);
		return objectMapper.writeValueAsString(orderStates);

	}
}
