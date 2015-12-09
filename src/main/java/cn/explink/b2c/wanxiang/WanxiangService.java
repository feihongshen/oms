package cn.explink.b2c.wanxiang;

import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.JacksonMapper;
import cn.explink.b2c.wanxiang.bean.WxResponse;
import cn.explink.b2c.wanxiang.bean.WxUnmarchal;
import cn.explink.dao.CustomerDAO;
import cn.explink.domain.B2CData;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.MD5.MD5Util;

@Service
public class WanxiangService {

	private Logger logger = LoggerFactory.getLogger(WanxiangService.class);

	@Autowired
	private B2cTools b2ctools;
	@Autowired
	CustomerDAO customerDAO;
	@Autowired
	B2CDataDAO b2cDataDAO;

	protected static ObjectMapper jacksonmapper = JacksonMapper.getInstance();

	// 获取配置信息
	public Wanxiang getWanxiang(int key) {
		Wanxiang lt = null;
		String objectMethod = this.b2ctools.getObjectMethod(key).getJoint_property();
		if (objectMethod != null) {
			JSONObject jsonObj = JSONObject.fromObject(objectMethod);
			lt = (Wanxiang) JSONObject.toBean(jsonObj, Wanxiang.class);
		} else {
			lt = new Wanxiang();
		}
		return lt;
	}

	public String postHTTPJsonDataToWanXiang(String jsoncontent, String post_url) throws Exception {

		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(post_url);
		postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");

		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(30000);
		// 填入各个表单域的值
		NameValuePair[] data = { new NameValuePair("JSONDATA", jsoncontent), };

		// 将表单的值放入postMethod中
		postMethod.setRequestBody(data);

		httpClient.executeMethod(postMethod); // post数据
		String str = postMethod.getResponseBodyAsString();
		postMethod.releaseConnection();

		return str;
	}

	public String postHTTPJsonDataToWanXiang_new(String user_name, String pass_word, String jsoncontent, String md5Data, String post_url) throws Exception {

		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(post_url);
		postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");

		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(20000);
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(20000);
		// 填入各个表单域的值
		NameValuePair[] data = { new NameValuePair("USER_NAME", user_name), new NameValuePair("PASSWORD", pass_word), new NameValuePair("para_xml", jsoncontent),
				new NameValuePair("md5Data", md5Data), };

		// 将表单的值放入postMethod中
		postMethod.setRequestBody(data);

		httpClient.executeMethod(postMethod); // post数据
		String str = postMethod.getResponseBodyAsString();
		postMethod.releaseConnection();

		return str;
	}

	public String getFlowEnum(long flowordertype, long deliverystate, String cwbordertypeid) {

		for (WanxiangFlowEnum em : WanxiangFlowEnum.values()) {
			if (em.getIsResultFlag() == 0) {
				if (flowordertype == em.getOnwer_code()) {
					return em.getWx_code();
				}
			}
		}

		// 审核
		if ((flowordertype == FlowOrderTypeEnum.YiShenHe.getValue()) && (deliverystate == DeliveryStateEnum.FenZhanZhiLiu.getValue())) {
			return WanxiangFlowEnum.FenZhanZhiLiu.getWx_code();
		}

		if ((flowordertype == FlowOrderTypeEnum.YiShenHe.getValue())
				&& ((deliverystate == DeliveryStateEnum.PeiSongChengGong.getValue()) || (deliverystate == DeliveryStateEnum.ShangMenTuiChengGong.getValue()) || (deliverystate == DeliveryStateEnum.ShangMenHuanChengGong
						.getValue()))) {
			return WanxiangFlowEnum.PeiSongChengGong.getWx_code();
		}
		if ((flowordertype == FlowOrderTypeEnum.YiShenHe.getValue())
				&& ((deliverystate == DeliveryStateEnum.BuFenTuiHuo.getValue()) || (deliverystate == DeliveryStateEnum.QuanBuTuiHuo.getValue()) || (deliverystate == DeliveryStateEnum.ShangMenJuTui
						.getValue()))) {
			return WanxiangFlowEnum.JuShou.getWx_code();
		}

		return null;

	}

	public String getFlowEnumNew(long flowordertype, long deliverystate, String cwbordertypeid, int version) {

		for (WanxiangNewFlowEnum em : WanxiangNewFlowEnum.values()) {
			if (em.getIsResultFlag() == 0) {
				if (flowordertype == em.getOnwer_code()) {
					return em.getWx_code();
				}
			}
		}

		// 审核
		if ((flowordertype == FlowOrderTypeEnum.YiShenHe.getValue()) && (deliverystate == DeliveryStateEnum.FenZhanZhiLiu.getValue())) {
			return WanxiangNewFlowEnum.FenZhanZhiLiu.getWx_code();
		}
		// 配送成功
		if ((flowordertype == FlowOrderTypeEnum.YiFanKui.getValue())
				&& ((deliverystate == DeliveryStateEnum.PeiSongChengGong.getValue()) || (deliverystate == DeliveryStateEnum.ShangMenTuiChengGong.getValue()))) {
			return WanxiangNewFlowEnum.PeiSongChengGong.getWx_code();
		}
		// 配送成功 审核
		if ((flowordertype == FlowOrderTypeEnum.YiShenHe.getValue())
				&& ((deliverystate == DeliveryStateEnum.PeiSongChengGong.getValue()) || (deliverystate == DeliveryStateEnum.ShangMenTuiChengGong.getValue()))) {
			return WanxiangNewFlowEnum.PeiSongChengGongConfrim.getWx_code();
		}
		// 上门换成功
		if ((flowordertype == FlowOrderTypeEnum.YiFanKui.getValue()) && (deliverystate == DeliveryStateEnum.ShangMenHuanChengGong.getValue())) {
			return WanxiangNewFlowEnum.ShangmenHuanChengGong.getWx_code();
		}

		if (version != 0) {
			if ((flowordertype == FlowOrderTypeEnum.YiShenHe.getValue())
					&& ((deliverystate == DeliveryStateEnum.QuanBuTuiHuo.getValue()) || (deliverystate == DeliveryStateEnum.ShangMenJuTui.getValue()))) {
				return WanxiangNewFlowEnum.JuShou.getWx_code();
			}
			if ((flowordertype == FlowOrderTypeEnum.YiShenHe.getValue()) && (deliverystate == DeliveryStateEnum.BuFenTuiHuo.getValue())) {
				return WanxiangNewFlowEnum.BufenTuihuo.getWx_code();
			}
		}

		return null;

	}

	/**
	 * 反馈[万象]订单信息
	 */
	public void feedback_status() {

		if (!this.b2ctools.isB2cOpen(B2cEnum.Wanxiang.getKey())) {
			this.logger.info("未开0万象0的对接!");
			return;
		}
		Wanxiang wx = this.getWanxiang(B2cEnum.Wanxiang.getKey());
		this.sendCwbStatus_To_wx(wx);

		this.feedbackFailedAgain(wx, FlowOrderTypeEnum.RuKu.getValue());
		this.feedbackFailedAgain(wx, FlowOrderTypeEnum.ChuKuSaoMiao.getValue());
		this.feedbackFailedAgain(wx, FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue());
		this.feedbackFailedAgain(wx, FlowOrderTypeEnum.FenZhanLingHuo.getValue());
		this.feedbackFailedAgain(wx, FlowOrderTypeEnum.YiFanKui.getValue());
		this.feedbackFailedAgain(wx, FlowOrderTypeEnum.YiShenHe.getValue());

	}

	/**
	 * 状态反馈接口开始
	 * 
	 * @param smile
	 */
	private void sendCwbStatus_To_wx(Wanxiang wx) {

		try {

			int i = 0;
			while (true) {
				List<B2CData> datalist = this.b2cDataDAO.getDataListByFlowStatus(wx.getCustomerid(), wx.getMaxCount());
				i++;
				if (i > 100) {
					this.logger.warn("查询0万象0状态反馈已经超过100次循环，可能存在程序未知异常,请及时查询并处理!");
					return;
				}

				if ((datalist == null) || (datalist.size() == 0)) {
					this.logger.info("当前没有要推送0万象0的数据");
					return;
				}
				this.DealWithBuildXMLAndSending(wx, datalist);

			}

		} catch (Exception e) {
			this.logger.error("发送0万象0状态反馈遇到不可预知的异常", e);
		}

	}

	private void DealWithBuildXMLAndSending(Wanxiang wx, List<B2CData> datalist) throws Exception {

		for (B2CData data : datalist) {
			try {
				String jsoncontent = data.getJsoncontent();
				jsoncontent = jsoncontent.substring(1, jsoncontent.length() - 1);

				this.logger.info("当前[推送]0万象0状态={},USER_NAME={},PASSWORD={},jsoncontent={}", new Object[] { data.getFlowordertype(), wx.getUser_name(), wx.getPass_word(), jsoncontent });

				String md5Data = MD5Util.md5(jsoncontent + wx.getPrivate_key());
				String response = this.postHTTPJsonDataToWanXiang_new(wx.getUser_name(), wx.getPass_word(), jsoncontent, md5Data, wx.getUrl());

				this.logger.info("当前0万象0[返回]状态={},response={},cwb={}" + data.getCwb(), data.getFlowordertype(), response);

				WxResponse wxresp = WxUnmarchal.Unmarchal(response);
				String state_total = wxresp.getState();
				if (!"S0".equals(state_total)) {

					this.logger.error("万象推送系统错误,return.response=" + response);
					this.b2cDataDAO.updateB2cIdSQLResponseStatus(data.getB2cid(), 2, "万象返回失败");

				}

				int state = wxresp.getLog().getError_code().equalsIgnoreCase("B0") ? 1 : 2;
				String remark = wxresp.getLog().getError_msg();
				if ((remark != null) && remark.contains("重发")) {
					state = 2;
				}

				this.b2cDataDAO.updateB2cIdSQLResponseStatus(data.getB2cid(), state, remark);
			} catch (Exception e) {
				int send_b2c_flag = 2;
				if ((e.getMessage() != null) && e.getMessage().contains("Read") && e.getMessage().contains("out")) {
					send_b2c_flag = 0;
				}
				if ((e.getMessage() != null) && e.getMessage().contains("重发")) {
					send_b2c_flag = 2;
				}

				this.b2cDataDAO.updateB2cIdSQLResponseStatus(data.getB2cid(), send_b2c_flag, e.getMessage());
			}

		}

	}

	/**
	 * 补发推送失败的信息 只推送结果为 2 send_b2c_flag=2 每 1 个小时 推送一次 最多能推送 20次
	 */
	public void feedbackFailedAgain(Wanxiang wx, long flowordertype) {

		try {

			List<B2CData> datalist = this.b2cDataDAO.getDataListFailedByFlowStatus(flowordertype, wx.getCustomerid(), wx.getMaxCount(), 5, null);
			if ((datalist == null) || (datalist.size() == 0)) {
				this.logger.info("当前没有待重发的失败的数据wangxiang");
				return;
			}

			this.DealWithBuildXMLAndSending(wx, datalist);

		} catch (Exception e) {
			this.logger.error("error info by request wangxiang interface Again!", e);
		}
	}

}
