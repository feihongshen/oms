package cn.explink.b2c.meilinkai;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.domain.B2CData;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.WebServiceHandler;

@Service
public class MLKService {
	@Autowired
	B2cTools b2ctools;
	@Autowired
	B2CDataDAO b2CDataDAO;
	private Logger logger = LoggerFactory.getLogger(MLKService.class);
	
	/**
	 * 【玫琳凯】反馈接口入口
	 */
	public void feedback_status() throws Exception{
		MeiLinKai meilinkai = (MeiLinKai)getEntitySettingMethod(B2cEnum.meilinkai.getKey(),MeiLinKai.class);
		//订单配送信息提交接口
		submitDeliveryInfo(FlowOrderTypeEnum.RuKu.getValue(),meilinkai);
		submitDeliveryInfo(FlowOrderTypeEnum.YiFanKui.getValue(),meilinkai);
	}
	/**
	 * 【玫琳凯】反馈货态主方法
	 * @param flowordertype
	 */
	private void submitDeliveryInfo(long flowordertype,MeiLinKai meilinkai) throws Exception{
		String hdTOlipsUrl = meilinkai.getHdtolipsUrl();
		String username = meilinkai.getUsrename();
		String pwd = meilinkai.getPwd();
		String methodCheck = meilinkai.getCheckUsermethod();
		String methodHDToLIPSByWebService = meilinkai.getHdtolipsmethod();
		//isB2cOpen(key)对接设置的开关判断
		if(!b2ctools.isB2cOpen(B2cEnum.meilinkai.getKey())) {
			logger.info("未开启【玫琳凯】状态反馈接口");
			return ;//当开关未开启时，停止下面的所有操作
		} 
		try {
			int i=0;
			while(true){
				//遍历数据库中每条
				long maxCount = "".equals(meilinkai.getMaxCount())?0:(Long.valueOf(meilinkai.getMaxCount()));
				List<B2CData> mlklist=this.b2CDataDAO.getDataListByFlowStatus(flowordertype,meilinkai.getCustomerid(),maxCount);
				i++;
				if(i>100){
					String warning="查询【玫琳凯】状态反馈已经超过100次循环，每次100条，可能存在程序未知异常,请及时查询并处理!";
					logger.warn(warning);
					return ;
				}
				
				if(mlklist==null||mlklist.size()==0){
					this.logger.info("当前没有要推送【玫琳凯】的数据");
					return ;
				}
				//遍历每一条表单整体信息
				for(B2CData b2cdata:mlklist){
					String cwb = b2cdata.getCwb();
					int send_b2c_flag = 1;
					try{
						//1.用户校验
						Object[] obje = new Object[]{username,pwd};
						this.logger.info("请求【玫琳凯】用户校验参数为:{},订单号:{}",Arrays.toString(obje),cwb);
						Integer inte = (Integer)WebServiceHandler.invokeWs(hdTOlipsUrl, methodCheck, obje);
						this.logger.info("【玫琳凯】返回的数据为:{},订单号:{}",inte,cwb);
						
						if(inte == HDtoLipsEnum.REASON_1.getResult()){
							this.logger.info("【玫琳凯】用户校验失败,订单号:{}",cwb);
							send_b2c_flag = 2;
							this.b2CDataDAO.updateB2cIdSQLResponseStatus(b2cdata.getB2cid(), send_b2c_flag,HDtoLipsEnum.REASON_1.getCheckUsertext());
							//continue;
						}else if(inte == HDtoLipsEnum.REASON_2.getResult()){
							this.logger.info("【玫琳凯】用户校验失败,订单号:{}",cwb);
							send_b2c_flag = 2;
							this.b2CDataDAO.updateB2cIdSQLResponseStatus(b2cdata.getB2cid(), send_b2c_flag,HDtoLipsEnum.REASON_2.getCheckUsertext());
							//continue;
						}else if(inte == HDtoLipsEnum.REASON_3.getResult()){
							this.logger.info("【玫琳凯】用户校验失败,订单号:{}",cwb);
							send_b2c_flag = 2;
							this.b2CDataDAO.updateB2cIdSQLResponseStatus(b2cdata.getB2cid(), send_b2c_flag,HDtoLipsEnum.REASON_3.getCheckUsertext());
							//continue;
						}
						//2.校验通过后开始执行第二步请求推送货态
						TrackData td = (TrackData)getMLKdataMethod(b2cdata.getJsoncontent(),TrackData.class);
						Object[] objec = new Object[]{td.getDataID(),td.getReceiveDate(),td.getComment(),td.getTransactionType()};
						this.logger.info("请求【玫琳凯】货态反馈请求参数为:{},订单号:{}",Arrays.toString(objec),cwb);
						
						String msg = "SUCCESS";
						Integer integ = (Integer)WebServiceHandler.invokeWs(hdTOlipsUrl, methodHDToLIPSByWebService, objec);
						if(integ == HDtoLipsEnum.REASON_1.getResult()){
							send_b2c_flag = 2;
							this.logger.info("【玫琳凯】返回-1,请求订单:{}",cwb);
							msg = HDtoLipsEnum.REASON_1.getHdtolipsresult();
						}else if(integ == HDtoLipsEnum.REASON_2.getResult()){
							send_b2c_flag = 2;
							this.logger.info("【玫琳凯】返回-2,请求订单:{}",cwb);
							msg = HDtoLipsEnum.REASON_2.getHdtolipsresult();
						}else if(integ == HDtoLipsEnum.REASON_3.getResult()){
							send_b2c_flag = 2;
							this.logger.info("【玫琳凯】返回-3,请求订单:{}",cwb);
							msg = HDtoLipsEnum.REASON_3.getHdtolipsresult();
						}
						this.b2CDataDAO.updateB2cIdSQLResponseStatus(b2cdata.getB2cid(), send_b2c_flag,msg);
					}catch(Exception e){
						this.logger.error("订单请求webservice异常,订单号:"+cwb+"原因:{}",e);
						send_b2c_flag = 2;
						this.b2CDataDAO.updateB2cIdSQLResponseStatus(b2cdata.getB2cid(), send_b2c_flag,"系统异常:"+e.getMessage()+",可以通过日志中查看具体原因");
					}
				}
			}
		} catch (Exception e) {
			logger.error("调用【玫琳凯】webservice服务器异常"+e.getMessage(),e);
		}
		
	}
	
	//获取配置信息==LX==
	@SuppressWarnings("unchecked")
	public <T> T getEntitySettingMethod(int key,Class<T> obj) {
		//b2ctools.getObjectMethod(key)为获取b2c 配置信息的接口
		String objectMethod=b2ctools.getObjectMethod(key).getJoint_property();
		if (objectMethod!=null) {
			JSONObject jsonObj = JSONObject.fromObject(objectMethod);
			return (T) JSONObject.toBean(jsonObj, obj);//将json对象转化为bean
		} else {
			return null;
		}
	}
	
	//将json字符串反序列化为任一个对象 ====LX=====(首先定义好一个javabean) 
	public <T> T getMLKdataMethod(String jsoncontent,Class<T> cla) throws JsonParseException, JsonMappingException, IOException {
		return (T) new ObjectMapper().readValue(jsoncontent,cla);
	}
	
	/**
	 * 【玫琳凯】
	 * @param flowOrdertype
	 * @param delivery_state
	 * @return
	 */
	public String filterMLKFlowEnum(long flowOrdertype, long delivery_state) {
		if(flowOrdertype == FlowOrderTypeEnum.RuKu.getValue()){
			return TrackEnum.Blank.getSign();
		}else if((flowOrdertype == FlowOrderTypeEnum.YiFanKui.getValue())&&(delivery_state == DeliveryStateEnum.PeiSongChengGong.getValue())){
			return TrackEnum.SHARRIVAL.getSign();
		}
		return null;
	}
    
}
