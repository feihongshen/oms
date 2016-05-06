package cn.explink.b2c.suning;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.sf.json.JSONObject;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.explink.b2c.suning.requestdto.WorkStatu;
import cn.explink.b2c.suning.responsedto.ResponseData;
import cn.explink.b2c.suning.responsedto.Result;
import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.domain.B2CData;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.RestHttpServiceHanlder;
import cn.explink.util.MD5.MD5Util;

@Service
public class SuNingService {
	
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	private B2CDataDAO b2CDataDAO;
	
	public B2cTools getB2ctools() {
		return b2ctools;
	}

	public void setB2ctools(B2cTools b2ctools) {
		this.b2ctools = b2ctools;
	}

	public B2CDataDAO getB2CDataDAO() {
		return b2CDataDAO;
	}

	public void setB2CDataDAO(B2CDataDAO b2cDataDAO) {
		b2CDataDAO = b2cDataDAO;
	}
	
	private static SuNingService suNingService = null;
	public SuNingService getSuNingService(){
		//由于只注入一次
		if(suNingService == null){
			suNingService = new SuNingService();
			suNingService.setB2ctools(b2ctools);
			suNingService.setB2CDataDAO(b2CDataDAO);
		}
		return suNingService;
	}
	
	private Logger logger =LoggerFactory.getLogger(this.getClass());
	
	public String filterSuningFlowEnum(long flowordertype,long deliverystate) {
		if(flowordertype!=FlowOrderTypeEnum.YiShenHe.getValue()){
			for(SuNingFlowEnum TEnum:SuNingFlowEnum.values()){
				if(flowordertype==TEnum.getFlowordertype()){
					return TEnum.getWork_type();//请求指令
				}
			}
		}
		if(flowordertype==FlowOrderTypeEnum.YiShenHe.getValue()){
			if((deliverystate==DeliveryStateEnum.PeiSongChengGong.getValue())
			||(deliverystate==DeliveryStateEnum.ShangMenTuiChengGong.getValue())
			||(deliverystate==DeliveryStateEnum.QuanBuTuiHuo.getValue())
					)
			{
				return SuNingFlowEnum.TMS_SIGN1.getWork_type();		
			}
		}
		return null;
		
	}
	
	//订单配送信息提交接口
	public void feedback_status(){
		SubmitDeliveryInfoSingle(FlowOrderTypeEnum.RuKu.getValue());
		SubmitDeliveryInfoSingle(FlowOrderTypeEnum.ChuKuSaoMiao.getValue());
		SubmitDeliveryInfoSingle(FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue());
		SubmitDeliveryInfoSingle(FlowOrderTypeEnum.FenZhanLingHuo.getValue());
		SubmitDeliveryInfoSingle(FlowOrderTypeEnum.YiShenHe.getValue());
	}
	
	//获取需要反馈给【苏宁易购】的信息封装对象
	public WorkStatu getWorkStatuMethod(String jsoncontent) throws JsonParseException, JsonMappingException, IOException {
		return JacksonMapper.getInstance().readValue(jsoncontent,new WorkStatu().getClass());
	}
	
	/**
	 * 订单配送流程反馈（批量处理）
	 */
	public void SubmitDeliveryInfo(long flowordertype){
		int key = B2cEnum.SuNing.getKey();
		SuNing suning=getSuNingSettingMethod(key);
		if(!b2ctools.isB2cOpen(key)) {
			logger.info("未开启【苏宁易购】状态反馈接口");
			return ;
		} 
		try {
			int i=0;
			while(true){
				//遍历数据库中每条
				List<B2CData> suNingList=b2CDataDAO.getDataListByFlowStatus(flowordertype,suning.getCustomerid(),suning.getMaxcount());
				i++;
				if(i>100){
					String warning="查询【苏宁易购】状态反馈已经超过100次循环，每次100条，可能存在程序未知异常,请及时查询并处理!";
					logger.warn(warning);
					return ;
				}
				if(suNingList==null||suNingList.size()==0){
					logger.info("当前没有要推送【苏宁易购】的数据");
					return ;
				}
				
				//List<WorkStatu> workStatusList = new ArrayList<WorkStatu>();
				List<String> jsoncontentList = new ArrayList<String>();
				
				//遍历每一条表单整体信息
				for(B2CData b2cdata:suNingList){
					String jsoncontent = b2cdata.getJsoncontent();
					jsoncontentList.add(jsoncontent);
				}
				
				String requestJsonBefore = "{\"body\":";
				String middleJsoncontent = "null";
				if(jsoncontentList!=null&&!jsoncontentList.isEmpty()){
					middleJsoncontent = "{\"workStatusList\":[";
					for(String str : jsoncontentList){
						middleJsoncontent += (str +",");
					}
					middleJsoncontent = middleJsoncontent.substring(0, middleJsoncontent.length()-1)+"]}";
				}
				//【苏宁易购】生成货态请求加密串====contentMesDigest
				String sign = "";
				String private_key = suning.getPrivate_key();
				if("null".equals(middleJsoncontent)){
					sign = MD5Util.md5(private_key);
				}else{
					sign = MD5Util.md5(middleJsoncontent+private_key);
				}
				this.logger.info("返回信息body生成的加密串sign:{}",sign);
				String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());//获取当前时间
				String requestJsonAfter = ",\"spCode\":\""+suning.getSpCode()+"\",\"contentMesDigest\":\""+sign+"\",\"time\":\""+time+"\"}";
				
				//字符串拼接成请求【苏宁易购】的数据
				String requestdataStr = requestJsonBefore+middleJsoncontent+requestJsonAfter;
				requestdataStr = URLEncoder.encode(requestdataStr, "UTF-8");
				logger.info("请求【苏宁易购】参数为:{}",requestdataStr);
				Map<String, String> strMap = new HashMap<String, String>();
				strMap.put("request", requestdataStr);
				String responseJson = "";
				try{
					responseJson = RestHttpServiceHanlder.sendHttptoServer(strMap, suning.getFeedbackUrl());
					logger.info("【苏宁易购】返回的数据为:{}",responseJson);
				}catch(Exception e){
					this.logger.error("请求【苏宁易购】返回信息异常,原因:{}",e);
				}
				ResponseData response = new ResponseData();
				try{
					response = JacksonMapper.getInstance().readValue(responseJson, new ResponseData().getClass());
				}catch(Exception e){
					this.logger.error("解析返回信息错误,原因:{}",e);
				}
				//返回为空时，按全部失败处理
				if(response.getBody() == null){
					String b2cids = "";
					for(B2CData b2cdata:suNingList){
						b2cids += (b2cdata.getB2cid()+",");
					}
					if(b2cids.length()>0){
						b2cids = b2cids.substring(0, b2cids.length()-1);
						this.b2CDataDAO.updataB2CrequestDataBYb2cids(b2cids,2,response.getFailedReason());
					}
				}else{
					for(B2CData b2cdata:suNingList){
						int send_b2c_flag=1;
						List<Result> results = response.getBody().getResults();
						for(Result rs : results){
							if((rs.getWork_id()).contains(b2cdata.getCwb())){
								if("E".equals(rs.getReturn_code())){
									send_b2c_flag = 2;
									this.b2CDataDAO.updateB2cIdSQLResponseStatus(b2cdata.getB2cid(), send_b2c_flag, rs.getReturn_message());
									this.logger.info("【苏宁易购】推送失败订单号为:{}",rs.getWork_id());
								}else{
									this.b2CDataDAO.updateB2cIdSQLResponseStatus(b2cdata.getB2cid(), send_b2c_flag, rs.getReturn_message());
									this.logger.info("【苏宁易购】推送成功的订单号:{}",rs.getWork_id());
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("【苏宁易购】货态反馈处理异常:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 订单配送流程反馈（单条处理）
	 */
	public void SubmitDeliveryInfoSingle(long flowordertype){
		int key = B2cEnum.SuNing.getKey();
		SuNing suning=getSuNingSettingMethod(key);
		if(!b2ctools.isB2cOpen(key)) {
			logger.info("未开启【苏宁易购】状态反馈接口");
			return ;
		} 
		try {
			int i=0;
			while(true){
				//遍历数据库中每条
				List<B2CData> suNingList=b2CDataDAO.getDataListByFlowStatus(flowordertype,suning.getCustomerid(),suning.getMaxcount());
				i++;
				if(i>100){
					String warning="查询【苏宁易购】状态反馈已经超过100次循环，每次100条，可能存在程序未知异常,请及时查询并处理!";
					logger.warn(warning);
					return ;
				}
				if(suNingList==null||suNingList.size()==0){
					logger.info("当前没有要推送【苏宁易购】的数据");
					return ;
				}
				//遍历每一条表单整体信息
				String requestJsonBefore = "{\"body\":";
				String middleJsoncontent = "{\"workStatusList\":[";
				
				
				 ExecutorService executor = Executors.newCachedThreadPool();
				//预设每个线程处理条数
				int everythread = suning.getEverythreaddonum();
				//每次查出的回传总条数
				int totalsum = suNingList.size();
				//所需线程数
				int needthread = (totalsum<everythread)?1:(totalsum%everythread==0?(totalsum/everythread):((totalsum/everythread)+1));
				
				CyclicBarrier barrier=new CyclicBarrier(needthread+1);
				int fromindex = 0;
				int toindex = everythread-1;
				for(int threadnum=0;threadnum<needthread;threadnum++){
					//只需要一个线程处理
					if(needthread==1){
						executor.execute(new SuNingRunnableIml(suNingList,requestJsonBefore,middleJsoncontent,suning,barrier,getSuNingService()));
						/*impl.getLists(suNingList,requestJsonBefore,middleJsoncontent,suning);
						Thread thread = new Thread(impl);
						thread.start();*/
						//justdealwith(suNingList,requestJsonBefore,middleJsoncontent,suning);
					}else{
						//需要多个线程去处理
						List<B2CData> bcList = getCurrentB2cdata(suNingList,fromindex,toindex);	
						executor.execute(new SuNingRunnableIml(bcList,requestJsonBefore,middleJsoncontent,suning,barrier,getSuNingService()));
						/*Thread thread = new Thread(impl);
						thread.start();*/
						//遍历循环拿到下一个线程需要处理的list索引位置
						fromindex = (threadnum+1)*everythread;
						if((toindex+everythread)>(suNingList.size()-1)){
							toindex = suNingList.size()-1;
						}else{
							toindex += everythread;
						}
					}
				}
				try{
					barrier.await();
				}catch(Exception e){
					e.printStackTrace();
					this.logger.error("线程等待异常,reason:{}",e);
				}
				executor.shutdown();
				this.logger.info("====此次处理总共开辟了【"+needthread+"】个线程====");
			}	
		} catch (Exception e) {
			logger.error("【苏宁易购】货态反馈处理异常:"+e.getMessage(),e);
		}
	}
	
	private List<B2CData> getCurrentB2cdata(List<B2CData> totallist,int fromIndex,int toIndex){
		return totallist.subList(fromIndex, toIndex);
	}
	
	
	
	//每个线程在此处处理
	public void justdealwith(List<B2CData> suNingList,String requestJsonBefore,String middleJsoncontent,SuNing suning) throws Exception {
		//单条线程处理开始
		this.logger.info("每条线程调用处理===【苏宁易购】===货态反馈开始=======");
		for(B2CData b2cdata:suNingList){
			String jsoncontent = b2cdata.getJsoncontent();
			String bodyStr = middleJsoncontent+jsoncontent+"]}";
			//【苏宁易购】生成货态请求加密串====contentMesDigest
			String contentMesDigest = MD5Util.md5(bodyStr+suning.getPrivate_key());
			this.logger.info("返回信息body生成的加密串sign:{},订单号:{}",contentMesDigest,b2cdata.getCwb());
			String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());//获取当前时间
			String requestJsonAfter = ",\"spCode\":\""+suning.getSpCode()+"\",\"contentMesDigest\":\""+contentMesDigest+"\",\"time\":\""+time+"\"}";
			//字符串拼接成请求【苏宁易购】的数据
			String requestdataStr = requestJsonBefore+bodyStr+requestJsonAfter;
			this.logger.info("请求【苏宁易购】参数为:{}",requestdataStr);
			requestdataStr = URLEncoder.encode(requestdataStr, "UTF-8");
			/*httpPost.setEntity(new UrlEncodedFormEntity(parameters,HTTP.UTF_8));*/
			String responseJson = "";
			ResponseData response = new ResponseData();
			Map<String, String> strMap = new HashMap<String, String>();
			strMap.put("request", requestdataStr);
			try{
				responseJson = RestHttpServiceHanlder.sendHttptoServer(strMap, suning.getFeedbackUrl());
				//responseJson=RestHttpServiceHanlder.sendHttptoServer(requestdataStr, suning.getFeedbackUrl());
				this.logger.info("【苏宁易购】返回的数据为:{}",responseJson);
				response=JacksonMapper.getInstance().readValue(responseJson, new ResponseData().getClass());	
				//返回为空时，按全部失败处理
				if(response.getBody() == null){
					this.b2CDataDAO.updateB2cIdSQLResponseStatus(b2cdata.getB2cid(),2,response.getFailedReason());
				}else{
					int send_b2c_flag = 1;
					List<Result> results = response.getBody().getResults();
					for(Result rs : results){
						if((rs.getWork_id()).contains(b2cdata.getCwb())){
							if("E".equals((rs.getReturn_code()))){
								send_b2c_flag = 2;
								this.b2CDataDAO.updateB2cIdSQLResponseStatus(b2cdata.getB2cid(), send_b2c_flag, rs.getReturn_message());
							}else{
								this.b2CDataDAO.updateB2cIdSQLResponseStatus(b2cdata.getB2cid(), send_b2c_flag, rs.getReturn_message());
							}
						}else{
							this.b2CDataDAO.updateB2cIdSQLResponseStatus(b2cdata.getB2cid(), 2, "原单号不包含返回(截取)单号");
							this.logger.info("原单号不包含返回(截取)单号,截取单号:{}",b2cdata.getCwb());
						}
					}
				}
			}catch(Exception e){
				this.logger.error("苏宁易购货态反馈处理异常,reason:{}",e);
				this.b2CDataDAO.updateB2cIdSQLResponseStatus(b2cdata.getB2cid(),2,"本地异常,reason:"+e.getMessage());
			}
		}
		this.logger.info("每条线程调用处理===【苏宁易购】===货态反馈结束=======");
	}
	
	//获取配置信息
	public SuNing getSuNingSettingMethod(int key) {
		SuNing suning = new SuNing();
		//b2ctools.getObjectMethod(key)为获取b2c 配置信息的接口
		String objectMethod=b2ctools.getObjectMethod(key).getJoint_property();
		if (objectMethod!=null) {
			JSONObject jsonObj = JSONObject.fromObject(objectMethod);
			return (SuNing)JSONObject.toBean(jsonObj,suning.getClass());//将json对象转化为bean
		} else {
			return suning;
		}
	}

	
	//test方法
	public static void main(String[] args) {
		SuNing suning = new SuNing();
		String str1;
		try {
			str1 = JacksonMapper.getInstance().writeValueAsString(suning);
			System.out.println(str1);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		setSuning(suning);
		try {
			String str2 = JacksonMapper.getInstance().writeValueAsString(suning);
			System.out.println(str2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void setSuning(SuNing suning){
		suning.setCustomerid("123");
		suning.setFeedbackUrl("23456");
	}
	
}
