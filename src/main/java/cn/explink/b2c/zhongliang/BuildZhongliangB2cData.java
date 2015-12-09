package cn.explink.b2c.zhongliang;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.zhongliang.xml.Request_Body;
import cn.explink.b2c.zhongliang.xml.Request_UpdateStatus;
import cn.explink.b2c.zhongliang.xml.Request_orderStatus;
import cn.explink.dao.GetDmpDAO;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;

@Service
public class BuildZhongliangB2cData {
	private Logger logger = LoggerFactory.getLogger(BuildZhongliangB2cData.class);
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;

	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	ZhongliangService zhongliangService;
	@Autowired
	private B2cTools b2ctools;

	public String buildZhongliangMethod(DmpOrderFlow orderFlow,
			long flowOrdertype, DmpCwbOrder cwbOrder, long delivery_state,
			DmpDeliveryState deliveryState, ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException {
		String dmsReceiveStatus = zhongliangService.getFlowEnum(flowOrdertype,
				delivery_state);
		if (dmsReceiveStatus == null) {
			logger.info("订单号：{} 不属于[中粮]所需要的json---,状态{}，return",
					cwbOrder.getCwb(), flowOrdertype);
			return null;
		}
		logger.info("订单号：{} 开始属于中粮所需要的json---,状态{}",cwbOrder.getCwb(),flowOrdertype);
		
		Request_orderStatus orderStatus = new Request_orderStatus();
		try {
			
			Request_UpdateStatus updateStatus=new Request_UpdateStatus();
			Request_Body body=new Request_Body();
			updateStatus.setSendorderID(cwbOrder.getCwb());
			updateStatus.setInfoType(cwbOrder.getCwbordertypeid());
			updateStatus.setOrderStatus(dmsReceiveStatus);
			updateStatus.setChangeTime(orderFlow.getCredate()+"");
			updateStatus.setMailNo(cwbOrder.getTranscwb());
			if (dmsReceiveStatus == ZhongliangTrackEnum.Daohuo.getState()) {
				updateStatus.setRemark(orderFlowDetail.getDetail(orderFlow));
			}
			body.setUpdateStatus(updateStatus);
			orderStatus.setResponse_body(body);
		} catch (Exception e) {
			logger.error("封装中粮json异常cwb="+orderFlow.getCwb(),e);
		}
		
		logger.info("订单号：{} 结束中粮所需要的json---,状态{}",cwbOrder.getCwb(),flowOrdertype);
		
		return objectMapper.writeValueAsString(orderStatus);

	}

	
}
