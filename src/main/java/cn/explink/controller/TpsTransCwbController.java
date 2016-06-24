package cn.explink.controller;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.explink.service.TpsTransCwbService;


@Controller
@RequestMapping("/tpsTransCwb")
public class TpsTransCwbController {

	@Autowired
	TpsTransCwbService tpsTransCwbService;
	
	
	@RequestMapping("/getTpsTransCwb/{cwbs}")
	public @ResponseBody String getCwbDetailsByCwb(@PathVariable("cwbs") String cwbs) {
		if (cwbs == null || cwbs.isEmpty()) {
			return "";
		}
		List<Map<String,String>> list = tpsTransCwbService.getTpsTransCwbByCwbs(cwbs);
		if (list == null || list.isEmpty()) {
			return "";
		}
		JSONArray jsonarray = JSONArray.fromObject(list);  
		return jsonarray.toString();
	}
	
	
	
}
