package cn.explink.b2c.suning;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.explink.b2c.suning.requestdto.WorkStatu;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.CacheBaseListener;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.UserDAO;
import cn.explink.domain.User;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;

@Service
public class BuildSuNingB2cData {
	private Logger logger=LoggerFactory.getLogger(this.getClass());
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;
	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	SuNingService suNingService;
	@Autowired
	UserDAO userDAO;
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	CacheBaseListener cacheBaseListener;

	public String buildSuNingMethod(DmpOrderFlow orderFlow, long flowOrdertype,DmpCwbOrder cwbOrder,DmpDeliveryState deliveryState,
			long delivery_state, ObjectMapper objectMapper) throws Exception{
				
		String receivedStatus=suNingService.filterJiuyeFlowEnum(flowOrdertype,delivery_state);
			
		if(receivedStatus==null){
			logger.info("订单号：{} 不属于【苏宁易购】所需要存储的json数据,状态{},return",cwbOrder.getCwb(),flowOrdertype);
			return null;
		}
		User user=this.cacheBaseListener.getUser(orderFlow.getUserid());//操作人信息
		User deliveryman = this.cacheBaseListener.getUser(cwbOrder.getDeliverid());//小件员信息
		String work_status = "01";
		if((receivedStatus!=null)&&("03").equals(receivedStatus)){
			if((delivery_state == DeliveryStateEnum.QuanBuTuiHuo.getValue())
			||(delivery_state == DeliveryStateEnum.BuFenTuiHuo.getValue())){
				work_status = "02";
			}
		}
		WorkStatu workStatu = new WorkStatu();
		workStatu.setWork_id(cwbOrder.getCwb());
		workStatu.setWork_type(receivedStatus);
		workStatu.setWork_status(work_status);
		workStatu.setWork_site(this.cacheBaseListener.getBranch(orderFlow.getBranchid()).getBranchcode());//站点编码
		workStatu.setWork_name(this.cacheBaseListener.getBranch(orderFlow.getBranchid()).getBranchname());//站点名字
		workStatu.setWork_user_id(deliveryman==null?null:(deliveryman.getRealname()));
		workStatu.setWork_mobilephone(deliveryman==null?null:(deliveryman.getUsermobile()));
		workStatu.setWork_oper_id(user==null?null:(user.getRealname()));
		workStatu.setWork_oper_time(orderFlow.getCredate().toString());
		workStatu.setWork_sign_info((deliveryState==null||(deliveryState.getSign_man()==null))?null:(deliveryState.getSign_man()));
		workStatu.setMark("");
		workStatu.setWeak(cwbOrder.getRemark1());
		workStatu.setSite(cwbOrder.getRemark2());
		
		logger.info("订单号：{}封装成【苏宁易购】所需要的json----状态：{}",cwbOrder.getCwb(),flowOrdertype);
		return objectMapper.writeValueAsString(workStatu);	
	}	
	
}
