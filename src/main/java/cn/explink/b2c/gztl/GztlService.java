package cn.explink.b2c.gztl;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.sf.json.JSONObject;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.gztl.returnData.TmsFeedback;
import cn.explink.b2c.gztl.webservice.GztlWebService;
import cn.explink.b2c.lefeng.LefengService;
import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.ExptReason;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.b2c.tools.JointEntity;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.B2CData;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.jms.dto.DmpCwbOrder;

/**
 * 广州通路Service
 *
 * @author Administrator
 *
 */
@Service
public class GztlService {
	@Autowired
	private GetDmpDAO getdmpDAO;
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	B2CDataDAO b2cDataDAO;

	private Logger logger = LoggerFactory.getLogger(LefengService.class);

	/**
	 * 获取反库状态
	 */
	public GztlEnum filterFlowState(long delivery_state, DmpCwbOrder cwbOrder, long flowordertype, long deliverystate, int cwbordertype) {
		for (GztlEnum e : GztlEnum.values()) {
			if ((flowordertype != FlowOrderTypeEnum.YiShenHe.getValue()) && (e.getFlowtype() == flowordertype)) {
				return e;
			}
		}
		if (flowordertype == FlowOrderTypeEnum.YiShenHe.getValue()) {
			if ((deliverystate == DeliveryStateEnum.PeiSongChengGong.getValue()) || (deliverystate == DeliveryStateEnum.ShangMenHuanChengGong.getValue())
					|| (deliverystate == DeliveryStateEnum.ShangMenTuiChengGong.getValue())) {
				return GztlEnum.Peisongchenggong;
			}
			if ((deliverystate == DeliveryStateEnum.BuFenTuiHuo.getValue())) {
				return GztlEnum.BufenJushou;
			}
			if (deliverystate == DeliveryStateEnum.HuoWuDiuShi.getValue()) {
				return GztlEnum.ShouGongdiushi;
			}

			if ((deliverystate == DeliveryStateEnum.QuanBuTuiHuo.getValue()) || (deliverystate == DeliveryStateEnum.ShangMenJuTui.getValue())) {

				return GztlEnum.Peisongshibai;
			}

			if (deliverystate == DeliveryStateEnum.FenZhanZhiLiu.getValue()) {

				ExptReason exptReason = this.b2ctools.getExptReasonByB2c(cwbOrder.getLeavedreasonid(), 0, String.valueOf(cwbOrder.getCustomerid()), delivery_state);

				if (exptReason.getExpt_code().equals(GztlEnum.KehuYanqi.getState())) {
					GztlEnum.KehuYanqi.setReturnMsg(exptReason.getExpt_msg());
					return GztlEnum.KehuYanqi;
				} else {
					GztlEnum.Peisongyanchi.setReturnMsg(exptReason.getExpt_msg());
					return GztlEnum.Peisongyanchi;
				}

			}

		}
		return null;
	}

	/**
	 * 获取广州通路配置信息并转化成为Gztl这个类中的信息
	 *
	 * @param key
	 * @return
	 */
	public Gztl getGztl(int key) {
		Gztl gztl = null;
		if (this.getObjectMethod(key) != null) {
			JSONObject jsonObj = JSONObject.fromObject(this.getObjectMethod(key));
			gztl = (Gztl) JSONObject.toBean(jsonObj, Gztl.class);
		} else {
			gztl = new Gztl();
		}
		return gztl;
	}

	/**
	 * 获取广州通路配置信息的接口
	 *
	 * @param key
	 * @return
	 */
	private String getObjectMethod(int key) {
		try {
			JointEntity obj = this.getdmpDAO.getJointEntity(key);
			return obj.getJoint_property();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 将订单状态返回给广州通路
	 */
	public void feedback_status() {

		Gztl gztl = this.getGztl(B2cEnum.Guangzhoutonglu.getKey());
		if (!this.b2ctools.isB2cOpen(B2cEnum.Guangzhoutonglu.getKey())) {
			this.logger.info("未开0广州通路0的对接!");
			return;
		}
		this.sendCwbStatus_To_gztl(gztl, FlowOrderTypeEnum.RuKu.getValue());
		this.sendCwbStatus_To_gztl(gztl, FlowOrderTypeEnum.ChuKuSaoMiao.getValue());
		this.sendCwbStatus_To_gztl(gztl, FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue());
		this.sendCwbStatus_To_gztl(gztl, FlowOrderTypeEnum.FenZhanLingHuo.getValue());
		this.sendCwbStatus_To_gztl(gztl, FlowOrderTypeEnum.YiShenHe.getValue());

		this.sendCwbStatus_To_gztl(gztl, FlowOrderTypeEnum.TuiGongYingShangChuKu.getValue());
		this.sendCwbStatus_To_gztl(gztl, FlowOrderTypeEnum.TuiHuoZhanRuKu.getValue());
		this.sendCwbStatus_To_gztl(gztl, FlowOrderTypeEnum.TuiHuoChuZhan.getValue());
		this.sendCwbStatus_To_gztl(gztl, FlowOrderTypeEnum.GongYingShangJuShouFanKu.getValue());
	}

	/**
	 * 状态反馈接口开始(广州通路)
	 *
	 * @param smile
	 */
	private void sendCwbStatus_To_gztl(Gztl gztl, long flowordertype) {
		try {

			int i = 0;
			while (true) {
				List<B2CData> gztlDataList = this.b2cDataDAO.getDataListByFlowStatus(flowordertype, gztl.getCustomerids(), gztl.getSearch_number());
				i++;
				if (i > 100) {
					String warning = "查询0广州通路0状态反馈已经超过100次循环，可能存在程序未知异常,请及时查询并处理!";
					this.logger.warn(warning);
					return;
				}

				if ((gztlDataList == null) || (gztlDataList.size() == 0)) {
					this.logger.info("当前没有要推送0广州通路0的数据");
					return;
				}
				this.DealWithBuildXMLAndSending(gztl, gztlDataList);

			}

		} catch (Exception e) {
			String errorinfo = "发送0广州通路0状态反馈遇到不可预知的异常";
			this.logger.error(errorinfo, e);
		}

	}

	/**
	 * 拼成广州通路所需要的xml格式字符串
	 *
	 * @param gztl
	 * @param datalist
	 * @throws Exception
	 */
	private void DealWithBuildXMLAndSending(Gztl gztl, List<B2CData> datalist) throws Exception {
		String b2cidsString = "";
		StringBuffer subBuffer = new StringBuffer();
		subBuffer.append("<TMS>");
		subBuffer.append("<TMSFeedbacks>");
		for (B2CData b2cData : datalist) {
			String jsoncontent = b2cData.getJsoncontent();
			GztlXmlNote note = this.getXMLNoteMethod(jsoncontent);
			b2cidsString += b2cData.getB2cid() + ",";
			subBuffer.append("<TMSFeedback>");
			subBuffer.append("<id>" + note.getId() + "</id>");// 序列号，用于接收成功后返回标识
			subBuffer.append("<myNo>" + note.getMyNo() + "</myNo>");// 运单编号
			subBuffer.append("<logisticid>" + note.getLogisticid() + "</logisticid>");// 订单号
			subBuffer.append("<custorderno>" + note.getMyNo() + "</custorderno>");// 客户订单号
			subBuffer.append("<opType>" + note.getOpType() + "</opType>");// 反馈类型(由飞远提供)
			subBuffer.append("<state>" + note.getState() + "</state>");// 订单状态(由飞远提供)
			subBuffer.append("<returnState>" + note.getReturnState() + "</returnState>");// 网点反馈状态(由飞远提供)
			subBuffer.append("<returnCause>" + note.getReturnCause() + "</returnCause>");// 网点反馈原因(由飞远提供)
			subBuffer.append("<returnRemark>" + "" + "</returnRemark>");// 网点反馈备注(由飞远提供)
			subBuffer.append("<signname>" + note.getSignname() + "</signname>");// 签收人
			subBuffer.append("<opDt>" + note.getOpDt() + "</opDt>");// 网点反馈时间/签收时间/导入时间/出入库时间
			subBuffer.append("<emp>" + note.getEmp() + "</emp>");// 网点反馈用户??d
			subBuffer.append("<unit>" + note.getUnit() + "</unit>");// 反馈网点名称??
			subBuffer.append("<empSend>" + note.getEmpSend() + "</empSend>");// 派件员/反馈人/导入信息人员/出入库人
			subBuffer.append("<returnStatedesc>" + note.getReturnStatedesc() + "</returnStatedesc>");// 订单状态详情
			subBuffer.append("<cuscode>" + note.getCuscode() + "</cuscode>");// 供货商代码(由飞远提供)??
			subBuffer.append("<receiverName>" + note.getReceiverName() + "</receiverName>");// 收件人
			subBuffer.append("<receiverMobile>" + note.getReceiverMobile() + "</receiverMobile>");// 收件人电话
			subBuffer.append("<customername>" + note.getCustomername() + "</customername>");// 供货商(由飞远提供)
			subBuffer.append("<senderName>" + "" + "</senderName>");// 寄件人
			subBuffer.append("<senderMobile>" + "" + "</senderMobile>");// 寄件手机
			subBuffer.append("<payinamount>" + note.getPayinamount() + "</payinamount>");// 代收货款
			subBuffer.append("<arrivedate>" + note.getArrivedate() + "</arrivedate>");// 最初扫描时间
			subBuffer.append("<lspabbr>" + "" + "</lspabbr>");// 配送区域
			subBuffer.append("</TMSFeedback>");
		}
		subBuffer.append("</TMSFeedbacks>");
		subBuffer.append("</TMS>");
		this.logger.info("生成符合广州通路的xml数据：{}", subBuffer.toString());
		b2cidsString = b2cidsString.length() > 0 ? b2cidsString.substring(0, b2cidsString.length() - 1) : b2cidsString;

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.getInInterceptors().add(new LoggingInInterceptor());
		factory.getOutInterceptors().add(new LoggingOutInterceptor());
		factory.setAddress(gztl.getSearch_url());
		factory.setWsdlURL("http://model.web.fyps.com");
		factory.setServiceClass(GztlWebService.class);
		GztlWebService service = (GztlWebService) factory.create();

		String responseString = service.orderAndFeedbackApi(gztl.getCode(), gztl.getInvokeMethod(), gztl.getSign(), subBuffer.toString());

		// String responseString =
		// RestHttpServiceHanlder.sendHttptoServer(gztl.getSign(),
		// subBuffer.toString());
		TmsFeedback tmsFeedback = (TmsFeedback) this.xmlToObject(responseString, TmsFeedback.class);
		if (tmsFeedback == null) {
			this.logger.warn("请求0广州通路0解析xml为空，跳出循环,throw Exception,xml={}", responseString);
			return;
		}
		if (tmsFeedback.getSuccess().equals("true")) {
			this.b2cDataDAO.updateMultiB2cIdSQLResponseStatus_AllSuccess(b2cidsString);

			this.logger.info("返回失败消息：xml={}" + tmsFeedback.getRemark());

		} else if (tmsFeedback.getSuccess().equals("false")) {
			this.b2cDataDAO.updateMultiB2cIdSQLResponseStatus_AllFaild(b2cidsString);

			this.logger.info("返回成功消息：xml={}" + tmsFeedback.getRemark());

		}
	}

	public Object xmlToObject(String xml, Object object) {
		StringReader stringReader = new StringReader(xml);
		Object obj = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			obj = unmarshaller.unmarshal(stringReader);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			this.logger.error("将xml解析成GztlXmlElement类时出错", e);
		}
		return obj;
	}

	public GztlXmlNote getXMLNoteMethod(String jsoncontent) throws JsonParseException, JsonMappingException, IOException {
		return JacksonMapper.getInstance().readValue(jsoncontent, GztlXmlNote.class);
	}

}
