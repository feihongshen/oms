package cn.explink.b2c.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.apache.camel.builder.RouteBuilder;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.vipshop.VipShop;
import cn.explink.b2c.vipshop.VipShopConfig;
import cn.explink.b2c.vipshop.mpspack.VipmpsFlowEnum;
import cn.explink.b2c.vipshop.mpspack.VipmpsNote;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.MqExceptionDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.Customer;
import cn.explink.domain.MqExceptionBuilder;
import cn.explink.domain.MqExceptionBuilder.MessageSourceEnum;
import cn.explink.domain.Transflowdata;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.jms.dto.CwbOrderWithDeliveryState;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpTranscwbOrderFlow;
import cn.explink.jms.dto.TransCwbDetail;
import cn.explink.util.DateTimeUtil;
import cn.explink.util.JsonUtil;

/**
 * 监听运单流程产生的JMS消息，
 *
 * @author Administrator
 *
 */
@Service
public class TransFlowFromJMSService {
	@Autowired
	private CamelContext camelContext;

	@Autowired
	TransflowdataDAO transflowdataDAO;
	@Autowired
	B2cJsonService b2cJsonService;
	@Autowired
	GetDmpDAO getDmpDAO;
	@Autowired
	CacheBaseListener cacheBaseListener;
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;



	private ObjectMapper objectMapper = new ObjectMapper();
	private ObjectReader dmpOrderFlowMapper = this.objectMapper.reader(DmpTranscwbOrderFlow.class);

	private Logger logger = LoggerFactory.getLogger(TransFlowFromJMSService.class);
	private List<String> flowList = new ArrayList<String>(); // 存储 对接所用的环节
	
	@Autowired
	private MqExceptionDAO mqExceptionDAO;
	
	private static final String MQ_FROM_URI = "jms:queue:VirtualTopicConsumers.omsb2c.transCwbOrderFlow";
	private static final String MQ_HEADER_NAME = "transCwbOrderFlow";

	@PostConstruct
	public void init() {
		try {

			this.flowList.add(FlowOrderTypeEnum.DaoRuShuJu.getMethod()); // 导入
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
	
			this.camelContext.addRoutes(new RouteBuilder() {
				@Override
				public void configure() throws Exception {

					this.from("jms:queue:VirtualTopicConsumers.omsb2c.transCwbOrderFlow?concurrentConsumers=15").to("bean:transFlowFromJMSService?method=saveTransCwbFlow").routeId("transcwbdetail_");
				}
			});

		} catch (Exception e) {
			this.logger.error("TransFlowFromJMSService监听JMS异常！",e);
		}
	}

	/**
	 * 接收环节消息，用于B2C对接，存储B2C_Send 表
	 *
	 * @param Object 
	 */
	public void saveTransCwbFlow(@Header("transCwbOrderFlow") String parm, @Header("MessageHeaderUUID") String messageHeaderUUID) {
	
	
		this.logger.info("transCwbOrderFlow send b2c 环节信息处理,{}", parm);
		
		
		try {
			DmpTranscwbOrderFlow transCwbOrderFlow = this.dmpOrderFlowMapper.readValue(parm);
			
			String floworderTypeMethod = transCwbOrderFlow.getFlowordertypeMethod();
			if (!this.flowList.contains(floworderTypeMethod)) {
				return;
			}
			
			CwbOrderWithDeliveryState cwbOrderWithDeliveryState = JsonUtil.readValue(transCwbOrderFlow.getFloworderdetail(), CwbOrderWithDeliveryState.class);
			if (cwbOrderWithDeliveryState == null) {
				return;
			}
			
			if (this.transflowdataDAO.checkIsRepeatDataFlag(cwbOrderWithDeliveryState.getTransCwbDetail().getTranscwb(), transCwbOrderFlow.getFlowordertype(), DateTimeUtil.formatDate(transCwbOrderFlow.getCredate())) > 0) {
				this.logger.info("RE: transOrderFlow send b2c 环节信息重复,已过滤,{}", parm);
				return;
			}

			this.logger.info("send transOrderFlow b2c:cwb={},flowordertype={}", transCwbOrderFlow.getCwb(), transCwbOrderFlow.getFlowordertype());

			
			
			

			// 存入正常流程的状态信息
			this.addExcuteFlowStatusMethod(transCwbOrderFlow,cwbOrderWithDeliveryState);
			
		} catch (Exception e1) {
			this.logger.error("error while handle transorderflow", e1);
			// 把未完成MQ插入到数据库中, start
			String functionName = "saveTransCwbFlow";
			String fromUri = MQ_FROM_URI;
			String body = null;
			String headerName = MQ_HEADER_NAME;
			String headerValue = parm;
			
			//消费MQ异常表
			this.mqExceptionDAO.save(MqExceptionBuilder.getInstance().buildExceptionCode(functionName)
					.buildExceptionInfo(e1.toString()).buildTopic(fromUri)
					.buildMessageHeader(headerName, headerValue)
					.buildMessageHeaderUUID(messageHeaderUUID).buildMessageSource(MessageSourceEnum.receiver.getIndex()).getMqException());
			// 把未完成MQ插入到数据库中, end
			
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
	private void addExcuteFlowStatusMethod(DmpTranscwbOrderFlow transcwbOrderFlow,CwbOrderWithDeliveryState cwbOrderWithDeliveryState) throws Exception {

			DmpCwbOrder cwbOrder = cwbOrderWithDeliveryState.getCwbOrder();
			TransCwbDetail transCwbDetail = cwbOrderWithDeliveryState.getTransCwbDetail();
			
			Customer customer = this.cacheBaseListener.getCustomer(cwbOrder.getCustomerid());
			if (!customer.getB2cEnum().equals(this.getB2cEnumKeys(customer, "vipshop"))) {
				return;
			}
			
			if(transCwbDetail==null){
				return;
			}
			
			DmpDeliveryState dmpDeliveryState = cwbOrderWithDeliveryState.getDeliveryState();
			
			String filterFlowState = this.filterVipshopMpsFlow(transcwbOrderFlow.getFlowordertype(),dmpDeliveryState!=null?dmpDeliveryState.getDeliverystate():0l );
			if(filterFlowState == null){
				logger.info("cwb={},transcwb={} 不属于指定推送集包流程",cwbOrder.getCwb(),transCwbDetail.getTranscwb());
				return ;
			}
			
			Transflowdata flowdata = new Transflowdata();
			flowdata.setTranscwb(transCwbDetail.getTranscwb());
			flowdata.setCwb(transcwbOrderFlow.getCwb());
			flowdata.setCustomerid((int)cwbOrder.getCustomerid());
			flowdata.setFlowordertype(transcwbOrderFlow.getFlowordertype());
			flowdata.setCreatetime(DateTimeUtil.getNowTime());
			flowdata.setFlowtime(DateTimeUtil.formatDate(transcwbOrderFlow.getCredate()));
			String jsonContent = buildVipshopmpsNote(transcwbOrderFlow, transCwbDetail, filterFlowState,cwbOrder,dmpDeliveryState);
			if(jsonContent==null){
				return ;
			}
			flowdata.setJsoncontent(jsonContent);
			
			
			this.transflowdataDAO.saveTransFlowData(flowdata);
			
			this.logger.info("RE: transcwbOrderFlow send b2c cwb={},transcwb={} ,flowordertype={}集包对接环节信息结束", new Object[]{cwbOrder.getCwb(),transCwbDetail.getTranscwb(), transcwbOrderFlow.getFlowordertype()});
		
	}

	private String buildVipshopmpsNote(DmpTranscwbOrderFlow transcwbOrderFlow,TransCwbDetail transCwbDetail, String filterFlowState,DmpCwbOrder cwbOrder,DmpDeliveryState dmpDeliveryState) throws JsonGenerationException, JsonMappingException, IOException {
					Branch branch = this.cacheBaseListener.getBranch(transcwbOrderFlow.getBranchid());
					VipmpsNote note = new VipmpsNote();
					note.setVersion(VipShopConfig.versionNew);
					note.setBox_id(transCwbDetail.getTranscwb());
					note.setCurrent_city_name(branch.getBranchname());
					note.setOrder_sn(transcwbOrderFlow.getCwb());
					note.setOrder_status(filterFlowState);
					note.setOrder_status_info(orderFlowDetail.getTranscwbFlowDetail(transcwbOrderFlow) == null ? "配送在途" : orderFlowDetail.getTranscwbFlowDetail(transcwbOrderFlow));
					if(Integer.valueOf(filterFlowState)==VipmpsFlowEnum.FenZhanZhiLiu.getVipshop_state()
							&&cwbOrder.getFlowordertype()==FlowOrderTypeEnum.YiShenHe.getValue()){
						ExptReason exptReason = b2ctools.getExptReasonByB2c(cwbOrder.getLeavedreasonid(), 0, String.valueOf(cwbOrder.getCustomerid()), dmpDeliveryState.getDeliverystate());
						String expt_msg = exptReason.getExpt_msg();
						String expt_code = exptReason.getExpt_code();
						if (expt_code != null && !expt_code.isEmpty()){
							note.setOrder_status_info(expt_msg);
							note.setOrder_status(expt_code);
						}else{
							return null;
						}
					}
					
					note.setOrder_status_time(DateTimeUtil.formatDate(transcwbOrderFlow.getCredate()));
					return JsonUtil.translateToJson(note);
	}

	public String getB2cEnumKeys(Customer customer, String constainsStr) {
		for (B2cEnum enums : B2cEnum.values()) {
			if (enums.getMethod().contains(constainsStr)) {
				if (customer.getB2cEnum().equals(String.valueOf(enums.getKey()))) {
					return String.valueOf(enums.getKey());
				}
			}
		}
		return null;
	}

	
	public String filterVipshopMpsFlow(long flowordertype, long deliverystate) {

		if (flowordertype != FlowOrderTypeEnum.YiShenHe.getValue()) {
			for (VipmpsFlowEnum TEnum : VipmpsFlowEnum.values()) {
				if (flowordertype == TEnum.getFlowordertype()) {
					return String.valueOf(TEnum.getVipshop_state());
				}
			}
		}
		
		
		if ((flowordertype == FlowOrderTypeEnum.YiShenHe.getValue()) && (deliverystate == DeliveryStateEnum.FenZhanZhiLiu.getValue())) {
			return String.valueOf(VipmpsFlowEnum.FenZhanZhiLiu.getVipshop_state());
		}
		
		return null;

	}
	
	// 获取VipShop配置信息
	public VipShop getVipshop(int key) {
		return this.b2ctools.getObject(key, VipShop.class);
	}


}
