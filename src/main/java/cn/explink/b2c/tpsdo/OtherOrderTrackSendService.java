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
import com.pjbest.deliveryorder.service.SignTrack;
import com.vip.osp.core.context.InvocationContext;
import com.vip.osp.core.exception.OspException;

import cn.explink.b2c.tpsdo.bean.OtherOrderTrackVo;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.User;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.jms.dto.CwbOrderWithDeliveryState;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsDateJsonBeanProcessor;
import net.sf.json.processors.JsDateJsonValueProcessor;

@Service
public class OtherOrderTrackSendService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private OtherOrderTrackService otherOrderTrackService;
	
	@Autowired
	private DmpTpsTrackMappingService dmpTpsTrackMappingService;
	
	@Autowired
	GetDmpDAO getDmpDAO;

	public void process() {
	       this.logger.info("other order track job process start...");
	       try {
		        //加载临时表数据
		        List<OtherOrderTrackVo> rowList= otherOrderTrackService.retrieveOtherOrderTrack(3000);
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
				send(req);
				
				otherOrderTrackService.completedTrackMsg(2,"",msgVo.getCwb(),msgVo.getFloworderid());
			} catch (Exception e) {
				logger.error("上传外单轨迹数据到TPS时出错.cwb="+msgVo.getCwb()+",floworderid="+msgVo.getFloworderid(),e);
				
				otherOrderTrackService.completedTrackMsg(3,e.getMessage(),msgVo.getCwb(),msgVo.getFloworderid());
			}
		}
	}
	
	private DoTrackFeedbackRequest prepareRequest(OtherOrderTrackVo msgVo,DmpOrderFlow orderFlow,CwbOrderWithDeliveryState orderWithState){
		if(orderWithState==null){
			throw new RuntimeException("CwbOrderWithDeliveryState is null.");
		}
		
		DmpCwbOrder cwbOrder=orderWithState.getCwbOrder();
		if(cwbOrder==null){
			throw new RuntimeException("CwbOrderWithDeliveryState.getCwbOrder is null.");
		}
		DmpDeliveryState ds=orderWithState.getDeliveryState();
		
		DoTrackFeedbackRequest req= new DoTrackFeedbackRequest();
		int tpsOperateType=dmpTpsTrackMappingService.getTpsOperateType(orderFlow.getFlowordertype());

		if(orderFlow.getFlowordertype()==FlowOrderTypeEnum.FenZhanLingHuo.getValue()){
			long deliveryid=0;
			if(ds!=null){
				deliveryid=ds.getDeliveryid();
			}else{
				deliveryid=cwbOrder.getDeliverid();
			}
			
			User user=getDmpDAO.getUserById(deliveryid);//
			DeliveryTrack deliveryTrack=new DeliveryTrack();
			deliveryTrack.setCourier(user==null?null:user.getUsername());
			deliveryTrack.setCourierId(""+deliveryid);//id or deliverManCode ???
			deliveryTrack.setCourierTel(user==null?null:user.getUsermobile());//or mobile??? format ?
			//deliveryTrack.setDestinationOrg(""+cwbOrder.getDeliverybranchid());
			req.setDelivery(deliveryTrack);
		}
		
		//需要审核or不用审核???
		if(orderFlow.getFlowordertype()==FlowOrderTypeEnum.YiFanKui.getValue()){
			
			if(ds==null){
				throw new RuntimeException("CwbOrderWithDeliveryState.getDeliveryState is null.");
			}
			
			//以下需要检查配送成功与否???
			//is it getReceivedfee???
			req.setActualFee(ds.getReceivedfee()==null?null:ds.getReceivedfee().doubleValue());
			req.setActualPayType(cwbOrder.getNewpaywayid());//cwbOrder.getPaywayid()???
			//if(ds.getSign_typeid()==1){//已签收
				SignTrack signTrack=new SignTrack();
				signTrack.setSignMan(ds.getSign_man());
				req.setSign(signTrack);
			//}
		} 
		
		req.setException(null);//
		req.setNextOrg(""+cwbOrder.getNextbranchid());//
		req.setOperateOrg(""+orderFlow.getBranchid());//
		req.setOperater(orderFlow.getUsername());//
		req.setOperateTime(orderFlow.getCredate());
		req.setOperateType(tpsOperateType);
		req.setReason(null);//???
		req.setTransportNo(msgVo.getTpsno());
		
		return req;
	}
	
	private void send(DoTrackFeedbackRequest request) throws OspException{
		InvocationContext.Factory.getInstance().setTimeout(10000);
		PjDeliveryOrderService pjDeliveryOrderService = new PjDeliveryOrderServiceHelper.PjDeliveryOrderServiceClient();
			
		pjDeliveryOrderService.feedbackDoTrack(request);//
	}
	
	private DmpOrderFlow parseOrderFlowObject(String json){
		 JsonConfig jsconfig = new JsonConfig(); 
		 jsconfig.registerJsonBeanProcessor(java.util.Date.class, new JsDateJsonBeanProcessor());
		 
		JSONObject jsonObj = JSONObject.fromObject(json);
		DmpOrderFlow orderFlow=(DmpOrderFlow) JSONObject.toBean(jsonObj, DmpOrderFlow.class);
		return orderFlow;
	}
	
	private CwbOrderWithDeliveryState parseDeliveryStateObject(String json){
		JSONObject jsonObj = JSONObject.fromObject(json);
		CwbOrderWithDeliveryState deliveryState=(CwbOrderWithDeliveryState) JSONObject.toBean(jsonObj, CwbOrderWithDeliveryState.class);
		return deliveryState; 
	}
}
