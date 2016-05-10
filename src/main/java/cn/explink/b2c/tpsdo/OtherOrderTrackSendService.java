package cn.explink.b2c.tpsdo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pjbest.deliveryorder.bizservice.PjDeliveryOrderService;
import com.pjbest.deliveryorder.bizservice.PjDeliveryOrderServiceHelper;
import com.pjbest.deliveryorder.service.DeliveryTrack;
import com.pjbest.deliveryorder.service.DoTrackFeedbackRequest;
import com.pjbest.deliveryorder.service.ExceptionTrack;
import com.pjbest.deliveryorder.service.SignTrack;
import com.vip.osp.core.context.InvocationContext;
import com.vip.osp.core.exception.OspException;

import cn.explink.b2c.tools.CacheBaseListener;
import cn.explink.b2c.tpsdo.bean.OtherOrderTrackVo;
import cn.explink.b2c.tpsdo.bean.ThirdPartyOrder2DOCfg;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.User;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.enumutil.PaytypeEnum;
import cn.explink.jms.dto.CwbOrderWithDeliveryState;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.JSONUtils;

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

	private static final String DATE_FORMAT="yyyy-MM-dd HH:mm:ss";
	
	public void process() {
	       this.logger.info("other order track job process start...");
	      
	       
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
				DoTrackFeedbackRequest req=prepareRequest(msgVo,orderFlow,orderWithState);
				if(req!=null){
					logger.info("上传外单cwb={}轨迹报文：{}", orderFlow.getCwb(), com.alibaba.fastjson.JSONObject.toJSONString(req));
					
					send(req,60000);
					otherOrderTrackService.completedTrackMsg(2,"",msgVo.getCwb(),msgVo.getFloworderid());
				}else{
					otherOrderTrackService.completedTrackMsg(4,"",msgVo.getCwb(),msgVo.getFloworderid());
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
		
		//需要审核or不用审核???
		if(orderFlow.getFlowordertype()==FlowOrderTypeEnum.YiShenHe.getValue()){
			
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
				ExceptionTrack et=new ExceptionTrack ();
				//et.setExceptionType(""+DeliveryStateEnum.QuanBuTuiHuo.getValue());
				et.setExceptionReason(DeliveryStateEnum.QuanBuTuiHuo.getText()+":"+ds.getBackreason());
				req.setException(et);
			}else if(ds.getDeliverystate()==DeliveryStateEnum.BuFenTuiHuo.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.BuFenTuiHuo.getValue());
				ExceptionTrack et=new ExceptionTrack ();
				//et.setExceptionType(""+DeliveryStateEnum.BuFenTuiHuo.getValue());
				et.setExceptionReason(DeliveryStateEnum.BuFenTuiHuo.getText()+":"+ds.getBackreason());
				req.setException(et);
			}else if(ds.getDeliverystate()==DeliveryStateEnum.ShangMenJuTui.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.ShangMenJuTui.getValue());
				ExceptionTrack et=new ExceptionTrack ();
				//et.setExceptionType(""+DeliveryStateEnum.ShangMenJuTui.getValue());
				et.setExceptionReason(""+DeliveryStateEnum.ShangMenJuTui.getText());//??????
				req.setException(et);
			}else if(ds.getDeliverystate()==DeliveryStateEnum.HuoWuDiuShi.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.HuoWuDiuShi.getValue());
				ExceptionTrack et=new ExceptionTrack ();
				//et.setExceptionType(""+DeliveryStateEnum.HuoWuDiuShi.getValue());
				et.setExceptionReason(""+DeliveryStateEnum.HuoWuDiuShi.getText());//??????
				req.setException(et);
			}else if(ds.getDeliverystate()==DeliveryStateEnum.FenZhanZhiLiu.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.FenZhanZhiLiu.getValue());
				ExceptionTrack et=new ExceptionTrack ();
				//et.setExceptionType(""+DeliveryStateEnum.FenZhanZhiLiu.getValue());
				et.setExceptionReason(DeliveryStateEnum.FenZhanZhiLiu.getText()+":"+ds.getLeavedreason());
				req.setException(et);
			}else if(ds.getDeliverystate()==DeliveryStateEnum.ZhiLiuZiDongLingHuo.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.FenZhanZhiLiu.getValue());//???
				ExceptionTrack et=new ExceptionTrack ();
				//et.setExceptionType(""+DeliveryStateEnum.ZhiLiuZiDongLingHuo.getValue());
				et.setExceptionReason(DeliveryStateEnum.ZhiLiuZiDongLingHuo.getText()+":"+ds.getLeavedreason());
				req.setException(et);
			}else if(ds.getDeliverystate()==DeliveryStateEnum.DaiZhongZhuan.getValue()){
				tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(FlowOrderTypeEnum.ZhongZhuanChuKuSaoMiao.getValue());
			}else{
				return null;
			}
		}
		
		User operateUser=getDmpDAO.getUserById(orderFlow.getUserid());//
		
		if(cwbOrder.getInfactfare()!=null&&cwbOrder.getInfactfare().doubleValue()>0){
			req.setActualFee(cwbOrder.getInfactfare().doubleValue());
			/**
			 *  需要根据dmp的支付方式映射成TPS的支付方式 updated by gordon.zhou 2016.5.3
			 */
			String newpaywayidStr = cwbOrder.getNewpaywayid();
			try{
				int newpaywayid = Integer.valueOf(newpaywayidStr);
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
			}catch(Exception e){
				
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
		
		//req.setException(null);//
		req.setNextOrg(nextOrg);//tps机构编码
		req.setOperateOrg(operateOrg);//tps机构编码
		req.setOperater(operateUser==null?null:operateUser.getRealname());//???
		req.setOperateTime(orderFlow.getCredate());
		req.setOperateType(tpsOperateType);
		req.setReason(null);//???
		req.setTransportNo(msgVo.getTpsno());
		
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
	
	private void send(DoTrackFeedbackRequest request,long timeout) throws OspException{
		InvocationContext.Factory.getInstance().setTimeout(timeout);
		PjDeliveryOrderService pjDeliveryOrderService = new PjDeliveryOrderServiceHelper.PjDeliveryOrderServiceClient();
			
		pjDeliveryOrderService.feedbackDoTrack(request);//
	}
	
	private DmpOrderFlow parseOrderFlowObject(String json){
		String[] formats={DATE_FORMAT};  
		 JSONUtils.getMorpherRegistry().registerMorpher(new TimestampMorpher(formats));  
		 
		JSONObject jsonObj = JSONObject.fromObject(json);
		DmpOrderFlow orderFlow=(DmpOrderFlow) JSONObject.toBean(jsonObj, DmpOrderFlow.class);
		
		return orderFlow;
	}
	
	private CwbOrderWithDeliveryState parseDeliveryStateObject(String json){
		String[] formats={DATE_FORMAT};  
		 JSONUtils.getMorpherRegistry().registerMorpher(new TimestampMorpher(formats));  
		 
		JSONObject jsonObj = JSONObject.fromObject(json);
		CwbOrderWithDeliveryState deliveryState=(CwbOrderWithDeliveryState) JSONObject.toBean(jsonObj, CwbOrderWithDeliveryState.class);
		return deliveryState; 

	}
	
	public void saveOtherOrderTrack(DmpOrderFlow orderFlow,CwbOrderWithDeliveryState deliveryState){
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
			DmpCwbOrder cwbOrder=deliveryState.getCwbOrder();
			if(cwbOrder!=null){
				//isOther=true; 
				isOther=tPOSendDoInfService.isThirdPartyCustomer(cwbOrder.getCustomerid());
			}
			if(!isOther){
				this.logger.info("不是外单，轨迹数据不作处理,cwb={},flowordertype={}", orderFlow.getCwb(), orderFlow.getFlowordertype());
				return;
			}
			
			int tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(orderFlow.getFlowordertype());
			if(tpsOperateType<0){
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
}
