package cn.explink.b2c.jiuye;
import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.jiuye.jsondto.JiuyeXMLNote;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.UserDAO;
import cn.explink.domain.User;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;

@Service
public class BuildJiuyeB2cData {
	private Logger logger=LoggerFactory.getLogger(BuildJiuyeB2cData.class);
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;
	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	JiuyeService jiuyeService;
	@Autowired
	UserDAO userDAO;
	@Autowired
	private B2cTools b2ctools;

	public String buildJiuYeMethod(DmpOrderFlow orderFlow, long flowOrdertype,DmpCwbOrder cwbOrder,DmpDeliveryState deliveryState,
			long delivery_state, ObjectMapper objectMapper)
			throws IOException, JsonGenerationException, JsonMappingException
	{
			
		String receivedStatus=jiuyeService.filterJiuyeFlowEnum(flowOrdertype,delivery_state);
			
			if(receivedStatus==null){
				logger.info("订单号：{} 不属于0九曳0所需要的json---,状态{}，return",cwbOrder.getCwb(),flowOrdertype);
				return null;
			}
			User user=getDmpdao.getUserById(orderFlow.getUserid());
			
			JiuyeXMLNote xmlnote=new JiuyeXMLNote();
			xmlnote.setClientCode(cwbOrder.getRemark5());//订单号(久耶系统使用)
			xmlnote.setWorkCode(cwbOrder.getCwb());
			xmlnote.setOperationPerson(user.getRealname());
			xmlnote.setOperationPhone(user.getUsermobile());
			xmlnote.setDelveryTel("");
			xmlnote.setOperationTime(DateTimeUtil.formatDate(orderFlow.getCredate()));
			xmlnote.setWorkStatus(receivedStatus);
			xmlnote.setOperationDesc(orderFlowDetail.getJiuYeDetail(orderFlow,cwbOrder));
			
		
			logger.info("订单号：{}封装成0九曳0所需要的json----状态：{}",cwbOrder.getCwb(),flowOrdertype);
			return objectMapper.writeValueAsString(xmlnote);
			
	}

	
}
