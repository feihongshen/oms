package cn.explink.b2c.gztl;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.sf.json.JSONObject;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.explink.b2c.gztl.returnData.TmsFeedback;
import cn.explink.b2c.lefeng.LefengService;
import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.b2c.tools.JointEntity;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.B2CData;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.RestHttpServiceHanlder;

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
	public GztlEnum filterFlowState(long flowordertype, long deliverystate, int cwbordertype) {
		for (GztlEnum e : GztlEnum.values()) {
			if ((flowordertype != FlowOrderTypeEnum.YiShenHe.getValue()) && (e.getFlowtype() == flowordertype)) {
				return e;
			}
		}
		if (flowordertype == FlowOrderTypeEnum.YiShenHe.getValue()) {
			if ((deliverystate == DeliveryStateEnum.PeiSongChengGong.getValue()) || (deliverystate == DeliveryStateEnum.ShangMenHuanChengGong.getValue())
					|| (deliverystate == DeliveryStateEnum.ShangMenTuiChengGong.getValue())) {
				return GztlEnum.Received;
			}
			if ((deliverystate == DeliveryStateEnum.QuanBuTuiHuo.getValue()) || (deliverystate == DeliveryStateEnum.BuFenTuiHuo.getValue())
					|| (deliverystate == DeliveryStateEnum.ShangMenJuTui.getValue())) {
				return GztlEnum.DeliveryFailed;
			}
			if (deliverystate == DeliveryStateEnum.FenZhanZhiLiu.getValue()) {
				return GztlEnum.DeliveryException;
			}
			if (deliverystate == DeliveryStateEnum.HuoWuDiuShi.getValue()) {
				return GztlEnum.DeliveryException;
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
			this.logger.info("未开0乐峰0的对接!");
			return;
		}
		this.sendCwbStatus_To_lefeng(gztl, FlowOrderTypeEnum.RuKu.getValue());
		this.sendCwbStatus_To_lefeng(gztl, FlowOrderTypeEnum.ChuKuSaoMiao.getValue());
		this.sendCwbStatus_To_lefeng(gztl, FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue());
		this.sendCwbStatus_To_lefeng(gztl, FlowOrderTypeEnum.FenZhanLingHuo.getValue());
		this.sendCwbStatus_To_lefeng(gztl, FlowOrderTypeEnum.YiShenHe.getValue());

	}

	/**
	 * 状态反馈接口开始
	 *
	 * @param smile
	 */
	private void sendCwbStatus_To_lefeng(Gztl gztl, long flowordertype) {
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
			String errorinfo = "发送0乐蜂网0状态反馈遇到不可预知的异常";
			this.logger.error(errorinfo, e);
		}

	}

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
			subBuffer.append("<id>" + "?????" + "</id>");// 序列号，用于接收成功后返回标识
			subBuffer.append("<myNo>" + note.getId() + "</myNo>");// 运单编号
			subBuffer.append("<logisticid>" + note.getLogisticid() + "</logisticid>");// 订单号
			subBuffer.append("<custorderno>" + "??" + "</custorderno>");// 客户订单号
			subBuffer.append("<opType>" + "??" + "</opType>");// 反馈类型(由飞远提供)
			subBuffer.append("<state>" + "??" + "</state>");// 订单状态(由飞远提供)
			subBuffer.append("<returnState>" + "??" + "</returnState>");// 网点反馈状态(由飞远提供)
			subBuffer.append("<returnCause>" + "??" + "</returnCause>");// 网点反馈原因(由飞远提供)
			subBuffer.append("<returnRemark>" + "??" + "</returnRemark>");// 网点反馈备注(由飞远提供)
			subBuffer.append("<signname>" + "??" + "</signname>");// 签收人
			subBuffer.append("<opDt>" + "??" + "</opDt>");// 网点反馈时间/签收时间/导入时间/出入库时间
			subBuffer.append("<emp>" + "??" + "</emp>");// 网点反馈用户
			subBuffer.append("<unit>" + "??" + "</unit>");// 反馈网点名称
			subBuffer.append("<empSend>" + "??" + "</empSend>");// 派件员/反馈人/导入信息人员/出入库人
			subBuffer.append("<returnStatedesc>" + "??" + "</returnStatedesc>");// 订单状态详情
			subBuffer.append("<cuscode>" + "??" + "</cuscode>");// 供货商代码(由飞远提供)
			subBuffer.append("<receiverName>" + "??" + "</receiverName>");// 收件人
			subBuffer.append("<receiverMobile>" + "??" + "</receiverMobile>");// 收件人电话
			subBuffer.append("<customername>" + "??" + "</customername>");// 供货商(由飞远提供)
			subBuffer.append("<senderName>" + "??" + "</senderName>");// 寄件人
			subBuffer.append("<senderMobile>" + "??" + "</senderMobile>");// 寄件手机
			subBuffer.append("<payinamount>" + "??" + "</payinamount>");// 代收货款
			subBuffer.append("<arrivedate>" + "??" + "</arrivedate>");// 最初扫描时间
			subBuffer.append("<lspabbr>" + "??" + "</lspabbr>");// 配送区域
			subBuffer.append("</TMSFeedback>");
		}
		subBuffer.append("</TMSFeedbacks>");
		subBuffer.append("</TMS>");
		this.logger.info("生成符合广州通路的xml数据：{}", subBuffer.toString());
		b2cidsString = b2cidsString.length() > 0 ? b2cidsString.substring(0, b2cidsString.length() - 1) : b2cidsString;
		String responseString = RestHttpServiceHanlder.sendHttptoServer(gztl.getSign(), subBuffer.toString());
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
