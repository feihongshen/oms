package cn.explink.b2c.liantongordercenter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.explink.b2c.cwbsearch.B2cDatasearchDAO;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;

@Controller
@RequestMapping("/liantongOrderCenter")
public class LianTongOrderCenterController {
	private Logger logger = LoggerFactory.getLogger(LianTongOrderCenterController.class);
	@Autowired
	LianTongOrderCenterService lianTongSCService;
	@Autowired
	B2cDatasearchDAO b2cDatasearchDAO;
	@Autowired
	private B2cTools b2ctools;

	@RequestMapping("/")
	public @ResponseBody String requestCwbSearch(HttpServletRequest request, HttpServletResponse response) {
		try {

			response.setCharacterEncoding("UTF-8");

			if (!this.b2ctools.isB2cOpen(B2cEnum.LianTongOrderCenter.getKey())) {
				return "未开启[联通]查询接口";
			}
			LianTongOrderCenter lt = this.lianTongSCService.getLiantong(B2cEnum.LianTongOrderCenter.getKey());

			String content = request.getParameter("xml");
			String verifyCode = request.getParameter("verifyCode");

			this.logger.info("[联通商城]请求跟踪信息content={}", content);

			String responseXML = this.lianTongSCService.requestCwbSearchInterface(lt, content, verifyCode);

			this.logger.info("返回联通信息={}", responseXML);

			return responseXML;
		} catch (Exception e) {
			this.logger.error("联通处理业务逻辑异常！", e);
			return "处理业务逻辑异常";
		}
	}
}
