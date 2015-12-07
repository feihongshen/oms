package cn.explink.b2c.weisuda;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.pjbest.deliveryorder.bizservice.PjDeliverOrder4DMPRequest;
import com.pjbest.deliveryorder.bizservice.PjDeliveryOrder4DMPResponse;
import com.pjbest.deliveryorder.bizservice.PjDeliveryOrder4DMPServiceHelper.PjDeliveryOrder4DMPServiceClient;

public class WeisudaTest {
	public static void main(String[] args) throws ParseException{
		 try {
	            PjDeliveryOrder4DMPServiceClient client=new PjDeliveryOrder4DMPServiceClient();
	            ArrayList<PjDeliverOrder4DMPRequest> doReqs1 = new ArrayList<PjDeliverOrder4DMPRequest>();
	            PjDeliverOrder4DMPRequest value2 = new PjDeliverOrder4DMPRequest();
	            value2.setCustOrderNo("14031970949939");
	            value2.setCustCode("GZPX");
	            value2.setCnorName("发货人");
	            value2.setCnorMobile("发货联系手机");
	            value2.setCneeProv("广东省");
	            value2.setCneeCity("广州市");
	            value2.setCneeRegion("荔湾区");
	            value2.setCneeAddr("花海街2号");
	            value2.setCneeName("收货人");
	            value2.setCneeMobile("13570507226");
	            value2.setCneeTel("13354845447");
	            value2.setCneePeriod("0");
	            value2.setCarriage(10);
	            value2.setOrderType(2);
	            value2.setOrderSum(1);
	            value2.setTotalNum("1");
	            value2.setTotalWeight(1);
	            value2.setTotalBox("1");
	            value2.setTotalVolume(1);
	            value2.setPayType("1");
	            value2.setDestOrg("1850207");
	            value2.setPayment("11");
	            value2.setAssuranceValue(1);
	            value2.setPayment("0");
	            value2.setIsCod("0");
	            
	            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	            String dstr="2015-10-11 12:00:11";  
	            java.util.Date date=sdf.parse(dstr); 
	            value2.setDistributer("3");
	            value2.setJoinTime(date);
	            value2.setAssuranceFee(0);
	            value2.setCalculateWeight(0);
	            value2.setCustName("客户名称");
	            value2.setCneeCorpName("收货人公司名称");
	            doReqs1.add(value2);
	            List<PjDeliveryOrder4DMPResponse> pjDeliveryOrderList = client.createDeliveryOrder(doReqs1);
	            
	            PjDeliveryOrder4DMPResponse pjDeliveryOrder = pjDeliveryOrderList.get(0);
	            
	            if(Integer.parseInt(pjDeliveryOrder.getResultCode()) >0){
	            	System.out.println("成功");
	            }else{
	            	System.out.println("失败");
	            	System.out.println(pjDeliveryOrder.getResultMsg());
	            }
	            
	        } catch(com.vip.osp.core.exception.OspException e){
	            e.printStackTrace();
	        }
	}
}
