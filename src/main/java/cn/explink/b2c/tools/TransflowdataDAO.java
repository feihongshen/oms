package cn.explink.b2c.tools;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.Transflowdata;

/**
 * 运单推送流程dao
 * @author zpk
 *
 */
@Component
public class TransflowdataDAO {
	@Autowired
	GetDmpDAO getDmpDAO;
	@Autowired
	JdbcTemplate  jdbcTemplate;
	
	private Logger logger = LoggerFactory.getLogger(TransflowdataDAO.class);

	private final class TransMapper implements RowMapper<Transflowdata> {
		@Override
		public Transflowdata mapRow(ResultSet rs, int rowNum) throws SQLException {
			Transflowdata flowdata = new Transflowdata();
			flowdata.setId(rs.getLong("id"));
			flowdata.setTranscwb(rs.getString("transcwb"));
			flowdata.setCwb(rs.getString("cwb"));
			flowdata.setCreatetime(rs.getString("createtime"));
			flowdata.setFlowtime(rs.getString("flowtime"));
			flowdata.setSendtime(rs.getString("sendtime"));
			flowdata.setFlowordertype(rs.getInt("flowordertype"));
			flowdata.setJsoncontent(rs.getString("jsoncontent"));
			flowdata.setSend_b2c_flag(rs.getInt("send_b2c_flag"));
			flowdata.setCustomerid(rs.getInt("customerid"));
			flowdata.setSelect_b2c_flag(rs.getInt("select_b2c_flag"));
			flowdata.setRemark(rs.getString("remark"));
			
			return flowdata;
		}
	}

	/**
	 * 能满足所有的对接插入数据的入库
	 *
	 * @param b2CData
	 */
	public void saveTransFlowData(final Transflowdata flowdata) {// tmall不为空，其他为空
		
			this.jdbcTemplate.update(
					"insert into express_send_transcwb_data(transcwb,cwb,createtime,flowtime,flowordertype,jsoncontent,customerid) " 
			+ "values(?,?,?,?,?,?,?)",
					new PreparedStatementSetter() {
						@Override
						public void setValues(PreparedStatement ps) throws SQLException {
							
							ps.setString(1,flowdata.getTranscwb());
							ps.setString(2,flowdata.getCwb());
							ps.setString(3,flowdata.getCreatetime());
							ps.setString(4,flowdata.getFlowtime());
							ps.setInt(5,flowdata.getFlowordertype());
							ps.setString(6,flowdata.getJsoncontent());
							ps.setInt(7,flowdata.getCustomerid());
						}
					});
	

	}
	
	/**
	 * 查询重发流程JMS的时候
	 *
	 * @param transcwb
	 * @param flowordertype
	 * @param flowtime
	 * @return
	 */
	public long checkIsRepeatDataFlag(String transcwb, long flowtype, String flowtime) {
		String sql = "SELECT count(1) FROM  express_send_transcwb_data where transcwb=? and flowordertype=? and flowtime=?";

		return this.jdbcTemplate.queryForLong(sql,transcwb,flowtype,flowtime);
	}

	
	public List<Transflowdata> getDataListByFlowStatus(long flowordertype, String customerids, long maxCount) {
			String sql = "select * from express_send_transcwb_data where send_b2c_flag=0  and  customerid in (" + customerids + ") and flowordertype=? "
					+ " order by flowtime LIMIT 0,?";
			try {
				return this.jdbcTemplate.query(sql, new TransMapper(),flowordertype,maxCount);
			} catch (DataAccessException e) {
				
				return null;
			}

	}
	
	public void updateTransflowStatus(long id, int send_b2c_flag, String remark)  {
		String sql = " update express_send_transcwb_data set  send_b2c_flag=?,remark=?,select_b2c_flag=select_b2c_flag+1 where  id=? ";
		this.jdbcTemplate.update(sql, send_b2c_flag, remark, id);
	}
	
}
