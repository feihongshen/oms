package cn.explink.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tpsdo.TPOSendDoInfDao;

@Service
public class TpsTransCwbService {
	
	@Autowired
	TPOSendDoInfDao tPOSendDoInfDao;
	
	public List<Map<String,String>> getTpsTransCwbByCwbs(String cwbs) {
		if (cwbs == null || cwbs.isEmpty()) {
			return null;
		}
		return tPOSendDoInfDao.getTpsTransCwbByCwbs(cwbs);
	}

}
