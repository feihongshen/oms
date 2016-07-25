package cn.explink.b2c.tpsdo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.explink.b2c.tpsdo.bean.OrderTrackToTPSVo;
import cn.explink.dao.CustomerDAO;

@Service
public class OrderTrackToTPSDAO {
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	CustomerDAO customerDAO;
	
	private final static String OTHER_ORDER_TRACK_UPDATE_SQL="update tpo_other_order_track set status=?,errinfo=?,trytime=trytime+1 where cwb=? and floworderid=?";

	private final static String OTHER_ORDER_TPS_TRANSCWB_QUERY_SQL="select transportno from tpo_send_do_inf where cwb=?";

	@Transactional
	public void saveOrderTrack(OrderTrackToTPSVo vo){
		String sql = "insert into express_ops_ordertrack_to_tps (cwb,floworderid,flowmsg,tracktime,createtime,status,trytime,errinfo,flowordertype,tpstranscwb,customerid) values(?,?,?,?,CURRENT_TIMESTAMP,?,?,?,?,?,?)";
		this.jdbcTemplate.update(sql, vo.getCwb(), vo.getFloworderid(),
				vo.getOrderFlowJson(), vo.getTracktime(), vo.getStatus(),vo.getTrytime(),vo.getErrinfo(),vo.getFlowordertype(),vo.getTpstranscwb(),vo.getCustomerid());
	}
	
	@Transactional
	public void orderTrackToTPSDAO(int status,String errorinfo,String cwb,long floworderid){
		this.jdbcTemplate.update(OTHER_ORDER_TRACK_UPDATE_SQL, status,errorinfo,cwb,floworderid);
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
	
	private final class OrderTrackToTPSVoMapper implements RowMapper<OrderTrackToTPSVo> {
		@Override
		public OrderTrackToTPSVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			OrderTrackToTPSVo vo=new OrderTrackToTPSVo();
			vo.setCwb(rs.getString("cwb"));
			vo.setTracktime(rs.getTimestamp("tracktime"));
			vo.setFloworderid(rs.getLong("floworderid"));
			vo.setOrderFlowJson(rs.getString("flowmsg"));
			vo.setStatus(rs.getInt("status"));
			vo.setTpstranscwb(rs.getString("tpstranscwb"));
			vo.setFlowordertype(rs.getLong("flowordertype"));
			vo.setCustomerid(rs.getLong("customerid"));
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

	public List<OrderTrackToTPSVo> getTrackListToSend(String customerids,int maxTry, int size) {
		String sql = "select * from express_ops_ordertrack_to_tps where customerid in ("+customerids+") and status in (1,3)  "
				+ "and trytime<? and ifnull(tpstranscwb,'')<>'' order by tracktime limit ?";
		List<OrderTrackToTPSVo> list = jdbcTemplate.query(sql, new OrderTrackToTPSVoMapper(), maxTry,size);
		return list;
	}
	
	public void completedTrackMsg(int status,String errorinfo,String cwb,long floworderid){
		String sql = "update express_ops_ordertrack_to_tps set status=?,errinfo=?,trytime=trytime+1 where cwb=? and floworderid=?";
		this.jdbcTemplate.update(sql, status,errorinfo,cwb,floworderid);
	}
	
}
