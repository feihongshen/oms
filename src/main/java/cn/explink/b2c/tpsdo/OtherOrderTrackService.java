package cn.explink.b2c.tpsdo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.explink.b2c.tpsdo.bean.OtherOrderTrackVo;
import cn.explink.dao.CustomerDAO;
import cn.explink.domain.Customer;

@Service
public class OtherOrderTrackService {
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	CustomerDAO customerDAO;
	
	private final static String OTHER_ORDER_TRACK_SAVE_SQL="insert into express_other_order_track (cwb,floworderid,flowmsg,deliverymsg,tracktime,createtime,status) values(?,?,?,?,?,CURRENT_TIMESTAMP,?)";
	private final static String OTHER_ORDER_TRACK_QUERY_SQL="select g.transportno,t.* from express_other_order_track t inner join TPO_SEND_DO_INF g on t.cwb=g.cwb where t.status=1 and g.transportno is not null and g.transportno!='' order by t.tracktime limit 0,?";
	private final static String OTHER_ORDER_TRACK_UPDATE_SQL="update express_other_order_track set status=?,errorinfo=? where cwb=? and floworderid=? and status=1";

	private final static String OTHER_CUSTOMER_QUERY_SQL="select count(1) from express_other_customer_list where customerid=?";
			
	@Transactional
	public void saveOtherOrderTrack(OtherOrderTrackVo vo){
		this.jdbcTemplate.update(OTHER_ORDER_TRACK_SAVE_SQL, vo.getCwb(), vo.getFloworderid(),
				vo.getOrderFlowJson(), vo.getDeliveryStateJson(), vo.getTracktime(), vo.getStatus());
	}
	
	@Transactional
	public void completedTrackMsg(int status,String errorinfo,String cwb,long floworderid){
		this.jdbcTemplate.update(OTHER_ORDER_TRACK_UPDATE_SQL, status,errorinfo,cwb,floworderid);
	}
	
	@Transactional
	public List<OtherOrderTrackVo> retrieveOtherOrderTrack(int size){
		List<OtherOrderTrackVo> rowList=jdbcTemplate.query(OTHER_ORDER_TRACK_QUERY_SQL, new OtherOrderTrackVoMapper(), size);

		return rowList;
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
			vo.setTpsno(rs.getString("transportno"));
			
			return vo;
		}
		
	}
	
	@Transactional
	public boolean isOtherCustomer(long customerid){
		int cnt=jdbcTemplate.queryForInt(OTHER_CUSTOMER_QUERY_SQL,customerid);
		boolean isOther=false;
		if(cnt>0){
			isOther=true;
		}
		return isOther;
	}
	
	@Transactional
	public List<Customer> queryAllCustomers(){
		return customerDAO.getAllCustomers();
	}
	
	@Transactional
	public void saveOtherCustomerList(List<Long> custList){
		String delSql="delete from express_other_customer_list";
		this.jdbcTemplate.update(delSql);
		
		String saveSql="insert express_other_customer_list (customerid,createtime) values(?,CURRENT_TIMESTAMP)";
		for(Long cust:custList){
			if(cust==null){
				continue;
			}
			this.jdbcTemplate.update(saveSql,cust);
		}
		
	}
	
}
