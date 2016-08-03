package cn.explink.b2c.tpsdo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.CacheBaseListener;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.b2c.tpsdo.bean.OrderTraceToTPSCfg;
import cn.explink.b2c.tpsdo.bean.OtherOrderTrackVo;
import cn.explink.b2c.tpsdo.bean.ThirdPartyOrder2DOCfg;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.ApplyEditDeliverystate;
import cn.explink.domain.Branch;
import cn.explink.domain.User;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.enumutil.PaytypeEnum;
import cn.explink.jms.dto.CwbOrderWithDeliveryState;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;

import com.pjbest.deliveryorder.bizservice.PjDeliveryOrderService;
import com.pjbest.deliveryorder.bizservice.PjDeliveryOrderServiceHelper;
import com.pjbest.deliveryorder.bizservice.PjDoStatusRequest;
import com.pjbest.deliveryorder.bizservice.PjDoStatusResponse;
import com.pjbest.deliveryorder.service.DeliveryTrack;
import com.pjbest.deliveryorder.service.DoTrackFeedbackRequest;
import com.pjbest.deliveryorder.service.ExceptionTrack;
import com.pjbest.deliveryorder.service.SignTrack;
import com.vip.osp.core.context.InvocationContext;
import com.vip.osp.core.exception.OspException;

@Service
public class OtherOrderTrackSendService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private OtherOrderTrackService otherOrderTrackService;
	
	@Autowired
	private DmpTpsTrackMappingService dmpTpsTrackMappingService;
	
	@Autowired
	GetDmpDAO getDmpDAO;
	
	@Autowired
	TPOSendDoInfService tPOSendDoInfService;
	
	@Autowired
	private CacheBaseListener cacheBaseListener;
	@Autowired
	private B2cTools b2ctools;

	private static final String DATE_FORMAT="yyyy-MM-dd HH:mm:ss";
	
	public void process() {
	       this.logger.info("other order track job process start...");
	       OrderTraceToTPSCfg orderTraceToTPSCfg = this.getOrderTraceToTPSCfg();
	       if(orderTraceToTPSCfg == null){
				logger.info("未配置外单轨迹推送DO服务配置信息!无法保存外单轨迹数据到临时表...");
				return;
			}
			if(orderTraceToTPSCfg.getTrackOpenFlag() != 1){
				logger.info("未开启外单轨迹推送接口.");
				return;
			}
	       try { 
		   		ThirdPartyOrder2DOCfg pushCfg = tPOSendDoInfService.getThirdPartyOrder2DOCfg();
				if(pushCfg == null){
					logger.info("未配置外单轨迹推送DO服务配置信息!无法推送外单轨迹数据...");
					return;
				}
				if(pushCfg.getTrackOpenFlag() != 1){
					logger.info("未开启外单轨迹推送接口.");
					return;
				}
		        //加载临时表数据
	    	    int maxRetry=pushCfg.getTrackMaxTryTime();
	    	   
		        List<OtherOrderTrackVo> rowList= otherOrderTrackService.retrieveOtherOrderTrack(maxRetry,3000);
		        //处理数据
		        handleData(rowList);
	      } catch (Throwable ex) {
	        logger.error("处理外单轨迹数据出错.",ex);
	     } 
	        
	        //handleTimeoutData();
	}
	
	private void handleData(List<OtherOrderTrackVo> msgVoList){
		long cnt=0;
		if(msgVoList!=null){
			cnt=msgVoList.size();
		}
		logger.info("other customer order track normal msg num:"+cnt);
		
		if(cnt==0){
			return;
		}

		for(OtherOrderTrackVo msgVo:msgVoList){
			try {
				DmpOrderFlow orderFlow=parseOrderFlowObject(msgVo.getOrderFlowJson());
				CwbOrderWithDeliveryState orderWithState=parseDeliveryStateObject(msgVo.getDeliveryStateJson());
				if(orderFlow.getFlowordertype()!=FlowOrderTypeEnum.ChongZhiFanKui.getValue()){
					//推送轨迹
					DoTrackFeedbackRequest req=prepareRequest(msgVo,orderFlow,orderWithState);
					if(req!=null){
						logger.info("上传外单cwb={}轨迹报文：{}", orderFlow.getCwb(), com.alibaba.fastjson.JSONObject.toJSONString(req));
						
						send(req,6000);
						otherOrderTrackService.completedTrackMsg(2,"",msgVo.getCwb(),msgVo.getFloworderid());
					}else{
						otherOrderTrackService.completedTrackMsg(4,"",msgVo.getCwb(),msgVo.getFloworderid());
					}
				}else{
					//推送重置反馈
					PjDoStatusRequest req=prepareResetStateRequest(msgVo,orderFlow);
					if(req!=null){
						logger.info("上传外单cwb={}重置反馈报文：{}", orderFlow.getCwb(), com.alibaba.fastjson.JSONObject.toJSONString(req));
						sendResetState(req,6000);
						otherOrderTrackService.completedTrackMsg(2,"",msgVo.getCwb(),msgVo.getFloworderid());
					}else{
						otherOrderTrackService.completedTrackMsg(4,"",msgVo.getCwb(),msgVo.getFloworderid());
					}
				}
				
			} catch (Exception e) {
				logger.error("上传外单轨迹数据到TPS时出错.cwb="+msgVo.getCwb()+",floworderid="+msgVo.getFloworderid(),e);
				
				otherOrderTrackService.completedTrackMsg(3,"上传外单轨迹数据到TPS时出错."+e.getMessage(),msgVo.getCwb(),msgVo.getFloworderid());
			}
		}
	}
	
	private DoTrackFeedbackRequest prepareRequest(OtherOrderTrackVo msgVo,DmpOrderFlow orderFlow,CwbOrderWithDeliveryState orderWithState){
		int tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(orderFlow.getFlowordertype());
		
		if(tpsOperateType<0){
			return null;
		}
		
		if(msgVo.getTpsno()==null){
			throw new RuntimeException("TPS运单号为空.");
		}
		
		if(orderWithState==null){
			throw new RuntimeException("订单及归班报文数据为空.");
		}
		
		DmpCwbOrder cwbOrder=orderWithState.getCwbOrder();
		if(cwbOrder==null){
			throw new RuntimeException("订单报文数据为空.");
		}
		DmpDeliveryState ds=orderWithState.getDeliveryState();
		
		DoTrackFeedbackRequest req= new DoTrackFeedbackRequest();
		

		if(orderFlow.getFlowordertype()==FlowOrderTypeEnum.FenZhanLingHuo.getValue()){
			long deliveryid=0;
			if(ds!=null){
				deliveryid=ds.getDeliveryid();
			}else{
				deliveryid=cwbOrder.getDeliverid();
			}
			
			User user=getDmpDAO.getUserById(deliveryid);//
			DeliveryTrack deliveryTrack=new DeliveryTrack();
			deliveryTrack.setCourier(user==null?null:user.getRealname());
			deliveryTrack.setCourierId(user==null||user.getUsername()==null?"":user.getUsername().toUpperCase());//id or deliverManCode ???
			deliveryTrack.setCourierTel(user==null?null:user.getUsermobile());//or mobile??? format ?
			//deliveryTrack.setDestinationOrg(""+cwbOrder.getDeliverybranchid());
			req.setDelivery(deliveryTrack);
		}
		
		if(orderFlow.getFlowordertype()==FlowOrderTypeEnum.ChuKuSaoMiao.getValue()
			&&ds==null&&cwbOrder.getFirstchangereasonid()!=0){
			//中转出站
			tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.ZhongZhuanChuKuSaoMiao.getValue());
			setReason(req,cwbOrder.getFirstchangereasonid(),cwbOrder.getChangereasonid());
		}else if(orderFlow.getFlowordertype()==FlowOrderTypeEnum.YiShenHe.getValue()){
			if(ds==null){
				throw new RuntimeException("归班反馈结果报文数据为空.");
			}
			
			if(ds.getDeliverystate()==DeliveryStateEnum.PeiSongChengGong.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.PeiSongChengGong.getValue());
					
				//以下需要检查配送成功与否??? 
				//is it getReceivedfee???
				//if(ds.getSign_typeid()==1){//已签收
					SignTrack signTrack=new SignTrack();
					signTrack.setSignMan(ds.getSign_man());//本人或代签???????????
					req.setSign(signTrack);
				//}
			}else if(ds.getDeliverystate()==DeliveryStateEnum.ShangMenTuiChengGong.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.ShangMenTuiChengGong.getValue());
			}else if(ds.getDeliverystate()==DeliveryStateEnum.ShangMenHuanChengGong.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.ShangMenHuanChengGong.getValue());
			}else if(ds.getDeliverystate()==DeliveryStateEnum.QuanBuTuiHuo.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.JuShou.getValue());
				setReason(req,cwbOrder.getBackreasonid(),0);
			}else if(ds.getDeliverystate()==DeliveryStateEnum.BuFenTuiHuo.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.BuFenTuiHuo.getValue());
				setReason(req,cwbOrder.getBackreasonid(),0);
			}else if(ds.getDeliverystate()==DeliveryStateEnum.ShangMenJuTui.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.ShangMenJuTui.getValue());
				setReason(req,cwbOrder.getBackreasonid(),0);
			}else if(ds.getDeliverystate()==DeliveryStateEnum.HuoWuDiuShi.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.HuoWuDiuShi.getValue());
				setReason(req,cwbOrder.getLosereasonid(),0);//todo
			}else if(ds.getDeliverystate()==DeliveryStateEnum.FenZhanZhiLiu.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.FenZhanZhiLiu.getValue());
				setReason(req,cwbOrder.getFirstlevelid(),cwbOrder.getLeavedreasonid());
			}else if(ds.getDeliverystate()==DeliveryStateEnum.ZhiLiuZiDongLingHuo.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.FenZhanZhiLiu.getValue());//???
				setReason(req,cwbOrder.getFirstlevelid(),cwbOrder.getLeavedreasonid());
			}else if(ds.getDeliverystate()==DeliveryStateEnum.DaiZhongZhuan.getValue()){
				//tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.ZhongZhuanChuKuSaoMiao.getValue());
				//待中转审核时不推送,中转出站时才推
				return null;
			}else{
				return null;
			}
		}


		if(ds!=null){
			if(ds.getInfactfare()!=null&&ds.getInfactfare().doubleValue()>0){
				//运费
				req.setActualFee(ds.getInfactfare().doubleValue());
			}
			if(ds.getReceivedfee()!=null&&ds.getReceivedfee().doubleValue()>0){
				//实收金额
				req.setCodAmount(ds.getReceivedfee().doubleValue());
			}
			
			if(ds.getReturnedfee()!=null&&ds.getReturnedfee().doubleValue()>0){
				//实退金额
				req.setReturnAmount(ds.getReturnedfee().doubleValue());
			}
			
			if(ds.getReceivedfee()!=null&&ds.getReceivedfee().doubleValue()>0||
				ds.getReturnedfee()!=null&&ds.getReturnedfee().doubleValue()>0||
				ds.getCash()!=null&&ds.getCash().doubleValue()>0||
				ds.getPos()!=null&&ds.getPos().doubleValue()>0||
				ds.getCheckfee()!=null&&ds.getCheckfee().doubleValue()>0||
				ds.getCodpos()!=null&&ds.getCodpos().doubleValue()>0||
				ds.getOtherfee()!=null&&ds.getOtherfee().doubleValue()>0
				){
				
				/**
				 *  需要根据dmp的支付方式映射成TPS的支付方式 updated by gordon.zhou 2016.5.3
				 */
				String newpaywayidStr = cwbOrder.getNewpaywayid();
				int newpaywayid =-1;
				try{
					newpaywayid = Integer.valueOf(newpaywayidStr);
				}catch(Exception e){
					throw new RuntimeException("解析归班反馈支付方式出错.newpaywayid="+newpaywayid+","+e.getMessage());
				}
					
				if(newpaywayid == PaytypeEnum.Xianjin.getValue()){
					req.setActualPayType(String.valueOf(0));
				}
				else if(newpaywayid == PaytypeEnum.Pos.getValue()){
					req.setActualPayType(String.valueOf(1));
				}
				else if(newpaywayid == PaytypeEnum.CodPos.getValue()){
					req.setActualPayType(String.valueOf(2));
				}
				else if(newpaywayid == PaytypeEnum.Zhipiao.getValue()){
					req.setActualPayType(String.valueOf(13));
				}
				else if(newpaywayid == PaytypeEnum.Qita.getValue()){
					req.setActualPayType(String.valueOf(14));
				}
				else{
					req.setActualPayType(null);
				}
				
			}
		}
		
		String nextOrg=null;
		String operateOrg=null;
		
		if(cwbOrder.getNextbranchid()>0){
			nextOrg=getTpsBranchCodeById(cwbOrder.getNextbranchid());
			if(nextOrg==null){
				throw new RuntimeException("没找到此下一站机构，branchid="+cwbOrder.getNextbranchid());
			}
		}
		
		if(orderFlow.getBranchid()>0){
			operateOrg=getTpsBranchCodeById(orderFlow.getBranchid());
			if(operateOrg==null){
				throw new RuntimeException("没找到此操作机构，branchid="+orderFlow.getBranchid());
			}
		}
		
		User operateUser=getDmpDAO.getUserById(orderFlow.getUserid());//
		

		req.setNextOrg(nextOrg);//tps机构编码
		req.setOperateOrg(operateOrg);//tps机构编码
		req.setOperater(operateUser==null?null:operateUser.getRealname());//???
		req.setOperateTime(orderFlow.getCredate());
		req.setOperateType(tpsOperateType);
		req.setTransportNo(msgVo.getTpsno());
		
		return req;
	}
	
	private void setReason(DoTrackFeedbackRequest req,long firstReasonid,long secondReasonid){
		if(firstReasonid>0){
			String reasonDesc=this.getDmpDAO.getReason(firstReasonid);
			ExceptionTrack et=new ExceptionTrack ();
			et.setExceptionReason(reasonDesc);
			req.setException(et);
			if(secondReasonid>0){
				reasonDesc=this.getDmpDAO.getReason(secondReasonid);
				req.setReason(reasonDesc);
			}
		}
		
	}
	private PjDoStatusRequest prepareResetStateRequest(OtherOrderTrackVo msgVo,DmpOrderFlow orderFlow){
		if(msgVo.getTpsno()==null){
			throw new RuntimeException("TPS运单号为空..");
		}
		
		if(orderFlow.getCwb()==null){
			throw new RuntimeException("订单号为空..");
		}

		ApplyEditDeliverystate aeds=getDmpDAO.getApplyEditDeliverystate(orderFlow.getCwb());
		if(aeds==null){
			throw new RuntimeException("从dmp没有获取到重置反馈信息");
		}
		String operateOrg=null;
		if(aeds.getApplybranchid()>0){
			operateOrg=getTpsBranchCodeById(aeds.getApplybranchid());
			if(operateOrg==null){
				throw new RuntimeException("没找到此操作机构，branchid="+aeds.getApplybranchid());
			}
		}
		
		Date applyDate=null;
		SimpleDateFormat sdf=new SimpleDateFormat(DATE_FORMAT);
		try {
			applyDate=sdf.parse(aeds.getApplytime());
		} catch (Exception e) {
			throw new RuntimeException("解析申请时间出错，aplytime="+aeds.getApplytime());
		}
		
		User operateUser=getDmpDAO.getUserById(aeds.getApplyuserid());//
		
		PjDoStatusRequest req= new PjDoStatusRequest();
		req.setTransportNo(msgVo.getTpsno());
		req.setType(6);//tps type 6 是重置状态
		req.setRemark(aeds.getEditreason());
		req.setOperName(operateUser==null?null:operateUser.getRealname());
		req.setOperOrgCode(operateOrg);
		req.setOperTime(applyDate);

		return req;
	}
	
	//get tpsbranchcode
	private String getTpsBranchCodeById(long branchid){
		String tpsBranchCode=null;	
		Branch branch = this.cacheBaseListener.getBranch(branchid);
		if (branch == null) {
			branch = this.getDmpDAO.getNowBranch(branchid);
		}
		if (branch != null) {
			tpsBranchCode=branch.getTpsbranchcode();
		}
		
		return tpsBranchCode;
	}
	
	//发送外单轨迹
	private void send(DoTrackFeedbackRequest request,long timeout) throws OspException{
		InvocationContext.Factory.getInstance().setTimeout(timeout);
		PjDeliveryOrderService pjDeliveryOrderService = new PjDeliveryOrderServiceHelper.PjDeliveryOrderServiceClient();
			
		pjDeliveryOrderService.feedbackDoTrack(request);//
	}
	
	//发送重置反馈
	private void sendResetState(PjDoStatusRequest req,long timeout) throws OspException{
		InvocationContext.Factory.getInstance().setTimeout(timeout);
		PjDeliveryOrderService pjDeliveryOrderService = new PjDeliveryOrderServiceHelper.PjDeliveryOrderServiceClient();

		List<PjDoStatusRequest> reqList=new ArrayList<PjDoStatusRequest>();
		reqList.add(req);
		List<PjDoStatusResponse> rspList=pjDeliveryOrderService.feedbackDoStatus(reqList);
		if(rspList==null||rspList.size()<1){
			throw new RuntimeException("TPS没返回响应");
		}else{
			PjDoStatusResponse rsp=rspList.get(0);
			if(rsp.getResultCode()!=1){
				throw new RuntimeException("TPS返回异常结果:"+rsp.getResultMsg());
			}
		}
		
	}
	
	private DmpOrderFlow parseOrderFlowObject(String json) throws Exception {
		ObjectMapper mapper = JacksonMapper.getInstance();
		DmpOrderFlow orderFlow = mapper.readValue(json, DmpOrderFlow.class);
		return orderFlow;
	}
	
	private CwbOrderWithDeliveryState parseDeliveryStateObject(String json) throws Exception{
		CwbOrderWithDeliveryState deliveryState= JacksonMapper.getInstance().readValue(json, CwbOrderWithDeliveryState.class);
		return deliveryState; 
	}
	
	public void saveOtherOrderTrack(DmpOrderFlow orderFlow,CwbOrderWithDeliveryState deliveryState,DmpCwbOrder co){
		try {
	   		ThirdPartyOrder2DOCfg pushCfg = tPOSendDoInfService.getThirdPartyOrder2DOCfg();
			if(pushCfg == null){
				logger.info("未配置外单轨迹推送DO服务配置信息!无法保存外单轨迹数据到临时表...");
				return;
			}
			if(pushCfg.getTrackOpenFlag() != 1){
				logger.info("未开启外单轨迹推送接口.");
				return;
			}
			this.logger.info("开始执行了外单轨迹TPS接口,cwb={},flowordertype={}", orderFlow.getCwb(), orderFlow.getFlowordertype());

			boolean isOther=false;
			DmpCwbOrder cwbOrder=(deliveryState==null)?null:deliveryState.getCwbOrder();
			if(cwbOrder==null){
				cwbOrder=co;
			}
			if(cwbOrder!=null){
				if(cwbOrder.getCustomerid()>0){ 
					isOther=tPOSendDoInfService.isThirdPartyCustomer(cwbOrder.getCustomerid());
				}
			}
			if(!isOther){
				this.logger.info("不是外单，轨迹数据不作处理,cwb={},flowordertype={}", orderFlow.getCwb(), orderFlow.getFlowordertype());
				return;
			}
			
			//Added by leoliao at 2016-08-03 归班反馈操作不保存在外单轨迹表
			if(orderFlow.getFlowordertype() == FlowOrderTypeEnum.YiFanKui.getValue()){
				this.logger.info("归班反馈操作不保存到外单轨迹表,cwb={},flowordertype={}", orderFlow.getCwb(), orderFlow.getFlowordertype());
				return;
			}
			//Added end
			
			int tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(orderFlow.getFlowordertype());
			if(tpsOperateType<0
					&&orderFlow.getFlowordertype()!=FlowOrderTypeEnum.ChongZhiFanKui.getValue()){
				this.logger.info("此轨迹不处理,cwb={},flowordertype={}", orderFlow.getCwb(), orderFlow.getFlowordertype());
				return ;
			}
			
			String orderFlowJson = bean2json(orderFlow);
			String deliveryStateJson = bean2json(deliveryState);
		
			OtherOrderTrackVo vo=new OtherOrderTrackVo();
			vo.setCwb(orderFlow.getCwb());
			vo.setFloworderid(orderFlow.getFloworderid());
			vo.setOrderFlowJson(orderFlowJson);
			vo.setDeliveryStateJson(deliveryStateJson);
			vo.setTracktime(orderFlow.getCredate());
			vo.setStatus(1);//
			vo.setTrytime(0);
			vo.setErrinfo("");
			vo.setFlowordertype(orderFlow.getFlowordertype());

			//为了性能，分站领货环节优先发送
			if(orderFlow.getFlowordertype()==FlowOrderTypeEnum.FenZhanLingHuo.getValue()){
				String sendTpsResult=sendFlowToTps(orderFlow, deliveryState);
				if(sendTpsResult==null){
					vo.setStatus(2);
					vo.setTrytime(1);
				}else{
					vo.setStatus(3);
					vo.setTrytime(1);
					vo.setErrinfo(sendTpsResult);
				}
			}
			
			this.otherOrderTrackService.saveOtherOrderTrack(vo);
			
		} catch (Exception e) {
			this.logger.error("保存外单轨迹TPS数据出错.cwb="+ orderFlow.getCwb()+",flowordertype="+orderFlow.getFlowordertype(),e);
		}
		
	}
	
	private String sendFlowToTps(DmpOrderFlow orderFlow,CwbOrderWithDeliveryState deliveryState){
		String result=null;
		try {
				String tpsno=otherOrderTrackService.getTpsTransportno(orderFlow.getCwb());
				
				OtherOrderTrackVo msgVo=new OtherOrderTrackVo();
				msgVo.setTpsno(tpsno);
				DoTrackFeedbackRequest req=prepareRequest(msgVo,orderFlow,deliveryState);
				if(req!=null){
					send(req,3000);
					this.logger.info("发送领货成功,cwb="+orderFlow.getCwb());
				}else{
					result="此外单轨迹环节忽略不处理";
				}
		} catch (Exception e) {
			result="发送外单领货轨迹到TPS时出错.原因:"+e.getMessage();
			this.logger.error("发送外单领货轨迹到TPS时出错.cwb="+ orderFlow.getCwb()+",flowordertype="+orderFlow.getFlowordertype(),e);
		}
		
		return result;
	}
	
	private String bean2json(Object object){
		if(object==null){
			return null;
		}
		 JsonConfig jsconfig = new JsonConfig(); 
		 jsconfig.registerJsonValueProcessor(java.sql.Timestamp.class, new cn.explink.b2c.tpsdo.TimestampJsonValueProcessor()); 
		 
		JSONObject jsonObj = JSONObject.fromObject(object,jsconfig);
		String json=jsonObj.toString();
		
		return json;
	}
	
	public void housekeepOtherOrder(){
   		ThirdPartyOrder2DOCfg pushCfg = tPOSendDoInfService.getThirdPartyOrder2DOCfg();
		if(pushCfg == null){
			logger.info("未配置外单临时表清理配置信息!不进行外单临时表清理...");
			return;
		}
		int trackday=pushCfg.getTrackHousekeepDay();
		int orderday=pushCfg.getHousekeepDay();
		
		if(pushCfg.getTrackOpenFlag()==1){
			if(trackday<7){
				trackday=7;
				logger.info("外单轨迹临时表数据保留天数至少为7.");
			}
			int tnum=otherOrderTrackService.housekeepOtherOrderTrack(trackday);
			logger.info("外单轨迹临时表数据清理了{}条.",tnum);
		}
		
		if(pushCfg.getOpenFlag()==1){
			if(orderday<15){
				orderday=15;
				logger.info("外单订单临时表数据保留天数至少为15.");
			}
			int onum=otherOrderTrackService.housekeepOtherOrder(orderday);
			logger.info("外单订单临时表数据清理了{}条.",onum);
		}
	}
	
	//获取配置信息
	public OrderTraceToTPSCfg getOrderTraceToTPSCfg() {
		OrderTraceToTPSCfg cfg = null;
		String objectMethod = this.b2ctools.getObjectMethod(B2cEnum.ThirdPartyOrder_2_DO.getKey()).getJoint_property();
		if (objectMethod != null) {
			JSONObject jsonObj = JSONObject.fromObject(objectMethod);
			cfg = (OrderTraceToTPSCfg) JSONObject.toBean(jsonObj, OrderTraceToTPSCfg.class);
		} 
		return cfg;
	}
}
