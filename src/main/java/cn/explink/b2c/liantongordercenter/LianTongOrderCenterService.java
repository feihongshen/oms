
package cn.explink.b2c.liantongordercenter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cn.explink.b2c.liantong.LiantongDAO;
import cn.explink.b2c.liantong.LiantongEntity;
import cn.explink.b2c.liantongordercenter.xmlDto.LianTongResponseDto;
import cn.explink.b2c.liantongordercenter.xmlDto.RequestDto;
import cn.explink.b2c.liantongordercenter.xmlDto.ResponseBodyDto;
import cn.explink.b2c.liantongordercenter.xmlDto.ResponseDto;
import cn.explink.b2c.liantongordercenter.xmlDto.RouteDto;
import cn.explink.b2c.liantongordercenter.xmlDto.RouteResponse;
import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.dao.CustomerDAO;
import cn.explink.domain.B2CData;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.JAXBUtil;
import cn.explink.util.MD5.SignUtils;
import net.sf.json.JSONObject;

@Service
public class LianTongOrderCenterService {

	private Logger logger = LoggerFactory.getLogger(LianTongOrderCenterService.class);
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	CustomerDAO customerDAO;
	@Autowired
	LiantongDAO ltDao;
	@Autowired
	B2CDataDAO b2cDataDAO;

	public String getFlowEnum(long flowordertype, long deliverystate) {
//		if(flowordertype==FlowOrderTypeEnum.DaoRuShuJu.getValue()){
//			return LianTongSCEnum.FaHuo.getValue();
//		}
		if(flowordertype==FlowOrderTypeEnum.RuKu.getValue()){
			return LianTongOrderCenterEnum.LanJian.getValue();
		}
		if(flowordertype==FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue()){
			return LianTongOrderCenterEnum.DaoHuo.getValue();
		}
		if(flowordertype==FlowOrderTypeEnum.FenZhanLingHuo.getValue()){
			return LianTongOrderCenterEnum.PaiSong.getValue();
		}
		if (flowordertype == FlowOrderTypeEnum.YiFanKui.getValue()) {
			if ((deliverystate == DeliveryStateEnum.PeiSongChengGong.getValue()) || (deliverystate == DeliveryStateEnum.ShangMenHuanChengGong.getValue())
						|| (deliverystate == DeliveryStateEnum.ShangMenTuiChengGong.getValue())) {
					return LianTongOrderCenterEnum.QianShou.getValue();
				} else if ((deliverystate == DeliveryStateEnum.QuanBuTuiHuo.getValue()) || (deliverystate == DeliveryStateEnum.BuFenTuiHuo.getValue())) {
					return LianTongOrderCenterEnum.JuShou.getValue();
				} else {
					return null;
				}
			}
		return null;

	}

	/**
	 * 联通商城轨迹回传
	 */
	public void feedback_status() {

		if (!this.b2ctools.isB2cOpen(B2cEnum.LianTongOrderCenter.getKey())) {
			this.logger.info("未开0联通0的状态反馈接口!");
			return;
		}
		LianTongOrderCenter lt = this.getLiantong(B2cEnum.LianTongOrderCenter.getKey()); // 获取配置信息
		this.sendCwbStatus_To_Liantong(lt);

	}

	/**
	 * 状态反馈接口开始
	 *
	 * @param
	 */
	private void sendCwbStatus_To_Liantong(LianTongOrderCenter lt) {

		try {

			int i = 0;
			while (true) {
				List<B2CData> datalist = this.b2cDataDAO.getDataListByFlowStatus(lt.getCustomerid(), lt.getMaxCount());
				i++;
				if (i > 50) {
					String warning = "查询0联通商城0状态反馈已经超过50次循环，可能存在程序未知异常,请及时查询并处理!";
					this.logger.warn(warning);
					return;
				}

				if ((datalist == null) || (datalist.size() == 0)) {
					this.logger.info("当前没有要推送0联通商城0的数据");
					return;
				}

				for (B2CData data : datalist) {
					LianTongOrderCenterXMLNote xmlnote = JacksonMapper.getInstance().readValue(data.getJsoncontent(), LianTongOrderCenterXMLNote.class);
					String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
					LinkedHashMap map = new LinkedHashMap();
					map.put("APP_KEY", lt.getAppkey());
					map.put("TIMESTAMP", format);
					map.put("TRANS_ID", "0");
					String TOKEN = SignUtils.sign(map, lt.getAppsecret());
					String xmlStr = "<UNI_BSS><UNI_BSS_HEAD><APP_KEY>" + lt.getAppkey() + "</APP_KEY><TRANS_ID>0</TRANS_ID><TIMESTAMP>" + format + "</TIMESTAMP><TOKEN>" + TOKEN
							+ "</TOKEN></UNI_BSS_HEAD><UNI_BSS_BODY><WaybillRoutes><WaybillRoute id=\"" + xmlnote.getId() + "\" mailno=\"" + xmlnote.getMailNO() + "\" orderid=\""
							+ xmlnote.getOrderId() + "\" acceptTime=\"" + xmlnote.getAcceptTime() + "\" acceptAddress=\"" + xmlnote.getAcceptAddress() + "\" remark=\"" + xmlnote.getRemark()
							+ "\" opCode=\"" + xmlnote.getOpcode() + "\"/></WaybillRoutes></UNI_BSS_BODY></UNI_BSS>";
					String VERIFY_CODE = SignUtils.signVeryfy(xmlStr, lt.getAppsecret());
					String xml = "<UNI_BSS><UNI_BSS_HEAD><APP_KEY>" + lt.getAppkey() + "</APP_KEY><TRANS_ID>0</TRANS_ID><TIMESTAMP>" + format + "</TIMESTAMP><TOKEN>" + TOKEN + "</TOKEN><VERIFY_CODE>"
							+ VERIFY_CODE + "</VERIFY_CODE></UNI_BSS_HEAD><UNI_BSS_BODY><WaybillRoutes><WaybillRoute id=\"" + xmlnote.getId() + "\" mailno=\"" + xmlnote.getMailNO() + "\" orderid=\""
							+ xmlnote.getOrderId() + "\" acceptTime=\"" + xmlnote.getAcceptTime() + "\" acceptAddress=\"" + xmlnote.getAcceptAddress() + "\" remark=\"" + xmlnote.getRemark()
							+ "\" opCode=\"" + xmlnote.getOpcode() + "\"/></WaybillRoutes></UNI_BSS_BODY></UNI_BSS>";

					String responseData = HttpRequestor.doPost(lt.getFeedback_url(), xml); // 请求并返回

					this.logger.info("当前联通返回XML={},cwb={}", responseData, data.getCwb());
					ResponseDto responseDto = JAXBUtil.convertObject(ResponseDto.class, responseData);
					if (null == responseDto) {
						this.logger.info("当前联通返回responseDto={}", responseDto);
						return;
					}
					int send_b2c_flag = 0;
					String remark = "";
					if ("00000".equals(responseDto.getRequestBodyDtoUni().getStatus())) {
						send_b2c_flag = 1;
					} else {
						send_b2c_flag = 2;
						remark = responseDto.getRequestHeadDto().getRESPDESC();
					}
					this.b2cDataDAO.updateB2cIdSQLResponseStatus(data.getB2cid(), send_b2c_flag, remark);

				}

			}

		} catch (Exception e) {
			this.logger.error("发送0联通0状态反馈遇到不可预知的异常", e);
		}

	}

	// 获取配置信息
	public LianTongOrderCenter getLiantong(int key) {
		LianTongOrderCenter lt = null;
		String objectMethod = this.b2ctools.getObjectMethod(key).getJoint_property();
		if (objectMethod != null) {
			JSONObject jsonObj = JSONObject.fromObject(objectMethod);
			lt = (LianTongOrderCenter) JSONObject.toBean(jsonObj, LianTongOrderCenter.class);
		} else {
			lt = new LianTongOrderCenter();
		}
		return lt;
	}

	public String requestCwbSearchInterface(LianTongOrderCenter lt, String content, String verifyCode) {
		try {

			if (!StringUtils.hasText(content)) {
				String response = "<Response service=\"RouteService\"><Head>ERR</Head><Error>" + "参数为空" + "</Error></Response>";
				return response;

			}
			RequestDto requestDto = JAXBUtil.convertObject(RequestDto.class, content);
			int typeflag = 0; // 默认按订单号查询 1按物流单号查询
			if (2 == requestDto.getRequestBodyDto().getRouteRequestDto().getTrackingType()) { // 根据订单号查询
				typeflag = 1;
			}

			String sign = SignUtils.getMd5Hex16Base64(content.trim(), lt.getPrivate_key());
			this.logger.info("联通签名={},本地签名={}", verifyCode, sign);
			if (!verifyCode.equals(sign)) {
				String response = "<Response service=\"RouteService\"><Head>ERR</Head><Error >" + "签名错误" + "</Error></Response>";
				return response;
			}

			LianTongResponseDto buildTrackInfoEntity = this.buildTrackInfoEntity(lt, requestDto.getRequestBodyDto().getRouteRequestDto().getTrackingNumber(), typeflag);
			String response = JAXBUtil.convertXml(buildTrackInfoEntity);
			if (!StringUtils.hasText(response)) {
				response = "<Response service=\"RouteService\"><Head>ERR</Head><Error >" + "没有此订单" + "</Error></Response>";
				return response;
			}
			this.logger.info("返回联通的内容为  response={}", response);
			return response;

		} catch (Exception e) {
			String error = "处理[联通商城]查询请求发生未知异常:" + e.getMessage();
			this.logger.error(error, e);
			String response = "<Response service=\"RouteService\"><Head>ERR</Head><Error >" + "未知异常" + "</Error></Response>";
			return response;
		}
	}

	private LianTongResponseDto buildTrackInfoEntity(LianTongOrderCenter lt, String cwb, int typeflag) {
		LianTongResponseDto ltresp = new LianTongResponseDto();
		RouteResponse routeResponse = new RouteResponse();
		ResponseBodyDto responseBodyDto = new ResponseBodyDto();
		routeResponse.setMailNo(cwb);
		List<RouteDto> RouteDtoList = this.buildStepList(lt, cwb, typeflag);
		routeResponse.setRouteDtos(RouteDtoList);
		routeResponse.setMailNo(cwb);
		responseBodyDto.setRouteResponse(routeResponse);
		ltresp.setResponseBodyDto(responseBodyDto);
		ltresp.setService("RouteService");
		ltresp.setHead("OK");
		return ltresp;

	}

	private List<RouteDto> buildStepList(LianTongOrderCenter lt, String cwb, int typeflag) {
		List<RouteDto> RouteDtoList = new ArrayList<RouteDto>();

		List<LiantongEntity> tracklist = this.ltDao.getDataByCwb(cwb);

		for (LiantongEntity ltet : tracklist) {
			RouteDto routeDto = new RouteDto();
			routeDto.setAcceptTime(ltet.getAcceptTime());
			routeDto.setAcceptAddress(ltet.getAcceptAddress());
			routeDto.setOpcode(String.valueOf(ltet.getFlowordertype()));
			routeDto.setRemark(ltet.getAcceptAction());
			RouteDtoList.add(routeDto);
		}
		return RouteDtoList;
	}

}
