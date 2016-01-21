package cn.explink.b2c.tpsdo;


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

import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.CacheBaseListener;
import cn.explink.b2c.tpsdo.bean.TPOSendDoInf;
import cn.explink.b2c.tpsdo.bean.ThirdPartyOrder2DOCfg;
import cn.explink.b2c.tpsdo.bean.ThirdPartyOrder2DORequestVo;
import cn.explink.dao.GetDmpDAO;
import cn.explink.util.JsonUtil;

@Service
public class TPSDOService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private CacheBaseListener cacheBaseListener;
	@Autowired
	private GetDmpDAO getDmpDAO;
	@Autowired
	TPOSendDoInfDao tPOSendDoInfDao;
	@Autowired
	TPOSendDoInfService tPOSendDoInfService;
	@Autowired
	private B2cTools b2ctools;
	
	public void thirdPartyOrderSend2DO(){
		ThirdPartyOrder2DOCfg pushCfg = tPOSendDoInfService.getThirdPartyOrder2DOCfg();
		if(pushCfg == null){
			logger.info("未配置外单推送DO服务配置信息!无法推送外单数据...");
			return;
		}
		if(pushCfg.getOpenFlag() == 1){
			List<TPOSendDoInf> list = this.tPOSendDoInfDao.getUnSentTPOSendDoInf(100, pushCfg.getMaxTryTime());
			if(list.isEmpty()){
				return;
			}
			PjDeliveryOrder4DMPServiceClient client=new PjDeliveryOrder4DMPServiceClient();
			List<PjDeliverOrder4DMPRequest> doReqs =new  ArrayList<PjDeliverOrder4DMPRequest>();

			for(TPOSendDoInf tPOSendDoInf : list){
				PjDeliverOrder4DMPRequest PjDeliverOrder = buildPjDeliverOrderRequest(tPOSendDoInf);
				if(PjDeliverOrder != null){
					doReqs.add(PjDeliverOrder);
				}
			}
			
			List<PjDeliveryOrder4DMPResponse>  pjDeliveryOrderList = null;
			try{
				this.logger.info("开始推送"+doReqs.size() + "条外单数据给DO服务...");
				long beginTime = new Date().getTime();
				pjDeliveryOrderList = client.createDeliveryOrder(doReqs);
				this.logger.info("获得外单推DO响应数据" + (pjDeliveryOrderList == null ? 0 : pjDeliveryOrderList.size()) + "条，耗时" + (new Date().getTime() - beginTime));
				
			}catch(Exception e){
			    this.logger.error("推送外单数据给DO服务失败！出现未知异常。",e);
			}
			
			if(pjDeliveryOrderList == null || pjDeliveryOrderList.isEmpty()){
				return;
			}
			
			for(PjDeliveryOrder4DMPResponse response : pjDeliveryOrderList ){
				int resultCode = 0;
				try{
					resultCode = Integer.parseInt(response.getResultCode());
				}catch(Exception e){	
					logger.error("外单数据发送失败！cwb=" + (response == null ? "" :  response.getCustOrderNo()), e);
				}
				try{
					if(resultCode == 1){
						this.logger.info("推送外单数据给DO成功！cwb={}",response.getCustOrderNo());
						this.tPOSendDoInfService.updateTPOSendDoInf(response.getCustOrderNo(), "", response.getTransportNo(), 1, "");
					}else{
						this.logger.info("推送外单数据给DO失败！cwb={},失败原因={}",response.getCustOrderNo(),response.getResultMsg());
						this.tPOSendDoInfService.updateTPOSendDoInf(response.getCustOrderNo(), "", response.getTransportNo(), 0, "");
					}
				}catch(Exception e){
					logger.error("更新外单接口表数据推送账单失败 cwb=" +(response == null ? "" :  response.getCustOrderNo()), e);
				}
			}
			
		}
	}
	
	
	/**
	 * 
	 * @param tPOSendDoInf
	 * @return
	 */
	private PjDeliverOrder4DMPRequest buildPjDeliverOrderRequest(TPOSendDoInf tPOSendDoInf){
		ThirdPartyOrder2DORequestVo requestVo  = null;
		try {
			requestVo = JsonUtil.readValue(tPOSendDoInf.getReqObjJson(),ThirdPartyOrder2DORequestVo.class);
		} catch (Exception e) {
			this.logger.error("外单推送DO请求参数json串转对象失败，cwb="+tPOSendDoInf.getCwb(), e);
			return null;
		}
		PjDeliverOrder4DMPRequest request = new  PjDeliverOrder4DMPRequest();
		request.setAcceptDept(requestVo.getAcceptDept());
		request.setAcceptOperator(requestVo.getAcceptOperator());
		request.setAccountMark(requestVo.getAccountMark());
		request.setActualFee(requestVo.getActualFee().doubleValue());
		request.setActualPayType(requestVo.getActualPayType());
		request.setAssuranceFee(requestVo.getAssuranceFee().doubleValue());
		request.setAssuranceValue(requestVo.getAssuranceValue().doubleValue());
		request.setCalculateWeight(requestVo.getCalculateWeight().doubleValue());
		request.setCarriage(requestVo.getCarriage().doubleValue());
		request.setCneeAddr(requestVo.getCneeAddr());
		request.setCneeCertificate(requestVo.getCneeCertificate());
		request.setCneeCity(requestVo.getCneeCity());
		request.setCneeCorpName(requestVo.getCneeCorpName());
		request.setCneeMobile(requestVo.getCneeMobile());
		request.setCneeName(requestVo.getCneeName());
		request.setCneeNo(requestVo.getCneeNo());
		request.setCneePeriod(requestVo.getCneePeriod()+"");
		request.setCneeProv(requestVo.getCneeProv());
		request.setCneeRegion(requestVo.getCneeRegion());
		request.setCneeRemark(requestVo.getCneeRemark());
		request.setCneeTel(requestVo.getCneeTel());
		request.setCneeTown(requestVo.getCneeTown());
		request.setCnorAddr(requestVo.getCnorAddr());
		request.setCnorCity(requestVo.getCnorCity());
		request.setCnorMobile(requestVo.getCnorMobile());
		request.setCnorName(requestVo.getCnorName());
		request.setCnorProv(requestVo.getCnorProv());
		request.setCnorRegion(requestVo.getCnorRegion());
		request.setCnorRemark(requestVo.getCnorRemark());
		request.setCnorTel(requestVo.getCnorTel());
		request.setCnorTown(requestVo.getCnorTown());
		request.setCodAmount(requestVo.getCodAmount().doubleValue() + "");
		request.setCustCode(requestVo.getCustCode());
		request.setCustName(requestVo.getCustName());
		request.setCustOrderNo(requestVo.getCustOrderNo());
		request.setDestOrg(requestVo.getDestOrg());
		request.setDistributer(requestVo.getDistributer());
		request.setIsCod(requestVo.getIsCod() + "");
		request.setJoinTime(requestVo.getJoinTime());
		request.setOrderSource(requestVo.getOrderSource());
		request.setOrderType(requestVo.getOrderType());
		request.setPayment(requestVo.getPayment() + "");
		request.setPayType(requestVo.getPayType() + "");
		request.setPickerTime(requestVo.getPickerTime());
		request.setSendCarrierCode(requestVo.getSendCarrierCode());
		request.setTotalBox(requestVo.getTotalBox() + "");
		request.setTotalNum(requestVo.getTotalNum() + "");
		request.setTotalVolume(requestVo.getTotalVolume().doubleValue());
		request.setTotalWeight(requestVo.getTotalWeight().doubleValue());
		request.setTransportNo(requestVo.getTransportNo());
		request.setWarehouse(requestVo.getWarehouse());
		request.setZipCode(requestVo.getZipCode());
		
		return request;
		
	}

}
