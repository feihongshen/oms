package cn.explink.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import utiltest.BaseTest;
import cn.explink.b2c.tpsdo.TrackSendToTPSService;

public class TraceTest extends BaseTest{
	@Autowired
	TrackSendToTPSService trackSendToTPSService;
	//调用订单临时表转主表定时器
	@Test
	public void testVipshopTimmerJob(){
		trackSendToTPSService.process();
	}
}
