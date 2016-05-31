package cn.explink.b2c.shenzhoushuma;

import java.text.SimpleDateFormat;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.shenzhoushuma.xmldto.StepNode;
import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.User;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;

/**
 * 神州数码
 * 
 * @author yurong.liang 2016-04-25
 */
@Service
public class BuildShenzhoushumaB2cData {
	private Logger logger = LoggerFactory.getLogger(BuildShenzhoushumaB2cData.class);
	
	@Autowired
	private B2cDataOrderFlowDetail orderFlowDetail;
	@Autowired
	private GetDmpDAO getDmpdao;
	
	/**
	 * 构建神州数码存存到express_send_b2c_data表的数据
	 */
	public String buildShenzhoushumaMethod(DmpOrderFlow orderFlow,long flowOrdertype, DmpCwbOrder cwbOrder,
			DmpDeliveryState deliveryState, long delivery_state,
			ObjectMapper objectMapper) throws Exception {
	
		String receivedStatus = getFlowEnum(orderFlow,cwbOrder,flowOrdertype, delivery_state);
		
		if(receivedStatus==null){
			logger.info("订单号：{} 不属于【神州数码】所需要存储的json数据,状态{},return",cwbOrder.getCwb(),flowOrdertype);
			return null;
		}
		
		User user = getDmpdao.getUserById(orderFlow.getUserid());//操作人信息
		String opDescribe=orderFlowDetail.getDetail(orderFlow);//轨迹信息
		String deliveryMan = "";//小件员名字
		String deliveryManPhone = "";//小件员电话
		User deliveryUser = null;//小件员
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String operateTime = df.format(orderFlow.getCredate()); //系统操作时间
		String operator=user.getRealname();//系统操作人
		String busiOperateTime=operateTime;
		String busiOperator=operator;
		String memo="";
		
		if (flowOrdertype == FlowOrderTypeEnum.FenZhanLingHuo.getValue()) {//分站领货
			deliveryUser = getDmpdao.getUserById(deliveryState.getDeliveryid());
			deliveryMan = deliveryUser.getRealname();
			deliveryManPhone = deliveryUser.getUsermobile();
		}
		if (flowOrdertype == FlowOrderTypeEnum.YiShenHe.getValue()) {//已审核
			if(deliveryState.getDeliverystate() == DeliveryStateEnum.PeiSongChengGong.getValue()
					||deliveryState.getDeliverystate() == DeliveryStateEnum.QuanBuTuiHuo.getValue()){
				busiOperateTime=deliveryState.getSign_time()==""?deliveryState.getCreatetime():deliveryState.getSign_time();
				busiOperator = deliveryState.getSign_man()==""?cwbOrder.getConsigneename():deliveryState.getSign_man();
			}
			if(deliveryState.getDeliverystate() == DeliveryStateEnum.QuanBuTuiHuo.getValue()){
				memo= "拒收原因:"+cwbOrder.getBackreason();//退货原因
			}
		}
		
		StepNode step =new StepNode();
		step.setDoId(orderFlow.getCwb());//捷科交货单号
		step.setOpPoint(receivedStatus);
		step.setOpDescribe(opDescribe);
		step.setOperateTime(operateTime);
		step.setOperator(operator);
		step.setBusiOperateTime(busiOperateTime);//业务操作时间
		step.setBusiOperator(busiOperator);//业务操作人
		step.setDeliveryMan(deliveryMan);//小件员
		step.setDeliveryManPhone(deliveryManPhone);//小件员电话
		step.setMemo(memo);//备注
		
		logger.info("订单号：{}封装成【神州数码】所需要的json----状态：{}",cwbOrder.getCwb(),flowOrdertype);
		return objectMapper.writeValueAsString(step);	
	}

	// 获取需要存到express_send_b2c_data表的轨迹
	public String getFlowEnum(DmpOrderFlow orderFlow,DmpCwbOrder cwbOrder,long flowordertype, long deliverystate) {	
		if(flowordertype==FlowOrderTypeEnum.RuKu.getValue()){
			return OpPointEnum.RuKu.getOpPointType();//入库
		}
		
		if(flowordertype==FlowOrderTypeEnum.ChuKuSaoMiao.getValue()){ 
			String carwarehouse= cwbOrder.getCarwarehouse();
			if(carwarehouse!=null && !"".equals(carwarehouse) && orderFlow.getBranchid()==Long.parseLong(carwarehouse) ){
				return OpPointEnum.ChuKu.getOpPointType();//出库-只要出库站点为订单入库站点的轨迹
			}
		}		
		
		if(flowordertype==FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue() && orderFlow.getBranchid()==cwbOrder.getDeliverybranchid()){
			return OpPointEnum.DaoHuo.getOpPointType();//分站到货-只要到达目的站点的轨迹
		}	
		
		if(flowordertype==FlowOrderTypeEnum.FenZhanLingHuo.getValue()){
			return OpPointEnum.LingHuo.getOpPointType();//分站领货
		}
		
		if (flowordertype == FlowOrderTypeEnum.YiShenHe.getValue() && deliverystate == DeliveryStateEnum.PeiSongChengGong.getValue()) {
			return OpPointEnum.QianShou.getOpPointType();// 签收
		}
		
		if (flowordertype == FlowOrderTypeEnum.YiShenHe.getValue() && deliverystate == DeliveryStateEnum.QuanBuTuiHuo.getValue()) {
			return OpPointEnum.JuShou.getOpPointType();//拒收
		}
		return null;
	}
}
