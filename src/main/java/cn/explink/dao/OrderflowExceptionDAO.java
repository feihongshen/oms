package cn.explink.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import cn.explink.domain.OrderflowException;

@Component
public class OrderflowExceptionDAO {

	private final class OrderflowExceptionRowMapper implements RowMapper<OrderflowException> {
		@Override
		public OrderflowException mapRow(ResultSet rs, int rowNum) throws SQLException {
			OrderflowException ofException = new OrderflowException();
			ofException.setId(rs.getLong("id"));
			ofException.setCwb(rs.getString("cwb"));
			ofException.setOrderflow(rs.getString("orderflow"));
			ofException.setRemarks(rs.getString("remarks"));
			ofException.setSendCount(rs.getInt("send_count"));
			ofException.setSendResult(rs.getInt("send_result"));
			ofException.setCreatedDtmLoc(rs.getDate("created_dtm_loc"));
			ofException.setUpdatedDtmLoc(rs.getTimestamp("updated_dtm_loc"));
			
			return ofException;
		}
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void create(OrderflowException ofException) {
		StringBuffer sbSql = new StringBuffer();
		sbSql.append("insert into express_ops_orderflow_exception(cwb, orderflow, send_count, send_result, remarks, created_dtm_loc, updated_dtm_loc) ");
		sbSql.append(" values(?, ?, ?, ?, ?, ?, ?)");
		
		Date currDt = new Date();
		
		jdbcTemplate.update(sbSql.toString(), ofException.getCwb(), ofException.getOrderflow(), ofException.getSendCount(), 
							ofException.getSendResult(), ofException.getRemarks(),  currDt, currDt);
	}

	public void updateResult(long id, int sendResult, String remarks) {
		StringBuffer sbSql = new StringBuffer();
		sbSql.append("update express_ops_orderflow_exception set send_count = send_count+1, send_result = ?, remarks = ?  where id=? ");
		
		jdbcTemplate.update(sbSql.toString(), sendResult, remarks, id);
	}
	
	public List<OrderflowException> getOrderflowException(String cwb) {
		String sql = "select * from express_ops_orderflow_exception where cwb=? ";

		return jdbcTemplate.query(sql, new OrderflowExceptionRowMapper(), cwb);
	}
	
	public List<OrderflowException> getOrderflowException(int sendCount, int maxCount) {
		if(sendCount <= 0){
			sendCount = 20;
		}
		
		if(maxCount <= 0){
			maxCount = 200;
		}
		
		String sql = "select * from express_ops_orderflow_exception where send_result <> 1 and send_count <=" + sendCount + " limit " + maxCount;

		return jdbcTemplate.query(sql, new OrderflowExceptionRowMapper());
	}

}
