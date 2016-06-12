package cn.explink.b2c.tpsdo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import cn.explink.b2c.tpsdo.bean.TPOSendDoInf;
import cn.explink.enumutil.TPOOperateTypeEnum;

@Component
public class TPOSendDoInfDao {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final class TPOSendDoInfMapper implements RowMapper<TPOSendDoInf> {
		@Override
		public TPOSendDoInf mapRow(ResultSet rs, int rowNum) throws SQLException {
			TPOSendDoInf tPOSendDoInf = new TPOSendDoInf();
			tPOSendDoInf.setId(rs.getLong("id"));
			tPOSendDoInf.setCwb(rs.getString("cwb"));
			tPOSendDoInf.setCreateTime(rs.getDate("create_time"));
			tPOSendDoInf.setCustcode(rs.getString("custcode"));
			tPOSendDoInf.setIsSent(rs.getInt("is_sent"));
			tPOSendDoInf.setRemark(rs.getString("remark"));
			tPOSendDoInf.setReqObjJson(rs.getString("req_obj_json"));
			tPOSendDoInf.setTransportno(rs.getString("transportno"));
			tPOSendDoInf.setTrytime(rs.getInt("trytime"));
			tPOSendDoInf.setUpdateTime(rs.getDate("update_time"));
			tPOSendDoInf.setState(rs.getInt("state"));
			tPOSendDoInf.setOperateType(rs.getInt("operate_type"));
			return tPOSendDoInf;
		}

	}
	
	public void saveTPOSendDoInf(final TPOSendDoInf tPOSendDoInf) {
		String sql = "INSERT INTO tpo_send_do_inf(cwb,custcode,transportno,req_obj_json,remark,trytime,is_sent,create_time,update_time,operate_type) VALUES (?,?,?,?,?,?,?,?,?,?)";
		this.jdbcTemplate.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, tPOSendDoInf.getCwb());
				ps.setString(2, tPOSendDoInf.getCustcode());
				ps.setString(3, tPOSendDoInf.getTransportno());
				ps.setString(4, tPOSendDoInf.getReqObjJson());
				ps.setString(5, tPOSendDoInf.getRemark());
				ps.setInt(6, tPOSendDoInf.getTrytime());
				ps.setInt(7, tPOSendDoInf.getIsSent());
				ps.setString(8, sdf.format(tPOSendDoInf.getCreateTime()));
				ps.setString(9, sdf.format(tPOSendDoInf.getUpdateTime()));
				ps.setInt(10, tPOSendDoInf.getOperateType());
			}
		});
	}
	
	/**
	 * 获取没有推送DO成功的 数据
	 * @param count
	 * @param maxTrytime
	 * @return
	 */
	public List<TPOSendDoInf> getUnSentTPOSendDoInf(int count,int maxTrytime){
		String sql = "select * from tpo_send_do_inf where is_sent=0 and trytime<? order by id limit ?";
		return this.jdbcTemplate.query(sql, new TPOSendDoInfMapper(),maxTrytime, count);
	}
	
	/**
	 * 更新推送状态和推送次数和绑定TPS运单号
	 * @param cwb
	 * @param custcode
	 * @param isSent
	 */
	public void updateTPOSendDoInf(long id, String transportNo, int isSent,int trytime, String remark){
		String sql = "update tpo_send_do_inf set transportno=?,is_sent=?,trytime=?,remark=? where id=?";
		this.jdbcTemplate.update(sql, transportNo, isSent, trytime, remark, id);
	}
	
	/**
	 * 
	 * @param cwb
	 * @return
	 */
	public TPOSendDoInf getTPOSendDoInfByCwb(String cwb){
		String sql = "select * from tpo_send_do_inf where cwb=? limit 0,1";
		try{
			return this.jdbcTemplate.queryForObject(sql, new TPOSendDoInfMapper(),cwb);
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * 根据订单号，操作类型获取接口表中未失效的记录
	 * @param cwb
	 * @param operateType
	 * @return
	 */
	public TPOSendDoInf getTPOSendDoInfByCwbAndOpertype(String cwb, TPOOperateTypeEnum operateType){
		String sql = "select * from tpo_send_do_inf where cwb=? and operate_type=? and state=1 limit 0,1";
		try{
			return this.jdbcTemplate.queryForObject(sql, new TPOSendDoInfMapper(), cwb, operateType.getValue());
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * 根据id 把state 值设为 0
	 * @param id
	 */
	public void invalidateTPOSendDoInfById(long id){
		String sql = "update tpo_send_do_inf set state=0 where id=?";
		this.jdbcTemplate.update(sql, id);
	}
	
	

}
