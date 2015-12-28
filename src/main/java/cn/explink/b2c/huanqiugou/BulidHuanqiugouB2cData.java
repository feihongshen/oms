	package cn.explink.b2c.huanqiugou;
import java.io.IOException;
import java.math.BigDecimal;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.hxgdms.Hxgdms;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.ExptReason;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.User;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.enumutil.PaytypeEnum;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;

@Service
public class BulidHuanqiugouB2cData {
	private Logger logger=LoggerFactory.getLogger(BulidHuanqiugouB2cData.class);
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;
	
	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	HuanqiugouService huanqiugouService;
	@Autowired
	private B2cTools b2ctools;
	
	public String buildHuanqiugouMethod(DmpOrderFlow orderFlow, long flowOrdertype,DmpCwbOrder cwbOrder, long delivery_state,DmpDeliveryState dmpDeliveryState ,ObjectMapper objectMapper)
			throws IOException, JsonGenerationException, JsonMappingException {
		
		String dmsReceiveStatus=huanqiugouService.getHxgdmsFlowEnum(flowOrdertype, delivery_state,cwbOrder.getCwbordertypeid());
		if(dmsReceiveStatus==null){
			logger.info("订单号：{} 不属于环球购物所需要的json---,状态{}，return",cwbOrder.getCwb(),flowOrdertype);
			return null;
		}
		Branch branch=this.getDmpdao.getNowBranch(orderFlow.getBranchid());
		User user=this.getDmpdao.getUserById(orderFlow.getUserid());
		
		HuanqiugouNote note=new HuanqiugouNote();
		note.setMailno(cwbOrder.getTranscwb());
		note.setSubmailno(cwbOrder.getCwb());
		note.setProcdate(DateTimeUtil.formatDate(orderFlow.getCredate()));
		note.setOrgfullname(branch.getBranchname());
		note.setOperator(user.getRealname());
		note.setAction(dmsReceiveStatus);
		note.setDescription(orderFlowDetail.getDetail(orderFlow));
		
		
		if(flowOrdertype==FlowOrderTypeEnum.YiShenHe.getValue()&&(delivery_state==DeliveryStateEnum.QuanBuTuiHuo.getValue()||delivery_state==DeliveryStateEnum.ShangMenJuTui.getValue()
		)){
			ExptReason exptReason = b2ctools.getExptReasonByB2c(0,cwbOrder.getBackreasonid(),cwbOrder.getCustomerid()+"",delivery_state);
			note.setException_action(exptReason.getExpt_code());
			note.setException_description(exptReason.getExpt_msg());
		}
		if(flowOrdertype==FlowOrderTypeEnum.YiShenHe.getValue()&&delivery_state==DeliveryStateEnum.FenZhanZhiLiu.getValue()){
			ExptReason exptReason = b2ctools.getExptReasonByB2c(cwbOrder.getLeavedreasonid(),0,cwbOrder.getCustomerid()+"",delivery_state);
			note.setException_action(exptReason.getExpt_code());
			note.setException_description(exptReason.getExpt_msg());
		}
		note.setNotes("");
		
		return objectMapper.writeValueAsString(note);
		
	}



	
	


}
