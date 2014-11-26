package cn.explink.b2c.smile;

import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;

/**
 * 思迈速递 反馈允许反馈的状态 三种：签收、拒收，异常反馈
 * 
 * @author Administrator
 *
 */
public enum SmileFlowEnum {

	PeiSongChengGong(DeliveryStateEnum.PeiSongChengGong.getValue(), "SC_Sign", "RequestState", "签收成功"), ShangMenTuiChengGong(DeliveryStateEnum.ShangMenTuiChengGong.getValue(), "SC_Sign",
			"RequestState", "上门退成功"), ShangMenHuanChengGong(DeliveryStateEnum.ShangMenHuanChengGong.getValue(), "SC_Sign", "RequestState", "上门换成功"), JuShou(DeliveryStateEnum.QuanBuTuiHuo.getValue(),
			"SC_Back", "RequestState", "拒收返货"), BufenJuShou(DeliveryStateEnum.BuFenTuiHuo.getValue(), "SC_Back", "RequestState", "部分拒收返货"), PeiSongYiChang1(DeliveryStateEnum.FenZhanZhiLiu.getValue(),
			"SC_Abnor", "RequestAbnor", "配送异常-滞留"), PeiSongYiChang2(DeliveryStateEnum.HuoWuDiuShi.getValue(), "SC_Abnor", "RequestAbnor", "配送异常-丢失"), ;
	private long delivery_state;
	private String smile_code;
	private String request_code; // 请求的指令

	private String text;

	private SmileFlowEnum(long delivery_state, String smile_code, String request_code, String text) {
		this.delivery_state = delivery_state;
		this.smile_code = smile_code;
		this.request_code = request_code;
		this.text = text;
	}

	public String getSmile_code() {
		return smile_code;
	}

	public void setSmile_code(String smile_code) {
		this.smile_code = smile_code;
	}

	public long getDelivery_state() {
		return delivery_state;
	}

	public void setDelivery_state(long delivery_state) {
		this.delivery_state = delivery_state;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getRequest_code() {
		return request_code;
	}

	public void setRequest_code(String request_code) {
		this.request_code = request_code;
	}

}
