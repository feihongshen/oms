package cn.explink.b2c.zhemeng;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.hxgdms.Hxgdms;
import cn.explink.b2c.hxgdms.HxgdmsJsonError;
import cn.explink.b2c.hxgdms.HxgdmsJsonTrack;
import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.ExptReason;
import cn.explink.b2c.tools.FlowFromJMSB2cService;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.b2c.tools.JointEntity;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.B2CData;
import cn.explink.domain.User;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;
import cn.explink.util.RestHttpServiceHanlder;
import cn.explink.util.MD5.MD5Util;

@Service
public class ZhemengService {
	private Logger logger = LoggerFactory.getLogger(ZhemengService.class);
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

	public String getFlowEnum(long flowordertype, long deliverystate) {
		if (flowordertype != FlowOrderTypeEnum.YiShenHe.getValue()) {
			for (ZhemengFlowEnum TEnum : ZhemengFlowEnum.values()) {
				if (flowordertype == TEnum.getFlowordertype()) {
					return TEnum.getTmall_state();
				}
			}
		}
		if (flowordertype == FlowOrderTypeEnum.YiShenHe.getValue()
				&& (deliverystate == DeliveryStateEnum.PeiSongChengGong.getValue() || deliverystate == DeliveryStateEnum.ShangMenTuiChengGong.getValue() || deliverystate == DeliveryStateEnum.ShangMenHuanChengGong
						.getValue())) {
			return ZhemengFlowEnum.OK.getTmall_state();
		}

		if (flowordertype == FlowOrderTypeEnum.YiShenHe.getValue()
				&& (deliverystate == DeliveryStateEnum.QuanBuTuiHuo.getValue() || deliverystate == DeliveryStateEnum.BuFenTuiHuo.getValue())

		) {
			return ZhemengFlowEnum.JS.getTmall_state();
		}
		
		if (flowordertype == FlowOrderTypeEnum.YiShenHe.getValue()
				&& (deliverystate == DeliveryStateEnum.HuoWuDiuShi.getValue()||deliverystate == DeliveryStateEnum.FenZhanZhiLiu.getValue() )

		) {
			return ZhemengFlowEnum.YC.getTmall_state();
		}

		return null;

	}

	public Zhemeng getZheMeng(int key) {
		Zhemeng  tk = null;
		if (this.getObjectMethod(key) != null) {
			JSONObject jsonObj = JSONObject.fromObject(getObjectMethod(key));
			 tk = (Zhemeng) JSONObject.toBean(jsonObj, Zhemeng.class);
		} else {
			 tk = new Zhemeng();
		}
		return  tk;
	}

	/**
	 * 获取哲盟-安达信配置信息的接口
	 * 
	 * @param key
	 * @return
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

	/**
	 * 反馈[哲盟-安达信]订单信息
	 */
	public void feedback_status() {

		Zhemeng dms = getZheMeng(B2cEnum.ZheMeng.getKey());
		if (!b2ctools.isB2cOpen(B2cEnum.ZheMeng.getKey())) {
			logger.info("未开[哲盟-安达信]的对接!");
			return;
		}

		sendCwbStatus_To_dms(dms,FlowOrderTypeEnum.RuKu.getValue());
		sendCwbStatus_To_dms(dms,FlowOrderTypeEnum.ChuKuSaoMiao.getValue());
		sendCwbStatus_To_dms(dms,FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue());
		sendCwbStatus_To_dms(dms,FlowOrderTypeEnum.FenZhanLingHuo.getValue());
		sendCwbStatus_To_dms(dms,FlowOrderTypeEnum.YiShenHe.getValue());

	}

	/**
	 * 状态反馈接口开始
	 * 
	 * @param tq
	 */
	private void sendCwbStatus_To_dms(Zhemeng tq,long flowordertype) {
		try {

			int i = 0;
			while (true) {
				List<B2CData> datalist = b2cDataDAO.getDataListByFlowStatus(flowordertype, String.valueOf(tq.getCustomerid()),100);
				i++;
				if (i > 100) {
					logger.warn("哲盟-安达信状态反馈已经超过100次循环，可能存在程序未知异常,请及时查询并处理");
					return;
				}

				if (datalist == null || datalist.size() == 0) {
					logger.info("当前没有要推送[哲盟-安达信]的数据");
					return;
				}
				dealWithBuildXMLAndSending(tq, datalist);

			}

		} catch (Exception e) {
			logger.error("推送哲盟-安达信系统发生未知异常", e);
		}

	}

	private void dealWithBuildXMLAndSending(Zhemeng dms, List<B2CData> datalist) throws Exception {
		String b2cids = "";
		try {
			
			for (B2CData data : datalist) {
				String jsoncontent = data.getJsoncontent();
				ZhemengXMLNote note = this.getNote(jsoncontent);
				
				String xml="<request>"
							+ "<tms_service_code>"+dms.getTms_service_code()+"</tms_service_code>"
							+ "<operator>"+note.getOperator()+"</operator>"
							+ "<operator_date>"+note.getOperator_date()+"</operator_date>"
							+ "<order_code>"+note.getOrder_code()+"</order_code>"
							+ "<out_biz_code>"+data.getB2cid()+"</out_biz_code>"
							+ "<status>"+note.getStatus()+"</status>"
							+ "<scanstano>"+note.getScanstano()+"</scanstano>"
							+ "<ctrname>"+note.getCtrname()+"</ctrname>"
							+ "<content>"+note.getContent()+"</content>"
							+ "<remark></remark>"
						+ "</request>";
				
				String sign = MD5Util.md5(xml + dms.getPrivate_key());
				
				Map<String, String> params = new HashMap<String, String>();
				params.put("service", "tms_order_info_sync");
				params.put("content",xml);
				params.put("sign", sign);

				String jsonresponse = RestHttpServiceHanlder.sendHttptoServer(params, dms.getSend_url());
				
				Map<String, Object> respMap = this.readXML(jsonresponse);
				String is_success = respMap.get("is_success").toString();

				int  send_b2c_flag = is_success.equals("T") ? 1 : 2;
				String is_remark = send_b2c_flag == 2 ? respMap.get("error").toString() : "";

				logger.info("当前哲盟-安达信返回jsonresponse={}",jsonresponse);
				
				b2cDataDAO.updateB2cIdSQLResponseStatus(data.getB2cid(), send_b2c_flag, is_remark);
			}
			
			
			
			
			
			

		

		} catch (Exception e) {
			logger.error("推送哲盟-安达信返回未知异常",e);
			b2cDataDAO.updateMultiB2cIdSQLResponseStatus_AllFaild(b2cids);
		}
		


		
	}

	public ZhemengXMLNote getNote(String jsoncontent) {
		try {
			return JacksonMapper.getInstance().readValue(jsoncontent, ZhemengXMLNote.class);
		} catch (Exception e) {
			logger.error("获取ZhemengXMLNote异常！jsoncontent:" + jsoncontent + e);
			return null;
		}
	}

	/**
	 * 跟踪日志反馈
	 * 
	 * @param orderFlow
	 * @param cwbOrder
	 */
	public void sendTrackInfoByHxgdms(DmpOrderFlow orderFlow, DmpCwbOrder cwbOrder, String dmsReceiveStatus, Hxgdms dms) {

		try {
			if (!b2ctools.isB2cOpen(B2cEnum.Hxgdms.getKey())) {
				logger.info("未开[哲盟-安达信]的对接!");
				return;
			}

			if (orderFlow.getFlowordertype() == FlowOrderTypeEnum.FenZhanLingHuo.getValue() || orderFlow.getFlowordertype() == FlowOrderTypeEnum.YiShenHe.getValue()) {
				logger.info("好享购订单流转信息取消领货，反馈，异常环节推送,cwb={}", orderFlow.getCwb());
				return;
			}

			User operatorUser = getdmpDAO.getUserById(orderFlow.getUserid());
			// //执行跟踪日志反馈的方法

			HxgdmsJsonTrack hxgdmsJsonTrack = new HxgdmsJsonTrack();
			hxgdmsJsonTrack.setWorkCode(cwbOrder.getCwb());
			hxgdmsJsonTrack.setFlowType(dmsReceiveStatus);
			hxgdmsJsonTrack.setOperationDesc(orderFlowDetail.getDetail(orderFlow));
			hxgdmsJsonTrack.setOperationName(operatorUser.getRealname());
			hxgdmsJsonTrack.setOperationTime(DateTimeUtil.formatDate(orderFlow.getCredate()));
			hxgdmsJsonTrack.setDelveryCode(dms.getDcode());

			String jsoncontent = null;
			try {
				jsoncontent = JacksonMapper.getInstance().writeValueAsString(hxgdmsJsonTrack);
			} catch (Exception e) {
				logger.error("jackson转化异常,cwb=" + cwbOrder.getCwb(), e);
			}
			String signed = MD5Util.md5(jsoncontent + dms.getSecretKey());

			Map<String, String> params = new HashMap<String, String>();
			params.put("Request", jsoncontent);
			params.put("Signed", signed);
			params.put("Action", "RequestSendTrackToDMS");
			params.put("Dcode", dms.getDcode());

			String jsonresponse = RestHttpServiceHanlder.sendHttptoServer(params, dms.getFeedbackUrl());
			logger.info("哲盟-安达信跟踪信息-返回,cwb={},jsonresponse={}", orderFlow.getCwb(), jsonresponse);
		} catch (Exception e) {
			logger.error("推送哲盟-安达信跟踪日志异常cwb=" + orderFlow.getCwb(), e);
		}

	}

	/**
	 * 处理推送失败问题
	 * 
	 * @param cwbOrder
	 * @param delivery_state
	 * @param dms
	 */
	public void sendErrorByHxgdms(DmpCwbOrder cwbOrder, long delivery_state, Hxgdms dms) {
		try {
			HxgdmsJsonError hxgdmsJsonError = new HxgdmsJsonError();
			hxgdmsJsonError.setWorkCode(cwbOrder.getCwb());
			hxgdmsJsonError.setDelveryCode(dms.getDcode());
			String errorCode = "";
			String errorMsg = "";

			if (delivery_state == DeliveryStateEnum.HuoWuDiuShi.getValue()) {
				ExptReason exptReasonLose = b2ctools.getDiushiReasonByB2c(cwbOrder.getLosereasonid(), cwbOrder.getCustomerid() + "");
				errorCode = exptReasonLose.getExpt_code();
				errorMsg = exptReasonLose.getExpt_msg();
				if (errorCode == null || errorCode.isEmpty()) {
					errorCode = "1";
					errorMsg = "派送失败_丢失";
				}

			} else if (delivery_state == DeliveryStateEnum.QuanBuTuiHuo.getValue()) {
				ExptReason exptReason = b2ctools.getExptReasonByB2c(0, cwbOrder.getBackreasonid(), cwbOrder.getCustomerid() + "", delivery_state);
				errorCode = exptReason.getExpt_code();
				errorMsg = exptReason.getExpt_msg();
				if (errorCode == null || errorCode.isEmpty()) {
					errorCode = "12";
					errorMsg = "拒收报备_其它";
				}
			} else if (delivery_state == DeliveryStateEnum.FenZhanZhiLiu.getValue()) {
				ExptReason exptReason = b2ctools.getExptReasonByB2c(cwbOrder.getLeavedreasonid(), 0, cwbOrder.getCustomerid() + "", delivery_state);
				errorCode = exptReason.getExpt_code();
				errorMsg = exptReason.getExpt_msg();
				if (errorCode == null || errorCode.isEmpty()) {
					errorCode = "5";
					errorMsg = "派送未果_其它";
				}
			}
			hxgdmsJsonError.setErrorCode(errorCode);
			hxgdmsJsonError.setErrorNote(errorMsg);

			String jsoncontent = null;
			try {
				jsoncontent = JacksonMapper.getInstance().writeValueAsString(hxgdmsJsonError);
			} catch (Exception e) {
				logger.error("jackson转化异常,cwb=" + cwbOrder.getCwb(), e);
			}
			String signed = MD5Util.md5(jsoncontent + dms.getSecretKey());

			Map<String, String> params = new HashMap<String, String>();
			params.put("Request", jsoncontent);
			params.put("Signed", signed);
			params.put("Action", "RequestSendErrorToDMS");
			params.put("Dcode", dms.getDcode());
			String jsonresponse = RestHttpServiceHanlder.sendHttptoServer(params, dms.getFeedbackUrl());
			logger.info("哲盟-安达信异常信息-返回,jsonresponse={}", jsonresponse);
		} catch (Exception e) {
			logger.error("推送哲盟-安达信异常反馈异常cwb=" + cwbOrder.getCwb(), e);
		}

	}
	
	public static Map<String, Object> readXML(String fileName) throws Exception {
		// File inputXml = new File(fileName);
		InputStream iStream = new ByteArrayInputStream(fileName.getBytes());
		SAXReader saxReader = new SAXReader();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Reader r = new InputStreamReader(iStream, "UTF-8");
		Document document = saxReader.read(r);
		Element employees = document.getRootElement();
		for (Iterator i = employees.elementIterator(); i.hasNext();) {
			Element employee = (Element) i.next();
			returnMap.put(employee.getName(), employee.getText());
			List<Map<String, Object>> jarry = new ArrayList<Map<String, Object>>();
			for (Iterator j = employee.elementIterator(); j.hasNext();) {
				Element node = (Element) j.next();
				returnMap.put(node.getName(), node.getText());

			}
		}
		return returnMap;
	}

}
