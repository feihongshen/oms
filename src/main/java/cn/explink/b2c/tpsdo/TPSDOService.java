package cn.explink.b2c.tpsdo;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.pjbest.deliveryorder.bizservice.PjDeliverOrder4DMPRequest;
import com.pjbest.deliveryorder.bizservice.PjDeliveryOrder4DMPResponse;
import com.pjbest.deliveryorder.bizservice.PjDeliveryOrder4DMPServiceHelper.PjDeliveryOrder4DMPServiceClient;

import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.CacheBaseListener;
import cn.explink.b2c.tpsdo.bean.TPOSendDoInf;
import cn.explink.b2c.tpsdo.bean.ThirdPartyOrder2DOCfg;
import cn.explink.b2c.tpsdo.bean.ThirdPartyOrder2DORequestVo;
import cn.explink.dao.CwbDAO;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.SystemInstall;
import cn.explink.enumutil.CwbOrderTypeIdEnum;
import cn.explink.enumutil.TpsOrderTypeEnum;
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
	@Autowired
	CwbDAO cwbDAO;
	
	@Transactional(propagation=Propagation.REQUIRED,isolation=Isolation.READ_COMMITTED)
	public void thirdPartyOrderSend2DO(){
		ThirdPartyOrder2DOCfg pushCfg = tPOSendDoInfService.getThirdPartyOrder2DOCfg();
		if(pushCfg == null){
			logger.info("未配置外单推送DO服务配置信息!无法推送外单数据...");
			return;
		}
		if(pushCfg.getOpenFlag() == 1){
			List<TPOSendDoInf> list = this.tPOSendDoInfDao.getUnSentTPOSendDoInf(500, pushCfg.getMaxTryTime());
			if(list.isEmpty()){
				return;
			}
			PjDeliveryOrder4DMPServiceClient client=new PjDeliveryOrder4DMPServiceClient();
			List<PjDeliverOrder4DMPRequest> doReqs =new  ArrayList<PjDeliverOrder4DMPRequest>();

			for(TPOSendDoInf tPOSendDoInf : list){
					PjDeliverOrder4DMPRequest pjDeliverOrder = null;
					try{
						pjDeliverOrder = buildPjDeliverOrderRequest(tPOSendDoInf);
					}catch(Exception e){
						logger.error("buildPjDeliverOrderRequest failed. cwb={}",tPOSendDoInf.getCwb(), e);
						try{
							this.tPOSendDoInfService.updateTPOSendDoInf(tPOSendDoInf.getId(), null, 0, tPOSendDoInf.getTrytime() + 1, "build request fail:"+e.getMessage());
						}catch(Exception ex){
							logger.error("buildPjDeliverOrderRequest db failed. cwb={}",tPOSendDoInf.getCwb(), ex);
						}
						continue;
					}
					
					if(pjDeliverOrder != null){
						// 过滤不需要传给tps的订单，并处理成功 add by jian_xie 20161103
						int trytime = tPOSendDoInf.getTrytime();
						if(!isNeedToTps(pjDeliverOrder)){
							tPOSendDoInfService.updateTPOSendDoInf(tPOSendDoInf.getId(), pjDeliverOrder.getTransportNo(), 1, trytime + 1, "该订单类型不需要发给tps");
							logger.info("该订单类型不需要发给tps,订单号{}", pjDeliverOrder.getCustOrderNo());
							continue;
						}
						doReqs.clear();
						doReqs.add(pjDeliverOrder);
						List<PjDeliveryOrder4DMPResponse>  pjDeliveryOrderList = null;
						try{
							this.logger.info("开始推送"+doReqs.size() + "条外单数据给DO服务...");
							long beginTime = new Date().getTime();
							pjDeliveryOrderList = client.createDeliveryOrder(doReqs);
							this.logger.info("获得外单推DO响应数据" + (pjDeliveryOrderList == null ? 0 : pjDeliveryOrderList.size()) + "条，耗时" + (new Date().getTime() - beginTime) + "ms");
							
						}catch(Exception e){
						    this.logger.error("推送外单数据给DO服务失败！出现未知异常。",e);
						}
						
						if(pjDeliveryOrderList == null || pjDeliveryOrderList.isEmpty()){
							continue;
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
									this.tPOSendDoInfService.updateTPOSendDoInf(tPOSendDoInf.getId(), response.getTransportNo(), 1, trytime + 1, "");
									this.cwbDAO.updateTpstranscwbByCwb(response.getCustOrderNo(),response.getTransportNo());
								}else{
									this.logger.info("推送外单数据给DO失败！cwb={},失败原因={}",response.getCustOrderNo(),response.getResultMsg());
									this.tPOSendDoInfService.updateTPOSendDoInf(tPOSendDoInf.getId(), response.getTransportNo(), 0, trytime + 1, response.getResultMsg());
								}
							}catch(Exception e){
								logger.error("更新外单接口表数据推送账单失败 cwb=" +(response == null ? "" :  response.getCustOrderNo()), e);
							}
						}
					}
				
			}
		}
	}
	
	/**
	 * 判断订单是否需要推tps
	 * @author jian.xie
	 * @param pjDeliverOrder
	 * @return
	 */
	private boolean isNeedToTps(PjDeliverOrder4DMPRequest pjDeliverOrder) {
		SystemInstall systemInstall = getDmpDAO.getSystemInstallByName("cwbOrderTypeIdNeedToTps");
		int orderType = pjDeliverOrder.getOrderType();
		if(systemInstall == null){
			//当没有配参数的时候，默认配送单推送
			if(orderType == TpsOrderTypeEnum.Peisong.getValue()){
				return true;
			}
		} else {
			String value = systemInstall.getValue();
			if(StringUtils.isEmpty(value)){
				// 当参数的值为空的时候，默认配送单推送
				if(orderType == TpsOrderTypeEnum.Peisong.getValue()){
					return true;
				}
			} else {
				String[] strTypes = value.split(",|，");
				for(int i = 0, len = strTypes.length; i < len; i++){
					if((orderType + "").equals(strTypes[i])){
						return true;
					}
				}
			}
		}
		return false;
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
			//return null;
			throw new RuntimeException("json串转对象失败:"+e.getMessage());
		}
		PjDeliverOrder4DMPRequest request = new  PjDeliverOrder4DMPRequest();
		request.setAcceptDept(requestVo.getAcceptDept());
		request.setAcceptOperator(requestVo.getAcceptOperator());
		request.setAccountMark(requestVo.getAccountMark());
		request.setActualFee(requestVo.getActualFee()==null?BigDecimal.ZERO.doubleValue():requestVo.getActualFee().doubleValue());
		request.setActualPayType(requestVo.getActualPayType());
		request.setAssuranceFee(requestVo.getAssuranceFee()==null?BigDecimal.ZERO.doubleValue():requestVo.getAssuranceFee().doubleValue());
		request.setAssuranceValue(requestVo.getAssuranceValue()==null?BigDecimal.ZERO.doubleValue():requestVo.getAssuranceValue().doubleValue());
		request.setCalculateWeight(requestVo.getCalculateWeight()==null?BigDecimal.ZERO.doubleValue():requestVo.getCalculateWeight().doubleValue());
		request.setCarriage(requestVo.getCarriage()==null?BigDecimal.ZERO.doubleValue():requestVo.getCarriage().doubleValue());
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
		request.setCodAmount(requestVo.getCodAmount()==null?BigDecimal.ZERO.doubleValue()+"":requestVo.getCodAmount().doubleValue() + "");
		request.setCustCode(requestVo.getCustCode());
		request.setCustName(requestVo.getCustName());
		request.setCustOrderNo(requestVo.getCustOrderNo());
		request.setDestOrg(requestVo.getDestOrg());
		request.setDistributer(requestVo.getDistributer());
		request.setIsCod(requestVo.getIsCod() + "");
		request.setJoinTime(requestVo.getJoinTime());
		request.setOrderSource(requestVo.getOrderSource());
		request.setOrderType(requestVo.getOrderType());
		request.setOperateType(requestVo.getOperateType());
		request.setPayment(requestVo.getPayment());
		request.setPayType(requestVo.getPayType() + "");
		request.setPickerTime(requestVo.getPickerTime());
		request.setSendCarrierCode(requestVo.getSendCarrierCode());
		request.setTotalBox(requestVo.getTotalBox() + "");
		request.setTotalNum(requestVo.getTotalNum() + "");
		request.setTotalVolume(requestVo.getTotalVolume()==null?BigDecimal.ZERO.doubleValue():requestVo.getTotalVolume().doubleValue());
		request.setTotalWeight(requestVo.getTotalWeight()==null?BigDecimal.ZERO.doubleValue():requestVo.getTotalWeight().doubleValue());
		request.setTransportNo(requestVo.getTransportNo());
		request.setWarehouse(requestVo.getWarehouse());
		request.setZipCode(requestVo.getZipCode());
		request.setReturnCredit(requestVo.getReturnAmount() == null ? 0 : requestVo.getReturnAmount().doubleValue());
		
		if(requestVo.getBoxinfos() != null){
			List<com.pjbest.deliveryorder.order.service.BoxInfoRequest> boxInfoRqList = new ArrayList<com.pjbest.deliveryorder.order.service.BoxInfoRequest>();
			for(ThirdPartyOrder2DORequestVo.Boxinfo boxinfo : requestVo.getBoxinfos()){
				com.pjbest.deliveryorder.order.service.BoxInfoRequest boxInfoRq = new com.pjbest.deliveryorder.order.service.BoxInfoRequest();
				boxInfoRq.setBoxNo(boxinfo.getBoxNo());
				boxInfoRq.setVolume(boxinfo.getVolume()==null?BigDecimal.ZERO.doubleValue():boxinfo.getVolume().doubleValue());
				boxInfoRq.setWeight(boxinfo.getWeight()==null?BigDecimal.ZERO.doubleValue():boxinfo.getWeight().doubleValue());
				boxInfoRqList.add(boxInfoRq);
			}
			request.setBoxInfos(boxInfoRqList);
		}
		
		if(requestVo.getDetails() != null){
			List<com.pjbest.deliveryorder.bizservice.PjDeliveryOrder4DMPCargoInfo> OrderCargoInfoList = new ArrayList<com.pjbest.deliveryorder.bizservice.PjDeliveryOrder4DMPCargoInfo>();
			for(ThirdPartyOrder2DORequestVo.OrderCargoInfo cargoInfo : requestVo.getDetails()){
				com.pjbest.deliveryorder.bizservice.PjDeliveryOrder4DMPCargoInfo cargoInfoRq = new com.pjbest.deliveryorder.bizservice.PjDeliveryOrder4DMPCargoInfo();
				cargoInfoRq.setCargoName(cargoInfo.getCargoName());
				cargoInfoRq.setCount(cargoInfo.getCount() + "");
				cargoInfoRq.setCargoLength(cargoInfo.getCargoLength()==null?BigDecimal.ZERO.doubleValue():cargoInfo.getCargoLength().doubleValue());
				cargoInfoRq.setCargoHeight(cargoInfo.getCargoHeight()==null?BigDecimal.ZERO.doubleValue():cargoInfo.getCargoHeight().doubleValue());
				cargoInfoRq.setCargoWidth(cargoInfo.getCargoWidth()==null?BigDecimal.ZERO.doubleValue():cargoInfo.getCargoWidth().doubleValue());
				cargoInfoRq.setWeight(cargoInfo.getWeight()==null?BigDecimal.ZERO.doubleValue():cargoInfo.getWeight().doubleValue());
				cargoInfoRq.setPrice(cargoInfo.getPrice()==null?BigDecimal.ZERO.doubleValue():cargoInfo.getPrice().doubleValue());
				cargoInfoRq.setUnit(cargoInfo.getUnit());
				cargoInfoRq.setVolume(cargoInfo.getVolume()==null?BigDecimal.ZERO.doubleValue():cargoInfo.getVolume().doubleValue());
				cargoInfoRq.setSizeSn(cargoInfo.getSizeSn());
				OrderCargoInfoList.add(cargoInfoRq);
			}
			request.setPjDeliveryOrderCargoInfos(OrderCargoInfoList);
		}
		return request;
		
	}

}
