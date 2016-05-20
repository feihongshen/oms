package cn.explink.b2c.moonbasa;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.xfire.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.cwbsearch.B2cDatasearch;
import cn.explink.b2c.cwbsearch.B2cDatasearchDAO;
import cn.explink.b2c.gztl.GztlXmlNote;
import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.b2c.tools.WsUtil;
import cn.explink.domain.B2CData;
import cn.explink.enumutil.DeliveryStateEnum;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.DateTimeUtil;
import cn.explink.util.JsonUtil;
import net.sf.json.JSONObject;

@Service
public class MoonbasaService {
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	B2cDatasearchDAO b2cDatasearchDAO;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private B2cTools b2cTools;
	@Autowired
	private B2CDataDAO b2CDataDAO;
	
	// 获取配置信息
	public Moonbasa getMoonbasa(int key) {
		Moonbasa mbs = null;
		String objectMethod = b2ctools.getObjectMethod(key).getJoint_property();
		if (objectMethod != null) {
			JSONObject jsonObj = JSONObject.fromObject(objectMethod);
			mbs = (Moonbasa) JSONObject.toBean(jsonObj, Moonbasa.class);
		} else {
			mbs = new Moonbasa();
		}
		return mbs;
	}

	/**
	 * 梦芭莎请求接口开始
	 * 
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	@Deprecated
	public String requestCwbSearchInterface(Moonbasa mbs, String from, String to) throws Exception {

		try {
			to = to == null || to.isEmpty() ? DateTimeUtil.getNowTime() : to;

			long dataSize = b2cDatasearchDAO.getDataCountByKey(Long.valueOf(mbs.getCustomerid()), from, to);

			if (dataSize == 0) {
				return "-2";
			}

			if (dataSize > 5000) {
				return "-3";
			}

			List<B2cDatasearch> datalist = b2cDatasearchDAO.getDataListByKey(Long.valueOf(mbs.getCustomerid()), from, to);

			// 构建Xml
			StringBuffer sub = new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			sub.append("<ArrayOfDeliveryInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
			for (B2cDatasearch data : datalist) {
				sub.append("<DeliveryInfo>");
				sub.append("<DeliveryCode>" + data.getCwb() + "</DeliveryCode>");
				sub.append("<Status>" + getStatus(data.getFlowordertype(), data.getContent()) + "</Status>");
				sub.append("<Desc>" + data.getContent() + "</Desc>");
				sub.append("<SignDate>" + getSignDate(data.getCretime(), data.getDeliverystate()) + "</SignDate>");
				sub.append("<Subscriber>" + getSignMan(data.getSignname(), data.getDeliverystate()) + "</Subscriber>");
				sub.append("<Carrier>" + data.getOperatorname() + "</Carrier>");
				sub.append("<Phone>" + data.getMobilephone() + "</Phone>");
				sub.append("</DeliveryInfo>");
			}

			sub.append("</ArrayOfDeliveryInfo>");
			return sub.toString();

		} catch (Exception e) {
			String error = "处理[梦芭莎]查询请求发生未知异常:" + e.getMessage();
			logger.error(error, e);
			return "-2";
		}

	}

	private String getStatus(long flowordertype, String deliverystate) {
		if((flowordertype == FlowOrderTypeEnum.YiShenHe.getValue() && deliverystate.indexOf("配送成功")>=0)
				||flowordertype == FlowOrderTypeEnum.GongHuoShangTuiHuoChenggong.getValue()) {
			return "1";
		} else if((flowordertype == FlowOrderTypeEnum.YiShenHe.getValue() && deliverystate.indexOf("配送失败")>=0)) {
			return "0";
		} else {
			return "2";
		}
		
		/*if (flowordertype == FlowOrderTypeEnum.FenZhanLingHuo.getValue()) {
			return "2";
		}
		if (deliverystate == DeliveryStateEnum.PeiSongChengGong.getValue() || deliverystate == DeliveryStateEnum.ShangMenHuanChengGong.getValue()
				|| deliverystate == DeliveryStateEnum.ShangMenTuiChengGong.getValue()) {
			return "1";
		}
		if (deliverystate == DeliveryStateEnum.FenZhanZhiLiu.getValue()) {
			return "3";
		}
		if (deliverystate == DeliveryStateEnum.QuanBuTuiHuo.getValue() || deliverystate == DeliveryStateEnum.BuFenTuiHuo.getValue()) {
			return "0";
		}
		return "1";*/
	}

	private String getSignDate(String datetime, long deliverystate) {
		if (deliverystate == DeliveryStateEnum.PeiSongChengGong.getValue() || deliverystate == DeliveryStateEnum.ShangMenHuanChengGong.getValue()
				|| deliverystate == DeliveryStateEnum.ShangMenTuiChengGong.getValue()) {
			return datetime;
		}

		return "";
	}

	private String getSignMan(String signName, long deliverystate) {
		if (deliverystate == DeliveryStateEnum.PeiSongChengGong.getValue() || deliverystate == DeliveryStateEnum.ShangMenHuanChengGong.getValue()
				|| deliverystate == DeliveryStateEnum.ShangMenTuiChengGong.getValue()) {
			return signName;
		}

		return "";
	}
	/**
	 * 
	 * @param deliveryCode
	 * @param status
	 * @param desc
	 * @param time
	 * @param signer
	 * @param deliverEmp
	 * @param deliverMobile
	 * @return
	 */
	public String transformData(String deliveryCode, String status, String desc, String time, String signer,
			  String deliverEmp, String deliverMobile)
	{
	    MBSArrayOfDeliveryInfo info = new MBSArrayOfDeliveryInfo();
	    MBSDeliveryInfo in = new MBSDeliveryInfo();
//	    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//	    String time = format.format(obj.getOpTm());
	    String _signer = signer == null ? "本人" : signer; 
	    in.setDeliveryCode(deliveryCode);
	    in.setStatus(status);
	    in.setDesc(desc );
	    in.setSignDate(time);
	    in.setSubscriber(_signer);
	    in.setCarrier(deliverEmp );
	    in.setPhone(deliverMobile );
	    info.setDeliveryInfo(in);

	    String StrXml = WsUtil.getXmlStrWithBlank(info);
	    if (logger.isDebugEnabled())
	    {
	      logger.debug("-----------------梦芭莎发送Str报文：" + StrXml);
	    }
	    return StrXml;
	}
	
    /**返回值说明: <br>
	   1	成功<br>
	  -1	用户名或密码错误<br>
	  -2	xml格式错误<br>
	  -3	异常<br>
	  -4	登陆过多次<br>
	  -5	传入参数不正确<br>
	  -6	无上传数据<br>
	  -7	xml格式错误2<br>
	  -8	记录数大于5000条<br>
	  -9	全部包裹均无效<br>
	*/
	public String sendData(Moonbasa moonbasa, String xmlStr)
	  {
	    String url = moonbasa.getSearch_url();
	    String user = moonbasa.getCustcode();
	    String password = moonbasa.getPwd();
	    Object[] result1 = (Object[])null;
	    Client client = null;
	    try {
	      client = new Client(new URL(url));
	      result1 = client.invoke("UploadData", new Object[] { user, password, xmlStr });
	    } catch (MalformedURLException e) {
	      logger.error(e.getMessage(), e);
	    } catch (Exception e) {
	      logger.error(e.getMessage(), e);
	    }
	    if (logger.isDebugEnabled())
	    {
	      logger.debug("-----------------梦芭莎反馈回来的Str报文：" + (Integer)result1[0]);
	    }

	    return Integer.toString(((Integer)result1[0]).intValue());
	}
	
	/**
	 * 
	 * @param cwbs  为NULL时是自动抓数据推送，非NULL时是手动推
	 * @param send_b2c_flag
	 * @return
	 */
	public int sendByCwbs(String cwbs, long send_b2c_flag) {
		Moonbasa mbs = getMoonbasa(B2cEnum.Moonbasa.getKey());
		if (!b2cTools.isB2cOpen(B2cEnum.Moonbasa.getKey())) {
			logger.info("未开[梦芭莎]状态反馈对接!");
			return 0;
		}
		Set<String> successOrders = new HashSet<String>();
		
		List<B2CData> datalist; 
		if(cwbs==null){
			datalist = b2CDataDAO.getDataListByFlowStatus(mbs.getCustomerid(), 5000);
		} else {
			datalist = b2CDataDAO.getDataListByCwb(cwbs, send_b2c_flag);
		}
		if (datalist == null || datalist.size() == 0) {
			logger.info("当前没有推送给[梦芭莎]的订单数据");
			return 0;
		} else {
			try {
				int i = 0;
				for (B2CData b2cData : datalist) { //单条发送，以免互相影响
					if(successOrders.contains(b2cData.getCwb())) {
						b2CDataDAO.updateB2cIdSQLResponseStatus(b2cData.getB2cid(),1,"前面已发送最终状态");
						continue;
					}
					GztlXmlNote jsonContent = JsonUtil.readValue(b2cData.getJsoncontent(), GztlXmlNote.class);
					String phone = (""+jsonContent.getReturnStatedesc()).replaceAll( "([\u4e00-\u9fa5\\s\\S]*电话\\[)([0-9]+[\\s\\S]+)(\\][\u4e00-\u9fa5\\s\\S]*)" , "$2" );
					if(phone.length()>20 || !phone.matches("[0-9]+[\\s\\S]+")) phone="";
					String status = getStatus(b2cData.getFlowordertype(), jsonContent.getState()); 
					String xml = transformData(b2cData.getCwb(), status, 
							jsonContent.getReturnStatedesc(),jsonContent.getOpDt(), jsonContent.getSignname(), jsonContent.getEmp(), 
							phone );
					String pram = xml.replaceAll("null", "");
					logger.info("当前推送给[梦芭莎]的订单数据,pram：{}", pram);
					String result = sendData(mbs, xml);
					logger.info("当前推送给[梦芭莎]订单信息={},当前[梦芭莎]返回 xml={}", b2cData.getCwb(), result);
					b2CDataDAO.updateB2cIdSQLResponseStatus(b2cData.getB2cid(), "1".equals(result)?1:2, result);
					if(status.equals("1"))successOrders.add(b2cData.getCwb());//已经发送1（配送成功/供货商退货成功）的，后面就不要发了
					i++;
				}
				return i;
			} catch (Exception e) {
				logger.error("[梦芭莎]状态反馈发生未知异常", e);
				return 0;
			}
		}
	}

	/**
	 * 反馈[梦芭莎]订单信息
	 */
	public void feedback_status() {

		if (!b2ctools.isB2cOpen(B2cEnum.Moonbasa.getKey())) {
			logger.info("未开梦芭莎的对接!");
			return;
		} 
		sendByCwbs(null,  B2cEnum.Moonbasa.getKey() );
	}
	
	public static void main(String...s) throws MalformedURLException, Exception{
		
		Client client = new Client(new URL("http://api.moonbasa.com:10000/Delivery.asmx?WSDL"));
		Object[]  result1 = client.invoke("UploadData", new Object[] { "GZTL", "moonbasa123",//"GZTL001", 
		 "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ArrayOfDeliveryInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><DeliveryInfo><DeliveryCode>1P33406161</DeliveryCode><Status>2</Status><Desc>货物已到[人和站]，联系方式[13032467283]</Desc><SignDate>2016-04-21 14:23:17</SignDate><Subscriber></Subscriber><Carrier>M1姜山</Carrier><Phone>13032467283</Phone></DeliveryInfo></ArrayOfDeliveryInfo>"
		});
		//"<?xml version=\"1.0\" encoding=\"UTF-8\"?><ArrayOfDeliveryInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><DeliveryInfo><DeliveryCode>2P33268977</DeliveryCode><Status>1</Status><Desc>货物已到 ，联系方式 </Desc><SignDate>2016-4-21 14:23:17</SignDate><Subscriber>不知道</Subscriber><Carrier>M1姜山</Carrier><Phone>123</Phone></DeliveryInfo></ArrayOfDeliveryInfo>" });
		//
		System.out.println((result1[0].toString()).replaceAll( "([\u4e00-\u9fa5\\s\\S]*电话\\[)([\\s\\S]+)(\\][\u4e00-\u9fa5\\s\\S]*)" , "$2" ));
		System.out.println(("货物已由[员村站]的小件员[G4陈清华]反馈为[配送成功],电话[13751818887]").replaceAll( "([\u4e00-\u9fa5\\s\\S]*电话\\[)([0-9]+[\\s\\S]+)(\\][\u4e00-\u9fa5\\s\\S]*)" , "$2" ));
		System.out.println(("货物由[梅州站]的派件员[P1何志勇]正在派件..电话[13430151547]").replaceAll( "([\u4e00-\u9fa5\\s\\S]*电话\\[)([0-9]+[\\s\\S]+)(\\][\u4e00-\u9fa5\\s\\S]*)" , "$2" ));
		System.out.println(("137-6332-1111").matches("[0-9]+[\\s\\S]+") );
	}
}
