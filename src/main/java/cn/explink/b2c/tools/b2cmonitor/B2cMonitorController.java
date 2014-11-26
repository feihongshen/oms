package cn.explink.b2c.tools.b2cmonitor;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cPublicService;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.B2CData;
import cn.explink.domain.Customer;
import cn.explink.domain.User;
import cn.explink.util.Page;

@Controller
@RequestMapping("/b2cjointmonitor")
public class B2cMonitorController {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	B2CDataDAO b2cJointMonitorDAO;
	@Autowired
	GetDmpDAO getDmpDAO;
	@Autowired
	B2cPublicService b2cPublicService;
	@Autowired
	B2cMonitorService b2cMonitorService;

	@RequestMapping("/list/{page}")
	public String list(@PathVariable("page") long page, Model model, @RequestParam(value = "cwb", required = false, defaultValue = "") String cwb,
			@RequestParam(value = "customerid", required = false, defaultValue = "0") String customerid, @RequestParam(value = "showflag", required = false, defaultValue = "0") long showflag) {
		if (showflag == 1) {
			if (cwb.trim().length() > 0) {
				StringBuffer str = new StringBuffer();
				String[] cwbs = cwb.trim().split("\r\n");
				List<String> cwbList = new ArrayList<String>();
				for (int i = 0; i < cwbs.length; i++) {
					if (!cwbList.contains(cwbs[i]) && cwbs[i].trim().length() > 0) {
						cwbList.add(cwbs[i]);
						str = str.append("'").append(cwbs[i]).append("',");
					}
				}
				cwb = str.substring(0, str.length() - 1);
				model.addAttribute("monitorlist", b2cJointMonitorDAO.getB2cMonitorDataListByCwb(cwb, page));
				model.addAttribute("page_obj", new Page(b2cJointMonitorDAO.getB2cMonitorDataListByCwbCount(cwb), page, Page.ONE_PAGE_NUMBER));
			} else {
				model.addAttribute("monitorlist", b2cJointMonitorDAO.selectB2cMonitorDataList(customerid, page));
				model.addAttribute("page_obj", new Page(b2cJointMonitorDAO.selectB2cMonitorDataCount(customerid), page, Page.ONE_PAGE_NUMBER));
			}
		} else {
			model.addAttribute("monitorlist", new ArrayList<B2CData>());
			model.addAttribute("page_obj", new Page(0, page, Page.ONE_PAGE_NUMBER));
		}
		model.addAttribute("customerlist", getDmpDAO.getAllCustomers());
		model.addAttribute("page", page);
		saveModel(model, customerid);
		// 保存查询条件到request
		return "b2cdj/b2cmonitorlist";
	}

	@RequestMapping("/send")
	public String send(Model model, @RequestParam(value = "customerid", required = false, defaultValue = "0") long customerid,
			@RequestParam(value = "type", required = false, defaultValue = "0") int type, HttpServletRequest request) {
		if (!"0".equals(customerid)) {
			Customer customer = getDmpDAO.getCustomer(customerid);
			User user = new User();
			try {
				String dmpid = request.getSession().getAttribute("dmpid") == null ? "" : request.getSession().getAttribute("dmpid").toString();
				user = getDmpDAO.getLogUser(dmpid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (type == 1) {// 推送包括失败的，先把失败的更新成未推送的状态
				logger.info("对接异常监控，手工推送包含推送失败的订单，供货商：{}，用户名：{}", customer.getCustomername(), user.getRealname());
				b2cJointMonitorDAO.updateFlagByCustomerid(customerid);
			} else if (type == 2) {// 修改失败推送的订单为已成功
				logger.info("对接异常监控，修改失败推送的订单为已成功，供货商：{}，用户名：{}", customer.getCustomername(), user.getRealname());
				b2cJointMonitorDAO.updateFlagAndRemarkByCustomerid(customerid);
				return list(1, model, "", customerid + "", 1);
			} else if (type == 0) {
				logger.info("对接异常监控，手工推送等待推送的订单，供货商：{}，用户名：{}", customer.getCustomername(), user.getRealname());
			}
			b2cPublicService.sendPublic(customer, customer.getB2cEnum());
		}
		// 保存查询条件到request
		return list(1, model, "", customerid + "", 1);
	}

	private void saveModel(Model model, String customerid) {
		model.addAttribute("customerid", customerid);
	}

	@RequestMapping("/setFlag/{b2cid}")
	public @ResponseBody String ExportB2cDataExptInfo(@PathVariable(value = "b2cid") long b2cid, HttpServletRequest request) {
		User user = new User();
		try {
			String dmpid = request.getSession().getAttribute("dmpid") == null ? "" : request.getSession().getAttribute("dmpid").toString();
			user = getDmpDAO.getLogUser(dmpid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("对接异常监控，手工标记为成功，b2cid：{}，用户名：{}", b2cid, user.getRealname());
		b2cJointMonitorDAO.updateFlagAndRemarkByCwb(b2cid, 3, user.getRealname() + "：标记为成功");
		return "ok";
	}

	@RequestMapping("/resetFlag/{b2cid}")
	public @ResponseBody String resetFlag(@PathVariable(value = "b2cid") long b2cid, HttpServletRequest request) {
		User user = new User();
		try {
			String dmpid = request.getSession().getAttribute("dmpid") == null ? "" : request.getSession().getAttribute("dmpid").toString();
			user = getDmpDAO.getLogUser(dmpid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("对接异常监控，手工重置，b2cid：{}，用户名：{}", b2cid, user.getRealname());
		b2cJointMonitorDAO.updateFlagAndRemarkByCwb(b2cid, 0, user.getRealname() + "：重置");
		return "ok";
	}

	/**
	 * 按照订单 重置订单推送状态为 未推送
	 * 
	 * @param cwbs
	 */
	@RequestMapping("/reset")
	public @ResponseBody String reset(@RequestParam(value = "cwbs", defaultValue = "") String cwbs, HttpServletRequest request) {
		if (cwbs.trim().length() > 0) {
			User user = new User();
			try {
				String dmpid = request.getSession().getAttribute("dmpid") == null ? "" : request.getSession().getAttribute("dmpid").toString();
				user = getDmpDAO.getLogUser(dmpid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("对接异常监控，手工批量重置，cwbs：{}，用户名：{}", cwbs, user.getRealname());
			StringBuffer str = new StringBuffer();
			String[] cwb = cwbs.trim().split("\n");
			List<String> cwbList = new ArrayList<String>();
			for (int i = 0; i < cwb.length; i++) {
				if (!cwbList.contains(cwb[i]) && cwb[i].trim().length() > 0) {
					cwbList.add(cwb[i]);
					str = str.append("'").append(cwb[i]).append("',");
				}
			}
			cwbs = str.substring(0, str.length() - 1);
			b2cJointMonitorDAO.updateSendB2cFlag(cwbs);
			return "{\"errorCode\":0,\"error\":\"操作成功\"}";
		}
		return "{\"errorCode\":1,\"error\":\"操作失败\"}";
	}

	@RequestMapping("/export")
	public void ExportB2cDataExptInfo(@RequestParam(value = "customerid", required = false, defaultValue = "0") String customerid, HttpServletResponse response, HttpServletRequest request) {
		try {
			String dmpid = request.getSession().getAttribute("dmpid") == null ? "" : request.getSession().getAttribute("dmpid").toString();
			User user = getDmpDAO.getLogUser(dmpid);
			long count = b2cJointMonitorDAO.selectB2cMonitorDataCount(customerid);
			logger.info("对接异常监控，导出数据，用户名：{},单数：{}", user.getRealname(), count);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		b2cMonitorService.exportB2cDataExptInfo(customerid, response);
	}
}
