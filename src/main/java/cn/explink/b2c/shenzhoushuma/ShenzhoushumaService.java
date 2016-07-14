package cn.explink.b2c.shenzhoushuma;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.shenzhoushuma.xmldto.RequestXmlData;
import cn.explink.b2c.shenzhoushuma.xmldto.ResponseXmlData;
import cn.explink.b2c.shenzhoushuma.xmldto.StepNode;
import cn.explink.b2c.shenzhoushuma.xmldto.StepsNode;
import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.b2c.tools.ObjectUnMarchal;
import cn.explink.b2c.vipshop.ReaderXMLHandler;
import cn.explink.domain.B2CData;
import cn.explink.util.MD5.MD5Util;

/**
 * 神州数码状态反馈 Service
 * @author yurong.liang 2016-04-26
 */
@Service
public class ShenzhoushumaService {
	private Logger logger = LoggerFactory.getLogger(ShenzhoushumaService.class);
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	private B2CDataDAO b2cDataDAO;
	/**
	 * 状态反馈方法入口
	 */
	public void feedback_status() {

		if (!b2ctools.isB2cOpen(B2cEnum.Shenzhoushuma.getKey())) {
			logger.info("未开【神州数码_状态反馈】的对接!");
			return;
		}
		ShenZhouShuMa shenZhouShuMa = this.getShenZhouShuMa(B2cEnum.Shenzhoushuma.getKey());
		//发送轨迹
		this.sendCwbStatusToshenZhouShuMa(shenZhouShuMa);
	}
	
	/**
	 * 状态反馈接口开始
	 */
	public void sendCwbStatusToshenZhouShuMa(ShenZhouShuMa shenZhouShuMa){
		try {
			int i = 0;
			while (true) {
				List<B2CData> datalist = b2cDataDAO.getDataListByFlowStatus(shenZhouShuMa.getCustomerId()+"",Long.parseLong(shenZhouShuMa.getMaxCount()+"") );
				i++;
				if (i > 100) {
					logger.warn("查询[神州数码]状态反馈已经超过100次循环，可能存在程序未知异常,请及时查询并处理!");
					return;
				}

				if (datalist == null || datalist.size() == 0) {
					logger.info("当前没有要推送给[神州数码]的轨迹数据");
					return;
				}
				//处理构建xml并发送数据
				this.dealWithBuildXMLAndSendData(shenZhouShuMa, datalist);
			}

		} catch (Exception e) {
			logger.error("发送[神州数码]状态反馈遇到不可预知的异常", e);
		}
	}
	
	private void dealWithBuildXMLAndSendData(ShenZhouShuMa shenZhouShuMa, List<B2CData> datalist) throws Exception {
		String feedBackUrl=shenZhouShuMa.getFeedBackUrl();
		//循环发送
		for (B2CData b2cdata : datalist) {
			try {
				StepNode step = getstep(b2cdata.getJsoncontent());
				//构建发送xml
				String requestXML= buildRequestXML(step,shenZhouShuMa);
				logger.info("发送[神州数码]的报文为:{}",requestXML);
				ResponseXmlData responseData =new ResponseXmlData();
				//发送报文
				String responseXML=SOAPHandler.httpInvokeWs(feedBackUrl, requestXML);
				responseXML=ReaderXMLHandler.parseBack(responseXML);
				responseXML= responseXML.substring(responseXML.indexOf("<ns:return>")+11, responseXML.indexOf("</ns:return>"));
				logger.info("[神州数码]返回的报文为:{}",responseXML);
				responseData = (ResponseXmlData)ObjectUnMarchal.XmltoPOJO(responseXML, responseData);
				int resultCode=responseData.isFlag()==true?1:2;
				String  resultDesc= responseData.getDesc()==null?"":responseData.getDesc();
				
				b2cDataDAO.updateB2cIdSQLResponseStatus(b2cdata.getB2cid(), resultCode,resultDesc);
			} catch (Exception e) {
				logger.error("反馈[神州数码]轨迹出现未知问题,cwb="+b2cdata.getCwb()+",flowordertype="+b2cdata.getFlowordertype()+",json:"+b2cdata.getJsoncontent()+"异常信息:"+e);
			}
		}
	}
	
	//构建发送的XML
	public String buildRequestXML(StepNode step,ShenZhouShuMa shenZhouShuMa) throws Exception{
		RequestXmlData  request =new RequestXmlData();
		String logisticProvider = shenZhouShuMa.getLogisticProvider();
		String logisticProviderId = shenZhouShuMa.getLogisticProviderId();
		String doId = step.getDoId();
		request.setLogisticProvider(logisticProvider);
		request.setLogisticProviderId(logisticProviderId);
		request.setDoId(doId);
		//加密内容
		String content=logisticProvider+logisticProviderId+doId+"null"+step.getOpPoint()+step.getBusiOperator()+step.getBusiOperateTime();
		String privateKey= shenZhouShuMa.getPrivateKey();//加密秘钥
		//MD5加密
		String MD5Key=MD5Util.md5(content+privateKey);
		request.setMD5Key(MD5Key);
		
		StepsNode steps=new StepsNode();
		List<StepNode> stepList=new ArrayList<StepNode>();
		stepList.add(step);
		steps.setStepList(stepList);
		request.setSteps(steps);
		StringBuffer requestXml=new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		String ObjectXml=ObjectUnMarchal.POJOtoXmlByParam(request,false,true);
		requestXml.append(ObjectXml);
		return requestXml.toString();
	}
	
	// 获取配置信息
	public ShenZhouShuMa getShenZhouShuMa(int key) {
		ShenZhouShuMa shenZhouShuMa = null;
		String objectMethod = b2ctools.getObjectMethod(key).getJoint_property();
		if (objectMethod != null) {
			JSONObject jsonObj = JSONObject.fromObject(objectMethod);
			shenZhouShuMa = (ShenZhouShuMa) JSONObject.toBean(jsonObj, ShenZhouShuMa.class);
		} else {
			shenZhouShuMa = new ShenZhouShuMa();
		}
		return shenZhouShuMa;
	}
	
	public StepNode getstep(String jsoncontent) throws Exception {
		return JacksonMapper.getInstance().readValue(jsoncontent, StepNode.class);
	}
}
