package cn.explink.b2c.haoyigou;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/haoyigou")
public class HYGController {
	@Autowired
	HYGService hygService;
	@RequestMapping("/intoFTP")
	public void intoFTP(){
		//上传到FTP
		this.hygService.feedback_status();
	}
}
