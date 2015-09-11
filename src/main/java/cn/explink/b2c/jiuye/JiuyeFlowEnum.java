package cn.explink.b2c.jiuye;

import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;


/**
 * 九曳 反馈允许反馈的状态
 * 三种：签收、拒收，异常反馈
 * @author Administrator
 *
 */
public enum JiuyeFlowEnum {
	
	TMS_ACCEPT("TMS_ACCEPT",FlowOrderTypeEnum.RuKu.getValue(),"入库信息反馈"),          //同时推送库房进 TMS_STATION_IN
	
	TMS_STATION_OUT("TMS_STATION_OUT",FlowOrderTypeEnum.ChuKuSaoMiao.getValue(),"库房出库"),  
	TMS_STATION_IN("TMS_STATION_IN",FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue(),"分站进"), 
	
	TMS_DELIVERING("TMS_DELIVERING",FlowOrderTypeEnum.FenZhanLingHuo.getValue(),"分站派送"),  //同时推送分站出 TMS_STATION_OUT
	
	TMS_SIGN("TMS_SIGN",FlowOrderTypeEnum.YiShenHe.getValue(),"签收"),
	TMS_FAILED("TMS_FAILED",FlowOrderTypeEnum.YiShenHe.getValue(),"拒收"),
	TMS_ERROR("TMS_ERROR",FlowOrderTypeEnum.YiShenHe.getValue(),"滞留"),
	
	;
	
	
	private String request_code; //请求的指令
	private int flowordertype; // 0 配送中， 1最终结果
	private String text;
	
	

	public String getRequest_code() {
		return request_code;
	}



	public void setRequest_code(String request_code) {
		this.request_code = request_code;
	}



	public int getFlowordertype() {
		return flowordertype;
	}



	public void setFlowordertype(int flowordertype) {
		this.flowordertype = flowordertype;
	}



	public String getText() {
		return text;
	}



	public void setText(String text) {
		this.text = text;
	}



	private JiuyeFlowEnum(String request_code,int flowordertype,String text){
		this.request_code=request_code;
		this.text=text;
		this.flowordertype=flowordertype;
	}
	
}
