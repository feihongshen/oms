package cn.explink.b2c.haoyigou;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/haoyigou")
public class HYGController {
	@Autowired
	HyGService hygService;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@RequestMapping("/intoFTP")
	public void intoFTP(HttpServletRequest request){
	
		//上传到FTP
		String strbegin = "请求【好易购】==FTP==开始";
		this.logger.info(strbegin);
		this.hygService.feedback_status();
		String strend = "请求【好易购】==FTP==结束";
		this.logger.info(strend);
	}
}
