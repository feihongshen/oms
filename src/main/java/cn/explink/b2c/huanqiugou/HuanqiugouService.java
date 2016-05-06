package cn.explink.b2c.huanqiugou;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.huanqiugou.respDto.ResponseDto;
import cn.explink.b2c.hxgdms.DmsResponse;
import cn.explink.b2c.hxgdms.Hxgdms;
import cn.explink.b2c.hxgdms.HxgdmsJsonError;
import cn.explink.b2c.hxgdms.HxgdmsJsonTrack;
import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.ExptReason;
import cn.explink.b2c.tools.FlowFromJMSB2cService;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.b2c.tools.JointEntity;
import cn.explink.b2c.weisuda.xml.ObjectUnMarchal;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.B2CData;
import cn.explink.domain.User;
import cn.explink.enumutil.CwbOrderTypeIdEnum;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;
import cn.explink.util.RestHttpServiceHanlder;
import cn.explink.util.MD5.Base64Utils;
import cn.explink.util.MD5.MD5Util;

@Service
public class HuanqiugouService {
	private Logger logger =LoggerFactory.getLogger(HuanqiugouService.class);
	@Autowired
	private GetDmpDAO getdmpDAO;
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	B2CDataDAO b2cDataDAO;
	@Autowired
	FlowFromJMSB2cService flowFromJMSB2cService;
	
	public String getHxgdmsFlowEnum(long flowordertype,long deliverystate,String cwbordertypeidstr) {
		long cwbordertypeid=Long.valueOf(cwbordertypeidstr);
	
		if(flowordertype==FlowOrderTypeEnum.RuKu.getValue()&&cwbordertypeid==CwbOrderTypeIdEnum.Peisong.getValue()
		){
			return HuanqiugouFlowEnum.Ruku.getDms_code();
		}
		if(flowordertype==FlowOrderTypeEnum.ChuKuSaoMiao.getValue()&&cwbordertypeid==CwbOrderTypeIdEnum.Peisong.getValue()
		){
			return HuanqiugouFlowEnum.ChuKuSaoMiao.getDms_code();
		}
		
		if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()&&(deliverystate==DeliveryStateEnum.PeiSongChengGong.getValue())
		){
			return HuanqiugouFlowEnum.QianShou.getDms_code();
		}
		if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()&&(deliverystate==DeliveryStateEnum.ShangMenTuiChengGong.getValue())
		){
			return HuanqiugouFlowEnum.TuiHuoDingDanShengCheng.getDms_code();
		}
		
		if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()&&(deliverystate==DeliveryStateEnum.ShangMenJuTui.getValue())
				){
					return HuanqiugouFlowEnum.QuJianShiBai.getDms_code();
				}
		
		if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()&&deliverystate==DeliveryStateEnum.QuanBuTuiHuo.getValue()
		){
			return HuanqiugouFlowEnum.JuShou.getDms_code();
		}
		if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()&&cwbordertypeid==CwbOrderTypeIdEnum.Peisong.getValue()
		&&deliverystate==DeliveryStateEnum.FenZhanZhiLiu.getValue()
		){
			return HuanqiugouFlowEnum.PaisongYiChang.getDms_code();
		}
		
		if(flowordertype==FlowOrderTypeEnum.FenZhanLingHuo.getValue()&&cwbordertypeid==CwbOrderTypeIdEnum.Shangmentui.getValue()
			
				){
					return HuanqiugouFlowEnum.TuiHuoQuJianZhong.getDms_code();
				}
				
		
		
		if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()&&cwbordertypeid==CwbOrderTypeIdEnum.Shangmentui.getValue()
				&&deliverystate==DeliveryStateEnum.FenZhanZhiLiu.getValue()
				){
					return HuanqiugouFlowEnum.QuJianYiChang.getDms_code();
				}
		
		
		if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()&&cwbordertypeid==CwbOrderTypeIdEnum.Peisong.getValue()
				&&deliverystate==DeliveryStateEnum.HuoWuDiuShi.getValue()
				){
					return HuanqiugouFlowEnum.DiuShi.getDms_code();
				}
		
		
		
		if(flowordertype==FlowOrderTypeEnum.TuiHuoZhanRuKu.getValue()&&cwbordertypeid==CwbOrderTypeIdEnum.Shangmentui.getValue()
				){
					return HuanqiugouFlowEnum.TuiHuoRuFenBoDian.getDms_code();
				}
		if(flowordertype==FlowOrderTypeEnum.TuiGongYingShangChuKu.getValue()&&cwbordertypeid==CwbOrderTypeIdEnum.Shangmentui.getValue()
				){
					return HuanqiugouFlowEnum.TuiHuoChuFenBoDian.getDms_code();
				}
		
		return null;
		
	}
	
	
	public Huanqiugou getHuanqiuGou(int key) {
		Huanqiugou smile = null;
		if (this.getObjectMethod(key)!=null) {
			JSONObject jsonObj = JSONObject.fromObject(getObjectMethod(key));
			smile = (Huanqiugou) JSONObject.toBean(jsonObj, Huanqiugou.class);
		} else {
			smile = new Huanqiugou();
		}
		return smile;
	}
	
	/**
	 * 获取环球购物配置信息的接口
	 * @param key
	 * @return
	 */
	private String getObjectMethod(int key) {
		try {
			JointEntity obj = getdmpDAO.getJointEntity(key);
			return obj.getJoint_property();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	

	
	
	/**
	 * 反馈[环球购物]订单信息
	 */
	public void feedback_status(){
		
		Huanqiugou dms=getHuanqiuGou(B2cEnum.HuanQiuGou.getKey());
		if(!b2ctools.isB2cOpen(B2cEnum.HuanQiuGou.getKey())) {
			logger.info("未开[环球购物]的对接!");
			return ;
		} 
		
		sendCwbStatus_To_dms(dms);  
	    
	    
	}
	
	/**
	 * 状态反馈接口开始
	 * @param dms
	 */
	private void sendCwbStatus_To_dms(Huanqiugou dms){
		try {
			
			int i=0;
			while(true){
				List<B2CData> datalist=b2cDataDAO.getDataListByFlowStatus(dms.getCustomerids(),dms.getMaxcount());
				i++;
				if(i>100){
					logger.warn("环球购物状态反馈已经超过100次循环，可能存在程序未知异常,请及时查询并处理");
					return ;
				}
				
				if(datalist==null||datalist.size()==0){
					logger.info("当前没有要推送[环球购物]的数据");
					return ;
				}
				DealWithBuildXMLAndSending(dms, datalist);
				
			}
		
		} catch (Exception e) {
			logger.error("推送环球购物系统发生未知异常",e);
		}
		
		
		
	}


	private void DealWithBuildXMLAndSending(Huanqiugou dms, List<B2CData> datalist)
			throws Exception {
		for(B2CData data:datalist){
			
			String jsoncontent=data.getJsoncontent();
			
			
			HuanqiugouNote note = JacksonMapper.getInstance().readValue(jsoncontent, HuanqiugouNote.class);
			String xml="<listexpressmail>"
							+"<expressmail>"
								+"<expressid>"+dms.getExpressid()+"</expressid>"
								+"<serialnumber>"+data.getB2cid()+"</serialnumber>"
								+"<mailno>"+note.getMailno()+"</mailno>"
								+"<submailno>"+note.getSubmailno()+"</submailno>"
								+"<procdate>"+note.getProcdate()+"</procdate>"
								+"<orgfullname>"+note.getOrgfullname()+"</orgfullname>"
								+"<operator>"+note.getOperator()+"</operator>"
								+"<action>"+note.getAction()+"</action>"
								+"<description>"+note.getDescription()+"</description>"
								+"<exception_action>"+(note.getException_action()==null?"":note.getException_action())+"</exception_action>"
								+"<exception_description>"+(note.getException_description()==null?"":note.getException_description())+"</exception_description>"
								+"<notes></notes>"
							+ "</expressmail>"
					+ "</listexpressmail>";
			
			String signed=MD5Util.md5(dms.getPrivateKey()+xml+dms.getPrivateKey());
			String base64=Base64Utils.base64(signed, "UTF-8").toUpperCase();
			
			Map<String,String> params=new HashMap<String, String>();
			params.put("data",xml);
			params.put("sign", base64);
			params.put("method", "putExpressStatus");
			params.put("expressid",dms.getExpressid());
			params.put("timestamp",DateTimeUtil.getNowTime());
			
			String jsonresponse=RestHttpServiceHanlder.sendHttptoServer(params, dms.getFeedbackUrl());
			
			logger.info("当前环球购物返回data={},flowordertype="+data.getFlowordertype()+",jsonresponse={}",data.getCwb(),jsonresponse);
			
			ResponseDto dmsResponse=(ResponseDto) ObjectUnMarchal.XmltoPOJO(jsonresponse,new ResponseDto());
			
			int send_b2c_flag=dmsResponse.getReturnDto().getReturnFlag()==1?1:2;
			String remark=dmsResponse.getReturnDto().getReturnDesc();
			
			b2cDataDAO.updateB2cIdSQLResponseStatus(data.getB2cid(),send_b2c_flag,remark);
		}
	}



	
	
	//获取tmall XML Note
	public HuanqiugouNote getSmileXMLNoteMethod(String jsoncontent) {
			try {
				return JacksonMapper.getInstance().readValue(jsoncontent,HuanqiugouNote.class);
			} catch (Exception e) {
				logger.error("获取SmileXMLNote异常！jsoncontent:"+jsoncontent+e);
				return null;
			}
	}
	/**
	 * 跟踪日志反馈
	 * @param orderFlow
	 * @param cwbOrder
	 */
	public void sendTrackInfoByHxgdms(DmpOrderFlow orderFlow,DmpCwbOrder cwbOrder,String dmsReceiveStatus,Hxgdms dms) {
		
		try {
			if(!b2ctools.isB2cOpen(B2cEnum.Hxgdms.getKey())) {
				logger.info("未开[环球购物]的对接!");
				return;
			}
			
			if(orderFlow.getFlowordertype()==FlowOrderTypeEnum.FenZhanLingHuo.getValue()||orderFlow.getFlowordertype()==FlowOrderTypeEnum.YiShenHe.getValue()){
				logger.info("好享购订单流转信息取消领货，反馈，异常环节推送,cwb={}",orderFlow.getCwb());
				return;
			}
			
			User operatorUser=getdmpDAO.getUserById(orderFlow.getUserid());
			////执行跟踪日志反馈的方法
			
			HxgdmsJsonTrack hxgdmsJsonTrack=new HxgdmsJsonTrack();
			hxgdmsJsonTrack.setWorkCode(cwbOrder.getCwb());
			hxgdmsJsonTrack.setFlowType(dmsReceiveStatus);
			hxgdmsJsonTrack.setOperationDesc(orderFlowDetail.getDetail(orderFlow));
			hxgdmsJsonTrack.setOperationName(operatorUser.getRealname());
			hxgdmsJsonTrack.setOperationTime(DateTimeUtil.formatDate(orderFlow.getCredate()));
			hxgdmsJsonTrack.setDelveryCode(dms.getDcode());
			
			String jsoncontent=null;
			try {
				jsoncontent = JacksonMapper.getInstance().writeValueAsString(hxgdmsJsonTrack);
			} catch (Exception e) {
				logger.error("jackson转化异常,cwb="+cwbOrder.getCwb(),e);
			} 
			String signed=MD5Util.md5(jsoncontent+dms.getSecretKey());
			
			Map<String,String> params=new HashMap<String, String>();
			params.put("Request",jsoncontent);
			params.put("Signed", signed);
			params.put("Action", "RequestSendTrackToDMS");
			params.put("Dcode",dms.getDcode());
			
			String jsonresponse=RestHttpServiceHanlder.sendHttptoServer(params, dms.getFeedbackUrl());
			logger.info("环球购物跟踪信息-返回,cwb={},jsonresponse={}",orderFlow.getCwb(),jsonresponse);
		} catch (Exception e) {
			logger.error("推送环球购物跟踪日志异常cwb="+orderFlow.getCwb(),e);
		}
			    
	}
	

	/**
	 * 处理推送失败问题
	 * @param cwbOrder
	 * @param delivery_state
	 * @param dms
	 */
	public void sendErrorByHxgdms(DmpCwbOrder cwbOrder, long delivery_state,Hxgdms dms) {
		try {
			HxgdmsJsonError hxgdmsJsonError=new HxgdmsJsonError();
			hxgdmsJsonError.setWorkCode(cwbOrder.getCwb());
			hxgdmsJsonError.setDelveryCode(dms.getDcode());
			String errorCode="";
			String errorMsg="";
			
			if(delivery_state==DeliveryStateEnum.HuoWuDiuShi.getValue()){
			 	 ExptReason exptReasonLose = b2ctools.getDiushiReasonByB2c(cwbOrder.getLosereasonid(), cwbOrder.getCustomerid()+"");
				 errorCode=exptReasonLose.getExpt_code();
				 errorMsg=exptReasonLose.getExpt_msg();
				 if(errorCode==null||errorCode.isEmpty()){
					 errorCode="1";
					 errorMsg="派送失败_丢失";
				 }
				 
			}else if(delivery_state==DeliveryStateEnum.QuanBuTuiHuo.getValue()){
				ExptReason exptReason = b2ctools.getExptReasonByB2c(0,cwbOrder.getBackreasonid(),cwbOrder.getCustomerid()+"",delivery_state);
				errorCode=exptReason.getExpt_code();
				errorMsg=exptReason.getExpt_msg();
				if(errorCode==null||errorCode.isEmpty()){
					 errorCode="12";
					 errorMsg="拒收报备_其它";
				}
			}else if(delivery_state==DeliveryStateEnum.FenZhanZhiLiu.getValue()){
				ExptReason exptReason = b2ctools.getExptReasonByB2c(cwbOrder.getLeavedreasonid(),0,cwbOrder.getCustomerid()+"",delivery_state);
				errorCode=exptReason.getExpt_code();
				errorMsg=exptReason.getExpt_msg();
				if(errorCode==null||errorCode.isEmpty()){
					 errorCode="5";
					 errorMsg="派送未果_其它";
				}
			}
			 hxgdmsJsonError.setErrorCode(errorCode);
			 hxgdmsJsonError.setErrorNote(errorMsg);
			 
			String jsoncontent=null;
			try {
				jsoncontent = JacksonMapper.getInstance().writeValueAsString(hxgdmsJsonError);
			} catch (Exception e) {
				logger.error("jackson转化异常,cwb="+cwbOrder.getCwb(),e);
			} 
			String signed=MD5Util.md5(jsoncontent+dms.getSecretKey());
			
			Map<String,String> params=new HashMap<String, String>();
			params.put("Request",jsoncontent);
			params.put("Signed", signed);
			params.put("Action", "RequestSendErrorToDMS");
			params.put("Dcode",dms.getDcode());
			String jsonresponse=RestHttpServiceHanlder.sendHttptoServer(params, dms.getFeedbackUrl());
			logger.info("环球购物异常信息-返回,jsonresponse={}",jsonresponse);
		} catch (Exception e) {
			logger.error("推送环球购物异常反馈异常cwb="+cwbOrder.getCwb(),e);
		}
		 
	}

	
	
	
	
	


	


}
