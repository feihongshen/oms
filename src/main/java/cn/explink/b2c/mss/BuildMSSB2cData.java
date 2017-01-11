package cn.explink.b2c.mss;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.feiniuwang.BuildFeiNiuWangData;
import cn.explink.b2c.feiniuwang.FNWFlowEnum;
import cn.explink.b2c.feiniuwang.FeiNiuWang;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.UserDAO;
import cn.explink.enumutil.CwbOrderTypeIdEnum;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;

@Service
public class BuildMSSB2cData {
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;
	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	UserDAO userDAO;
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	private MssService mssService;
	private Logger logger = LoggerFactory.getLogger(BuildMSSB2cData.class);

	public String buildShenzhoushumaMethod(DmpOrderFlow orderFlow, long flowOrdertype, DmpCwbOrder cwbOrder, DmpDeliveryState deliveryState, long delivery_state, ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException {
		String receivedStatus = getAction(flowOrdertype, delivery_state, Long.valueOf(cwbOrder.getCwbordertypeid()));
		if (receivedStatus == null) {
			logger.info("订单号：{} 不属于0美食送(http)0所需要的json---,状态{}，return", cwbOrder.getCwb(), flowOrdertype);
			return null;
		}
		Mss mss = this.mssService.getMss(B2cEnum.MSS.getKey());
		ReturnData returnData = new ReturnData();
		returnData.setPartnerOrderId(cwbOrder.getCwb());
		returnData.setMessage(orderFlowDetail.getDetail(orderFlow));
		logger.info("订单号：{}封装成0美食送(http)0所需要的json----状态：{}", cwbOrder.getCwb(), flowOrdertype);
		return objectMapper.writeValueAsString(returnData);
	}

	private String getAction(long flowordertype, long deliverystate, long cwbordertypeid) {
		if (cwbordertypeid == CwbOrderTypeIdEnum.Peisong.getValue()) {
			if(flowordertype<=FlowOrderTypeEnum.YiShenHe.getValue()){
				return FlowOrderTypeEnum.getText(Integer.valueOf(String.valueOf(flowordertype))).getMethod();
			}
			
		}
		return null;
	}

}
