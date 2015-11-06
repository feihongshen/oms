package cn.explink.b2c.weisuda;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.explink.b2c.cwbsearch.B2cDatasearchDAO;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.enumutil.pos.PosEnum;

@Controller
@RequestMapping("/weisuda")
public class WeisudaController {
	private Logger logger = LoggerFactory.getLogger(WeisudaController.class);

	@Autowired
	B2cDatasearchDAO b2cDatasearchDAO;
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	WeisudaService weisudaService;
	@Autowired
	WeisudaServiceExtends weisudaServiceExtends;
	/**
	 * 唯速达手动绑定
	 */
	@RequestMapping("/bound")
	public @ResponseBody
	String requestCwbSearch(HttpServletRequest request, HttpServletResponse response) {

		response.setCharacterEncoding("UTF-8");

		if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			this.logger.info("未开启[唯速达]查询接口");
			return "未开启[唯速达]查询接口";
		}
		this.logger.info("进入唯速达_01数据对接Controller");
		this.weisudaService.selectWeisudaCwb();

		return "";

	}

	/**
	 * 唯速达手动绑定 批量
	 */
	@RequestMapping("/bounds")
	public @ResponseBody
	String bounds(HttpServletRequest request, HttpServletResponse response) {

		response.setCharacterEncoding("UTF-8");

		this.logger.info("进入唯速达_01数据对接Controller");
		this.weisudaServiceExtends.boundDeliveryToApp();

		return "";

	}
	
	
	@RequestMapping("/update")
	public @ResponseBody
	String getUnVerifyOrders(HttpServletRequest request, HttpServletResponse response) {

		response.setCharacterEncoding("UTF-8");

		if (!this.b2ctools.isB2cOpen(PosEnum.Weisuda.getKey())) {
			this.logger.info("未开启[唯速达]查询接口");
			return "未开启[唯速达]查询接口";
		}
		this.weisudaService.getUnVerifyOrders();
		this.weisudaService.getback_getAppOrders();

		return "";

	}

	@RequestMapping("/delete")
	public @ResponseBody
	String deleteData(HttpServletRequest request, HttpServletResponse response) {

		int nums = this.weisudaService.deleteData();

		return "唯速达_删除过期订单信息,总共删除" + nums + "条记录";
	}
	@RequestMapping("/updatebranch")
	public @ResponseBody
	String updataAllBranch(HttpServletRequest request, HttpServletResponse response) {


		return "<textarea rows='60' cols='100'>"+this.weisudaService.updataAllBranch()+"</textarea>";
	}
	@RequestMapping("/updateuser")
	public @ResponseBody
	String updataAlluser(HttpServletRequest request, HttpServletResponse response) {
		return "<textarea rows='60' cols='100'>"+this.weisudaService.updataAlluser()+"</textarea>";
	}
	
	@RequestMapping("/updateuserbyId/{branchid}")
	public @ResponseBody
	String updateuserbyId(HttpServletRequest request, HttpServletResponse response,@PathVariable("branchid") long branchid) {
		return "<textarea rows='60' cols='100'>"+this.weisudaService.updataAlluserById(branchid)+"</textarea>";
	}
}
