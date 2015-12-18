package cn.explink.b2c.feiniuwang;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.explink.b2c.feiniuwang.Responseitems;
import cn.explink.b2c.feiniuwang.ReturnAllDataResponse;
import cn.explink.b2c.feiniuwang.ReturnData;
import cn.explink.b2c.feiniuwang.MD5Util;
import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.domain.B2CData;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.RestHttpServiceHanlder;

@Service
public class FNWService {
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	private B2CDataDAO b2CDataDAO;
	private Logger logger =LoggerFactory.getLogger(FNWService.class);
	
	//获取页面配置信息
	public FeiNiuWang getFeiniuwang(int key){
		FeiNiuWang feiniuwang = null;
		String jspcontent = b2ctools.getObjectMethod(key).getJoint_property();
		if(jspcontent!=null){
			JSONObject object = JSONObject.fromObject(jspcontent);
			feiniuwang = (FeiNiuWang)JSONObject.toBean(object, FeiNiuWang.class);
		}else{
			feiniuwang = new FeiNiuWang();
		}
		return feiniuwang;
	}
	
	public void feedback_status(){
			submitReturn(FlowOrderTypeEnum.RuKu.getValue());
			submitReturn(FlowOrderTypeEnum.ChuKuSaoMiao.getValue());
			submitReturn(FlowOrderTypeEnum.FenZhanLingHuo.getValue());
//			submitReturn(FlowOrderTypeEnum.TuiHuoZhanRuKu.getValue());
			submitReturn(FlowOrderTypeEnum.YiShenHe.getValue());
//			submitReturn(FlowOrderTypeEnum.TuiHuoChuZhan.getValue());
		
	}
	
	public void submitReturn(long flowordertype){
		FeiNiuWang feiniuwang = getFeiniuwang(B2cEnum.Feiniuwang.getKey());
		if(!b2ctools.isB2cOpen(B2cEnum.Feiniuwang.getKey())) {
			logger.info("未开启0飞牛网(http)0状态反馈接口");
			return ;
		} 
		try {
			
			int i=0;
			while(true){
				//遍历数据库中每条
				List<B2CData> feiniulist=b2CDataDAO.getDataListByFlowStatus(flowordertype,feiniuwang.getCustomerid(),feiniuwang.getMaxCount());
				i++;
				if(i>100){
					String warning="查询0飞牛网(http)0状态反馈已经超过100次循环，每次100条，可能存在程序未知异常,请及时查询并处理!";
					logger.warn(warning);
					return ;
				}
				if(feiniulist==null||feiniulist.size()==0){
					logger.info("当前没有要推送0飞牛网(http)0的数据");
					return ;
				}
				
				//遍历每一条表单整体信息
				
				String b2cids = "";
				long b2cid = 0;
				for(B2CData b2cdata:feiniulist){
					ReturnData returnData = getXMLNoteMethod(b2cdata.getJsoncontent());
					b2cid = b2cdata.getB2cid();
					b2cids+=b2cdata.getB2cid()+",";
					
					Map<String, String> params = buildSendJsondata(returnData, feiniuwang);
					logger.info("请求飞牛网(http)参数为:{}",params);
					String responseJson=RestHttpServiceHanlder.sendHttptoServer(params, feiniuwang.getFeedbackUrl());
					logger.info("飞牛网(http)返回的数据为:{}",responseJson);
					ReturnAllDataResponse response=JacksonMapper.getInstance().readValue(responseJson, ReturnAllDataResponse.class);
					List<Responseitems> responseitemlist = response.getResponseitems();
					for(Responseitems responseitems:responseitemlist){
						int result = 0;
						String reason = "";
						if(!responseitems.getSuccess().equals("true")){
							b2CDataDAO.updateMultiB2cIdSQLResponseStatus_AllFaild(b2cids.length()>0?b2cids.substring(0,b2cids.length()-1):"-1");
			 			}else{
							result = responseitems.getSuccess().equals("true")?1:2;
							reason = responseitems.getReason();
							b2CDataDAO.updateB2cIdSQLResponseStatus(b2cid, result, reason);			
						}
					}
				}
				}
				
				
			
		} catch (Exception e) {
			logger.error("调用0飞牛网(http)0webservice服务器异常"+e.getMessage(),e);
		}
	}
	//得到表单字段对应的javabean
	public ReturnData getXMLNoteMethod(String jsoncontent) throws JsonParseException, JsonMappingException, IOException {
		return JacksonMapper.getInstance().readValue(jsoncontent,ReturnData.class);
	}

	//创建请求的数据
	public Map<String,String> buildSendJsondata(ReturnData returnData,FeiNiuWang feiniuwang) throws Exception{
		
		Map<String,String> params = new HashMap<String, String>();
		String rd = JacksonMapper.getInstance().writeValueAsString(returnData);
		String data_digest = MD5Util.doSign(rd,feiniuwang.getResponseKey(),"UTF-8");
		params.put("logistics_interface", rd);
		params.put("data_digest", data_digest);
		params.put("msg_type", "TRACEPUSH");
		List<Traceslist> traList = returnData.getTraceslist();
		String logistic_provider_id = "";
		for(Traceslist tra : traList){
			logistic_provider_id = tra.getLogisticproviderid();
		}
		params.put("logistic_provider_id", logistic_provider_id);
		
		return params;
	}
}
