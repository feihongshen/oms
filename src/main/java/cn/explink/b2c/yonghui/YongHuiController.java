package cn.explink.b2c.yonghui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/yongHui")
public class YongHuiController {
	@Autowired
	YongHuiServices YongHuiServices;

	@RequestMapping("/orderflow")
	public @ResponseBody
	String timmer() {
		this.YongHuiServices.feedback_status();
		return "【永辉超市】手动状态回转";
	}

}
