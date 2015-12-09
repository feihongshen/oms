package cn.explink.b2c.wanxiang;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.ExptReason;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.UserDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.User;
import cn.explink.enumutil.CwbOrderTypeIdEnum;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;

@Service
public class BuildWanxiangB2cData {
	private Logger logger = LoggerFactory.getLogger(BuildWanxiangB2cData.class);
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;
	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	WanxiangService wanxiangService;
	@Autowired
	UserDAO userDAO;
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	B2CDataDAO b2CDataDAO;

	public String BuildWanXiangMethod(DmpOrderFlow orderFlow, long flowOrdertype, DmpCwbOrder cwbOrder, DmpDeliveryState deliveryState, long delivery_state, ObjectMapper objectMapper)
			throws IOException, JsonGenerationException, JsonMappingException {
		if (!this.b2ctools.isB2cOpen(B2cEnum.Wanxiang.getKey())) {
			this.logger.info("未开0万象0的状态反馈接口!");
			return null;
		}
		String xmlnote = "";
		Wanxiang wx = this.wanxiangService.getWanxiang(B2cEnum.Wanxiang.getKey());


		String receivedStatus = this.wanxiangService.getFlowEnumNew(flowOrdertype, delivery_state, cwbOrder.getCwbordertypeid(), wx.getVersion());
		if (receivedStatus == null) {
			this.logger.info("订单号：{} 不属于[万象]所需要的json---,状态{}，return", cwbOrder.getCwb(), flowOrdertype);
			return null;
		}

		xmlnote = this.getVersionNewSendDatas(orderFlow, flowOrdertype, cwbOrder, deliveryState, receivedStatus,delivery_state, wx);

		if ((xmlnote == null) || xmlnote.isEmpty()) {
			return null;
		}
		return objectMapper.writeValueAsString(xmlnote);
	}

	

	/**
	 * 万象 新版开发 推送功能
	 * 
	 * @param orderFlow
	 * @param flowOrdertype
	 * @param cwbOrder
	 * @param deliveryState
	 * @param receivedStatus
	 * @param wx
	 * @throws IOException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 */
	private String getVersionNewSendDatas(DmpOrderFlow orderFlow, long flowOrdertype, DmpCwbOrder cwbOrder, DmpDeliveryState deliveryState, String receivedStatus,long delivery_state, Wanxiang wx) throws IOException,
			JsonGenerationException, JsonMappingException {
		try {

			if(wx.getShangmentuiSupport()==1){ //只支持配送单
				if(Long.valueOf(cwbOrder.getCwbordertypeid())!=CwbOrderTypeIdEnum.Peisong.getValue()){
					return null;
				}
			}
			
			if(delivery_state==DeliveryStateEnum.QuanBuTuiHuo.getValue()||delivery_state==DeliveryStateEnum.BuFenTuiHuo.getValue()){
				if(wx.getJushousendflag()==1){ //拒收不推送
					return null;
				}
			}
			
			User deliveryUser = null;
			if (deliveryState != null) {
				deliveryUser = this.getDmpdao.getUserById(deliveryState.getDeliveryid());
			}
			User operatorUser = this.getDmpdao.getUserById(orderFlow.getUserid());
			Branch operatorBranch = this.getDmpdao.getNowBranch(orderFlow.getBranchid());

			StringBuffer subxml = new StringBuffer("<?xml version='1.0' encoding='utf-8' standalone='yes' ?>");

			String reason="";
			if ((delivery_state == DeliveryStateEnum.QuanBuTuiHuo.getValue()) || (delivery_state == DeliveryStateEnum.BuFenTuiHuo.getValue())|| (delivery_state == DeliveryStateEnum.ShangMenJuTui.getValue())) {
				ExptReason exptReason = this.b2ctools.getExptReasonByB2c(0, cwbOrder.getBackreasonid(), String.valueOf(cwbOrder.getCustomerid()), delivery_state);
				reason=exptReason.getExpt_msg()==null?"其他退件原因":exptReason.getExpt_msg();
			}else if(delivery_state == DeliveryStateEnum.FenZhanZhiLiu.getValue()){
				ExptReason exptReason = this.b2ctools.getExptReasonByB2c(cwbOrder.getLeavedreasonid(), 0, String.valueOf(cwbOrder.getCustomerid()), delivery_state);
				reason=exptReason.getExpt_msg()==null?"其它延缓原因":exptReason.getExpt_msg();
			}
			
			if (wx.getVersion() == 0) {

				subxml.append("<request>");
				subxml.append("<bill_no>" + cwbOrder.getCwb() + "</bill_no>");
				subxml.append("<packageid></packageid>");
				subxml.append("<operateman>" + operatorUser.getRealname() + "</operateman>");
				subxml.append("<operatedep>" + operatorBranch.getBranchname() + "</operatedep>");
				subxml.append("<operatetype>" + receivedStatus + "</operatetype>");
				subxml.append("<operatetime>" + DateTimeUtil.formatDate(orderFlow.getCredate()) + "</operatetime>");
				subxml.append("<reason>"+reason+"</reason>");
				subxml.append("<remark>" + (this.orderFlowDetail.getDetail(orderFlow) == null ? "正在派件" : this.orderFlowDetail.getDetail(orderFlow)) + "</remark>");
				subxml.append("<operatorname>" + (deliveryUser == null ? "" : deliveryUser.getRealname()) + "</operatorname>");
				subxml.append("<operatortel>" + (deliveryUser == null ? "" : deliveryUser.getUsermobile()) + "</operatortel>");
				subxml.append("<customer_code>" + cwbOrder.getConsigneeno() + "</customer_code>");
				subxml.append("</request>");
			} else {
				
				
				
				
				String nextsendtime = cwbOrder.getResendtime() == null ? "" : cwbOrder.getResendtime();
				subxml.append("<request>");
				subxml.append("<bill_no>" + cwbOrder.getCwb() + "</bill_no>");
				subxml.append("<packageid></packageid>");
				subxml.append("<operateman>" + operatorUser.getRealname() + "</operateman>");
				subxml.append("<operatedep>" + operatorBranch.getBranchname() + "</operatedep>");
				subxml.append("<operatetype>" + receivedStatus + "</operatetype>");
				subxml.append("<operatetime>" + DateTimeUtil.formatDate(orderFlow.getCredate()) + "</operatetime>");
				subxml.append("<nextsendtime>" + nextsendtime + "</nextsendtime>");
				subxml.append("<reason>"+reason+"</reason>");
				subxml.append("<remark>" + this.orderFlowDetail.getDetail(orderFlow) + "</remark>");
				subxml.append("<operatorname>" + (deliveryUser == null ? "" : deliveryUser.getRealname()) + "</operatorname>");
				subxml.append("<operatortel>" + (deliveryUser == null ? "" : deliveryUser.getUsermobile()) + "</operatortel>");
				subxml.append("<defined_1></defined_1>");
				subxml.append("<defined_2></defined_2>");
				subxml.append("<user_name>" + wx.getUser_name() + "</user_name>");
				subxml.append("</request>");
			}

			

			return subxml.toString();
		} catch (Exception e) {
			this.logger.error("推送万象接口发生未知异常,cwb=" + cwbOrder.getCwb(), e);
			return null;
		}

	}
}
