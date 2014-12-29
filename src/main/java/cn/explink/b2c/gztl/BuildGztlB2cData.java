package cn.explink.b2c.gztl;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.explink.b2c.lefeng.BuildLefengB2cData;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.dao.CommonSendDataDAO;
import cn.explink.dao.GetDmpDAO;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;

public class BuildGztlB2cData {
	private Logger logger = LoggerFactory.getLogger(BuildLefengB2cData.class);
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;

	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	GztlService gztlService;
	@Autowired
	CommonSendDataDAO commonSendDataDAO;
	@Autowired
	private B2cTools b2ctools;

	public String buildLefengMethod(DmpOrderFlow orderFlow, long flowOrdertype, DmpCwbOrder cwbOrder, long delivery_state, DmpDeliveryState dmpDeliveryState, ObjectMapper objectMapper)
			throws IOException, JsonGenerationException, JsonMappingException {

		// 入库 出库到货，领货 N10
		// 成功 S00
		// 拒收 E30
		// 滞留E31 E32
		GztlEnum cmstate = this.gztlService.filterFlowState(flowOrdertype, delivery_state, Integer.parseInt(cwbOrder.getCwbordertypeid())) == null ? null : this.gztlService.filterFlowState(
				flowOrdertype, delivery_state, Integer.parseInt(cwbOrder.getCwbordertypeid()));

		if (cmstate == null) {
			this.logger.warn("该流程不属于乐峰网推送流程cwb={},deliverystate={}", cwbOrder.getCwb(), delivery_state);
			return null;
		}
		this.logger.info("订单号：{}封装成0乐峰网0所需要的json----开始,状态：{}", cwbOrder.getCwb(), flowOrdertype);
		/*
		 * <routes> <route code="N10">
		 * <time>2013-07-01T22:50:35.000+08:00</time>
		 * <state>已进行柜台到件扫描,扫描员是CC</state> </route>
		 */
		GztlXmlNote gztlXmlNote = new GztlXmlNote();
		gztlXmlNote.setId(cwbOrder.getOpscwbid() + "");// ??
		gztlXmlNote.setMyNo(cwbOrder.getTranscwb());
		gztlXmlNote.setCustorderno(cwbOrder.getTranscwb());
		gztlXmlNote.setOpType(cmstate.getText());// ??
		gztlXmlNote.setState(cmstate.getText());// ??
		gztlXmlNote.setReturnState(cmstate.getText());// ??
		gztlXmlNote.setReturnCause("");// ??
		gztlXmlNote.setReturnRemark("");// ??

		String sign_man = (dmpDeliveryState.getSign_man() == null) || dmpDeliveryState.getSign_man().isEmpty() ? cwbOrder.getConsigneename() : dmpDeliveryState.getSign_man();
		gztlXmlNote.setSignname(sign_man);// 签收人
		gztlXmlNote.setOpDt(DateTimeUtil.formatDateLong(orderFlow.getCredate(), "yyyy-MM-dd HH:mm:ss"));// ??网点反馈时间/签收时间/导入时间/出入库时间
		gztlXmlNote.setEmp("");// ???
		gztlXmlNote.setUnit(orderFlow.getBranchname());// ??
		gztlXmlNote.setEmpSend(orderFlow.getUsername());// ??
		gztlXmlNote.setReturnStatedesc(this.orderFlowDetail.getDetail(orderFlow));// 订单状态详情
		gztlXmlNote.setCuscode("");// ??
		gztlXmlNote.setReceiverName(cwbOrder.getConsigneename());
		gztlXmlNote.setReceiverMobile(cwbOrder.getConsigneemobile());
		gztlXmlNote.setCustomername("");// ??供货商
		gztlXmlNote.setPayinamount(String.valueOf(cwbOrder.getReceivablefee().doubleValue()));// ??
		gztlXmlNote.setArrivedate(DateTimeUtil.formatDateLong(orderFlow.getCredate(), "yyyy-MM-dd HH:mm:ss"));// ??最初扫描时间
		gztlXmlNote.setLspabbr("");// ??
		// DateTimeUtil.formatDate(orderFlow.getCredate())
		// lefengXmlNote.setTime(DateTimeUtil.formatDate(orderFlow.getCredate(),
		// "yyyy-MM-dd'T'HH:mm:ss"));
		this.logger.info("订单号：{}封装成0乐峰网0所需要的json----结束,状态：{}", cwbOrder.getCwb(), cwbOrder.getFlowordertype());
		return objectMapper.writeValueAsString(gztlXmlNote);

	}
	/**
	 * private String id;// 序列号，用于接收成功后返回标识 private String myNo;// 运单编号 private
	 * String logisticid;// 订单号 private String custorderno;// 客户订单号 private
	 * String opType;// 反馈类型(由飞远提供) private String state;// 订单状态(由飞远提供) private
	 * String returnState;// 网点反馈状态(由飞远提供) private String returnCause;//
	 * 网点反馈原因(由飞远提供) private String returnRemark;// 网点反馈备注(由飞远提供) private String
	 * signname;// 签收人 private String opDt;// 网点反馈时间/签收时间/导入时间/出入库时间 private
	 * String emp;// 网点反馈用户 private String unit;// 反馈网点名称 private String
	 * empSend;// 派件员/反馈人/导入信息人员/出入库人 private String returnStatedesc;// 订单状态详情
	 * private String cuscode;// 供货商代码(由飞远提供) private String receiverName;// 收件人
	 * private String receiverMobile;// 收件人电话 private String customername;//
	 * 供货商(由飞远提供) private String senderName;// 寄件人?? private String
	 * senderMobile;// 寄件手机?? private String payinamount;// 代收货款 private String
	 * arrivedate;// 最初扫描时间 private String lspabbr;// 配送区域
	 */
}
