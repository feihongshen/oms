package cn.explink.b2c.haoyigou;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.haoyigou.dto.HaoYiGou;
import cn.explink.b2c.haoyigou.dto.PeisongAndTuihuoData;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.CacheBaseListener;
import cn.explink.domain.User;
import cn.explink.enumutil.CwbOrderTypeIdEnum;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;
@Service
public class BuildHYGsenddata {
	@Autowired
	B2cDataOrderFlowDetail b2cDataOrderFlowDetail;
	@Autowired
	HyGService hygService;
	@Autowired
	CacheBaseListener cacheBaseListener;
	
	public String buildSendData(DmpOrderFlow orderFlow, long flowOrdertype,DmpCwbOrder cwbOrder,DmpDeliveryState deliveryState,
			long delivery_state, ObjectMapper objectMapper) throws Exception{
		HaoYiGou hyg = this.hygService.getHYGSettingMethod(B2cEnum.HaoYiGou.getKey());
		
		return buildData(orderFlow,flowOrdertype,cwbOrder,deliveryState,delivery_state,objectMapper,hyg);
	}
	
	
	//创建需要推送给好易购的数据
	public String buildData(DmpOrderFlow orderFlow, long flowOrdertype,DmpCwbOrder cwbOrder,DmpDeliveryState deliveryState,
			long delivery_state, ObjectMapper objectMapper,HaoYiGou hyg) throws Exception{
		String deliverystateStr = "";
		String cwbordertypeid = cwbOrder.getCwbordertypeid();
		if((flowOrdertype != FlowOrderTypeEnum.FenZhanLingHuo.getValue())&&(flowOrdertype != FlowOrderTypeEnum.YiShenHe.getValue())){
			return null;
		}
		if(String.valueOf(CwbOrderTypeIdEnum.Peisong.getValue()).equals(cwbordertypeid)){
			deliverystateStr = getHaoyigouCode(flowOrdertype,delivery_state, deliverystateStr);
			if(deliverystateStr.isEmpty()){
				return null;
			}
			//==========================存储需要反馈给【好易购】--配送单--的数据信息======================================
			return this.setPesiongfeedback(orderFlow,cwbOrder,deliveryState,deliverystateStr,objectMapper,hyg);
		}
		else if((String.valueOf(CwbOrderTypeIdEnum.Shangmentui.getValue()).equals(cwbordertypeid))&&(flowOrdertype == FlowOrderTypeEnum.YiShenHe.getValue())){
			if(delivery_state == DeliveryStateEnum.ShangMenTuiChengGong.getValue()){
				deliverystateStr = FeedbackEnum.SMTsuccess.getValue();
				//==========================存储需要反馈给【好易购】--退货单--的数据信息======================================
				return this.setTuihuofeedback(orderFlow,cwbOrder,deliveryState,deliverystateStr,objectMapper,hyg);
			}
		}
		return null;
	}


	private String getHaoyigouCode(long flowOrdertype, long delivery_state,
			String deliverystateStr) {
		if(flowOrdertype == FlowOrderTypeEnum.FenZhanLingHuo.getValue()){
			deliverystateStr = FeedbackEnum.linghuo.getValue(); 
		}else if(flowOrdertype == FlowOrderTypeEnum.YiShenHe.getValue()){
			if(delivery_state == DeliveryStateEnum.PeiSongChengGong.getValue()){
				deliverystateStr = FeedbackEnum.success.getValue();
			}else if(delivery_state == DeliveryStateEnum.BuFenTuiHuo.getValue()){
				deliverystateStr = FeedbackEnum.bufenSuccess.getValue();
			}else if(delivery_state == DeliveryStateEnum.QuanBuTuiHuo.getValue()){
				deliverystateStr = FeedbackEnum.failure.getValue();
			}
		}
		return deliverystateStr;
	}
	
	//封装配送单数据
	private String setPesiongfeedback(DmpOrderFlow orderFlow,DmpCwbOrder cwbOrder,DmpDeliveryState deliveryState,
			String deliverystateStr, ObjectMapper objectMapper,HaoYiGou hyg) throws Exception{
		PeisongAndTuihuoData pstd = new PeisongAndTuihuoData();
		pstd.setDispatchid(hyg.getSendCode());//货运公司自定义code
		pstd.setCustomerid(cwbOrder.getRemark2());//导入时存入remark2字段中
		pstd.setShiporderno(cwbOrder.getRemark1());//电商订单表主键id===TODO(出库单号==需要导入到remark1字段)
		pstd.setDeliveryorderno(cwbOrder.getCwb());//本系统cwb
		pstd.setReceivername(cwbOrder.getConsigneename());//收件人姓名
		pstd.setDeliverydate(orderFlow.getCredate().toString());//当前配送节点时间
		pstd.setNumberofcartons(String.valueOf(orderFlow.getFlowordertype()));//当前流程阶段
		pstd.setDeliverystatusdescription(getOrderFlow(orderFlow,cwbOrder));//当前操作流程面熟
		pstd.setDeliverystaus(deliverystateStr);//描述标记
		String deliveryName = "";
		String deliveryPhone = "";
		
		long deliveryid = deliveryState.getDeliveryid();
		if(deliveryid>0){
			User deliveryUser = cacheBaseListener.getUser(cwbOrder.getDeliverid());
			deliveryName = deliveryUser.getRealname();
			deliveryPhone = deliveryUser.getUsermobile();
		}

		pstd.setDeliveryperson(deliveryName);
		pstd.setDeliverypersonphone(deliveryPhone);
		
		pstd.setReceipttime(DateTimeUtil.formatDate(orderFlow.getCredate()));//反馈或者审核时间
		pstd.setLongitude("");
		pstd.setDimensionvalue("");
		pstd.setUndefinedone("");
		pstd.setUndefinedtwo("");
		pstd.setWhichone(1);
		return objectMapper.writeValueAsString(pstd);
	}
	//封装上门退订单数据
	private String setTuihuofeedback(DmpOrderFlow orderFlow,DmpCwbOrder cwbOrder,DmpDeliveryState deliveryState,
			String deliverystateStr, ObjectMapper objectMapper,HaoYiGou hyg) throws Exception{
		PeisongAndTuihuoData pstd = new PeisongAndTuihuoData();
		pstd.setDeliveryid("");
		pstd.setShiporderno(cwbOrder.getCwb()+"2");//收货单号+序号（文档中描述为2）
		pstd.setCompanyid(hyg.getCompanyid());//默认好易购，基础设置中配置
		String edidate = cwbOrder.getRemark1();//excel导入时存入remark1字段
		pstd.setEdidate(edidate);//产生日期，暂处理为 销退单建立的时间(存在remark1中)
		pstd.setNumberofcartons(cwbOrder.getSendcarnum()==0?"1":(String.valueOf(cwbOrder.getSendcarnum())));//默认为发货件数
		pstd.setStatusupdatedate(DateTimeUtil.formatDate(orderFlow.getCredate()));//收回日期
		pstd.setDeliverystatusdescription(getOrderFlow(orderFlow,cwbOrder));
		pstd.setClosuredate("");//默认空
		pstd.setDeliverystatus(deliverystateStr);
		pstd.setPickupperson(cwbOrder.getConsigneename());//收件人
		pstd.setPickuppersonphone(cwbOrder.getConsigneemobile());//收件人电话
		pstd.setLongitude("");
		pstd.setDimensionvalue("");
		pstd.setReturndeliveryorderid("");//面单号码(可以为空)
		pstd.setUndefinedone("");
		pstd.setUndefinedtwo("");
		pstd.setWhichone(2);
		return objectMapper.writeValueAsString(pstd);
	}
	//封装流程信息的描述
	public String getOrderFlow(DmpOrderFlow orderFlow,DmpCwbOrder cwbOrder){
		String orderflowstr = this.b2cDataOrderFlowDetail.getHYGDetail(orderFlow,cwbOrder);
		return orderflowstr;
	}
	
}
