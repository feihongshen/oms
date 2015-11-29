package cn.explink.b2c.feiniuwang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.feiniuwang.FNWFlowEnum;
import cn.explink.b2c.feiniuwang.FNWService;
import cn.explink.b2c.feiniuwang.ReturnData;
import cn.explink.b2c.feiniuwang.Traces;
import cn.explink.b2c.feiniuwang.Traceslist;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.ExptReason;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.UserDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.User;
import cn.explink.enumutil.CwbOrderTypeIdEnum;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;
@Service
public class BuildFeiNiuWangData {
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;
	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	UserDAO userDAO;
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	FNWService fnwService;
	
	private Logger logger=LoggerFactory.getLogger(BuildFeiNiuWangData.class);
	
	public String buildFNWMethod(DmpOrderFlow orderFlow, long flowOrdertype,DmpCwbOrder cwbOrder,
			long delivery_state,DmpDeliveryState deliveryState,ObjectMapper objectMapper)
			throws IOException, JsonGenerationException, JsonMappingException
	{
		
		String receivedStatus=getAction(flowOrdertype,delivery_state,Long.valueOf(cwbOrder.getCwbordertypeid()));
			
		if(receivedStatus==null){
			logger.info("订单号：{} 不属于0飞牛网(http)0所需要的json---,状态{}，return",cwbOrder.getCwb(),flowOrdertype);
			return null;
		}
		
//		if(receivedStatus.equals("OTHER")&&Integer.valueOf(cwbOrder.getCwbordertypeid())==CwbOrderTypeIdEnum.Peisong.getValue()){
//			logger.info("======正常配送订单号=cwb={}============",orderFlow.getCwb());
//			return null;
//		}
//		if((!receivedStatus.equals("OTHER"))&&Integer.valueOf(cwbOrder.getCwbordertypeid())==CwbOrderTypeIdEnum.Shangmentui.getValue()){
//			logger.info("======上门退订单号=cwb={}==============",orderFlow.getCwb());
//			return null;
//		}
		
		ReturnData returnData = new ReturnData();
		List<Traceslist> traceslist = new ArrayList<Traceslist>();
		List<Traces> traces = new ArrayList<Traces>();
		Traces trace = setTraces(cwbOrder,orderFlow,receivedStatus,deliveryState);
		traces.add(trace);
		Traceslist tracelist = setTraceslist(cwbOrder,orderFlow,receivedStatus,deliveryState);
		traceslist.add(tracelist);
		returnData.setTraceslist(traceslist);
		logger.info("订单号：{}封装成0飞牛网(http)0所需要的json----状态：{}",cwbOrder.getCwb(),flowOrdertype);
		return objectMapper.writeValueAsString(returnData);
	}
	
	private  String getAction(long flowordertype,long deliverystate,long cwbordertypeid){
		if(cwbordertypeid==CwbOrderTypeIdEnum.Peisong.getValue()){
			if(flowordertype==FlowOrderTypeEnum.RuKu.getValue()){
				return FNWFlowEnum.ARRIVAL.getSign();
			}
			if(flowordertype==FlowOrderTypeEnum.ChuKuSaoMiao.getValue() ){
				return FNWFlowEnum.DEPARTURE.getSign();
			}
			if(flowordertype==FlowOrderTypeEnum.FenZhanLingHuo.getValue()){
				return FNWFlowEnum.SENT_SCAN.getSign();
			}
			if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()&&
					(deliverystate==DeliveryStateEnum.QuanBuTuiHuo.getValue()||deliverystate==DeliveryStateEnum.BuFenTuiHuo.getValue())){
				return FNWFlowEnum.PROBLEM.getSign();
			}
			if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()&&
					deliverystate==DeliveryStateEnum.FenZhanZhiLiu.getValue()
					){
				return FNWFlowEnum.RETENTION.getSign();
			}
			if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()&&
					deliverystate==DeliveryStateEnum.PeiSongChengGong.getValue()){
				return FNWFlowEnum.SIGNED.getSign();
			}
		}
		if(cwbordertypeid==CwbOrderTypeIdEnum.Shangmentui.getValue()){
			if(flowordertype==FlowOrderTypeEnum.FenZhanLingHuo.getValue()){
				return FNWFlowEnum.OTHER1.getSign();
			}
			if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()){
				if(deliverystate==DeliveryStateEnum.ShangMenHuanChengGong.getValue()){
					return FNWFlowEnum.OTHER2.getSign();
				}
			}
		}
		return null;
	}
	
	private Traces setTraces(DmpCwbOrder cwbOrder,DmpOrderFlow orderFlow,String receivedStatus,DmpDeliveryState deliveryState){
	
		long branchid =orderFlow.getBranchid();
		Branch branch = getDmpdao.getNowBranch(branchid);
		long userid=orderFlow.getUserid();
		if(orderFlow.getFlowordertype()==FlowOrderTypeEnum.FenZhanLingHuo.getValue()){
			userid=deliveryState.getDeliveryid();
		}
		User user = getDmpdao.getUserById(userid);
		Traces trace = new Traces();
		String time = DateTimeUtil.getNowTime();//配送当前环节时间
		trace.setTime(time);
		trace.setWeight(cwbOrder.getCarrealweight()==null?"":String.valueOf(cwbOrder.getCarrealweight()));//重量
		trace.setDesc(orderFlowDetail.getDetail(orderFlow));//跟踪描述
		trace.setCity(cwbOrder.getCwbcity()==null?"":cwbOrder.getCwbcity());
		trace.setFacilityname(branch.getBranchname()==null?"":branch.getBranchname());//操作站点
		trace.setFacilityno(String.valueOf(branch.getBranchid()));//站点编号
		trace.setFacilitytype(String.valueOf(branch.getSitetype()));//站点类型
		trace.setContacter(user.getRealname()==null?"":user.getRealname());//操作站点联系人
		trace.setContactphone(user.getUsermobile()==null?"":user.getUsermobile());//操作站点联系方式
		int value = deliveryState == null ? 0 : (int)deliveryState.getDeliverystate();
		int backreasonid = (int)cwbOrder.getBackreasonid();
		int leavedreasonid = (int)cwbOrder.getLeavedreasonid();
		if(value == DeliveryStateEnum.FenZhanZhiLiu.getValue()){
			backreasonid = 0;
		}else if(value==DeliveryStateEnum.QuanBuTuiHuo.getValue()||value==DeliveryStateEnum.BuFenTuiHuo.getValue()){
			leavedreasonid = 0;
		}
		ExptReason exptReason = this.b2ctools.getExptReasonByB2c(leavedreasonid, backreasonid, String.valueOf(cwbOrder.getCustomerid()), deliveryState == null ? 0: deliveryState.getDeliverystate());
		String type = String.valueOf(exptReason.getExpt_type());//异常类型
		String msg = exptReason.getExpt_msg()==null?"":exptReason.getExpt_msg();//异常原因
		trace.setQuestiontype(type);//问题件、留仓件类型
		trace.setQuestionreason(msg);//问题件、留仓件原因
		
		long deliveryid= deliveryState == null ? 0 : deliveryState.getDeliveryid();
		User user2 = getDmpdao.getUserById(deliveryid);
		trace.setReceivejober(user2.getRealname()==null?"":user2.getRealname());//收件业务员
		trace.setAllotman(user.getRealname()==null?"":user.getRealname());
		trace.setRemark("");
		trace.setWeight(String.valueOf(cwbOrder.getCarrealweight()));
		trace.setSignman(cwbOrder.getPodrealname()==null?"":cwbOrder.getPodrealname());//签收人
		trace.setAction(receivedStatus);
		String actiondesc = FNWFlowEnum.getMsg(receivedStatus) == null ?"" : FNWFlowEnum.getMsg(receivedStatus) .getText();
		trace.setActiondesc(actiondesc);//扫描类型描述
		trace.setNextSite(branch.getCwbtobranchid()==null?"":branch.getCwbtobranchid());//上一个或者下一个站点
		return trace;
	}
	private Traceslist setTraceslist(DmpCwbOrder cwbOrder,DmpOrderFlow orderFlow,String receivedStatus,DmpDeliveryState deliveryState){
		Traceslist traceslist =new Traceslist();
		
		traceslist.setLogisticproviderid(cwbOrder.getRemark2());//物流公司代码
		traceslist.setTxlogisticid(cwbOrder.getTranscwb());
		traceslist.setMailno(cwbOrder.getCwb());
		traceslist.setReturnno("");//返单号
		traceslist.setTransitno("");//转单号
		Traces trace = setTraces(cwbOrder,orderFlow,receivedStatus,deliveryState);
		List<Traces> traces = new ArrayList<Traces>();
		traces.add(trace);
		traceslist.setTraces(traces);
		return traceslist;
	}
}
