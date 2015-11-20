/**
 * 
 */
package cn.explink.b2c.gxdx;

import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
/**
 /**
 * 
 */

/**
 * @ClassName: GxDxOrderTypeEnum 
 * @Description: TODO
 * @Author: 王强
 * @Date: 2015年11月11日上午11:14:54
 */
public enum GxDxOrderTypeEnum {
		//订单状态
		SC01("SC01",FlowOrderTypeEnum.RuKu.getValue(),DeliveryStateEnum.WeiFanKui.getValue(),"已接收（已入库）"),
		SC02("SC02",FlowOrderTypeEnum.FenZhanLingHuo.getValue(),DeliveryStateEnum.WeiFanKui.getValue(),"配送中（第一次分配配送员）"),
		SC06_1("SC06", FlowOrderTypeEnum.ChuKuSaoMiao.getValue(),DeliveryStateEnum.WeiFanKui.getValue(),"中转中（第一次分配给站点）"),
		SC06_2("SC06", FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue(),DeliveryStateEnum.WeiFanKui.getValue(),"中转中（第一次分配给站点）"),
		//SC06_2("SC06",FlowOrderTypeEnum.ZhongZhuanChuKuSaoMiao.getValue(), DeliveryStateEnum.WeiFanKui.getValue(),"中转中，所有的中转扫描"), 
		//SC06_3("SC06", FlowOrderTypeEnum.TuiHuoChuKuSaoMiao.getValue(),DeliveryStateEnum.WeiFanKui.getValue(), "分站退货出站"),
		//配送结果
		SC03("SC03",FlowOrderTypeEnum.YiShenHe.getValue(),DeliveryStateEnum.PeiSongChengGong.getValue(),"妥投"),
		SC04("SC04",FlowOrderTypeEnum.YiShenHe.getValue(),DeliveryStateEnum.FenZhanZhiLiu.getValue(),"滞留"),
		SC05("SC05",FlowOrderTypeEnum.YiShenHe.getValue(),DeliveryStateEnum.QuanBuTuiHuo.getValue(),"拒收"),
		SC07("SC07",FlowOrderTypeEnum.YiShenHe.getValue(),DeliveryStateEnum.HuoWuDiuShi.getValue(),"货物丢失"),
		SC08("SC08",FlowOrderTypeEnum.YiShenHe.getValue(),DeliveryStateEnum.FenZhanZhiLiu.getValue(),"退货（退甲方，出库时）"),
		SC09("SC09",FlowOrderTypeEnum.YiShenHe.getValue(), DeliveryStateEnum.ShangMenTuiChengGong.getValue(), "改货款（提交配送结果时）-上门退成功"),
		SC10("SC10",FlowOrderTypeEnum.YiShenHe.getValue(),DeliveryStateEnum.ShangMenJuTui.getValue(), "上门退成功");
		
		private String state;
		private int flowordertype;
		private int deliveryState;
		private String text;
		
		private GxDxOrderTypeEnum(String state,int flowordertype,int deliveryState,String text) {
			// TODO Auto-generated constructor stub
			this.state=state;
			this.flowordertype=flowordertype;
			this.deliveryState = deliveryState;
			this.text=text;
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
		public String getState() {
			return state;
		}
		public void setState(String state) {
			this.state = state;
		}
		public int getDeliveryState() {
			return deliveryState;
		}
		public void setDeliveryState(int deliveryState) {
			this.deliveryState = deliveryState;
		}
		
}
