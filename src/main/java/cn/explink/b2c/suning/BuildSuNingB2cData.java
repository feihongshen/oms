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
import cn.explink.enumutil.CwbOrderTypeIdEnum;
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
				
		String receivedStatus=suNingService.filterSuningFlowEnum(flowOrdertype,delivery_state);
			
		if(receivedStatus==null){
			logger.info("订单号：{} 不属于【苏宁易购】所需要存储的json数据,状态{},return",cwbOrder.getCwb(),flowOrdertype);
			return null;
		}
		User user=this.cacheBaseListener.getUser(orderFlow.getUserid());//操作人信息
		User deliveryman = null;
		if(cwbOrder.getDeliverid() >0){
			deliveryman = this.cacheBaseListener.getUser(cwbOrder.getDeliverid());//灏忎欢鍛樹俊鎭�
		}
		String work_status = "01";
		if((receivedStatus!=null)&&("03").equals(receivedStatus)){
			if((delivery_state == DeliveryStateEnum.QuanBuTuiHuo.getValue())
			||(delivery_state == DeliveryStateEnum.BuFenTuiHuo.getValue())){
				work_status = "02";
			}
		}
		WorkStatu workStatu = new WorkStatu();
		String cwb = cwbOrder.getCwb();
		if((Integer.valueOf(cwbOrder.getCwbordertypeid())==CwbOrderTypeIdEnum.Peisong.getValue())
				&&(cwb.matches("[0-9]+"))){
			//将数字补齐为20位纯数字
			cwb = getNeedZeroStr(cwb,20);//补齐20位
		}
		workStatu.setWork_id(cwb);
		workStatu.setWork_type(receivedStatus);
		workStatu.setWork_status(work_status);
		workStatu.setWork_site(this.cacheBaseListener.getBranch(orderFlow.getBranchid()).getBranchcode());//站点编码
		workStatu.setWork_name(this.cacheBaseListener.getBranch(orderFlow.getBranchid()).getBranchname());//站点名字
		//deliveryman==null?null:(deliveryman.getRealname())
		String work_user_id = deliveryman==null?"":(deliveryman.getUsername());//登录名
		if(!work_user_id.matches("[0-9]+")){//如果是纯数字则直接赋值，如果不是纯数字则获取手机号
			work_user_id = deliveryman==null?"":(deliveryman.getUsermobile()==null?"":(deliveryman.getUsermobile()));
		}
		workStatu.setWork_user_id(work_user_id);
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
	
	// 不足位数前面补0
	private String getNeedZeroStr(String str, int lennum) {
		String zerostr = "";
		str = "0000000000" + str;
		for(int i=0;i<100;i++){
			str = "0000000000"+ str;
			if(str.length()>lennum){
				break;
			}
		}
		zerostr = str.substring(str.length() - lennum, str.length());
		return zerostr;
	}
	
}
