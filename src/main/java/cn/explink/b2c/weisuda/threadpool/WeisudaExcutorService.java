package cn.explink.b2c.weisuda.threadpool;

import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.explink.b2c.weisuda.Weisuda;
import cn.explink.b2c.weisuda.xml.GetUnVerifyOrders_back_Item;
import cn.explink.dao.GetDmpDAO;
import cn.explink.dao.WeisudaDAO;

/**
 * 系统之间的对接（上游） 接收下游状态回传
 *  启用多线程模式执行 
 * @author Administrator
 *
 */

@Service
public class WeisudaExcutorService {
	private Logger logger = LoggerFactory.getLogger(WeisudaExcutorService.class);

	

	/**
	 * 执行每个具体的承运商业务
	 * @param com
	 */
	public void excuteRequestDmp(List<GetUnVerifyOrders_back_Item> items,WeisudaDAO weisudaDAO,Weisuda weisuda,GetDmpDAO getDmpDAO) {
			
		 ExecutorService executor = Executors.newCachedThreadPool();
		 int threadCounts=10;
		 int totalSize=items.size();
			
		 int len=totalSize/threadCounts;//平均分割List   
         if(len==0){   
            threadCounts=totalSize;//采用一个线程处理List中的一个元素   
            len=totalSize/(threadCounts==0?1:threadCounts);//重新平均分割List   
         }   
         //CyclicBarrier  barrier=new CyclicBarrier(threadCounts+1);   
         for(int i=0;i<threadCounts;i++){     //创建线程任务   
        	 
        	List<GetUnVerifyOrders_back_Item> sublist = null;
        	 
            if(i==threadCounts-1){ //最后一个线程承担剩下的所有元素的计算   
            	sublist = items.subList(i*len,totalSize);
            }else{   
                sublist = items.subList(i*len, len*(i+1)>totalSize?totalSize:len*(i+1));
            }   
            
            executor.execute(new SubExcuteWeisudaTask(sublist,weisudaDAO,weisuda,getDmpDAO));   
         }
         
         try {   
            // barrier.await();//关键，使该线程在障栅处等待，直到所有的线程都到达障栅处   
         } catch (Exception e) {   
        	 logger.error("多线程执行异常:"+Thread.currentThread().getName(),e);
         }  
         executor.shutdown();   
				
				
		

	}



}
