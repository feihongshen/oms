package cn.explink.b2c.zhemeng;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.User;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;

@Service
public class BulidZhemengB2cData {
	private Logger logger = LoggerFactory.getLogger(BulidZhemengB2cData.class);
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;

	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	ZhemengService zhemengService;

	public String buildZhemengMethod(DmpOrderFlow orderFlow, long flowOrdertype, DmpCwbOrder cwbOrder, long delivery_state, DmpDeliveryState dmpDeliveryState, ObjectMapper objectMapper)
			throws IOException, JsonGenerationException, JsonMappingException {

		String flowStatus = zhemengService.getFlowEnum(flowOrdertype, delivery_state);
		if (flowStatus == null) {
			logger.info("订单号：{} 不属于[zhemeng]所需要的json---,状态{}，return", cwbOrder.getCwb(), flowOrdertype);
			return null;
		}
		
		Branch branch=getDmpdao.getNowBranch(orderFlow.getBranchid());
		User user = getDmpdao.getUserById(orderFlow.getUserid());
		
		ZhemengXMLNote note = new ZhemengXMLNote();
		note.setOrder_code(cwbOrder.getCwb());
		note.setTms_service_code("");
		note.setOperator(user.getRealname());
		note.setOperator_date(DateTimeUtil.formatDate(orderFlow.getCredate()));
		note.setStatus(flowStatus);
		note.setScanstano(branch.getBranchname());
		note.setCtrname(user.getRealname());
		note.setContent(orderFlowDetail.getDetail(orderFlow));
		
		
		
		return objectMapper.writeValueAsString(note);

	}

	

}
