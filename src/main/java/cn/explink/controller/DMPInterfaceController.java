package cn.explink.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tpsdo.OrderTrackToTPSDAO;
import cn.explink.b2c.tpsdo.TPOSendDoInfDao;
import cn.explink.b2c.vipshop.VipShopMD5Util;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.SystemInstall;

/**
 * dmp调用oms功能入口
 * @author jian.xie
 * @date 2016年12月9日 上午10:44:41
 */
@RequestMapping("/DMPInterface")
@Controller
public class DMPInterfaceController {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	TPOSendDoInfDao tPOSendDoInfDao;
	
	@Autowired
	OrderTrackToTPSDAO orderTrackToTPSDAO;
	
	@Autowired
	B2CDataDAO b2CDataDAO;
	
	@Autowired 
	GetDmpDAO  getDmpDao;
	
	/**
	 * 返回拼好字符串
	 * @param cwbs
	 * @return
	 */
	private String getStringResult(String cwbs){
		String[] cwbArr = cwbs.split(",");
		StringBuilder sb = new StringBuilder();
		for (int i = 0, len = cwbArr.length; i < len; i++) {
			if (!StringUtils.isEmpty(cwbArr[i])) {
				sb.append("'");
				sb.append(cwbArr[i]);
				sb.append("'");
				sb.append(",");
			}
		}
		if(sb.length() > 0){
			return sb.substring(0, sb.length() - 1);
		}else{
			return sb.toString();
		}
	}
	
	/**
	 * 校验密钥
	 */
	private boolean validate(String cwbs, String requesttime, String ciphertext){
		SystemInstall systemInstall = getDmpDao.getSystemInstallByName("dmpAndOmsSecretKey");
		if(systemInstall == null || StringUtils.isEmpty(systemInstall.getValue())){
			return false;
		}
		String sign = VipShopMD5Util.MD5(cwbs + requesttime + systemInstall.getValue()).toLowerCase(); 
		if(sign.equals(ciphertext)){
			return true;
		} else {
			return false;
		}			
	}
	
	/**
	 * 
	 * @param cwbs
	 * @param type
	 */
	@RequestMapping("/reTpoSendDoInf")
	public void reTpoSendDoInf(String cwbs, String type, String md5, String requesttime){
		if(!validate(cwbs, requesttime, md5)){
			logger.info("dmp与oms通讯签名不对");
			return;
		}
		String cwbParam=getStringResult(cwbs);
		if("all".equals(type)){
			tPOSendDoInfDao.updateTPOSendDoInfByCwbs(cwbParam, true);
		}if("fail".equals(type)){
			tPOSendDoInfDao.updateTPOSendDoInfByCwbs(cwbParam, false);
		}
	}
	
	@RequestMapping("/reTpoOtherOrderTrack")
	public void reTpoOtherOrderTrack(String cwbs, String type, String md5, String requesttime){
		if(!validate(cwbs, requesttime, md5)){
			logger.info("dmp与oms通讯签名不对");
			return;
		}
		String cwbParam=getStringResult(cwbs);
		if("all".equals(type)){
			orderTrackToTPSDAO.updateTpoOtherOrderTrackByCwbs(cwbParam, true);
		}if("fail".equals(type)){
			orderTrackToTPSDAO.updateTpoOtherOrderTrackByCwbs(cwbParam, false);
		}
	}
	
	@RequestMapping("/reOpsSendB2cData")
	public void reOpsSendB2cData(String cwbs, String type, String md5, String requesttime){
		if(!validate(cwbs, requesttime, md5)){
			logger.info("dmp与oms通讯签名不对");
			return;
		}
		String cwbParam=getStringResult(cwbs);
		if("all".equals(type)){
			b2CDataDAO.updateOpsSendB2cDataByCwbs(cwbParam, true);
		}if("fail".equals(type)){
			b2CDataDAO.updateOpsSendB2cDataByCwbs(cwbParam, false);
		}
	}
}
