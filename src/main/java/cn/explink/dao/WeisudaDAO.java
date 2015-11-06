package cn.explink.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import cn.explink.b2c.weisuda.WeisudaCwb;
import cn.explink.util.StringUtil;

@Component
public class WeisudaDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final class WSMapper implements RowMapper<WeisudaCwb> {
		@Override
		public WeisudaCwb mapRow(ResultSet rs, int rowNum) throws SQLException {
			WeisudaCwb weisudaCwb = new WeisudaCwb();
			weisudaCwb.setId(StringUtil.nullConvertToEmptyString(rs.getString("id")));
			weisudaCwb.setCwb(StringUtil.nullConvertToEmptyString(rs.getString("cwb")));
			weisudaCwb.setCwbordertypeid(Integer.parseInt(StringUtil.nullConvertToEmptyString(rs.getString("cwbordertypeid"))));
			weisudaCwb.setBound_time(StringUtil.nullConvertToEmptyString(rs.getString("bound_time")));
			weisudaCwb.setCourier_code(StringUtil.nullConvertToEmptyString(rs.getString("courier_code")));
			weisudaCwb.setIsqianshou(StringUtil.nullConvertToEmptyString(rs.getString("isqianshou")));
			weisudaCwb.setIstuisong(StringUtil.nullConvertToEmptyString(rs.getString("istuisong")));
			weisudaCwb.setOperationTime(StringUtil.nullConvertToEmptyString(rs.getString("operationTime")));
			return weisudaCwb;
		}

	}

	public void insertWeisuda(WeisudaCwb weisudaCwb) {
		this.jdbcTemplate.update("INSERT INTO express_b2cdata_weisuda (cwb,cwbordertypeid,courier_code,operationTime)" + " VALUES(?,?,?,?)", weisudaCwb.getCwb(), weisudaCwb.getCwbordertypeid(),
				weisudaCwb.getCourier_code(), weisudaCwb.getOperationTime());
	}

	public List<WeisudaCwb> getWeisudaCwb(String istuisong) {
		try {
			return this.jdbcTemplate.query("select * from express_b2cdata_weisuda  where istuisong=? limit 0,500", new WSMapper(), istuisong);
		} catch (Exception e) {
			return null;
		}
	}
	
	public List<WeisudaCwb> getBoundWeisudaCwbs(String istuisong,long cwbordertypeid,int maxcount) {
		try {
			return this.jdbcTemplate.query("select * from express_b2cdata_weisuda  where istuisong=? and cwbordertypeid=?  limit 0,"+maxcount, new WSMapper(), istuisong,cwbordertypeid);
		} catch (Exception e) {
			return null;
		}
	}

	public WeisudaCwb getWeisudaCwbIstuisong(String cwb) {
		try {

			return this.jdbcTemplate.queryForObject("select * from express_b2cdata_weisuda  where cwb=?  limit 1", new WSMapper(), cwb);

		} catch (Exception e) {
			return null;
		}
	}

	public WeisudaCwb getWeisudaCwb(String cwb, String orderTime) {
		try {

			return this.jdbcTemplate.queryForObject("select * from express_b2cdata_weisuda  where cwb=? and operationTime=? limit 1", new WSMapper(), cwb, orderTime);

		} catch (Exception e) {
			return null;
		}
	}

	public WeisudaCwb getWeisudaCwbByOrder(String cwb) {
		try {

			return this.jdbcTemplate.queryForObject("select * from express_b2cdata_weisuda  where cwb=?  ORDER BY operationTime desc limit 1", new WSMapper(), cwb);

		} catch (Exception e) {
			return null;
		}
	}

	public void updateWeisuda(String cwb, String flag, String remark) {

		this.jdbcTemplate.update("update express_b2cdata_weisuda set remark='" + remark + "',istuisong='" + flag + "',bound_time=NOW() where cwb='" + cwb + "'");
	}

	public void updateBoundState(String cwbs, String flag,String remark) {

		this.jdbcTemplate.update("update express_b2cdata_weisuda set remark='"+remark+"',istuisong='" + flag + "',bound_time=NOW() where cwb in ("+cwbs+") ");
	}
	
	
	public void updataWeisudaCwbIsqianshou(String cwb, String flag, String remark) {
		this.jdbcTemplate.update("update express_b2cdata_weisuda set isqianshou='" + flag + "' ,remark='" + remark + "' where  cwb='" + cwb + "'");

	}

	public WeisudaCwb getWeisudaCwbByOrderAndIsTuisong(String cwb, int istuisong) {
		try {

			return this.jdbcTemplate.queryForObject("select * from express_b2cdata_weisuda  where cwb=? and istuisong=?  ORDER BY operationTime desc limit 1", new WSMapper(), cwb, istuisong);

		} catch (Exception e) {
			return null;
		}
	}

	public void deleteWeisudaCwbNotuisong(String cwb, String flag) {
		this.jdbcTemplate.update("delete from express_b2cdata_weisuda  where  cwb='" + cwb + "' and istuisong='" + flag + "'");

	}

	public int deleteData(String opreationTime) {
		try {
			return this.jdbcTemplate.update(" DELETE FROM express_b2cdata_weisuda WHERE  `operationTime`<='" + opreationTime + "' ");

		} catch (Exception e) {
			return 0;
		}
	}
}
