package cn.explink.b2c.weisuda;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pjbest.deliveryorder.bizservice.PjDeliverOrder4DMPRequest;
import com.pjbest.deliveryorder.bizservice.PjDeliveryOrder4DMPResponse;
import com.pjbest.deliveryorder.bizservice.PjDeliveryOrder4DMPServiceHelper.PjDeliveryOrder4DMPServiceClient;

import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.Customer;
import cn.explink.domain.User;
import cn.explink.enumutil.CwbOrderTypeIdEnum;
import cn.explink.enumutil.PaytypeEnum;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.util.StringUtil;

@Service
public class WeiSuDaWaiDanService {
	@Autowired
	GetDmpDAO getDmpDAO;
	private Logger logger = LoggerFactory.getLogger(WeiSuDaWaiDanService.class);

	public void sendCwb(DmpCwbOrder cwbOrder,Weisuda weisuda,Customer customer)  {
		try {
			String posString = this.getDmpDAO.getNowCustomerPos(customer.getCustomerid());
			User deliverUser = this.getDmpDAO.getUserById(cwbOrder.getDeliverid());
			int cwbOrderType = Integer.parseInt(cwbOrder.getCwbordertypeid());
			int orderType = 0;
			if(cwbOrderType == CwbOrderTypeIdEnum.OXO.getValue()){
				orderType = 1;
			}else if(cwbOrderType == CwbOrderTypeIdEnum.Peisong.getValue()){
				orderType = 2;
			} else if(cwbOrderType == CwbOrderTypeIdEnum.Shangmenhuan.getValue()){
				orderType = 4;
			} else if(cwbOrderType  == CwbOrderTypeIdEnum.Shangmentui.getValue()){
				orderType =3;
			};
			
            PjDeliveryOrder4DMPServiceClient client=new PjDeliveryOrder4DMPServiceClient();
            ArrayList<PjDeliverOrder4DMPRequest> doReqs1 = new ArrayList<PjDeliverOrder4DMPRequest>();
            PjDeliverOrder4DMPRequest value2 = new  PjDeliverOrder4DMPRequest();
            value2.setCustOrderNo(StringUtil.nullConvertToEmptyString(cwbOrder.getCwb()));//客户订单号
            value2.setTransportNo(StringUtil.nullConvertToEmptyString(cwbOrder.getTranscwb()));
            value2.setCustCode(StringUtil.nullConvertToEmptyString(weisuda.getCode()));// 发件客户编码
            value2.setCustName(StringUtil.nullConvertToEmptyString(customer.getCustomername()));
            value2.setOrderType(orderType);//订单类型
            value2.setDistributer(StringUtil.nullConvertToEmptyString(deliverUser.getRealname()));//小件员
            value2.setCneeProv(StringUtil.nullConvertToEmptyString(cwbOrder.getCwbprovince()));
            value2.setCneeCity(StringUtil.nullConvertToEmptyString(cwbOrder.getCwbcity()));
            value2.setCneeRegion(StringUtil.nullConvertToEmptyString(cwbOrder.getCwbcounty()));
            value2.setCneeName(StringUtil.nullConvertToEmptyString(cwbOrder.getConsigneename()));
            value2.setCneeMobile(StringUtil.nullConvertToEmptyString(cwbOrder.getConsigneemobile()));
            value2.setCneeTel(StringUtil.nullConvertToEmptyString(cwbOrder.getConsigneephone()));
            value2.setZipCode(StringUtil.nullConvertToEmptyString(cwbOrder.getConsigneepostcode()));
            value2.setCneeAddr(StringUtil.nullConvertToEmptyString(cwbOrder.getConsigneeaddress()));
            value2.setCneeRemark(StringUtil.nullConvertToEmptyString(cwbOrder.getPodsignremark()));
            value2.setDestOrg(StringUtil.nullConvertToEmptyString(String.valueOf(cwbOrder.getDeliverybranchid())));
            value2.setOrderSource(3);
            value2.setWarehouse(StringUtil.nullConvertToEmptyString(cwbOrder.getCarwarehouse()));
            //出仓时间
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dstr=cwbOrder.getEmaildate();  
            Date date= new Date();
			try {
				date = sdf.parse(dstr);
			} catch (ParseException e) {
				e.printStackTrace();
				this.logger.info(cwbOrder.getCwb()+"唯速达外单出仓时间格式转换异常！");
			}
            value2.setJoinTime(date);
            if(cwbOrder.getReceivablefee().intValue() > 0){
            	value2.setIsCod("1");//是否货到付款
            	value2.setCodAmount(StringUtil.nullConvertToEmptyString(cwbOrder.getReceivablefee().toString()));
            }else{
            	value2.setIsCod("0");
            }
            value2.setCarriage(0);//运费合计
            value2.setTotalNum(String.valueOf(cwbOrder.getSendcarnum()));//商品数量
            value2.setTotalWeight(Double.parseDouble(cwbOrder.getCarrealweight().toString()));
            value2.setCalculateWeight(0);
            value2.setTotalVolume(0);//合计体积
            value2.setTotalBox("0");
            value2.setTotalVolume(0);
            value2.setAssuranceFee(0);
            value2.setPayType("-1");//支付方式
            value2.setAccountMark(posString);
            if(cwbOrder.getPaywayid() == PaytypeEnum.Xianjin.getValue()){
            	value2.setPayment("0");//付款方式
            } else if(cwbOrder.getPaywayid() == PaytypeEnum.Pos.getValue()){
            	value2.setPayment("1");
            } else {
            	value2.setPayment("-1");
            }
            value2.setPickerTime(StringUtil.nullConvertToEmptyString(cwbOrder.getEmaildate()));//揽件时间
            doReqs1.add(value2);
            List<PjDeliveryOrder4DMPResponse>  pjDeliveryOrderList = client.createDeliveryOrder(doReqs1);
            PjDeliveryOrder4DMPResponse pjDeliveryOrder = pjDeliveryOrderList.get(0);
            if(Integer.parseInt(pjDeliveryOrder.getResultCode()) >0){
            	this.logger.info("唯速达 外单信息发送成功！");
            }else{
            	this.logger.info("唯速达 外单信息发送失败！",pjDeliveryOrder.getResultMsg());
            	System.out.println(pjDeliveryOrder.getResultMsg());
            }
        } catch(com.vip.osp.core.exception.OspException e){
            this.logger.error("唯速达 外单信息发送失败！",e);
            e.printStackTrace();
        }
	}
	
}
