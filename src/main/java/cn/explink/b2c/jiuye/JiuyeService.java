package cn.explink.b2c.jiuye;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.jiuye.jsondto.JiuYe_request;
import cn.explink.b2c.jiuye.jsondto.JiuYe_response;
import cn.explink.b2c.jiuye.jsondto.JiuyeXMLNote;
import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.domain.B2CData;
import cn.explink.domain.Customer;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.DateTimeUtil;
import cn.explink.util.RestHttpServiceHanlder;
import cn.explink.util.MD5.MD5Util;

@Service
public class JiuyeService {
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	private B2CDataDAO b2CDataDAO;
	private Logger logger =LoggerFactory.getLogger(JiuyeService.class);
	
	public String filterJiuyeFlowEnum(long flowordertype,long deliverystate) {
		if(flowordertype!=FlowOrderTypeEnum.YiShenHe.getValue()){
			for(JiuyeFlowEnum TEnum:JiuyeFlowEnum.values()){
				if(flowordertype==TEnum.getFlowordertype()){
					return TEnum.getRequest_code();//请求指令
				}
			}
		}
		
		if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()&&
				(deliverystate==DeliveryStateEnum.PeiSongChengGong.getValue()||deliverystate==DeliveryStateEnum.ShangMenTuiChengGong.getValue()
						||deliverystate==DeliveryStateEnum.ShangMenHuanChengGong.getValue())
		){
			return JiuyeFlowEnum.TMS_SIGN.getRequest_code();
		}
		
		if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()&&deliverystate==DeliveryStateEnum.FenZhanZhiLiu.getValue()
		){
			return JiuyeFlowEnum.TMS_ERROR.getRequest_code();
		}
		
		if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()&&
				(deliverystate==DeliveryStateEnum.QuanBuTuiHuo.getValue()||deliverystate==DeliveryStateEnum.BuFenTuiHuo.getValue())
				
		){
			return JiuyeFlowEnum.TMS_FAILED.getRequest_code();
		}
		
		return null;
		
	}
	
	
	//获取配置信息
	public JiuYe getJiuyeSettingMethod(int key) {
		JiuYe jiuye = null;
		//b2ctools.getObjectMethod(key)为获取b2c 配置信息的接口
		String objectMethod=b2ctools.getObjectMethod(key).getJoint_property();
		if (objectMethod!=null) {
			JSONObject jsonObj = JSONObject.fromObject(objectMethod);
			jiuye = (JiuYe) JSONObject.toBean(jsonObj, JiuYe.class);//将json对象转化为bean
		} else {
			jiuye = new JiuYe();
		}
		return jiuye;
	}
	
	
	public void feedback_status(){
		//订单配送信息提交接口
		List<B2cEnum> enumsList=getB2cEnumKeys("jiuye_");
		if(enumsList==null||enumsList.size()==0){
			return;
		}
		for(B2cEnum enums:enumsList){
			SubmitDeliveryInfo(FlowOrderTypeEnum.RuKu.getValue(),enums);
			SubmitDeliveryInfo(FlowOrderTypeEnum.ChuKuSaoMiao.getValue(),enums);
			SubmitDeliveryInfo(FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue(),enums);
			SubmitDeliveryInfo(FlowOrderTypeEnum.FenZhanLingHuo.getValue(),enums);
			SubmitDeliveryInfo(FlowOrderTypeEnum.YiShenHe.getValue(),enums);
		}
	}
	
	public List<B2cEnum> getB2cEnumKeys(String constainsStr) {
		List<B2cEnum> enumsList=new ArrayList<B2cEnum>();
		for (B2cEnum enums : B2cEnum.values()) {
			if (enums.getMethod().contains(constainsStr)) {
				enumsList.add(enums);
			}
		}
		return enumsList;
	}
	
	//得到表单字段对应的javabean
	public JiuyeXMLNote getXMLNoteMethod(String jsoncontent) throws JsonParseException, JsonMappingException, IOException {
		return new ObjectMapper().readValue(jsoncontent,JiuyeXMLNote.class);
	}
	
	/**
	 * 订单配送流程反馈
	 */
	public void SubmitDeliveryInfo(long flowordertype,B2cEnum enums){
		JiuYe jiuye=getJiuyeSettingMethod(enums.getKey());
		//isB2cOpen(key)对接设置的开关判断
		if(!b2ctools.isB2cOpen(enums.getKey())) {
			logger.info("未开启0九曳0状态反馈接口");
			return ;//当开关未开启时，停止下面的所有操作
		} 
		
		try {
		
			int i=0;
			while(true){
				//遍历数据库中每条
				List<B2CData> jiuyelist=b2CDataDAO.getDataListByFlowStatus(flowordertype,jiuye.getCustomerid(),jiuye.getMaxCount());
				i++;
				if(i>100){
					String warning="查询0九曳0状态反馈已经超过100次循环，每次100条，可能存在程序未知异常,请及时查询并处理!";
					logger.warn(warning);
					return ;
				}
				
				if(jiuyelist==null||jiuyelist.size()==0){
					logger.info("当前没有要推送0九曳0的数据");
					return ;
				}
				//遍历每一条表单整体信息
				for(B2CData b2cdata:jiuyelist){
					JiuyeXMLNote note=getXMLNoteMethod(b2cdata.getJsoncontent());
					JiuYe_request jiuyereq=new JiuYe_request();
					jiuyereq.setRequestName("RequestOrderStateToDMS");
					jiuyereq.setDelveryCode(jiuye.getDmsCode());
					String timeStamp=DateTimeUtil.getNowTime();
					jiuyereq.setSign(MD5Util.md5(timeStamp+jiuye.getPrivate_key()));
					jiuyereq.setTimeStamp(timeStamp);
					jiuyereq.setContent(note);
					
					String requestJson=JacksonMapper.getInstance().writeValueAsString(jiuyereq);
					logger.info("请求九曵参数为:{}",requestJson);
					String responseJson=RestHttpServiceHanlder.sendHttptoServer(requestJson, jiuye.getFeedbackUrl());
					logger.info("九曵返回的数据为:{}",responseJson);
					JiuYe_response response=JacksonMapper.getInstance().readValue(responseJson, JiuYe_response.class);
					
					int send_b2c_flag=1;
					if(response.getSuccess().equals("true")){
						send_b2c_flag=1;
					}else{
						send_b2c_flag=2;
					}
					
					
					b2CDataDAO.updateB2cIdSQLResponseStatus(b2cdata.getB2cid(), send_b2c_flag, response.getMsg());
					
				}
				
			
			}
			
			
		} catch (Exception e) {
			logger.error("调用0九曳0webservice服务器异常"+e.getMessage(),e);
		}
		
		
	}

	
	
}
