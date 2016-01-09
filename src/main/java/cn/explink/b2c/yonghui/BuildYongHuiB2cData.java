package cn.explink.b2c.yonghui;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.b2c.yonghui.domain.Content;
import cn.explink.b2c.yonghui.domain.Order;
import cn.explink.b2c.yonghui.domain.YongHui;
import cn.explink.b2c.zhongliang.BuildZhongliangB2cData;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.B2CData;
import cn.explink.domain.User;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;

@Service
public class BuildYongHuiB2cData {
	private Logger logger = LoggerFactory.getLogger(BuildZhongliangB2cData.class);
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;

	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	YHServices yonghuiServices;
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	B2CDataDAO b2cDataDAO;
	@Autowired
	GetDmpDAO dmpDAO;

	public String buildYongHuiMethod(DmpOrderFlow orderFlow, long flowOrdertype, DmpCwbOrder cwbOrder, long delivery_state, DmpDeliveryState deliveryState, ObjectMapper objectMapper)
			throws IOException {
		// this.logger.info("-永辉-对接订单号：{} 插入express_send_b2c_data表,floweordertype={}",
		// cwbOrder.getCwb(), flowOrdertype);

		YongHuiFlowEnum state = this.yonghuiServices.getFlowEnum(flowOrdertype, delivery_state);
		if (state == null) {
			this.logger.info("订单号：{} 不属于[-永辉-]所需要的json---,状态{}，return", cwbOrder.getCwb(), flowOrdertype);
			return null;
		}
		Content con = new Content();
		if (state.getState() == YongHuiFlowEnum.ZhiLiu.getState()) {
			state = this.filterSate(flowOrdertype, cwbOrder);
		}
		YongHui yh = this.yonghuiServices.getYongHui(B2cEnum.YongHui.getKey());

		con.setClient_id(yh.getClient_id());
		con.setSecret(yh.getSecret());
		Order order = new Order();
		order.setSheetid(cwbOrder.getCwb());
		order.setBagno(cwbOrder.getTranscwb());
		order.setFlag(state.getState());
		order.setSdate(DateTimeUtil.formatDate(orderFlow.getCredate()));
		if (deliveryState != null) {
			User user = this.getDmpdao.getUserById(deliveryState.getDeliveryid());
			order.setSender(user.getRealname());
			order.setSendphone(user.getUserphone());
		}
		order.setNote(this.orderFlowDetail.getDetail(orderFlow));
		con.setOrderList(order);
		String json = objectMapper.writeValueAsString(con);
		this.logger.info("-永辉-订单状态回传,cwb={},回传json={}", cwbOrder.getCwb(), json);
		return json;
	}

	private YongHuiFlowEnum filterSate(long flowOrdertype, DmpCwbOrder cwbOrder) throws IOException, JsonParseException, JsonMappingException {

		List<B2CData> b2cDatalist = this.b2cDataDAO.getB2CDataList(cwbOrder.getCwb(), flowOrdertype);
		if ((b2cDatalist != null) && (b2cDatalist.size() > 0)) {
			for (B2CData b2cData : b2cDatalist) {
				Content content = JacksonMapper.getInstance().readValue(b2cData.getJsoncontent(), Content.class);
				if (content.getOrderList().getFlag() == YongHuiFlowEnum.ZhiLiu.getState()) {
					return YongHuiFlowEnum.ZhiLiuTwo;
				}
			}
		}
		return YongHuiFlowEnum.ZhiLiu;
	}

}
