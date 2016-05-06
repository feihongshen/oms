package cn.explink.b2c.homegobj;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.MessageFormat;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.User;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.jms.dto.CwbOrderWithDeliveryState;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;
import cn.explink.util.RestHttpServiceHanlder;
import cn.explink.util.MD5.MD5Util;

@Service
public class BuildHomegobjB2cData {
	private Logger logger = LoggerFactory.getLogger(BuildHomegobjB2cData.class);
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;

	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	HomegobjService homegobjService;
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	private GetDmpDAO getdmpDao;
	
	public String buildHomegobjMethod(DmpOrderFlow orderFlow, long flowOrdertype, DmpCwbOrder cwbOrder, long delivery_state, DmpDeliveryState dmpDeliveryState, ObjectMapper objectMapper)
			throws IOException, JsonGenerationException, JsonMappingException {

		String dmsReceiveStatus = homegobjService.getFlowEnum(flowOrdertype, delivery_state);
		if (dmsReceiveStatus == null) {
			logger.info("订单号：{} 不属于[家有购物北京]所需要的json---,状态{}，return", cwbOrder.getCwb(), flowOrdertype);
			return null;
		}

		Homegobj hg = homegobjService.getHomegobj(B2cEnum.HomegoBJ.getKey());

		String exptRemark = "";
		if (flowOrdertype == FlowOrderTypeEnum.YiShenHe.getValue() && delivery_state == DeliveryStateEnum.FenZhanZhiLiu.getValue()) {
			dmsReceiveStatus = HomegobjTrackEnum.YiChang1.getState();
			exptRemark = cwbOrder.getLeavedreason();
		}
		if (flowOrdertype == FlowOrderTypeEnum.YiShenHe.getValue() && delivery_state == DeliveryStateEnum.FenZhanZhiLiu.getValue()) {
			dmsReceiveStatus = HomegobjTrackEnum.YiChang2.getState();
			exptRemark = cwbOrder.getLeavedreason();
		}

		String xml = buildTrackStatusXML(orderFlow, cwbOrder, dmsReceiveStatus, hg, exptRemark);

		logger.info("家有购物北京-跟踪信息请求xml={}", xml);

		String responseXML = null;
		try {
			responseXML = RestHttpServiceHanlder.sendHttptoServerHomego(xml, hg.getFeedback_url());
		} catch (Exception e) {
			logger.error("家有购物跟踪异常" + orderFlow.getCwb(), e);
		}

		logger.info("家有购物北京-跟踪信息返回xml={}", responseXML);

		String order_status = getOrderResultEnum(flowOrdertype, delivery_state);// 获取配送结果

		if (order_status == null) {
			return null;
		}

		String remark = "";
		if (order_status.equals(OrderStatusEnum.JuShou.getState())) { // 拒收

			String reason_code = b2ctools.getExptReasonByB2c(0, cwbOrder.getBackreasonid(), cwbOrder.getCustomerid() + "", delivery_state).getExpt_code();
			order_status = reason_code == null ? order_status : reason_code;
			remark = cwbOrder.getBackreason();

		}

		if (order_status.equals(OrderStatusEnum.ZhiLiu.getState())) { // 滞留

			String reason_code = b2ctools.getExptReasonByB2c(cwbOrder.getLeavedreasonid(), 0, cwbOrder.getCustomerid() + "", delivery_state).getExpt_code();
			order_status = reason_code == null ? order_status : reason_code;
			remark = cwbOrder.getLeavedreason();
		}

		HomegobjXmlNote note = new HomegobjXmlNote();
		note.setInvc_id(cwbOrder.getCwb());
		note.setOrd_id(cwbOrder.getTranscwb());
		note.setOrd_stat(order_status);
		note.setRemark(remark);
		note.setStat_time(DateTimeUtil.formatDate(orderFlow.getCredate(), "yyyyMMddHHmmss"));

		if (order_status.equals(OrderStatusEnum.DuanXinYuYue.getState())) { // 短信预约
			long deliveryid = dmpDeliveryState.getDeliveryid();
			note.setDelivery_id(deliveryid + "");
			User user = getDmpdao.getUserById(deliveryid);
			note.setDelivery_name(user.getRealname());
			note.setDelivery_phone(user.getUsermobile());
			note.setDelivery_time(DateTimeUtil.formatDate(orderFlow.getCredate(), "yyyyMMddHHmmss"));
		}

		return objectMapper.writeValueAsString(note);

	}

	private String buildTrackStatusXML(DmpOrderFlow orderFlow, DmpCwbOrder cwbOrder, String dmsReceiveStatus, Homegobj hg, String exptRemark) {
		String signXml = "<Jiayou>" + "<header>" + "<function_id>JIAYOU0003</function_id>" + "<version>1.0</version>" + "<app_id>" + hg.getExpress_id() + "</app_id>" + "<charset>01</charset>"
				+ "<sign_type>02</sign_type>" + "<data_type>01</data_type>" + "<request_time>" + DateTimeUtil.formatDate(orderFlow.getCredate(), "yyyyMMddHHmmss") + "</request_time>" + "</header>"
				+ "<body>" + "<orders>" + "<order>" + "<invc_id>" + cwbOrder.getCwb() + "</invc_id>" + "<ord_id>" + cwbOrder.getTranscwb() + "</ord_id>" + "<ord_stat>" + dmsReceiveStatus
				+ "</ord_stat>" + "<remark>" + exptRemark + "</remark>" + "<steps>" + "<step>" + "<step_time>" + DateTimeUtil.formatDate(orderFlow.getCredate(), "yyyyMMddHHmmss") + "</step_time>"
				+ "<step_address>" + getDetail(orderFlow) + "</step_address>" + "</step>" + "</steps>" + "</order>" + "</orders>" + "</body>" + "</Jiayou>";

		String signed = MD5Util.md5(signXml + hg.getPrivate_key());

		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<Jiayou>" + "<header>" + "<function_id>JIAYOU0003</function_id>" + "<version>1.0</version>" + "<app_id>" + hg.getExpress_id()
				+ "</app_id>" + "<charset>01</charset>" + "<sign_type>02</sign_type>" + "<signed>" + signed.toUpperCase() + "</signed>" + "<data_type>01</data_type>" + "<request_time>"
				+ DateTimeUtil.formatDate(orderFlow.getCredate(), "yyyyMMddHHmmss") + "</request_time>" + "</header>" + "<body>" + "<orders>" + "<order>" + "<invc_id>" + cwbOrder.getCwb()
				+ "</invc_id>" + "<ord_id>" + cwbOrder.getTranscwb() + "</ord_id>" + "<ord_stat>" + dmsReceiveStatus + "</ord_stat>" + "<remark>" + exptRemark + "</remark>" + "<steps>" + "<step>"
				+ "<step_time>" + DateTimeUtil.formatDate(orderFlow.getCredate(), "yyyyMMddHHmmss") + "</step_time>" + "<step_address>" + getDetail(orderFlow) + "</step_address>"
				+ "</step>" + "</steps>" + "</order>" + "</orders>" + "</body>" + "</Jiayou>";
		return xml;
	}

	//获取轨迹详情
	public String getDetail(DmpOrderFlow orderFlow) {
		try {
			User operatorUser = getUserById(orderFlow.getUserid());
			logger.info("====cwb={},flowordertype="+orderFlow.getFlowordertype()+",user={}==================",orderFlow.getCwb(),JacksonMapper.getInstance().writeValueAsString(operatorUser));
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.DaoRuShuJu.getValue()) {
				return MessageFormat.format("从[{0}]导入数据", getBranchById(orderFlow.getBranchid()).getBranchname());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.RuKu.getValue()) {
				return MessageFormat.format("从[{0}]入库", getBranchById(orderFlow.getBranchid()).getBranchname(), operatorUser.getUsermobile());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.ChuKuSaoMiao.getValue()) {
				CwbOrderWithDeliveryState cwbOrderWithDeliveryState = new ObjectMapper().readValue(orderFlow.getFloworderdetail().toString(), CwbOrderWithDeliveryState.class);
				Branch nextBranch = getBranchById(cwbOrderWithDeliveryState.getCwbOrder().getNextbranchid());
				return MessageFormat.format("从[{0}]出库，下一站[{1}]，联系电话[{2}]", getBranchById(orderFlow.getBranchid()).getBranchname(), nextBranch.getBranchname(), operatorUser.getUsermobile());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.CheXiaoFanKui.getValue()) {
				return MessageFormat.format("货物由[{0}]撤销反馈", getBranchById(orderFlow.getBranchid()).getBranchname());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.KuDuiKuTuiHuoChuKu.getValue()) {
				return MessageFormat.format("货物已由[{0}]进行库对库退货出库", getBranchById(orderFlow.getBranchid()).getBranchname());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.ErJiFenBo.getValue()) {
				return MessageFormat.format("货物从[{0}]进行二级分拨操作", getBranchById(orderFlow.getBranchid()).getBranchname());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.EeJiZhanTuiHuoChuZhan.getValue()) {
				return MessageFormat.format("货物已由二级站[{0}]退货出站", getBranchById(orderFlow.getBranchid()).getBranchname());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue()) {
				return MessageFormat.format("货物已到[{0}]，联系方式[{1}]", getBranchById(orderFlow.getBranchid()).getBranchname(), operatorUser.getUsermobile());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.FenZhanDaoHuoYouHuoWuDanSaoMiao.getValue()) {
				return MessageFormat.format("货物已到[{0}]", getBranchById(orderFlow.getBranchid()).getBranchname());
			}
			
			//分站领货
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.FenZhanLingHuo.getValue()) {
				CwbOrderWithDeliveryState cwbOrderWithDeliveryState = new ObjectMapper().readValue(orderFlow.getFloworderdetail().toString(), CwbOrderWithDeliveryState.class);
				User deliverUser = getUserById(cwbOrderWithDeliveryState.getDeliveryState().getDeliveryid());

				String comment = orderFlow.getComment();
				if (comment != null && !comment.isEmpty() && comment.contains("正在派件") && comment.contains("派件人")) {
					return MessageFormat.format("货物已从[{0}]" + comment+",站点联系电话[{1}]", getBranchById(orderFlow.getBranchid()).getBranchname(),getBranchPhone(getBranchById(orderFlow.getBranchid())));
				} else {
					return MessageFormat.format("货物由[{0}]的派件员[{1}]正在派件..小件员电话[{2}],站点联系电话[{3}]", getBranchById(orderFlow.getBranchid()).getBranchname(), deliverUser.getRealname(), deliverUser.getUsermobile(),getBranchPhone(getBranchById(orderFlow.getBranchid())));
				}

			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.YiFanKui.getValue()) {
				CwbOrderWithDeliveryState cwbOrderWithDeliveryState = new ObjectMapper().readValue(orderFlow.getFloworderdetail().toString(), CwbOrderWithDeliveryState.class);
				DmpDeliveryState deliverstate = cwbOrderWithDeliveryState.getDeliveryState();

				return MessageFormat.format("货物已由[{0}]的派送员[{1}]反馈为[{2}],电话[{3}]", getBranchById(orderFlow.getBranchid()).getBranchname(), getUserById(deliverstate.getDeliveryid()).getRealname(),
						DeliveryStateEnum.getByValue((int) deliverstate.getDeliverystate()).getText(), getUserById(deliverstate.getDeliveryid()).getUsermobile());

			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.TuiHuoChuKuSaoMiao.getValue()) {
				return MessageFormat.format("货物已从[{0}]进行退货出库", getBranchById(orderFlow.getBranchid()).getBranchname());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.TuiHuoZhanRuKu.getValue()) {
				return MessageFormat.format("货物已到退货站[{0}]", getBranchById(orderFlow.getBranchid()).getBranchname());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.TuiHuoZhanZaiTouSaoMiao.getValue()) {
				return MessageFormat.format("货物由[{0}]进行退货站再投", getBranchById(orderFlow.getBranchid()).getBranchname());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.TuiGongYingShangChuKu.getValue()) {
				return MessageFormat.format("货物已由[{0}]退供货商出库", getBranchById(orderFlow.getBranchid()).getBranchname());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.GongHuoShangTuiHuoChenggong.getValue()) {
				return MessageFormat.format("货物已由[{0}]退供货商成功", getBranchById(orderFlow.getBranchid()).getBranchname());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.YiShenHe.getValue()) {
				CwbOrderWithDeliveryState cwbOrderWithDeliveryState = new ObjectMapper().readValue(orderFlow.getFloworderdetail().toString(), CwbOrderWithDeliveryState.class);
				DmpDeliveryState deliverstate = cwbOrderWithDeliveryState.getDeliveryState();
				String exptMsg = "";
				if (deliverstate.getDeliverystate() == DeliveryStateEnum.QuanBuTuiHuo.getValue() || deliverstate.getDeliverystate() == DeliveryStateEnum.BuFenTuiHuo.getValue()
						|| deliverstate.getDeliverystate() == DeliveryStateEnum.FenZhanZhiLiu.getValue() || deliverstate.getDeliverystate() == DeliveryStateEnum.HuoWuDiuShi.getValue()) {
					exptMsg = ",原因:" + cwbOrderWithDeliveryState.getCwbOrder().getBackreason() + "" + cwbOrderWithDeliveryState.getCwbOrder().getLeavedreason();
				}
				return MessageFormat.format("货物已由[{0}]的小件员[{1}]反馈为[{2}],电话[{3}]" + exptMsg, getBranchById(orderFlow.getBranchid()).getBranchname(), getUserById(deliverstate.getDeliveryid())
						.getRealname(), DeliveryStateEnum.getByValue((int) deliverstate.getDeliverystate()).getText(), getUserById(deliverstate.getDeliveryid()).getUsermobile());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.ZhongZhuanChuKuSaoMiao.getValue()) {
				return MessageFormat.format("货物已从[{0}]进行中转出库", getBranchById(orderFlow.getBranchid()).getBranchname());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.ZhongZhuanZhanRuKu.getValue()) {
				return MessageFormat.format("货物已到中转站[{0}]", getBranchById(orderFlow.getBranchid()).getBranchname());
			}
			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.ZhongZhuanZhanChuKuSaoMiao.getValue()) {
				return MessageFormat.format("货物已从[{0}]进行中转站出库", getBranchById(orderFlow.getBranchid()).getBranchname());
			}
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
		return null;
	}
	
	//获取站点联系电话
	private String getBranchPhone(Branch branch) {
		if(branch!=null){
			String branchMobile= branch.getBranchmobile();
			String branchphone= branch.getBranchphone();
			if(branchMobile!=null&&!"".equals(branchMobile)){
				return branchMobile;
			}
			if(branchphone!=null&&!"".equals(branchphone)){
				return branchphone;
			}
		}
		return "";
	}
	
	
	//获取站点
	private Branch getBranchById(long branchid) {
		return getdmpDao.getNowBranch(branchid);
	}
	
	private User getUserById(long userid) {
		return getdmpDao.getUserById(userid);
	}
	
	private String getOrderResultEnum(long flowOrdertype, long delivery_state) {
		String order_status = null;
		if (flowOrdertype == FlowOrderTypeEnum.YiShenHe.getValue()
				&& (delivery_state == DeliveryStateEnum.PeiSongChengGong.getValue() || delivery_state == DeliveryStateEnum.ShangMenTuiChengGong.getValue() || delivery_state == DeliveryStateEnum.ShangMenHuanChengGong
						.getValue())) {
			order_status = OrderStatusEnum.Qianshou.getState();
		}

		if (flowOrdertype == FlowOrderTypeEnum.YiShenHe.getValue() && delivery_state == DeliveryStateEnum.FenZhanZhiLiu.getValue()) {
			order_status = OrderStatusEnum.ZhiLiu.getState();
		}

		if (flowOrdertype == FlowOrderTypeEnum.YiShenHe.getValue() && delivery_state == DeliveryStateEnum.HuoWuDiuShi.getValue()) {
			order_status = OrderStatusEnum.DiuShi.getState();
		}

		if (flowOrdertype == FlowOrderTypeEnum.YiShenHe.getValue()
				&& (delivery_state == DeliveryStateEnum.QuanBuTuiHuo.getValue() || delivery_state == DeliveryStateEnum.BuFenTuiHuo.getValue() || delivery_state == DeliveryStateEnum.ShangMenJuTui
						.getValue())) {
			order_status = OrderStatusEnum.JuShou.getState();
		}
		if (flowOrdertype == FlowOrderTypeEnum.FenZhanLingHuo.getValue()) {
			order_status = OrderStatusEnum.DuanXinYuYue.getState();
		}
		return order_status;
	}

}
