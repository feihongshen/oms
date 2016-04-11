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
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.B2cUtil;
import cn.explink.util.DateTimeUtil;
import cn.explink.util.RestHttpServiceHanlder;
import cn.explink.util.MD5.MD5Util;

@Service
public class JiuyeService {
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	private B2CDataDAO b2CDataDAO;
	@Autowired
	B2cUtil b2cUtil;
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
	
	public void feedback_status(){
		//订单配送信息提交接口
	/*	List<B2cEnum> enumsList=getB2cEnumKeys("jiuye_");
		if(enumsList==null||enumsList.size()==0){
			return;
		}*/
		JiuYe jiuye = this.b2cUtil.getEntitySettingMethod(B2cEnum.JiuYe1.getKey(), JiuYe.class);
		submitDeliveryInfo(FlowOrderTypeEnum.RuKu.getValue(),jiuye);
		submitDeliveryInfo(FlowOrderTypeEnum.ChuKuSaoMiao.getValue(),jiuye);
		submitDeliveryInfo(FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue(),jiuye);
		submitDeliveryInfo(FlowOrderTypeEnum.FenZhanLingHuo.getValue(),jiuye);
		submitDeliveryInfo(FlowOrderTypeEnum.YiShenHe.getValue(),jiuye);
	}
	
	/**
	 * 订单配送流程反馈
	 */
	public void submitDeliveryInfo(long flowordertype,JiuYe jiuye){
		//isB2cOpen(key)对接设置的开关判断
		if(!b2ctools.isB2cOpen(B2cEnum.JiuYe1.getKey())) {
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
					try{
						JiuyeXMLNote note = this.b2cUtil.getDataMethod(b2cdata.getJsoncontent(),JiuyeXMLNote.class);
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
					}catch (Exception e) {
						logger.error("0九曳0状态反馈异常,b2cid="+b2cdata.getB2cid()+",单号="+b2cdata.getCwb()+",异常信息:"+e.getMessage(),e);
					}
					}
			}
		} catch (Exception e) {
			logger.error("调用0九曳0webservice服务器异常"+e.getMessage(),e);
		}
	}
}
