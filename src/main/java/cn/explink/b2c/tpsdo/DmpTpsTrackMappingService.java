package cn.explink.b2c.tpsdo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.enumutil.TPSFlowOrderTypeEnum;

@Service
public class DmpTpsTrackMappingService {

	private Map<Integer,Integer> map=null;
	public int getTpsOperateType(int dmpOperateType){
		if(map==null){
			init();
		}
		
		Integer value=map.get(dmpOperateType);
		if(value==null){
			throw new RuntimeException("没有对应的TPS操作环节映射,dmpOperateType="+dmpOperateType);
		}
		
		return value.intValue();
	}
	
	public void init(){
		map=new HashMap<Integer,Integer>();
		map.put(FlowOrderTypeEnum.RuKu.getValue(), TPSFlowOrderTypeEnum.inboundScan.getValue());
		//......
	}
}
