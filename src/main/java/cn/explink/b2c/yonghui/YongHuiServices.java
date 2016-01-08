package cn.explink.b2c.yonghui;

import java.util.List;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.FlowFromJMSB2cService;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.b2c.yonghui.domain.OrderBack;
import cn.explink.b2c.yonghui.domain.YongHui;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.B2CData;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.MySSLProtocolSocketFactory;

@Service
public class YongHuiServices {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	B2cTools b2ctools;
	@Autowired
	B2CDataDAO b2cDataDAO;
	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;
	@Autowired
	FlowFromJMSB2cService flowFromJMSB2cService;

	public YongHuiFlowEnum getFlowEnum(long flowordertype, long delivery_state) {
		if (flowordertype != FlowOrderTypeEnum.YiShenHe.getValue()) {
			for (YongHuiFlowEnum TEnum : YongHuiFlowEnum.values()) {
				if (flowordertype == TEnum.getFlowordertype()) {
					return TEnum;
				}
			}
		}
		if ((delivery_state == DeliveryStateEnum.PeiSongChengGong.getValue()) || (delivery_state == DeliveryStateEnum.ShangMenHuanChengGong.getValue())
				|| (delivery_state == DeliveryStateEnum.ShangMenTuiChengGong.getValue())) {
			return YongHuiFlowEnum.PeiSongChengGong;
		}
		if ((delivery_state == DeliveryStateEnum.QuanBuTuiHuo.getValue()) || (delivery_state == DeliveryStateEnum.BuFenTuiHuo.getValue())) {
			return YongHuiFlowEnum.JuShou;
		}

		if (delivery_state == DeliveryStateEnum.FenZhanZhiLiu.getValue()) {
			return YongHuiFlowEnum.ZhiLiu;
		}

		return null;

	}

	public YongHui getYongHui(int key) {
		YongHui yh = null;
		String objectMethod = this.b2ctools.getObjectMethod(key).getJoint_property();
		if (objectMethod != null) {
			JSONObject jsonObj = JSONObject.fromObject(objectMethod);
			yh = (YongHui) JSONObject.toBean(jsonObj, YongHui.class);
		} else {
			yh = new YongHui();
		}
		return yh;
	}

	/**
	 * 反馈[永辉超市]订单信息
	 */
	public void feedback_status() {
		if (!this.b2ctools.isB2cOpen(B2cEnum.YongHui.getKey())) {
			this.logger.info("未开-永辉-超市的对接!");
			return;
		}
		YongHui yh = this.getYongHui(B2cEnum.YongHui.getKey());
		this.sendCwbStatus_To_YongHui(yh);
	}

	/**
	 * 状态反馈接口开始
	 * 
	 * @param smile
	 */
	private void sendCwbStatus_To_YongHui(YongHui yh) {
		try {
			int i = 0;
			while (true) {
				List<B2CData> datalist = this.b2cDataDAO.getDataListByFlowStatus(yh.getCustomerid() + "", yh.getOrderStateCount());
				i++;
				if (i > 100) {
					this.logger.warn("查询0永辉超市0状态反馈已经超过100次循环，可能存在程序未知异常,请及时查询并处理!");
					return;
				}
				if ((datalist == null) || (datalist.size() == 0)) {
					this.logger.info("当前没有要推送0永辉超市0的数据");
					return;
				}
				this.DealWithBuildXMLAndSending(yh, datalist);
			}

		} catch (Exception e) {
			this.logger.error("发送0永辉超市0状态反馈遇到不可预知的异常", e);
		}
	}

	private void DealWithBuildXMLAndSending(YongHui yh, List<B2CData> datalist) throws Exception {

		for (B2CData b2cdata : datalist) {
			String jsoncontent = b2cdata.getJsoncontent();
			logger.info("永辉推送信息cwb"+b2cdata.getCwb()+",flowordertype="+b2cdata.getFlowordertype()+",json:"+jsoncontent);
			String responseInfo = MySSLProtocolSocketFactory.callRestful(jsoncontent, yh.getOrderState_url(), null);

			// String responseInfo = HttpClienCommon.post(paramsMap, null,
			// yh.getOrderState_url(), 5000, 5000, "utf-8");

			this.logger.info("状态反馈-永辉-[返回信息]-json={}", responseInfo);

			OrderBack returnDto = JacksonMapper.getInstance().readValue(responseInfo, OrderBack.class);
			String remark = "00".equals(returnDto.getStatus()) ? "成功" : (returnDto.getStatus() + returnDto.getMessage());
			int sendflag = "00".equals(returnDto.getStatus()) ? 1 : 2;
			this.b2cDataDAO.updateFlagAndRemarkByCwb(b2cdata.getB2cid(), sendflag, remark);

		}
	}
}
