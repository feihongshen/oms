package cn.explink.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;



import cn.explink.b2c.tpsdo.TPOSendDoInfService;

@Controller
@RequestMapping("/thirdParty")
public class TPOSendDoInfController {
	@Autowired
	TPOSendDoInfService tPOSendDoInfService;
	
	
	/**
	 * 定时任务管理列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping("/genThirdPartyINF")
	public ModelAndView index(HttpServletRequest request) {
		return new ModelAndView("thirdpartyorder/genTPOInfRcd");
	}
	/**
	 * 插入指定订单到外单接口表
	 * @param jobName
	 * @since 1.0
	 */
	@RequestMapping(value = "/insertToInfTable", method = RequestMethod.POST)
	@ResponseBody
	public String insert2Inf(@RequestParam("cwbs") String cwbs, @RequestParam("acceptDept") String acceptDept) {
		if(StringUtils.isBlank(cwbs) || StringUtils.isBlank(acceptDept)){
			return "订单号和接单机构必填";
		}
		try{
			return this.tPOSendDoInfService.genExpThirdPartyInfRec(cwbs, acceptDept);
		}catch(Exception e){
			e.printStackTrace();
			return "系统异常";
		}
	}
}
