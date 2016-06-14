package cn.explink.b2c.zhemeng.track;

import java.io.IOException;
import java.util.HashMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.tools.B2cDataOrderFlowDetail;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.User;
import cn.explink.jms.dto.DmpCwbOrder;
import cn.explink.jms.dto.DmpDeliveryState;
import cn.explink.jms.dto.DmpOrderFlow;
import cn.explink.util.DateTimeUtil;
/**
 * 构建哲盟_轨迹 的数据实体
 * @author yurong.liang 2016-05-30
 */
@Service
public class BulidZhemengTrackB2cData {
	private Logger logger = LoggerFactory.getLogger(BulidZhemengTrackB2cData.class);
	@Autowired
	B2cDataOrderFlowDetail orderFlowDetail;

	@Autowired
	GetDmpDAO getDmpdao;
	@Autowired
	ZhemengTrackService zhemengService;

	public String buildZhemengMethod(DmpOrderFlow orderFlow, long flowOrdertype, DmpCwbOrder cwbOrder, long delivery_state, DmpDeliveryState dmpDeliveryState, ObjectMapper objectMapper)
			throws IOException, JsonGenerationException, JsonMappingException {

		String flowStatus = zhemengService.getFlowEnum(flowOrdertype, delivery_state);
		if (flowStatus == null) {
			logger.info("订单号：{} 不属于[zhemeng]所需要的json---,状态{}，return", cwbOrder.getCwb(), flowOrdertype);
			return null;
		}
		ZhemengTrack dms = zhemengService.getZheMeng(B2cEnum.ZheMeng_track.getKey());
		HashMap<String, String> tmsServiceCode = getTmsServiceCode(dms);
		Branch branch=getDmpdao.getNowBranch(orderFlow.getBranchid());
		User user = getDmpdao.getUserById(orderFlow.getUserid());
		
		ZhemengTrackXMLNote note = new ZhemengTrackXMLNote();
		note.setOrder_code(cwbOrder.getCwb());
		note.setTms_service_code(tmsServiceCode.get(cwbOrder.getCustomerid()+""));
		note.setOperator(user.getRealname());
		note.setOperator_date(DateTimeUtil.formatDate(orderFlow.getCredate()));
		note.setStatus(flowStatus);
		note.setScanstano(branch.getBranchname());
		note.setCtrname(user.getRealname());
		note.setContent(orderFlowDetail.getDetail(orderFlow));
		note.setRemark(cwbOrder.getCwb());
		return objectMapper.writeValueAsString(note);
	}
	
	//构造 承运商code与客户ID的关系
	private  HashMap<String, String> getTmsServiceCode(ZhemengTrack dms){
		try {
			HashMap<String, String> tmsServiceCode=new HashMap<String, String>();
			String[] customerids = dms.getCustomerid().split(",");
			String[] tms_service_codes = dms.getTms_service_code().split(",");
			
			if(tms_service_codes.length>1){
				for (int i=0;i<customerids.length;i++) {
					tmsServiceCode.put(customerids[i], tms_service_codes[i]);
				}
			}else{
				for (int i=0;i<customerids.length;i++) {
					tmsServiceCode.put(customerids[i], tms_service_codes[0]);
				}
			}
			return tmsServiceCode;
		}catch (Exception e) {
			logger.error("获取承运商code和供货商ID的对应关系出现异常,请检查接口配置信息！"+e.toString());
		}
		
		return new HashMap<String, String>();
	}
}
