package cn.explink.b2c.meilinkai;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.UserDAO;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
@Service
public class BuildMLKB2cData {
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;
	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	MLKService mlkService;
	@Autowired
	UserDAO userDAO;
	@Autowired
	private B2cTools b2ctools;

	private Logger logger=LoggerFactory.getLogger(this.getClass());
	
	public String buildMLKMethod(DmpOrderFlow orderFlow, long flowOrdertype,DmpCwbOrder cwbOrder,DmpDeliveryState deliveryState,
			long delivery_state, ObjectMapper objectMapper)
			throws IOException, JsonGenerationException, JsonMappingException
	{
		String receivedStatus = this.mlkService.filterMLKFlowEnum(flowOrdertype,delivery_state);
		
		if(receivedStatus==null){
			logger.info("订单号：{} 不属于【玫琳凯】所需要的json---,状态{}，return",cwbOrder.getCwb(),flowOrdertype);
			return null;
		}
		TrackData trackData = new TrackData();
		trackData.setDataID(cwbOrder.getCwb());//jde+seq
		trackData.setReceiveDate(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
		trackData.setComment(cwbOrder.getCwb());
		trackData.setTransactionType(receivedStatus);
		logger.info("订单号：{}封装成【玫琳凯】所需要的json----状态：{}",cwbOrder.getCwb(),flowOrdertype);
		return objectMapper.writeValueAsString(trackData);
		
	}

	
}
