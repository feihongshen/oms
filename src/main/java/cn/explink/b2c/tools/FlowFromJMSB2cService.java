package cn.explink.b2c.tools;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;

import net.sf.json.JSONObject;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Header;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cn.explink.b2c.amazon.BulidAmazonB2cData;
import cn.explink.b2c.explink.code_down.EPaiInterfaceService;
import cn.explink.b2c.telecomsc.TelecomJsonService;
import cn.explink.b2c.tpsdo.OtherOrderTrackSendService;
import cn.explink.b2c.tpsdo.TPOSendDoInfService;
import cn.explink.b2c.tpsdo.TrackSendToTPSService;
import cn.explink.b2c.tpsdo.bean.OrderTraceToTPSCfg;
import cn.explink.dao.ExpressSysMonitorDAO;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.MqExceptionDAO;
import cn.explink.domain.B2CData;
import cn.explink.domain.Customer;
import cn.explink.domain.ExpressSysMonitor;
import cn.explink.domain.MqExceptionBuilder;
import cn.explink.domain.MqExceptionBuilder.MessageSourceEnum;
import cn.explink.domain.SystemInstall;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.enumutil.IsmpsflagEnum;
import cn.explink.jms.dto.CwbOrderWithDeliveryState;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;

/**
 * 监听流程产生的JMS消息，用于B2C对接反馈
 *
 * @author Administrator
 *
 */
@Service
public class FlowFromJMSB2cService {
	@Autowired
	private CamelContext camelContext;

	@Autowired
	B2CDataDAO b2CDataDAO;
	@Autowired
	B2cJsonService b2cJsonService;
	@Autowired
	GetDmpDAO getDmpDAO;
	@Autowired
	BulidAmazonB2cData bulidAmazonB2cData;
	@Autowired
	MobileAppService mobileAppService;
	@Autowired
	EPaiInterfaceService ePaiInterfaceService;

	@Autowired
	ExpressSysMonitorDAO expressSysMonitorDAO;
	@Autowired
	TPOSendDoInfService tPOSendDoInfService;
	@Autowired
	OtherOrderTrackSendService otherOrderTrackSendService;
	@Autowired
	TelecomJsonService telecomJsonService;
	@Produce(uri = "jms:queue:sendBToCToDmp")
	ProducerTemplate sendBToCToDmpProducer;
	
	@Autowired
	TrackSendToTPSService trackSendToTPSService;
	@Autowired
	B2cTools b2ctools;

	private ObjectMapper objectMapper = JacksonMapper.getInstance();
	private ObjectReader dmpOrderFlowMapper = this.objectMapper.reader(DmpOrderFlow.class);
	private ObjectReader cwbOrderWithDeliveryStateReader = this.objectMapper.reader(CwbOrderWithDeliveryState.class);
	private ObjectReader dmpOrderReader = this.objectMapper.reader(DmpCwbOrder.class);

	private Logger logger = LoggerFactory.getLogger(FlowFromJMSB2cService.class);
	private List<String> flowList = new ArrayList<String>(); // 存储 对接所用的环节
	
	@Autowired
	private MqExceptionDAO mqExceptionDAO;
	
	private static final String MQ_FROM_URI = "jms:queue:VirtualTopicConsumers.omsb2c.orderFlow";

	@PostConstruct
	public void init() {
		try {

			this.flowList.add(FlowOrderTypeEnum.DaoRuShuJu.getMethod()); // 导入
			this.flowList.add(FlowOrderTypeEnum.TiHuo.getMethod()); // 提货
																	// 2013-07-31
			// add
			this.flowList.add(FlowOrderTypeEnum.RuKu.getMethod()); // 入库
			this.flowList.add(FlowOrderTypeEnum.ChuKuSaoMiao.getMethod()); // 出库扫描
			this.flowList.add(FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getMethod()); // 分站到货扫描
			this.flowList.add(FlowOrderTypeEnum.FenZhanDaoHuoYouHuoWuDanSaoMiao.getMethod()); // 分站到错货扫描
			this.flowList.add(FlowOrderTypeEnum.ZhongZhuanChuKuSaoMiao.getMethod()); // 分站中转出库（tmall分站出,不太重要）
			this.flowList.add(FlowOrderTypeEnum.FenZhanLingHuo.getMethod()); // 分站领货
			this.flowList.add(FlowOrderTypeEnum.TuiHuoZhanRuKu.getMethod()); // 退货站入库
			this.flowList.add(FlowOrderTypeEnum.TuiGongYingShangChuKu.getMethod());
			this.flowList.add(FlowOrderTypeEnum.GongHuoShangTuiHuoChenggong.getMethod());
			this.flowList.add(FlowOrderTypeEnum.GongYingShangJuShouFanKu.getMethod());
			this.flowList.add(FlowOrderTypeEnum.PosZhiFu.getMethod()); // pos支付（北京银联商务支付和签收一起）
			this.flowList.add(FlowOrderTypeEnum.YiFanKui.getMethod()); // 已反馈
																		// (当系统不使用归班时，用反馈的JMS)
			this.flowList.add(FlowOrderTypeEnum.YiShenHe.getMethod()); // 已审核（当系统使用归班功能时，用归班的JMS）
			this.flowList.add(FlowOrderTypeEnum.TuiHuoChuKuSaoMiao.getMethod()); // 退货出站（国美对接用）
			this.flowList.add(FlowOrderTypeEnum.YiChangDingDanChuLi.getMethod()); // 异常单处理（国美对接使用）
			this.flowList.add(FlowOrderTypeEnum.BaoGuoweiDao.getMethod()); // 包裹未到（亚马逊对接使用）
			this.flowList.add(FlowOrderTypeEnum.ZhongZhuanyanwu.getMethod()); // 中转延误（亚马逊对接使用）
			this.flowList.add(FlowOrderTypeEnum.ShouGongdiushi.getMethod()); // 货物丢失（亚马逊对接使用）
			
			this.flowList.add(FlowOrderTypeEnum.BingEmsTrans.getMethod());// 转运邮政  add by vic.liang@pjbest.com 2016-09-05
			this.camelContext.addRoutes(new RouteBuilder() {
				@Override
				public void configure() throws Exception {

					this.from(MQ_FROM_URI + "?concurrentConsumers=20").to("bean:flowFromJMSB2cService?method=saveFlowB2cSend").routeId("omsb2c_");
				}
			});

		} catch (Exception e) {
			this.logger.error("B2C监听JMS异常！" + e);
		}
	}

	public void saveFlowB2cSend(@Header("orderFlow") String parm, @Header("MessageHeaderUUID") String messageHeaderUUID) {
		try {
			doSaveFlowB2cSend(parm);
		} catch (Exception e1) {
			this.logger.error("error while handle orderflow", e1);
			// 把未完成MQ插入到数据库中, start
			//消费MQ异常表
			this.mqExceptionDAO.save(MqExceptionBuilder.getInstance().buildExceptionCode(this.getClass().getSimpleName() + ".saveFlowB2cSend")
					.buildExceptionInfo(e1.toString()).buildTopic(MQ_FROM_URI)
					.buildMessageHeader("orderFlow", parm)
					.buildMessageHeaderUUID(messageHeaderUUID).buildMessageSource(MessageSourceEnum.receiver.getIndex()).getMqException());
			// 把未完成MQ插入到数据库中, end
		}
	}
	/**
	 * 接收环节消息，用于B2C对接，存储B2C_Send 表
	 *
	 * @param parm
	 * @throws Exception 
	 */
	public void doSaveFlowB2cSend(String parm) throws Exception {
		long start = System.currentTimeMillis();
		this.logger.info("进入b2c消息，开始：" + start);
		this.logger.info("orderFlow send b2c 环节信息处理,{}", parm);
		try {
			DmpOrderFlow orderFlow = this.dmpOrderFlowMapper.readValue(parm);
			String floworderTypeMethod = orderFlow.getFlowordertypeMethod();
			if (!this.flowList.contains(floworderTypeMethod)) {
				this.logger.warn("RE:cwb=" + orderFlow.getCwb() + ",flowordertype=" + orderFlow.getFlowordertype() + "排除封装-return");
				return;
			}
			
			this.logger.info("b2c消息,cwb="+orderFlow.getCwb()+",flowordertype="+orderFlow.getFlowordertype());
			
			CwbOrderWithDeliveryState cwbOrderWithDeliveryState = JacksonMapper.getInstance().readValue(orderFlow.getFloworderdetail(), CwbOrderWithDeliveryState.class);
			
			/**
			 * 如果是一票多件的订单，那么将慢的flowordertype(mpsoptstate)赋值给flowordertype
			 */
			translateMpsoptstate(orderFlow, cwbOrderWithDeliveryState);
			
			
			if (this.b2CDataDAO.checkIsRepeatDataFlag(orderFlow.getCwb(), orderFlow.getFlowordertype(), DateTimeUtil.formatDate(orderFlow.getCredate())) > 0) {
				this.logger.info("RE: orderFlow send b2c 环节信息重复,已过滤,{}", parm);
				// buidCod(orderFlow);//存cod表
				return;
			}

			this.logger.info("send b2c:cwb={},flowordertype={}", orderFlow.getCwb(), orderFlow.getFlowordertype());

			if (cwbOrderWithDeliveryState != null) {
				if ((cwbOrderWithDeliveryState.getDeliveryState() != null) && (cwbOrderWithDeliveryState.getDeliveryState().getCodpos().compareTo(BigDecimal.ZERO) > 0)) {
					cwbOrderWithDeliveryState.getDeliveryState().setPos(cwbOrderWithDeliveryState.getDeliveryState().getCodpos());
					cwbOrderWithDeliveryState.getDeliveryState().setPosremark("POS反馈");
				}
				if ((cwbOrderWithDeliveryState.getCwbOrder() != null) && "5".equals(cwbOrderWithDeliveryState.getCwbOrder().getNewpaywayid())) {
					cwbOrderWithDeliveryState.getCwbOrder().setNewpaywayid("2");
				}
			}
			//外单数据插入到外单推DO服务接口表
			this.tPOSendDoInfService.insert2_TPO_SEND_DO_INF(cwbOrderWithDeliveryState, orderFlow);
			// 手机终端 入口
			this.mobileAppService.buildDeliveryingToMobileApps(cwbOrderWithDeliveryState, orderFlow);

			// 易派系统对接
			this.ePaiInterfaceService.buildFlowOrdersToEPaiAPI(cwbOrderWithDeliveryState, orderFlow);

			// 推送支持35状态的订单
			this.excuteFlowStatusMethod_Contains35(orderFlow, cwbOrderWithDeliveryState);

			// 存入正常流程的状态信息
			this.AddExcuteFlowStatusMethod(orderFlow);
			
			// 将所有订单的轨迹信息存到轨迹接口表（推送给tps的轨迹）
			OrderTraceToTPSCfg orderTraceToTPSCfg = b2ctools.getOrderTraceToTPSCfg();
			// 将所有订单的轨迹信息存到轨迹接口表（推送给tps的轨迹）
			if((orderTraceToTPSCfg!=null && b2ctools.isTraceToTpsCustomer(orderTraceToTPSCfg.getCustomerids(),cwbOrderWithDeliveryState.getCwbOrder().getCustomerid()))){
	    	/*if(cwbOrderWithDeliveryState.getCwbOrder().getTpstranscwb()!=null 
					&&!cwbOrderWithDeliveryState.getCwbOrder().getTpstranscwb().isEmpty() 
					&&Integer.parseInt(cwbOrderWithDeliveryState.getCwbOrder().getCwbordertypeid())!=6){*/
				if(cwbOrderWithDeliveryState.getCwbOrder().getTpstranscwb()!=null 
						&&!cwbOrderWithDeliveryState.getCwbOrder().getTpstranscwb().isEmpty() 
						&&Integer.parseInt(cwbOrderWithDeliveryState.getCwbOrder().getCwbordertypeid())!=6){
					logger.info("轨迹存入tps轨迹推送接口表开始，订单号为："+cwbOrderWithDeliveryState.getCwbOrder().getCwb()+"操作状态为："+orderFlow.getFlowordertype());
					trackSendToTPSService.saveOrderTrack(orderFlow,cwbOrderWithDeliveryState,null);
					logger.info("轨迹存入tps轨迹推送接口表成功，订单号为："+cwbOrderWithDeliveryState.getCwbOrder().getCwb()+"操作状态为："+orderFlow.getFlowordertype());
				}else{
					tPOSendDoInfService.insertPopOrderTo_TPO_SEND_DO_INF(cwbOrderWithDeliveryState, orderFlow);
					otherOrderTrackSendService.savePopOrderTrackToTpo(orderFlow,cwbOrderWithDeliveryState,null);
				}
			}else{
				logger.info("快递类型的订单，以及没有tps运单号的订单的轨迹信息不存，tps轨迹推送接口表，订单号为："+cwbOrderWithDeliveryState.getCwbOrder().getCwb());
			}
			
			//外单轨迹数据保存到临时表
			
			otherOrderTrackSendService.saveOtherOrderTrack(orderFlow,cwbOrderWithDeliveryState,null);
			
			// 临时注释 一段时间没有问题后，可以删除  2016-11-08 jian_xie
//			try {
//				// TODO jms异常写入监控表
//				String optime = DateTimeUtil.getNowTime();
//				ExpressSysMonitor monitor = this.expressSysMonitorDAO.getMaxOpt("JMSB2CFlow");
//				// 系统上线第一次加载
//				if (monitor == null) {
//					ExpressSysMonitor newmonitor = new ExpressSysMonitor();
//					newmonitor.setOptime(optime);
//					newmonitor.setType("JMSB2CFlow");
//					this.expressSysMonitorDAO.save(newmonitor);
//				} else {
//					// 后续加载 yyyy-MM-dd HH:m
//					String preoptime = monitor.getOptime();
//					if (!optime.substring(0, 15).equals(preoptime.substring(0, 15))) {
//						ExpressSysMonitor newmonitor = new ExpressSysMonitor();
//						newmonitor.setOptime(optime);
//						newmonitor.setType("JMSB2CFlow");
//						this.expressSysMonitorDAO.save(newmonitor);
//					}
//				}
//			} catch (Exception e5) {
//				this.logger.error("error while monitor orderflow", e5);
//			}
		} catch (Exception e1) {
			this.logger.error("error while handle orderflow", e1);
			throw e1;
		}
		long end = System.currentTimeMillis();
		this.logger.info("进入b2c消息，结束：" + end + "时差:" + (end - start));
	}

	
	private void translateMpsoptstate(DmpOrderFlow orderFlow,CwbOrderWithDeliveryState cwbOrderWithDeliveryState) {
		
		DmpCwbOrder cwbOrder =cwbOrderWithDeliveryState.getCwbOrder();
		DmpDeliveryState deliveryState = cwbOrderWithDeliveryState.getDeliveryState();
		if (cwbOrder.getIsmpsflag() == IsmpsflagEnum.yes.getValue()) {
			
			orderFlow.setFlowordertype(cwbOrder.getMpsoptstate());
			cwbOrder.setFlowordertype(cwbOrder.getMpsoptstate());
			cwbOrderWithDeliveryState.setCwbOrder(cwbOrder);
			if(deliveryState!=null){
				deliveryState.setFlowordertype(cwbOrder.getMpsoptstate());
				cwbOrderWithDeliveryState.setDeliveryState(deliveryState);
			}
		}
	}

	/**
	 * 接收对接数据存储，处理35，36
	 *
	 * @param orderFlow
	 * @return
	 */
	public void excuteFlowStatusMethod_Contains35(DmpOrderFlow orderFlow, CwbOrderWithDeliveryState cwbOrderWithDeliveryState) {

		try {
			String Jsoncontent = this.telecomJsonService.orderToJson(cwbOrderWithDeliveryState, orderFlow, orderFlow.getFlowordertype());
			if (Jsoncontent == null) {
				return;
			}

			DmpCwbOrder cwbOrder = cwbOrderWithDeliveryState.getCwbOrder();
			B2CData b2cData = new B2CData();
			b2cData.setCwb(orderFlow.getCwb());
			b2cData.setCustomerid(cwbOrder.getCustomerid());
			b2cData.setFlowordertype(orderFlow.getFlowordertype());
			b2cData.setPosttime(DateTimeUtil.formatDate(orderFlow.getCredate()));
			b2cData.setSend_b2c_flag(0);
			b2cData.setJsoncontent(Jsoncontent); // 封装的JSON格式.
			String multi_shipcwb = StringUtils.hasLength(cwbOrder.getMulti_shipcwb()) ? cwbOrder.getMulti_shipcwb() : null;
			this.b2CDataDAO.saveB2CData(b2cData, multi_shipcwb);
			this.logger.info("RE: orderFlow send b2c -----------订单号:{} flowordertype:{}对接环节信息结束", cwbOrder.getCwb(), orderFlow.getFlowordertype());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 执行正常的流程，存入待推送表中
	 *
	 * @param cwb
	 * @param credate
	 * @param flowordertype
	 * @param operatorUser
	 * @throws Exception
	 */
	private void AddExcuteFlowStatusMethod(DmpOrderFlow orderFlow) throws Exception {
		
		SystemInstall useAudit = this.getDmpDAO.getSystemInstallByName("useAudit");
		if ((useAudit != null) && "no".equals(useAudit.getValue())) {// 不需要归班
			this.AddExcuteFlowStatusMethodByNotAudit(orderFlow);
		} else {// 需要归班

			CwbOrderWithDeliveryState cwbOrderWithDeliveryState = this.cwbOrderWithDeliveryStateReader.readValue(orderFlow.getFloworderdetail());

			if (cwbOrderWithDeliveryState != null) {
				if ((cwbOrderWithDeliveryState.getDeliveryState() != null) && (cwbOrderWithDeliveryState.getDeliveryState().getCodpos().compareTo(BigDecimal.ZERO) > 0)) {
					cwbOrderWithDeliveryState.getDeliveryState().setPos(cwbOrderWithDeliveryState.getDeliveryState().getCodpos());
					cwbOrderWithDeliveryState.getDeliveryState().setPosremark("POS反馈");
				}
				if ((cwbOrderWithDeliveryState.getCwbOrder() != null) && "5".equals(cwbOrderWithDeliveryState.getCwbOrder().getNewpaywayid())) {
					cwbOrderWithDeliveryState.getCwbOrder().setNewpaywayid("2");
				}
			}

			SystemInstall sendPOS_flag = this.getDmpDAO.getSystemInstallByName("sendPOS_flag");
			if(sendPOS_flag == null){
				sendPOS_flag = new SystemInstall();
			}
			//SystemInstall sendPOS_flag = this.getDmpDAO.getSystemInstallByName("sendPOS_flag") == null ? new SystemInstall() : this.getDmpDAO.getSystemInstallByName("sendPOS_flag");
			// 1现金和0款 2滞留和拒收 -1 无效
			int posSendflag = Integer.valueOf(sendPOS_flag.getValue() == null ? "1" : sendPOS_flag.getValue()); // 增加获取 dmp 开关表

			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.YiShenHe.getValue()) { // pos支付，直接存储到对接表中
				DmpDeliveryState ds = cwbOrderWithDeliveryState.getDeliveryState();
				if (null != ds && (ds.getPos().compareTo(BigDecimal.ZERO) > 0) && ds.getPosremark().equals("POS刷卡")) {// pos支付，已经推送过，不再存储
					this.logger.info("RE: orderFlow send b2c pos支付，已经推送过，不再存储,{}", ds.getCwb());
					return;
				}
				// /=======================新增POS开关==================================================
				if (posSendflag == 1) {
					if (null != ds && ((ds.getDeliverystate() == DeliveryStateEnum.PeiSongChengGong.getValue()) || (ds.getDeliverystate() == DeliveryStateEnum.ShangMenHuanChengGong.getValue()) || (ds
							.getDeliverystate() == DeliveryStateEnum.ShangMenTuiChengGong.getValue())) && ds.getPosremark().contains("POS反馈")) {// pos支付，已经推送过，不再存储
						this.logger.info("RE: orderFlow send b2c pos-现金和0款反馈，已经推送过，不再存储,{}", ds.getCwb());
						return;
					}
				}
				if (posSendflag == 2) {
					if (null != ds && ds.getPosremark().contains("POS反馈")) { // pos反馈，已经推送过，不再存储
						this.logger.info("RE: orderFlow send b2c pos-所有反馈，已经推送过，不再存储,{}", ds.getCwb());
						return;
					}
				}
				// /=======================新增POS开关==================================================
			}

			if ((orderFlow.getFlowordertype() == FlowOrderTypeEnum.YiFanKui.getValue()) || (orderFlow.getFlowordertype() == FlowOrderTypeEnum.PosZhiFu.getValue())) {// pos支付，直接存储到对接表中
				DmpDeliveryState ds = cwbOrderWithDeliveryState.getDeliveryState();
				if (ds == null) {// 如果对象为空
					this.logger.info("RE: orderFlow send b2c 反馈对象为空,{}，订单号：{}", ds, orderFlow.getCwb());
					return;
				}
				if ((orderFlow.getFlowordertype() == FlowOrderTypeEnum.YiFanKui.getValue()) && (ds.getPos().compareTo(BigDecimal.ZERO) > 0)
						&& (ds.getPosremark().equals("POS刷卡") || ds.getPosremark().contains("POS反馈"))) { // 不是pos支付，不存储到对接表中
					orderFlow.setFlowordertype(FlowOrderTypeEnum.YiShenHe.getValue());
					this.logger.info("RE: orderFlow send b2c pos签收，转化成归班,{}", ds.getCwb());
				}
				// /=======================新增POS开关==================================================
				else if ((orderFlow.getFlowordertype() == FlowOrderTypeEnum.YiFanKui.getValue()) && (posSendflag == 1)) {
					if (((ds.getDeliverystate() == DeliveryStateEnum.PeiSongChengGong.getValue()) || (ds.getDeliverystate() == DeliveryStateEnum.ShangMenHuanChengGong.getValue()) || (ds
							.getDeliverystate() == DeliveryStateEnum.ShangMenTuiChengGong.getValue())) && ds.getPosremark().contains("POS反馈")) { // pos支付，已经推送过，不再存储
						this.logger.info("RE: orderFlow send b2c pos-现金和0款反馈，转化成归班,{}", ds.getCwb());
						orderFlow.setFlowordertype(FlowOrderTypeEnum.YiShenHe.getValue());
					} else {
						return;
					}
				} else if ((orderFlow.getFlowordertype() == FlowOrderTypeEnum.YiFanKui.getValue()) && (posSendflag == 2)) {
					if (ds.getPosremark().contains("POS反馈")) { // pos反馈，已经推送过，不再存储
						this.logger.info("RE: orderFlow send b2c pos-所有反馈，转化成归班,{}", ds.getCwb());
						orderFlow.setFlowordertype(FlowOrderTypeEnum.YiShenHe.getValue());
					} else {
						return;
					}
				}

				// /=====================新增POS开关====================================================
				else { // 不是pos支付
					this.logger.info("RE: orderFlow send b2c 不是pos签收，不存入对接表,{}", ds.getCwb());
					return;
				}

			}

			/**
			 * 提货转化为入库-tmall 2013-07-31
			 */
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.TiHuo.getValue()) {
				Customer customer = this.getDmpDAO.getCustomer(cwbOrderWithDeliveryState.getCwbOrder().getCustomerid());
				if (customer.getB2cEnum().equals(this.b2cJsonService.getB2cEnumKeys(customer, "tmall"))) {
					orderFlow.setFlowordertype(FlowOrderTypeEnum.RuKu.getValue());
				}
			}

			DmpCwbOrder cwbOrder = cwbOrderWithDeliveryState.getCwbOrder();
			String Jsoncontent = this.b2cJsonService.orderToJson(cwbOrderWithDeliveryState, orderFlow, orderFlow.getFlowordertype());
			if (Jsoncontent == null) { // 不满足封装的条件
				return;
			}
			B2CData b2cData = new B2CData();
			b2cData.setCwb(orderFlow.getCwb());
			b2cData.setCustomerid(cwbOrder.getCustomerid());
			b2cData.setFlowordertype(orderFlow.getFlowordertype());
			b2cData.setPosttime(DateTimeUtil.formatDate(orderFlow.getCredate()));
			b2cData.setSend_b2c_flag(0);
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.YiShenHe.getValue()) {
				try {
					DmpDeliveryState del = cwbOrderWithDeliveryState.getDeliveryState();
					b2cData.setDelId(del.getId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			b2cData.setJsoncontent(Jsoncontent); // 封装的JSON格式.
			String multi_shipcwb = StringUtils.hasLength(cwbOrder.getMulti_shipcwb()) ? cwbOrder.getMulti_shipcwb() : null;
			this.b2CDataDAO.saveB2CData(b2cData, multi_shipcwb);
			this.logger.info("RE: orderFlow send b2c -----------订单号:{} flowordertype:{}对接环节信息结束", cwbOrder.getCwb(), orderFlow.getFlowordertype());
		}
	}

	private void AddExcuteFlowStatusMethodByNotAudit(DmpOrderFlow orderFlow) throws Exception {
		CwbOrderWithDeliveryState cwbOrderWithDeliveryState = this.cwbOrderWithDeliveryStateReader.readValue(orderFlow.getFloworderdetail());
		if (cwbOrderWithDeliveryState != null) {
			if ((cwbOrderWithDeliveryState.getDeliveryState() != null) && (cwbOrderWithDeliveryState.getDeliveryState().getCodpos().compareTo(BigDecimal.ZERO) > 0)) {
				cwbOrderWithDeliveryState.getDeliveryState().setPos(cwbOrderWithDeliveryState.getDeliveryState().getCodpos());
				cwbOrderWithDeliveryState.getDeliveryState().setPosremark("POS反馈");
			}
			if ((cwbOrderWithDeliveryState.getCwbOrder() != null) && cwbOrderWithDeliveryState.getCwbOrder().getNewpaywayid().equals("5")) {
				cwbOrderWithDeliveryState.getCwbOrder().setNewpaywayid("2");
			}
		}
		DmpCwbOrder cwbOrder = cwbOrderWithDeliveryState.getCwbOrder();
		B2CData b2cData = new B2CData();
		if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.YiShenHe.getValue()) {// 不处理归班jms
			return;
		}
		if ((orderFlow.getFlowordertype() == FlowOrderTypeEnum.YiFanKui.getValue()) || (orderFlow.getFlowordertype() == FlowOrderTypeEnum.PosZhiFu.getValue())) {// pos支付，直接存储到对接表中
			DmpDeliveryState ds = cwbOrderWithDeliveryState.getDeliveryState();
			if (ds == null) {// 如果对象为空
				this.logger.info("RE: orderFlow send b2c 反馈对象为空,{}，订单号：{}", ds, orderFlow.getCwb());
				return;
			}
			if ((orderFlow.getFlowordertype() == FlowOrderTypeEnum.PosZhiFu.getValue()) && (ds.getDeliverystate() == 1) && (ds.getPos().compareTo(BigDecimal.ZERO) > 0)
					&& ds.getPosremark().equals("POS刷卡") && (ds.getDeliverstateremark().indexOf("chinaums") > -1)) {// 北京银联商务支付和签收一起
				orderFlow.setFlowordertype(FlowOrderTypeEnum.YiShenHe.getValue());
				cwbOrderWithDeliveryState.getDeliveryState().setDeliverystate(1);// 不管是否反馈，都置成配送成功状态
				this.logger.info("RE: orderFlow send b2c pos签收，转化成归班,{}", ds.getCwb());
			} else if ((ds.getDeliverystate() == 1) && (ds.getPos().compareTo(BigDecimal.ZERO) > 0) && ds.getPosremark().equals("POS刷卡") && (ds.getDeliverstateremark().indexOf("chinaums") < 0)) {// 不是pos支付，不存储到对接表中
				orderFlow.setFlowordertype(FlowOrderTypeEnum.YiShenHe.getValue());
				this.logger.info("RE: orderFlow send b2c pos签收，转化成归班,{}", ds.getCwb());
			} else {// 不是pos支付
				this.logger.info("RE: orderFlow send b2c 不是pos签收，不存入对接表,{}", ds.getCwb());
				return;
			}
		}
		if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.YiFanKui.getValue()) {
			orderFlow.setFlowordertype(FlowOrderTypeEnum.YiShenHe.getValue());
		}
		String Jsoncontent = this.b2cJsonService.orderToJson(cwbOrderWithDeliveryState, orderFlow, orderFlow.getFlowordertype());
		if (Jsoncontent == null) { // 不满足封装的条件
			return;
		}
		if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.YiShenHe.getValue()) {
			List<B2CData> list = this.b2CDataDAO.getB2CDataList(orderFlow.getCwb(), FlowOrderTypeEnum.YiShenHe.getValue());
			DmpDeliveryState del = cwbOrderWithDeliveryState.getDeliveryState();
			if ((list != null) && (list.size() > 0)) {
				for (B2CData b2cData2 : list) {
					b2cData = b2cData2;
					b2cData.setJsoncontent(Jsoncontent); // 封装的JSON格式.
					b2cData.setPosttime(DateTimeUtil.formatDate(orderFlow.getCredate()));
					b2cData.setDelId(del.getId());
					b2cData.setSend_b2c_flag(0);
					this.b2CDataDAO.updateB2CData(b2cData);
				}
			} else {
				b2cData.setCwb(orderFlow.getCwb());
				b2cData.setCustomerid(cwbOrder.getCustomerid());
				b2cData.setFlowordertype(orderFlow.getFlowordertype());
				b2cData.setPosttime(DateTimeUtil.formatDate(orderFlow.getCredate()));
				b2cData.setSend_b2c_flag(0);
				b2cData.setDelId(del.getId());
				SystemInstall delayToB2CState = this.getDmpDAO.getSystemInstallByName("delayToB2CState");
				if (delayToB2CState != null) {
					try {
						long timeout = Long.parseLong(delayToB2CState.getValue());
						b2cData.setTimeout(timeout);
						long sd = orderFlow.getCredate().getTime() + (timeout * 60 * 1000);
						Date dat = new Date(sd);
						GregorianCalendar gc = new GregorianCalendar();
						gc.setTime(dat);
						String timeoutdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(gc.getTime());
						b2cData.setTimeoutdate(timeoutdate);
					} catch (Exception e) {
						this.logger.error("封装超时时间异常！long或者时间转换异常", e);
					}

				}
				b2cData.setJsoncontent(Jsoncontent); // 封装的JSON格式.
				String multi_shipcwb = StringUtils.hasLength(cwbOrder.getMulti_shipcwb()) ? cwbOrder.getMulti_shipcwb() : null;
				this.b2CDataDAO.saveB2CData(b2cData, multi_shipcwb);
			}
		} else {
			b2cData.setCwb(orderFlow.getCwb());
			b2cData.setCustomerid(cwbOrder.getCustomerid());
			b2cData.setFlowordertype(orderFlow.getFlowordertype());
			b2cData.setPosttime(DateTimeUtil.formatDate(orderFlow.getCredate()));
			b2cData.setSend_b2c_flag(0);
			b2cData.setJsoncontent(Jsoncontent); // 封装的JSON格式.
			String multi_shipcwb = StringUtils.hasLength(cwbOrder.getMulti_shipcwb()) ? cwbOrder.getMulti_shipcwb() : null;
			this.b2CDataDAO.saveB2CData(b2cData, multi_shipcwb);
		}
		this.logger.info("RE: orderFlow send b2c -----------订单号:{} flowordertype:{}对接环节信息结束", cwbOrder.getCwb(), orderFlow.getFlowordertype());
	}

	private void buidCod(DmpOrderFlow orderFlow) throws Exception {
		if (orderFlow.getFlowordertype() != FlowOrderTypeEnum.YiShenHe.getValue()) {
			return;
		}
		CwbOrderWithDeliveryState cwbOrderWithDeliveryState = this.cwbOrderWithDeliveryStateReader.readValue(orderFlow.getFloworderdetail());
		Customer customer = this.getDmpDAO.getCustomer(cwbOrderWithDeliveryState.getCwbOrder().getCustomerid());
		if (customer.getB2cEnum().equals(this.b2cJsonService.getB2cEnumKeys(customer, "yamaxun"))) {
			this.bulidAmazonB2cData.buildAmazonCod(orderFlow, orderFlow.getFlowordertype(), cwbOrderWithDeliveryState.getDeliveryState(), cwbOrderWithDeliveryState.getCwbOrder());
		}
	}

	public void sendTodmp(String b2cids) {
		JSONObject json = new JSONObject();
		try {
			List<B2CData> list = this.b2CDataDAO.getB2CDataListByIds(b2cids);
			if ((list != null) && (list.size() > 0)) {
				String delIds = "";
				for (B2CData b2cData : list) {
					delIds += b2cData.getDelId() + ",";
				}
				delIds = delIds.length() > 0 ? delIds.substring(0, delIds.length() - 1) : "";
				
				json.put("ids", delIds);
				json.put("pushtime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				this.logger.info("消息发送端：sendBToCToDmpProducer, delIds={}", json.toString());
				this.sendBToCToDmpProducer.sendBodyAndHeader(null, "delIds", json.toString());
			}
		} catch (CamelExecutionException e) {
			logger.error("", e);
			//写MQ异常表
			this.mqExceptionDAO.save(MqExceptionBuilder.getInstance().buildExceptionCode(this.getClass().getSimpleName() + ".sendTodmp")
					.buildExceptionInfo(e.toString()).buildTopic(this.sendBToCToDmpProducer.getDefaultEndpoint().getEndpointUri())
					.buildMessageHeader("delIds", json.toString()).getMqException());
		
		}

	}
}
