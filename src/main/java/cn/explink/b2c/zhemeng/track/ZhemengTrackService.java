package cn.explink.b2c.zhemeng.track;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import cn.explink.b2c.tools.JointEntity;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.B2CData;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.RestHttpServiceHanlder;

/**
 * 哲盟_轨迹 接口service
 * @author yurong.liang 2016-05-30
 */
@Service
public class ZhemengTrackService {
	private Logger logger = LoggerFactory.getLogger(ZhemengTrackService.class);
	@Autowired
	private GetDmpDAO getdmpDAO;
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	B2CDataDAO b2cDataDAO;
	@Autowired
	FlowFromJMSB2cService flowFromJMSB2cService;

	/**
	 * 反馈[哲盟-轨迹]订单信息
	 */
	public void feedback_status() {

		ZhemengTrack dms = getZheMeng(B2cEnum.ZheMeng_track.getKey());
		if (!b2ctools.isB2cOpen(B2cEnum.ZheMeng_track.getKey())) {
			logger.info("未开[哲盟-轨迹]的对接!");
			return;
		}
		sendCwbStatus_To_dms(dms, FlowOrderTypeEnum.RuKu.getValue());
		sendCwbStatus_To_dms(dms, FlowOrderTypeEnum.ChuKuSaoMiao.getValue());
		sendCwbStatus_To_dms(dms,FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue());
		sendCwbStatus_To_dms(dms, FlowOrderTypeEnum.FenZhanLingHuo.getValue());
		sendCwbStatus_To_dms(dms, FlowOrderTypeEnum.YiShenHe.getValue());
	}

	/**
	 * 状态反馈接口开始
	 */
	private void sendCwbStatus_To_dms(ZhemengTrack tq, long flowordertype) {
		try {
			int i = 0;
			while (true) {
				List<B2CData> datalist = b2cDataDAO.getDataListByFlowStatus(flowordertype, String.valueOf(tq.getCustomerid()), 100);
				i++;
				if (i > 100) {
					logger.warn("【哲盟_轨迹】状态反馈已经超过100次循环，可能存在程序未知异常,请及时查询并处理");
					return;
				}

				if (datalist == null || datalist.size() == 0) {
					logger.info("当前没有要推送【哲盟_轨迹】的数据");
					return;
				}
				dealWithBuildXMLAndSending(tq, datalist);
			}
			
		} catch (Exception e) {
			logger.error("推送【哲盟_轨迹】接口发生未知异常", e);
		}
	}

	private void dealWithBuildXMLAndSending(ZhemengTrack dms,List<B2CData> datalist) throws Exception {
		for (B2CData data : datalist) {
			try {
				String jsoncontent = data.getJsoncontent();
				ZhemengTrackXMLNote note = this.getNote(jsoncontent);
				String xml = "<request>" 
							+ "<tms_service_code>"+ note.getTms_service_code() + "</tms_service_code>"
							+ "<operator>" + note.getOperator() + "</operator>"
							+ "<operator_date>" + note.getOperator_date()+ "</operator_date>"
							+ "<order_code>"+ note.getOrder_code() + "</order_code>"
							+ "<out_biz_code>" + data.getB2cid()+ "</out_biz_code>" 
							+ "<status>" + note.getStatus()+ "</status>"
							+ "<scanstano>" + note.getScanstano()+ "</scanstano>"
							+ "<ctrname>" + note.getCtrname()+ "</ctrname>" 
							+ "<content>" + note.getContent()+ "</content>"
							+ "<remark>"+note.getRemark()+"</remark>" 
							+ "</request>";

				//String sign = MD5Util.md5(xml + dms.getPrivate_key());
				Map<String, String> params = new HashMap<String, String>();
				params.put("service", "tms_order_info_sync");
				params.put("content", xml);
				//params.put("sign", sign);

				String jsonresponse = RestHttpServiceHanlder.sendHttptoServer(params, dms.getSend_url());
				String is_success= jsonresponse.substring(jsonresponse.indexOf("<is_success>")+12, jsonresponse.indexOf("</is_success>"));

				int send_b2c_flag = is_success.equals("T") ? 1 : 2;
				String is_remark = send_b2c_flag == 2 ? jsonresponse.substring(jsonresponse.indexOf("<error>")+7, jsonresponse.indexOf("</error>")) : "";

				logger.info("当前【哲盟_轨迹】接口返回jsonresponse={}", jsonresponse);
				b2cDataDAO.updateB2cIdSQLResponseStatus(data.getB2cid(),send_b2c_flag, is_remark);
			} catch (Exception e) {
				logger.error("推送【哲盟_轨迹】发生未知异常", e);
				b2cDataDAO.updateB2cIdSQLResponseStatus(data.getB2cid(),2,"推送轨迹发生未知异常");
			}
		}
	}

	public String getFlowEnum(long flowordertype, long deliverystate) {
		if (flowordertype != FlowOrderTypeEnum.YiShenHe.getValue()) {
			for (ZhemengTrackFlowEnum TEnum : ZhemengTrackFlowEnum.values()) {
				if (flowordertype == TEnum.getFlowordertype()) {
					return TEnum.getTmall_state();
				}
			}
		}
		if (flowordertype == FlowOrderTypeEnum.YiShenHe.getValue()
				&& (deliverystate == DeliveryStateEnum.PeiSongChengGong.getValue()|| deliverystate == DeliveryStateEnum.ShangMenTuiChengGong.getValue() 
				|| deliverystate == DeliveryStateEnum.ShangMenHuanChengGong.getValue())) {
			return ZhemengTrackFlowEnum.OK.getTmall_state();
		}

		if (flowordertype == FlowOrderTypeEnum.YiShenHe.getValue()&& (deliverystate == DeliveryStateEnum.QuanBuTuiHuo.getValue() 
				|| deliverystate == DeliveryStateEnum.BuFenTuiHuo.getValue())) {
			return ZhemengTrackFlowEnum.JS.getTmall_state();
		}

		if (flowordertype == FlowOrderTypeEnum.YiShenHe.getValue()&& 
				(deliverystate == DeliveryStateEnum.HuoWuDiuShi.getValue() 
				|| deliverystate == DeliveryStateEnum.FenZhanZhiLiu.getValue())) {
			return ZhemengTrackFlowEnum.YC.getTmall_state();
		}
		
		return null;
	}

	public ZhemengTrackXMLNote getNote(String jsoncontent) {
		try {
			return JacksonMapper.getInstance().readValue(jsoncontent,
					ZhemengTrackXMLNote.class);
		} catch (Exception e) {
			logger.error("获取ZhemengXMLNote异常！jsoncontent:" + jsoncontent + e);
			return null;
		}
	}

	public ZhemengTrack getZheMeng(int key) {
		ZhemengTrack tk = null;
		if (this.getObjectMethod(key) != null) {
			JSONObject jsonObj = JSONObject.fromObject(getObjectMethod(key));
			tk = (ZhemengTrack) JSONObject.toBean(jsonObj, ZhemengTrack.class);
		} else {
			tk = new ZhemengTrack();
		}
		return tk;
	}

	/**
	 * 获取【哲盟_轨迹】配置信息的接口
	 */
	private String getObjectMethod(int key) {
		try {
			JointEntity obj = getdmpDAO.getJointEntity(key);
			return obj.getJoint_property();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
}
