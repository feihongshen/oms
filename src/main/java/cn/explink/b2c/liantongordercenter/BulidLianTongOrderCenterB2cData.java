package cn.explink.b2c.liantongordercenter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cn.explink.b2c.liantong.BulidLiantongB2cData;
import cn.explink.b2c.liantong.LiantongDAO;
import cn.explink.b2c.liantongordercenter.HttpRequestor;
import cn.explink.b2c.liantongordercenter.LianTongOrderCenter;
import cn.explink.b2c.liantongordercenter.LianTongOrderCenterService;
import cn.explink.b2c.liantongordercenter.LianTongOrderCenterXMLNote;
import cn.explink.b2c.liantongordercenter.xmlDto.ResponseDto;
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
import cn.explink.util.JAXBUtil;
import cn.explink.util.MD5.SignUtils;

@Service
public class BulidLianTongOrderCenterB2cData {

	@Autowired
	LianTongOrderCenterService lianTongSCService;
	@Autowired
	GetDmpDAO getDmpDAO;
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;
	@Autowired
	LiantongDAO liantongDAO;
	@Autowired
	BulidLiantongB2cData bulidLiantongB2cData;
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	B2CDataDAO b2CDataDAO;
	/**
	 * 构建联通商城对象，并存储
	 *
	 * @param cwbOrderWothDeliverystate
	 * @param orderFlow
	 * @param cwbOrder
	 * @param flowOrdertype
	 * @param delivery_state
	 * @param customer
	 * @return
	 */
	private Logger logger = LoggerFactory.getLogger(BulidLianTongOrderCenterB2cData.class);

	public void buildB2cData(CwbOrderWithDeliveryState cwbOrderWothDeliverystate, DmpOrderFlow orderFlow, DmpCwbOrder cwbOrder, long flowOrdertype, long delivery_state, Customer customer) throws JsonGenerationException, JsonMappingException, IOException {
		String json="";
		try {
			this.bulidLiantongB2cData.saveLiantongDatas(orderFlow, cwbOrder, flowOrdertype, delivery_state);
			String flowStatus = this.lianTongSCService.getFlowEnum(flowOrdertype, delivery_state);
			if (!StringUtils.hasText(flowStatus)) {
				this.logger.info("订单{}不需要存储到b2cdata表的数据,floworder={}", cwbOrder.getCwb(), orderFlow.getFlowordertype());
				return;
			}
			if(!b2ctools.isB2cOpen(B2cEnum.Telecomshop.getKey())) {
				logger.info("未开0电信商城0的状态反馈接口!");
				return;
			} 
			String acceptName = ""; // 签收人
			DmpDeliveryState dmpDeliveryState = cwbOrderWothDeliverystate.getDeliveryState();
			if ((dmpDeliveryState != null) && ((dmpDeliveryState.getDeliverystate() == DeliveryStateEnum.PeiSongChengGong.getValue())
					&& (dmpDeliveryState.getDeliverystate() == DeliveryStateEnum.ShangMenHuanChengGong.getValue())
					&& (dmpDeliveryState.getDeliverystate() == DeliveryStateEnum.ShangMenTuiChengGong.getValue()))) {
				acceptName = cwbOrder.getConsigneename();
			}
			LianTongOrderCenter lt = lianTongSCService.getLiantong(B2cEnum.LianTongOrderCenter.getKey());
			LianTongOrderCenterXMLNote xmlnote = new LianTongOrderCenterXMLNote();
			xmlnote.setMailNO(cwbOrder.getCwb());
			xmlnote.setOrderId(cwbOrder.getCwb());
			xmlnote.setAcceptTime(DateTimeUtil.formatDate(orderFlow.getCredate()));
			xmlnote.setAcceptAddress(this.orderFlowDetail.getDetail(orderFlow));
			xmlnote.setOpcode(flowStatus);
			xmlnote.setRemark("配送");
			String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			LinkedHashMap map = new LinkedHashMap();
			map.put("APP_KEY", lt.getAppkey());
			map.put("TIMESTAMP", format);
			map.put("TRANS_ID", "0");
			String TOKEN = SignUtils.sign(map, lt.getAppsecret());
			String xmlStr = "<UNI_BSS><UNI_BSS_HEAD><APP_KEY>" + lt.getAppkey() + "</APP_KEY><TRANS_ID>0</TRANS_ID><TIMESTAMP>" + format + "</TIMESTAMP><TOKEN>" + TOKEN
					+ "</TOKEN></UNI_BSS_HEAD><UNI_BSS_BODY><WaybillRoutes><WaybillRoute id=\"" + xmlnote.getId() + "\" mailno=\"" + xmlnote.getMailNO() + "\" orderid=\""
					+ xmlnote.getOrderId() + "\" acceptTime=\"" + xmlnote.getAcceptTime() + "\" acceptAddress=\"" + xmlnote.getAcceptAddress() + "\" remark=\"" + xmlnote.getRemark()
					+ "\" opCode=\"" + xmlnote.getOpcode() + "\"/></WaybillRoutes></UNI_BSS_BODY></UNI_BSS>";
			String VERIFY_CODE = SignUtils.signVeryfy(xmlStr, lt.getAppsecret());
			String xml = "<UNI_BSS><UNI_BSS_HEAD><APP_KEY>" + lt.getAppkey() + "</APP_KEY><TRANS_ID>0</TRANS_ID><TIMESTAMP>" + format + "</TIMESTAMP><TOKEN>" + TOKEN + "</TOKEN><VERIFY_CODE>"
					+ VERIFY_CODE + "</VERIFY_CODE></UNI_BSS_HEAD><UNI_BSS_BODY><WaybillRoutes><WaybillRoute id=\"" + xmlnote.getId() + "\" mailno=\"" + xmlnote.getMailNO() + "\" orderid=\""
					+ xmlnote.getOrderId() + "\" acceptTime=\"" + xmlnote.getAcceptTime() + "\" acceptAddress=\"" + xmlnote.getAcceptAddress() + "\" remark=\"" + xmlnote.getRemark()
					+ "\" opCode=\"" + xmlnote.getOpcode() + "\"/></WaybillRoutes></UNI_BSS_BODY></UNI_BSS>";
			json = JacksonMapper.getInstance().writeValueAsString(xmlnote);
			String responseData = HttpRequestor.doPost(lt.getFeedback_url(), xml); // 请求并返回

			this.logger.info("当前联通返回XML={},cwb={}", responseData, cwbOrder.getCwb());
			ResponseDto responseDto = JAXBUtil.convertObject(ResponseDto.class, responseData);
			if (null == responseDto) {
				this.logger.info("当前联通返回responseDto={}", responseDto);
				return;
			}
			int send_b2c_flag = 0;
			String remark = "";
			if ("00000".equals(responseDto.getRequestBodyDtoUni().getStatus())) {
				send_b2c_flag = 1;
			} else {
				send_b2c_flag = 2;
				remark = responseDto.getRequestHeadDto().getRESPDESC();
			}
		//	this.b2cDataDAO.updateB2cIdSQLResponseStatus(data.getB2cid(), send_b2c_flag, remark);

		} catch (Exception e) {
			sendFaildAndSaveB2cData(orderFlow, cwbOrder, json);  
			this.logger.error("联通商城对接处理失败", e);
		}
	}
	private void sendFaildAndSaveB2cData(DmpOrderFlow orderFlow,DmpCwbOrder cwbOrder,String json)throws IOException, JsonGenerationException, JsonMappingException {
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
