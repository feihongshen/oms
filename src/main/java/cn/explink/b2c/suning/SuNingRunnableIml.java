package cn.explink.b2c.suning;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.springframework.beans.factory.annotation.Autowired;

import cn.explink.domain.B2CData;


public class SuNingRunnableIml implements Runnable{
	@Autowired
	SuNingService suNingService;
	private List<B2CData> b2cdataList;
	private String requestJsonBefore;
	private String middleJsoncontent;
	private SuNing suning; 
	private CyclicBarrier barrier=null;
	/*public void getLists(List<B2CData> b2cList,String requestJsonBefore,String middleJsoncontent,SuNing suning){
		this.b2cdataList = b2cList;
		this.requestJsonBefore = requestJsonBefore;
		this.middleJsoncontent = middleJsoncontent;
		this.suning = suning;
	}*/
	
	public SuNingRunnableIml(List<B2CData> b2cList,String requestJsonBefore,String middleJsoncontent,SuNing suning,CyclicBarrier barrier){
		this.b2cdataList = b2cList;
		this.requestJsonBefore = requestJsonBefore;
		this.middleJsoncontent = middleJsoncontent;
		this.suning = suning;
		this.barrier = barrier;
	}
	
	@Override
	public void run() {
		feedbackb2cdata(b2cdataList);
		try {
			barrier.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		}
	}

	private void feedbackb2cdata(List<B2CData> b2cLists){
		try {
			this.suNingService.justdealwith(b2cLists, requestJsonBefore, middleJsoncontent, suning);
		} catch (Exception e) {
			e.printStackTrace();
		}
	};
}
