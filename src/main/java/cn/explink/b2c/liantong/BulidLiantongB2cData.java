package cn.explink.b2c.liantong;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.B2CData;
import cn.explink.domain.Customer;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.jms.dto.CwbOrderWithDeliveryState;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;
import cn.explink.util.JSONReslutUtil;
import cn.explink.util.MD5.MD5Util;
import net.sf.json.JSONObject;

@Service
public class BulidLiantongB2cData {

	/**
	 * 构建联通对象 并存储
	 */
	@Autowired
	LiantongService liantongService;
	@Autowired
	LiantongDAO liantongDAO;
	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	private B2CDataDAO b2CDataDAO;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public void buildB2cData(CwbOrderWithDeliveryState cwbOrderWothDeliverystate, DmpOrderFlow orderFlow, DmpCwbOrder cwbOrder, long flowOrdertype, long delivery_state, Customer customer) {

		String json="";
		try {
			Liantong lt = liantongService.getLiantong(B2cEnum.Liantong.getKey()); // 获取配置信息
			this.saveLiantongDatas(orderFlow, cwbOrder, flowOrdertype, delivery_state); // 存入联通查询表

			String flowStatus = this.liantongService.getFlowEnum(flowOrdertype, delivery_state);
			if (flowStatus == null) {
				this.logger.info("订单{}不属于要存储到b2cdata表的数据,flowordertype={}", cwbOrder.getCwb(), orderFlow.getFlowordertype());
				return ;
			}
			if (!b2ctools.isB2cOpen(B2cEnum.Liantong.getKey())) {
				logger.info("未开0联通0的状态反馈接口!");
				return ;
			}
			String acceptName = ""; // 签收人
			DmpDeliveryState dmpDeliveryState = cwbOrderWothDeliverystate.getDeliveryState();
			if ((dmpDeliveryState != null) && ((dmpDeliveryState.getDeliverystate() == DeliveryStateEnum.PeiSongChengGong.getValue())
					&& (dmpDeliveryState.getDeliverystate() == DeliveryStateEnum.ShangMenHuanChengGong.getValue())
					&& (dmpDeliveryState.getDeliverystate() == DeliveryStateEnum.ShangMenTuiChengGong.getValue()))) {
				acceptName = cwbOrder.getConsigneename();
			}
			LiantongXMLNote liantongXMLNote = new LiantongXMLNote();
			liantongXMLNote.setOrder(cwbOrder.getCwb());
			liantongXMLNote.setMailNo(cwbOrder.getTranscwb());
			liantongXMLNote.setAcceptTime(DateTimeUtil.formatDate(orderFlow.getCredate()));
			liantongXMLNote.setAcceptName(acceptName);
			liantongXMLNote.setAcceptAddress(this.orderFlowDetail.getDetail(orderFlow));
			liantongXMLNote.setAcceptState(flowStatus);
			json= JacksonMapper.getInstance().writeValueAsString(liantongXMLNote);
			String reqxml = "<Orders>" + "<Order>" + "<OrderNo>" + cwbOrder.getCwb() + "</OrderNo>" + "<MailNo>" + cwbOrder.getTranscwb() + "</MailNo>" + "<Steps>" + "<Step>" + "<AcceptState>"
					+ flowStatus + "</AcceptState>" + "<AcceptTime>" + DateTimeUtil.formatDate(orderFlow.getCredate()) + "</AcceptTime>" + "<AcceptAddress>" + this.orderFlowDetail.getDetail(orderFlow)
					+ "</AcceptAddress>" + "<AcceptName>" + acceptName + "</AcceptName>" + "</Step>" + "</Steps>" + "</Order>" + "</Orders>";
			Map<String, String> params = buildRequestMaps(lt, cwbOrder.getCwb(), reqxml); // 构建请求的参数
			String signstrs=LiantongCore.createLinkString(params);    //生成签名字符串
			String MD5Strs=lt.getSignSecurity()+"$"+signstrs+"$"+lt.getSignSecurity();
			logger.info("MD5加密字符串：{}",MD5Strs);
			String sign=MD5Util.md5_liantong(MD5Strs);
			params.put("sign",sign); //追加计算出来的sign参数
			String responseData=JSONReslutUtil.sendHTTPServerByParms(params, lt.getFeedback_url()); //请求并返回
			
			logger.info("当前联通返回XML={},cwb={}",responseData,cwbOrder.getCwb());
			
			JSONObject jsonobj=JSONObject.fromObject(responseData);
			
//			LtResponse ltresp=JacksonMapper.getInstance().readValue(responseData, LtResponse.class);
			String detail=jsonobj.get("detail")==null?null:jsonobj.getString("detail");
			if(detail!=null){
				logger.info("系统级别异常{},cwb={}",responseData,cwbOrder.getCwb());
				return ;
			}
			String respcode=jsonobj.getString("respcode");
			String respdesc=jsonobj.getString("respdesc");
			int send_b2c_flag="0000".equals(respcode)?1:2;
		} catch (Exception e) {
			sendFaildAndSaveB2cData(orderFlow, cwbOrder, json);  //推送失败,然后存储起来,定时发送
			this.logger.error("联通对接处理失败", e);
		}

	}

	/**
	 * 存入联通查询表
	 * 
	 * @param orderFlow
	 * @param cwbOrder
	 * @param flowOrdertype
	 */
	public void saveLiantongDatas(DmpOrderFlow orderFlow, DmpCwbOrder cwbOrder, long flowOrdertype, long delivery_state) {

		try {
			String trackText = liantongService.getTrackEnum(flowOrdertype, delivery_state);

			if (trackText == null) {
				logger.warn("当前不属于存储[联通]所需要的状态cwb=" + orderFlow.getCwb() + ",orderFlow=" + orderFlow.getFlowordertype());
				return;
			}
			LiantongEntity lt = new LiantongEntity();
			lt.setCwb(orderFlow.getCwb());
			lt.setTranscwb(cwbOrder.getTranscwb());
			lt.setAcceptTime(DateTimeUtil.formatDate(orderFlow.getCredate()));
			lt.setAcceptAddress(getDmpdao.getNowBranch(orderFlow.getBranchid()).getBranchname());
			lt.setAcceptAction(orderFlowDetail.getDetail(orderFlow));
			lt.setAcceptName(getDmpdao.getUserById(orderFlow.getUserid()).getRealname());
			lt.setFlowordertype(flowOrdertype);
			liantongDAO.save(lt);
		} catch (Exception e) {
			logger.error("存入查询表异常，联通cwb=" + orderFlow.getCwb(), e);
		}
	}
	private Map<String, String> buildRequestMaps(Liantong lt, String cwb, String reqxml) {
		Map<String, String> params = new HashMap<String, String>();
		String apptx = cwb + DateTimeUtil.getNowTime("yyyyMMddHHmmss");
		String timestamp = DateTimeUtil.getNowTime("yyyy-MM-dd HH:mm:ss");

		params.put("appkey", lt.getAppkey());
		params.put("method", lt.getMethod());
		params.put("apptx", apptx); // 流水号
		params.put("timestamp", timestamp); // 时间戳

		params.put("wlcompanycode", lt.getAppcode()); // 物流公司编码
		params.put("busitype", "02");
		params.put("reqxml", reqxml);

		return params;
	}
	private void sendFaildAndSaveB2cData(DmpOrderFlow orderFlow,DmpCwbOrder cwbOrder, String json) {
		B2CData b2cData=new B2CData();
		b2cData.setCwb(orderFlow.getCwb());
		b2cData.setCustomerid(cwbOrder.getCustomerid());
		b2cData.setFlowordertype(orderFlow.getFlowordertype());
		b2cData.setPosttime(DateTimeUtil.formatDate(orderFlow.getCredate()));
		b2cData.setSend_b2c_flag(0);
   
		b2cData.setJsoncontent(json);  //封装的JSON格式.
		String multi_shipcwb=StringUtils.hasLength(cwbOrder.getMulti_shipcwb())?cwbOrder.getMulti_shipcwb():null;
		b2CDataDAO.saveB2CData(b2cData,multi_shipcwb);
	}
}
