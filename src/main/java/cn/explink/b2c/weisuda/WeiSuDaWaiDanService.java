package cn.explink.b2c.weisuda;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pjbest.deliveryorder.bizservice.PjDeliverOrder4DMPRequest;
import com.pjbest.deliveryorder.bizservice.PjDeliveryOrder4DMPResponse;
import com.pjbest.deliveryorder.bizservice.PjDeliveryOrder4DMPServiceHelper.PjDeliveryOrder4DMPServiceClient;

import cn.explink.b2c.tools.B2cTools;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.WeisudaDAO;
import cn.explink.domain.Customer;
import cn.explink.domain.User;
import cn.explink.enumutil.CwbOrderTypeIdEnum;
import cn.explink.enumutil.PaytypeEnum;
import cn.explink.enumutil.pos.PosEnum;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.util.DateTimeUtil;
import cn.explink.util.JsonUtil;
import cn.explink.util.StringUtil;

@Service
public class WeiSuDaWaiDanService {
	@Autowired
	GetDmpDAO getDmpDAO;
	
	@Autowired
	WeisudaDAO weisudaDAO;
	@Autowired
	WeisudaService weisudaService;
 	
	@Autowired
	private B2cTools b2ctools;
	private Logger logger = LoggerFactory.getLogger(WeiSuDaWaiDanService.class);

	public void sendCwb()  {
			if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
				this.logger.info("唯速达_01未开启[唯速达]接口");
				return;
			}
			
			Weisuda weisuda = this.weisudaService.getWeisuda(PosEnum.Weisuda.getKey());
			if(weisuda.getIsSend() == 0){
				this.logger.info("未开启品骏达外单推送！");
				return;
			}
			
			try {
				
				int i = 0;
				while (true) {
					List<WeisudaCwb> weisudaCwbs = this.weisudaDAO.getWeisudaCwb("0",1);
					i++;
					if (i > 100) {
						String warning = "查询0品骏达外单0状态反馈已经超过100次循环，可能存在程序未知异常,请及时查询并处理!";
						this.logger.warn(warning);
						return;
					}

					if ((weisudaCwbs == null) || (weisudaCwbs.size() == 0)) {
						this.logger.info("品骏达外单当前没有要推送0品骏达外单0的数据");
						return;
					}

					this.sendCwbToWeiSuDa(weisudaCwbs, weisuda);
				}

			} catch (Exception e) {
				String errorinfo = "品骏达外单发送0品骏达0状态反馈遇到不可预知的异常";
				this.logger.error(errorinfo, e);
			}
			
			
		
	}
	private void sendCwbToWeiSuDa(List<WeisudaCwb> weisudaCwbs, Weisuda weisuda) throws JsonParseException, JsonMappingException, IOException {
		
			for(WeisudaCwb weisudaCwb:weisudaCwbs){
				try{
					PjDeliveryOrder4DMPServiceClient client=new PjDeliveryOrder4DMPServiceClient();
					
					ArrayList<PjDeliverOrder4DMPRequest> doReqs1 = buildPjDeliverOrderRequest(weisuda, weisudaCwb);
			        
			        List<PjDeliveryOrder4DMPResponse>  pjDeliveryOrderList = client.createDeliveryOrder(doReqs1);
					
						PjDeliveryOrder4DMPResponse pjDeliveryOrder = pjDeliveryOrderList.get(0);
						if(Integer.parseInt(pjDeliveryOrder.getResultCode()) >0){
							this.weisudaDAO.updateWeisudawaidan(weisudaCwb.getCwb(),"1","品骏达外单推送成功",1);
							this.logger.info("品骏达外单信息发送成功！cwb={}",weisudaCwb.getCwb());
						}else{
							this.logger.info("品骏达外单信息发送失败！cwb={},失败原因={}",weisudaCwb.getCwb(),pjDeliveryOrder.getResultMsg());
							this.weisudaDAO.updateWeisudawaidan(weisudaCwb.getCwb(),"2",pjDeliveryOrder.getResultMsg(),1);
						}
					
				 } catch(com.vip.osp.core.exception.OspException e){
				        this.logger.error("品骏达外单信息发送失败！cwb={},失败原因={}",e);
				        this.weisudaDAO.updateWeisudawaidan(weisudaCwb.getCwb(),"2",e.getMessage(),1);
				 }
			}
		
			
	   
	}
	
	private ArrayList<PjDeliverOrder4DMPRequest> buildPjDeliverOrderRequest(Weisuda weisuda, WeisudaCwb weisudaCwb)
			throws JsonParseException, JsonMappingException, IOException {
		ArrayList<PjDeliverOrder4DMPRequest> doReqs1 = new ArrayList<PjDeliverOrder4DMPRequest>();
		PjDeliverOrder4DMPRequest value2 = new  PjDeliverOrder4DMPRequest();
		WeiSuDaWaiDan weiSuDaWaiDan = new WeiSuDaWaiDan();
		weiSuDaWaiDan = JsonUtil.readValue(weisudaCwb.getWaidanjson(),weiSuDaWaiDan.getClass());

		value2.setCustOrderNo(weiSuDaWaiDan.getCustOrderNo());//客户订单号
		value2.setCustCode(StringUtil.nullConvertToEmptyString(weisuda.getCode()));// 发件客户编码
		if(StringUtils.isBlank(weiSuDaWaiDan.getCneeName())){
			value2.setCneeName("******");
		}else{
			value2.setCneeName(weiSuDaWaiDan.getCneeName());
		}
		value2.setCustName(weiSuDaWaiDan.getCustName());
		value2.setOrderType(weiSuDaWaiDan.getOrderType());//订单类型
		value2.setDistributer(weiSuDaWaiDan.getDistributer());//小件员
		value2.setCneeProv(weiSuDaWaiDan.getCneeProv());
		value2.setCneeCity(weiSuDaWaiDan.getCneeCity());
		value2.setCneeRegion(weiSuDaWaiDan.getCneeRegion());
		
		//如果收件人手机电话都为空，则传默认值
		if(StringUtils.isBlank(weiSuDaWaiDan.getCneeMobile()) && StringUtils.isBlank(weiSuDaWaiDan.getCneeTel())){
			value2.setCneeMobile("******");
		}else {
			value2.setCneeMobile(weiSuDaWaiDan.getCneeMobile());
			value2.setCneeTel(weiSuDaWaiDan.getCneeTel());
		}
		value2.setZipCode(weiSuDaWaiDan.getZipCode());
		value2.setCneeAddr(weiSuDaWaiDan.getCneeAddr());
		value2.setCneeRemark(weiSuDaWaiDan.getCneeRemark());
		value2.setDestOrg(weiSuDaWaiDan.getDestOrg());
		value2.setOrderSource(weiSuDaWaiDan.getOrderSource());
		value2.setWarehouse(weiSuDaWaiDan.getWarehouse());
		value2.setJoinTime(weiSuDaWaiDan.getJoinTime());
		value2.setIsCod(weiSuDaWaiDan.getIsCod());//是否货到付款
		value2.setCodAmount(weiSuDaWaiDan.getCodAmount());
		value2.setCarriage(weiSuDaWaiDan.getCarriage());//运费合计
		value2.setTotalNum(weiSuDaWaiDan.getTotalNum());//商品数量
		value2.setTotalWeight(weiSuDaWaiDan.getTotalWeight());
		value2.setCalculateWeight(weiSuDaWaiDan.getCalculateWeight());
		value2.setTotalVolume(weiSuDaWaiDan.getTotalVolume());//合计体积
		value2.setTotalBox(weiSuDaWaiDan.getTotalBox());
		value2.setAssuranceFee(weiSuDaWaiDan.getAssuranceFee());
		value2.setPayType(weiSuDaWaiDan.getPayType());//支付方式
		value2.setAccountMark(weiSuDaWaiDan.getAccountMark());
		value2.setPayment(weiSuDaWaiDan.getPayment());//付款方式
		value2.setPickerTime(weiSuDaWaiDan.getPickerTime());//揽件时间
		doReqs1.add(value2);
		return doReqs1;
	}
	
	
	//将外单信息存入表中
	public void saveWeiSuDa(DmpCwbOrder cwbOrder, Weisuda weisuda, Customer customer) {
		WeiSuDaWaiDan weiSuDaWaiDan = new WeiSuDaWaiDan();
		WeisudaCwb weisudaCwb = new WeisudaCwb();
		
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
			
            //我们系统中的运单号是对方的箱号，需要存储在custPackNo 中
			weiSuDaWaiDan.setCustOrderNo(StringUtil.nullConvertToEmptyString(cwbOrder.getCwb()));//客户订单号
            //value2.setTransportNo(StringUtil.nullConvertToEmptyString(cwbOrder.getTranscwb()));
			weiSuDaWaiDan.setCustCode(StringUtil.nullConvertToEmptyString(weisuda.getCode()));// 发件客户编码
            if(StringUtils.isBlank(cwbOrder.getConsigneename())){
            	weiSuDaWaiDan.setCneeName("******");
            }else{
            	weiSuDaWaiDan.setCneeName(cwbOrder.getConsigneename());
            }
            weiSuDaWaiDan.setCustName(StringUtil.nullConvertToEmptyString(customer.getCustomername()));
            weiSuDaWaiDan.setOrderType(orderType);//订单类型
            weiSuDaWaiDan.setDistributer(StringUtil.nullConvertToEmptyString(deliverUser.getUsername()));//小件员
            weiSuDaWaiDan.setCneeProv(StringUtil.nullConvertToEmptyString(cwbOrder.getCwbprovince()));
            weiSuDaWaiDan.setCneeCity(StringUtil.nullConvertToEmptyString(cwbOrder.getCwbcity()));
            weiSuDaWaiDan.setCneeRegion(StringUtil.nullConvertToEmptyString(cwbOrder.getCwbcounty()));
            
            //如果收件人手机电话都为空，则传默认值
            if(StringUtils.isBlank(cwbOrder.getConsigneemobile()) && StringUtils.isBlank(cwbOrder.getConsigneephone())){
            	weiSuDaWaiDan.setCneeMobile("******");
            }else {
            	weiSuDaWaiDan.setCneeMobile(cwbOrder.getConsigneemobile());
            	weiSuDaWaiDan.setCneeTel(cwbOrder.getConsigneephone());
            }
            weiSuDaWaiDan.setZipCode(StringUtil.nullConvertToEmptyString(cwbOrder.getConsigneepostcode()));
            weiSuDaWaiDan.setCneeAddr(StringUtil.nullConvertToEmptyString(cwbOrder.getConsigneeaddress()));
            weiSuDaWaiDan.setCneeRemark(StringUtil.nullConvertToEmptyString(cwbOrder.getPodsignremark()));
            weiSuDaWaiDan.setDestOrg(StringUtil.nullConvertToEmptyString(String.valueOf(cwbOrder.getDeliverybranchid())));
            weiSuDaWaiDan.setOrderSource(3);
            weiSuDaWaiDan.setWarehouse(StringUtil.nullConvertToEmptyString(cwbOrder.getCarwarehouse()));
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
			weiSuDaWaiDan.setJoinTime(date);
            if(cwbOrder.getReceivablefee().intValue() > 0){
            	weiSuDaWaiDan.setIsCod("1");//是否货到付款
            	weiSuDaWaiDan.setCodAmount(StringUtil.nullConvertToEmptyString(cwbOrder.getReceivablefee().toString()));
            }else{
            	weiSuDaWaiDan.setIsCod("0");
            }
            weiSuDaWaiDan.setCarriage(0);//运费合计
            weiSuDaWaiDan.setTotalNum(String.valueOf(cwbOrder.getSendcarnum()));//商品数量
            weiSuDaWaiDan.setTotalWeight(Double.parseDouble(cwbOrder.getCarrealweight().toString()));
            weiSuDaWaiDan.setCalculateWeight(0);
            weiSuDaWaiDan.setTotalVolume(0);//合计体积
            weiSuDaWaiDan.setTotalBox("0");
            weiSuDaWaiDan.setAssuranceFee(0);
            weiSuDaWaiDan.setPayType("-1");//支付方式
            weiSuDaWaiDan.setAccountMark(posString);
            if(cwbOrder.getPaywayid() == PaytypeEnum.Xianjin.getValue()){
            	weiSuDaWaiDan.setPayment("0");//付款方式
            } else if(cwbOrder.getPaywayid() == PaytypeEnum.Pos.getValue()){
            	weiSuDaWaiDan.setPayment("1");
            } else {
            	weiSuDaWaiDan.setPayment("-1");
            }
            weiSuDaWaiDan.setPickerTime(StringUtil.nullConvertToEmptyString(cwbOrder.getEmaildate()));//揽件时间
            
            String weiSuDaWaiDanjson =  JsonUtil.translateToJson(weiSuDaWaiDan);
            
            String orderTime = DateTimeUtil.formatDate(new Date());
            
            weisudaCwb.setWaidanjson(weiSuDaWaiDanjson);
            weisudaCwb.setCwb(cwbOrder.getCwb());
            weisudaCwb.setCwbordertypeid(orderType);
            weisudaCwb.setOperationTime(weiSuDaWaiDanjson);
            weisudaCwb.setCourier_code(deliverUser.getUsername());
			weisudaCwb.setOperationTime(orderTime);
            
            this.weisudaDAO.insertWeisuda(weisudaCwb, 1);
            this.logger.error("品骏达外单信息存储到express_b2cdata_weisuda 表中成功！cwb={}",cwbOrder.getCwb());
            
		} catch(Exception e){
            this.logger.error("品骏达外单信息存储到express_b2cdata_weisuda 表中失败！cwb={},失败原因={}",cwbOrder.getCwb(),e);
            e.printStackTrace();
        }
	}
	
}
