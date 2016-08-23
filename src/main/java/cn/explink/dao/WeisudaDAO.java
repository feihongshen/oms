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
			weisudaCwb.setWaidanjson(StringUtil.nullConvertToEmptyString(rs.getString("waidanjson")));
			weisudaCwb.setOrdertype(rs.getInt("ordertype"));
			return weisudaCwb;
		}

	}

	public void insertWeisuda(WeisudaCwb weisudaCwb,int orderType ) {
		this.jdbcTemplate.update("INSERT INTO express_b2cdata_weisuda (cwb,cwbordertypeid,courier_code,operationTime,waidanjson,ordertype)" + " VALUES(?,?,?,?,?,?)", weisudaCwb.getCwb(), weisudaCwb.getCwbordertypeid(),
				weisudaCwb.getCourier_code(), weisudaCwb.getOperationTime(),weisudaCwb.getWaidanjson(),orderType);
	}
	
	//add by zhouhuan 2016-08-23
	public void insertTPSOrderToWeisuda(WeisudaCwb weisudaCwb,int orderType ) {
		this.jdbcTemplate.update("INSERT INTO express_b2cdata_weisuda (cwb,cwbordertypeid,courier_code,operationTime,waidanjson,ordertype,istuisong)" + " VALUES(?,?,?,?,?,?,1)", weisudaCwb.getCwb(), weisudaCwb.getCwbordertypeid(),
				weisudaCwb.getCourier_code(), weisudaCwb.getOperationTime(),weisudaCwb.getWaidanjson(),orderType);
	}

	public List<WeisudaCwb> getWeisudaCwb(String istuisong,int orderType) {
		try {
			return this.jdbcTemplate.query("select * from express_b2cdata_weisuda  where istuisong=? and ordertype=? limit 0,500", new WSMapper(), istuisong,orderType);
		} catch (Exception e) {
			return null;
		}
	}
	
	public List<WeisudaCwb> getBoundWeisudaCwbs(String istuisong, long cwbordertypeid, int maxcount, int orderType, String isqianshou) {
		try {
			String strSql = "select * from express_b2cdata_weisuda  where istuisong=? and cwbordertypeid=? and ordertype=? ";
			
			//Added by leoliao at 2016-05-04 加上过滤已签收的条件,即已签收的不再发小件员绑定关系给品骏达
			if(isqianshou != null && !isqianshou.trim().equals("")){
				strSql += " and isqianshou='" + isqianshou.trim() + "' ";
			}
			//Added end
			
			strSql += " limit 0,"+maxcount;
			
			return this.jdbcTemplate.query(strSql, new WSMapper(), istuisong,cwbordertypeid,orderType);
			//return this.jdbcTemplate.query("select * from express_b2cdata_weisuda  where istuisong=? and cwbordertypeid=? and ordertype=?  limit 0,"+maxcount, new WSMapper(), istuisong,cwbordertypeid,orderType);
		} catch (Exception e) {
			return null;
		}
	}
	
	public List<WeisudaCwb> getBoundWeisudaCwbsRepeat(String istuisong,long cwbordertypeid,int maxcount,int orderType) {
		try {
			return this.jdbcTemplate.query("select * from express_b2cdata_weisuda  where istuisong=? and cwbordertypeid=? and ordertype=? and sendedcount >= 1 and sendedcount <= 20  limit 0,"+maxcount, new WSMapper(), istuisong,cwbordertypeid,orderType);
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

	public WeisudaCwb getWeisudaCwb(String cwb, String orderTime,int orderType) {
		try {

			return this.jdbcTemplate.queryForObject("select * from express_b2cdata_weisuda  where cwb=? and operationTime=? and ordertype=? limit 1", new WSMapper(), cwb, orderTime,orderType);

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

		this.jdbcTemplate.update("update express_b2cdata_weisuda set remark='" + remark + "',istuisong='" + flag + "',bound_time=NOW(),sendedcount = 1 where cwb='" + cwb + "'");
	}
	
	public void updateWeisudaRepeat(String cwb, String flag, String remark) {

		this.jdbcTemplate.update("update express_b2cdata_weisuda set remark='" + remark + "',istuisong='" + flag + "',bound_time=NOW(),sendedcount = sendedcount+1 where cwb='" + cwb + "'");
	}
	public void updateWeisudawaidan(String cwb, String flag, String remark) {
		
		this.jdbcTemplate.update("update express_b2cdata_weisuda set remark=?,istuisong='" + flag + "',bound_time=NOW() where cwb='" + cwb + "'",remark);
	}

	public void updateBoundState(String cwbs, String flag,String remark) {

		this.jdbcTemplate.update("update express_b2cdata_weisuda set remark='"+remark+"',istuisong='" + flag + "',bound_time=NOW(),sendedcount = sendedcount + 1 where cwb in ("+cwbs+") ");
	}
	public void updateBoundStateRepeat(String cwbs, String flag,String remark) {

		this.jdbcTemplate.update("update express_b2cdata_weisuda set remark='"+remark+"',istuisong='" + flag + "',bound_time=NOW(),sendedcount = sendedcount + 1 where cwb in ("+cwbs+") ");
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
	
	/**
	 * 获取指定数量的发给唯速达的订单
	 * @author leo01.liao 2016-03-08
	 * @param istuisong
	 * @param orderType
	 * @param maxcount
	 * @return
	 */
	public List<WeisudaCwb> getWeisudaCwb(String istuisong,int orderType, int maxcount) {
		try {
			if(maxcount <= 0){
				maxcount = 500;
			}
			return this.jdbcTemplate.query("select * from express_b2cdata_weisuda  where istuisong=? and ordertype=? limit 0," + maxcount, new WSMapper(), istuisong,orderType);
		} catch (Exception e) {
			return null;
		}
	}
	
	
	/**
	 * 获取指定数量的重发给唯速达的订单
	 */
	public List<WeisudaCwb> getWeisudaCwbRepeat(String istuisong,int orderType, int maxcount) {
		try {
			if(maxcount <= 0){
				maxcount = 500;
			}
			return this.jdbcTemplate
					.query("select * from express_b2cdata_weisuda  where istuisong=? and ordertype=? "
							+ " and sendedcount >= 1 and sendedcount <= 20 limit 0,"
							+ maxcount, new WSMapper(), istuisong, orderType);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 新增发给唯速达的订单
	 * @author leo01.liao
	 * @param weisudaCwb
	 */
	public void creWeisuda(WeisudaCwb weisudaCwb) {
		String sql = "INSERT INTO express_b2cdata_weisuda (cwb, cwbordertypeid, courier_code, bound_time, istuisong, isqianshou, remark, operationTime, waidanjson, ordertype, sendedcount) "+
	                 " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";
		this.jdbcTemplate.update(sql, weisudaCwb.getCwb(), weisudaCwb.getCwbordertypeid(), weisudaCwb.getCourier_code(), weisudaCwb.getBound_time(), 
									  weisudaCwb.getIstuisong(), weisudaCwb.getIsqianshou(), weisudaCwb.getRemark(), weisudaCwb.getOperationTime(), 
									  weisudaCwb.getWaidanjson(), weisudaCwb.getOrdertype());
	}
	
	/**
	 * 修改签收标识
	 * @author leo01.liao
	 * @param id
	 * @param isqianshou
	 */
	public void updateIsqianshou(String id, String isqianshou, String remark) {
		this.jdbcTemplate.update("update express_b2cdata_weisuda set isqianshou=?, remark=? where id=? ", isqianshou, remark, Long.parseLong(id));
	}
	
}
