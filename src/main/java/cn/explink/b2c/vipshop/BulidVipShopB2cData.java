package cn.explink.b2c.vipshop;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.explink.core_up.EpaiCoreService_Receiver;
import cn.explink.b2c.tmall.TmallService;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.CacheBaseListener;
import cn.explink.b2c.tools.ExptReason;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.WarehouseCommenDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.Common;
import cn.explink.domain.User;
import cn.explink.enumutil.CwbOrderTypeIdEnum;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;

@Service
public class BulidVipShopB2cData {
	private Logger logger = LoggerFactory.getLogger(BulidVipShopB2cData.class);
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;
	@Autowired
	TmallService tmallService;
	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	VipShopCwbFeedBackService vipshopService;
	@Autowired
	WarehouseCommenDAO warehouseCommenDAO;
	@Autowired
	EpaiCoreService_Receiver epaiCoreService_Receiver;
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	CacheBaseListener cacheBaseListener;

	public String BuildVipShopMethod(String b2cenum, DmpOrderFlow orderFlow, long flowOrdertype, DmpCwbOrder cwbOrder, DmpDeliveryState deliveryState, long delivery_state, ObjectMapper objectMapper)
			throws IOException, JsonGenerationException, JsonMappingException {
		String VipShopReceiveStatus = vipshopService.getVipShopFlowEnum(flowOrdertype, delivery_state, Long.valueOf(cwbOrder.getCwbordertypeid()));
		if (VipShopReceiveStatus == null) {
			logger.info("订单号：{} 不属于 唯品会所需要的json---,状态{}，return", cwbOrder.getCwb(), flowOrdertype);
			return null;
		}
		if (cwbOrder.getCwbstate() == 2 && !"-2".equals(VipShopReceiveStatus) && !"5".equals(VipShopReceiveStatus)
				&& Long.valueOf(cwbOrder.getCwbordertypeid()) == CwbOrderTypeIdEnum.Peisong.getValue()) {
			logger.info("订单号：{} 隔离退货之后的流程{}，return", cwbOrder.getCwb(), flowOrdertype);
			return null;
		}

		else {

			logger.info("订单号：{}封装成唯品会所需要的json----开始,状态：{}", cwbOrder.getCwb(), flowOrdertype);

			VipShopXMLNote vipshopXMLNote = new VipShopXMLNote();
			vipshopXMLNote.setOrder_sn(cwbOrder.getCwb());
			vipshopXMLNote.setOrder_status(VipShopReceiveStatus);
			vipshopXMLNote.setCwbordertypeid(Long.valueOf(cwbOrder.getCwbordertypeid()));

			if (Long.valueOf(cwbOrder.getCwbordertypeid()) == CwbOrderTypeIdEnum.Shangmentui.getValue()) {
				if (VipShopReceiveStatus.equals(String.valueOf(VipShopFlowEnum.ShangMenTuiChengGong_t.getVipshop_state()))
						|| VipShopReceiveStatus.equals(String.valueOf(VipShopFlowEnum.ShengMenJuTui_t.getVipshop_state()))) {
					String details = filterGoodDetails(cwbOrder, delivery_state);
					vipshopXMLNote.setDetails(details);
				}
				vipshopXMLNote.setShangmenlanshoutime(deliveryState != null ? deliveryState.getShangmenlanshoutime() : "");

				if (VipShopReceiveStatus.equals(String.valueOf(VipShopFlowEnum.FenZhanLingHuo_t.getVipshop_state()))) {
					// User
					// deliverUser=getDmpdao.getUserById(cwbOrder.getDeliverid());
					User deliverUser = cacheBaseListener.getUser(cwbOrder.getDeliverid());

					String delivername = deliverUser.getRealname();
					String usermobile = deliverUser.getUsermobile();
					vipshopXMLNote.setDeliver_name(delivername);
					vipshopXMLNote.setDeliver_mobile(usermobile);
				}

			}

			if (deliveryState != null && deliveryState.getDeliverystate() != 0) {
				String currenttime = deliveryState.getDeliverytime() != null && !deliveryState.getDeliverytime().isEmpty() ? deliveryState.getDeliverytime() : DateTimeUtil.getNowTime();
				String shouldtime = "";
				try {// 以反馈时间作为推送唯品会的时间
					shouldtime = deliveryState.getSign_time() != null && !deliveryState.getSign_time().isEmpty() ? deliveryState.getSign_time() : currenttime;

				} catch (Exception e) {
					logger.error("唯品会提前时间未知异常", e);
					shouldtime = currenttime;
				}

				vipshopXMLNote.setOrder_status_time(shouldtime);
			} else {
				vipshopXMLNote.setOrder_status_time(DateTimeUtil.formatDate(orderFlow.getCredate()));
			}
			vipshopXMLNote.setVersion("1.0");

			if (delivery_state == DeliveryStateEnum.QuanBuTuiHuo.getValue() || delivery_state == DeliveryStateEnum.BuFenTuiHuo.getValue()) {
				ExptReason exptReason = b2ctools.getExptReasonByB2c(cwbOrder.getLeavedreasonid(), cwbOrder.getBackreasonid(), String.valueOf(cwbOrder.getCustomerid()), delivery_state);
				String expt_msg = (exptReason.getExpt_msg() == null || exptReason.getExpt_msg().equals("")) ? "其他原因" : exptReason.getExpt_msg();
				vipshopXMLNote.setOrder_status_info(expt_msg);
			} else if (delivery_state == DeliveryStateEnum.FenZhanZhiLiu.getValue()) { // 20131105
																						// 唯品会新增
																						// 滞留状态一定要存储编码，拒收不需要
				ExptReason exptReason = b2ctools.getExptReasonByB2c(cwbOrder.getLeavedreasonid(), 0, String.valueOf(cwbOrder.getCustomerid()), delivery_state);
				String expt_msg = exptReason.getExpt_msg();
				String expt_code = exptReason.getExpt_code();
				if (expt_code == null || expt_code.isEmpty()) { // 如果不满足匹配条件
					return null;
				}

				vipshopXMLNote.setOrder_status_info(expt_msg);
				vipshopXMLNote.setOrder_status(expt_code);

			} else {
				vipshopXMLNote.setOrder_status_info(orderFlowDetail.getDetail(orderFlow) == null ? "成功" : orderFlowDetail.getDetail(orderFlow));
			}
			String currentCityName = cacheBaseListener.getBranch(orderFlow.getBranchid()).getBranchname() == null ? "分站" : cacheBaseListener.getBranch(orderFlow.getBranchid()).getBranchname();

			vipshopXMLNote.setCurrent_city_name(currentCityName);
			String consigneename = cwbOrder.getConsigneename() != null && !"".equals(cwbOrder.getConsigneename()) ? cwbOrder.getConsigneename() : "本人";
			vipshopXMLNote.setSign_man("5".equals(VipShopReceiveStatus) ? consigneename : "");

			// 33状态

			if ("33".equals(VipShopReceiveStatus)) {
				// User
				// deliverUser=getDmpdao.getUserById(cwbOrder.getDeliverid());
				User deliverUser = cacheBaseListener.getUser(cwbOrder.getDeliverid());
				String delivername = deliverUser.getRealname();
				String usermobile = deliverUser.getUsermobile();
				try {
					if (delivername != null && delivername.length() > 4) {
						delivername = delivername.substring(0, 4);
					}
					if (usermobile != null && usermobile.length() > 12) {
						usermobile = usermobile.substring(0, 12);
					}
				} catch (Exception e) {
				}
				if (cwbOrder.getRemark5().contains("信息：") && cwbOrder.getRemark5().contains("_")) {
					delivername = cwbOrder.getRemark5().substring(3, cwbOrder.getRemark5().indexOf("_"));
					usermobile = cwbOrder.getRemark5().substring(cwbOrder.getRemark5().indexOf("_") + 1);
				}

				String message = "您的订单:" + cwbOrder.getCwb() + "已到配送站点。详情请联系配送员：" + delivername + "，电话：" + usermobile + "。请留意收件";
				vipshopXMLNote.setOrder_status_info(message);
				vipshopXMLNote.setDeliverUser(deliverUser.getRealname());
				vipshopXMLNote.setDeliverMobile(deliverUser.getUsermobile());
				vipshopXMLNote.setDeliverBranch(getBranchById(orderFlow.getBranchid()).getBranchname());

			}
			if (flowOrdertype == FlowOrderTypeEnum.ChuKuSaoMiao.getValue()) {
				List<Common> comList = epaiCoreService_Receiver.getCommonList();
				if (comList != null && comList.size() > 0) {
					for (Common common : comList) {
						if (common.getBranchid() > 0 && cwbOrder.getNextbranchid() == common.getBranchid()) {
							vipshopXMLNote.setOrder_status_info("因超区,转" + common.getCommonname() + ",唯品会订单号：" + cwbOrder.getCwb() + ",承运商查单电话：" + common.getPhone());
							break;
						}

					}
				}
			}

			if (vipshopXMLNote.getOrder_status_info() == null) {
				vipshopXMLNote.setOrder_status_info("处理完成");
			}

			logger.info("订单号：{}封装成唯品会所需要的json----结束,状态：{}", cwbOrder.getCwb(), flowOrdertype);
			return objectMapper.writeValueAsString(vipshopXMLNote);
		}
	}

	private String filterGoodDetails(DmpCwbOrder cwbOrder, long delivery_state) throws IOException, JsonParseException, JsonMappingException, JsonGenerationException {
		String goodDetails = getDmpdao.getOrderGoods(cwbOrder.getCwb());

		List<OrderGoods> orderGoodslist = JacksonMapper.getInstance().readValue(goodDetails, new TypeReference<List<OrderGoods>>() {
		});
		if (orderGoodslist != null && orderGoodslist.size() > 0) {
			for (OrderGoods good : orderGoodslist) {
				if (delivery_state == DeliveryStateEnum.ShangMenTuiChengGong.getValue()) {
					if (good.getShituicount() == 0 && good.getWeituicount() == 0) {
						good.setShituicount(Integer.valueOf(good.getGoods_num()));
					}
				}

			}
		}
		String details = JacksonMapper.getInstance().writeValueAsString(orderGoodslist);
		return details;
	}

	private Branch getBranchById(long branchid) {
		return cacheBaseListener.getBranch(branchid);
	}

}
