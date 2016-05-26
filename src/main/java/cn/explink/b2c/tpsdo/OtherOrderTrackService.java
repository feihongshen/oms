package cn.explink.b2c.tpsdo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.explink.b2c.tpsdo.bean.OtherOrderTrackVo;
import cn.explink.dao.CustomerDAO;

@Service
public class OtherOrderTrackService {
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	CustomerDAO customerDAO;
	
	private final static String OTHER_ORDER_TRACK_SAVE_SQL="insert into tpo_other_order_track (cwb,floworderid,flowmsg,deliverymsg,tracktime,createtime,status,trytime,errinfo,flowordertype) values(?,?,?,?,?,CURRENT_TIMESTAMP,?,?,?,?)";
	private final static String OTHER_ORDER_TRACK_QUERY_SQL="select * from tpo_other_order_track where status in (1,3) and trytime<? order by tracktime limit ?";
	private final static String OTHER_ORDER_TRACK_UPDATE_SQL="update tpo_other_order_track set status=?,errinfo=?,trytime=trytime+1 where cwb=? and floworderid=?";

	private final static String OTHER_ORDER_QUERY_SQL="select cwb,transportno from tpo_send_do_inf where cwb in ";
	private final static String OTHER_ORDER_TPS_TRANSCWB_QUERY_SQL="select transportno from tpo_send_do_inf where cwb=?";

	@Transactional
	public void saveOtherOrderTrack(OtherOrderTrackVo vo){
		this.jdbcTemplate.update(OTHER_ORDER_TRACK_SAVE_SQL, vo.getCwb(), vo.getFloworderid(),
				vo.getOrderFlowJson(), vo.getDeliveryStateJson(), vo.getTracktime(), vo.getStatus(),vo.getTrytime(),vo.getErrinfo(),vo.getFlowordertype());
	}
	
	@Transactional
	public void completedTrackMsg(int status,String errorinfo,String cwb,long floworderid){
		this.jdbcTemplate.update(OTHER_ORDER_TRACK_UPDATE_SQL, status,errorinfo,cwb,floworderid);
	}
	
	@Transactional
	public List<OtherOrderTrackVo> retrieveOtherOrderTrack(int maxTry,int size){
		List<OtherOrderTrackVo> rowList=jdbcTemplate.query(OTHER_ORDER_TRACK_QUERY_SQL, new OtherOrderTrackVoMapper(), maxTry,size);

		if(rowList==null||rowList.size()<1){
			return null;
		}
		
		Map<String,String> cwbTransMap=new HashMap<String,String>();
		Map<String,String> cwbMap=new HashMap<String,String>();
		for(OtherOrderTrackVo vo:rowList){
			String cwb=vo.getCwb();
			if(!cwbMap.containsKey(cwb)){
				cwbMap.put(cwb, null);
			}
		}
		
		Iterator it=cwbMap.keySet().iterator();
		StringBuilder sb=new StringBuilder();
		int cnt=0;
		while(it.hasNext()){
			String cwb=(String) it.next();
			sb.append("'").append(cwb).append("',");
			cnt=cnt+1;
			if(cnt>=15){//
				String cwbs=sb.toString();
				cwbs=cwbs.substring(0, cwbs.length()-1);
				getCwbTransportno(cwbs,cwbTransMap);
				sb=new StringBuilder();
				cnt=0;
			}
		}
		
		if(cnt>0){
			String lastcwbs=sb.toString();
			lastcwbs=lastcwbs.substring(0, lastcwbs.length()-1);
			getCwbTransportno(lastcwbs,cwbTransMap);
		}
		
		for(OtherOrderTrackVo vo:rowList){
			String transno=cwbTransMap.get(vo.getCwb());
			if(transno!=null){
				vo.setTpsno(transno);
			}
		}
		
		return rowList;
	}
	
	public String getTpsTransportno(String cwb){
		String transportno=null;
		List<Map<String, Object>> list=jdbcTemplate.queryForList(OTHER_ORDER_TPS_TRANSCWB_QUERY_SQL,cwb);
		if(list!=null&&list.size()>0){
			Object ob=list.get(0).get("transportno");
			if(ob!=null){
				transportno=ob.toString();
			}
		}
		return transportno;
	}
	
	private void getCwbTransportno(String cwbs,Map<String,String> cwbTransMap){
		String sql=OTHER_ORDER_QUERY_SQL+"("+cwbs+") order by id";
		List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
		if(list==null||list.size()<1){
			return;
		}
		for(Map<String, Object> row:list){
			String transportno=(String) row.get("transportno");
			String cwb=(String) row.get("cwb");
			if(transportno!=null){
				transportno=transportno.trim();
			}
			if(transportno!=null&&transportno.length()>0){
				cwbTransMap.put(cwb, transportno);
			}
		}
	}
	
	private final class OtherOrderTrackVoMapper implements RowMapper<OtherOrderTrackVo> {
		@Override
		public OtherOrderTrackVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			OtherOrderTrackVo vo=new OtherOrderTrackVo();
			vo.setCwb(rs.getString("cwb"));
			vo.setTracktime(rs.getTimestamp("tracktime"));
			vo.setDeliveryStateJson(rs.getString("deliverymsg"));
			vo.setFloworderid(rs.getLong("floworderid"));
			vo.setOrderFlowJson(rs.getString("flowmsg"));
			vo.setStatus(rs.getInt("status"));
			//vo.setTpsno(rs.getString("transportno"));
			vo.setFlowordertype(rs.getLong("flowordertype"));
			
			return vo;
		}
		
	}
	
	@Transactional
	public int housekeepOtherOrderTrack(int day){
		String trackSql="delete from tpo_other_order_track where tracktime<DATE_SUB(NOW(),INTERVAL ? DAY) or status=2 or status=4";
		return jdbcTemplate.update(trackSql,day);
		
	}
	
	@Transactional
	public int housekeepOtherOrder(int day){
		String orderSql="delete from tpo_send_do_inf where create_time<DATE_SUB(NOW(),INTERVAL ? DAY)";
		return jdbcTemplate.update(orderSql,day);
		
	}
}
