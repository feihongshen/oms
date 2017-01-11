package cn.explink.b2c.mss;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.domain.B2CData;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.RestHttpServiceHanlder;
import net.sf.json.JSONObject;

@Service
public class MssService {
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	private B2CDataDAO b2CDataDAO;
	private Logger logger = LoggerFactory.getLogger(MssService.class);

	// 获取页面配置信息
	public Mss getMss(int key) {
		Mss mss = null;
		String jspcontent = b2ctools.getObjectMethod(key).getJoint_property();
		if (jspcontent != null) {
			JSONObject object = JSONObject.fromObject(jspcontent);
			mss = (Mss) JSONObject.toBean(object, Mss.class);
		} else {
			mss = new Mss();
		}
		return mss;
	}

	public void feedback_status() {
		submitReturn(FlowOrderTypeEnum.RuKu.getValue());
		submitReturn(FlowOrderTypeEnum.ChuKuSaoMiao.getValue());
		submitReturn(FlowOrderTypeEnum.FenZhanLingHuo.getValue());
		submitReturn(FlowOrderTypeEnum.YiShenHe.getValue());
		submitReturn(FlowOrderTypeEnum.TuiHuoChuZhan.getValue());
		submitReturn(FlowOrderTypeEnum.TuiHuoZhanRuKu.getValue());

	}

	public void submitReturn(long flowordertype) {
		Mss mss = getMss(B2cEnum.MSS.getKey());
		if (!b2ctools.isB2cOpen(B2cEnum.MSS.getKey())) {
			logger.info("未开启0美食送(http)0状态反馈接口");
			return;
		}
		try {

			int i = 0;
			while (true) {
				// 遍历数据库中每条
				List<B2CData> feiniulist = b2CDataDAO.getDataListByFlowStatus(flowordertype, mss.getCustomerid(), mss.getMaxCount());
				i++;
				if (i > 100) {
					String warning = "查询0美食送(http)0状态反馈已经超过100次循环，每次100条，可能存在程序未知异常,请及时查询并处理!";
					logger.warn(warning);
					return;
				}
				if (feiniulist == null || feiniulist.size() == 0) {
					logger.info("当前没有要推送0美食送(http)0的数据");
					return;
				}

				// 遍历每一条表单整体信息

				String b2cids = "";
				long b2cid = 0;
				for (B2CData b2cdata : feiniulist) {
					b2cid = b2cdata.getB2cid();
					b2cids += b2cdata.getB2cid() + ",";
					ReturnData returnData = getXMLNoteMethod(b2cdata.getJsoncontent());
					String params = buildSendJsondata(returnData, mss);
					logger.info("请求美食送(http)参数为:{}", params);
					String responseJson = RestHttpServiceHanlder.sendHttptoServer(params, mss.getFeedbackUrl());
					logger.info("美食送(http)返回的数据为:{}", responseJson);
					ObjectMapper mapper = new ObjectMapper();
					Map<String, Object> productMap = mapper.readValue(responseJson, Map.class);// 转成map
					Map<String, String> body = (Map<String, String>) productMap.get("body");
					int result = 0;
					String reason = "";
					if (!body.get("status").equals("0")&&!body.get("message").equals("success")) {
						b2CDataDAO.updateMultiB2cIdSQLResponseStatus_AllFaild(b2cids.length() > 0 ? b2cids.substring(0, b2cids.length() - 1) : "-1");
					} else {
						result = body.get("status").equals("0") ? 1 : 2;
						reason = body.get("message");
						b2CDataDAO.updateB2cIdSQLResponseStatus(b2cid, result, reason);
					}

				}
			}

		} catch (Exception e) {
			logger.error("调用0美食送(http)0webservice服务器异常" + e.getMessage(), e);
		}
	}

	private String buildSendJsondata(ReturnData returnData, Mss mss) {
		long time = System.currentTimeMillis();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("access_key", mss.getAccess_key());
		params.put("cmd", mss.getCmd());
		params.put("version", mss.getVersion());
		params.put("ticket", mss.getTicket());
		params.put("body", returnData);
		params.put("time", time / 1000);
		params.put("sign", SignUtil.getSign(SignUtil.sortMapByKey(params), mss.getSecret_key()));
		Map<String, Object> response = SignUtil.sortMapByKey(params);
		return SignUtil.toJson(response);
	}

	// 得到表单字段对应的javabean
	public ReturnData getXMLNoteMethod(String jsoncontent) throws JsonParseException, JsonMappingException, IOException {
		return JacksonMapper.getInstance().readValue(jsoncontent, ReturnData.class);
	}
}
