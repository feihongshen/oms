package cn.explink.b2c.haoyigou;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import net.sf.json.JSONObject;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.explink.b2c.dongfangcj.DongFangCJFTPUtils;
import cn.explink.b2c.haoyigou.dto.HaoYiGou;
import cn.explink.b2c.haoyigou.dto.PeisongAndTuihuoData;
import cn.explink.b2c.tools.B2CDataDAO;
import cn.explink.b2c.tools.B2cEnum;
import cn.explink.b2c.tools.B2cTools;
import cn.explink.domain.B2CData;
import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.util.DateTimeUtil;

@Service
public class HyGService {
	@Autowired
	private B2cTools b2ctools;
	@Autowired
	private B2CDataDAO b2CDataDAO;
	private Logger logger =LoggerFactory.getLogger(this.getClass());
	
	

		
	public void feedback_status(){
		//订单配送信息提交接口
		SubmitDeliveryInfo(FlowOrderTypeEnum.FenZhanLingHuo.getValue());
		SubmitDeliveryInfo(FlowOrderTypeEnum.YiShenHe.getValue());
	}
	
	//获取配置信息
		public HaoYiGou getHYGSettingMethod(int key) {
			HaoYiGou hyg = null;
			//b2ctools.getObjectMethod(key)为获取b2c 配置信息的接口
			String objectMethod=b2ctools.getObjectMethod(key).getJoint_property();
			if (objectMethod!=null) {
				JSONObject jsonObj = JSONObject.fromObject(objectMethod);
				hyg = (HaoYiGou) JSONObject.toBean(jsonObj, HaoYiGou.class);//将json对象转化为bean
			} else {
				hyg = new HaoYiGou();
			}
			return hyg;
		}
	
	
	/**
	 * 订单配送流程反馈
	 */
	public long SubmitDeliveryInfo(long flowordertype){
		
		HaoYiGou hyg=getHYGSettingMethod(B2cEnum.HaoYiGou.getKey());
		
		if (!this.b2ctools.isB2cOpen(B2cEnum.HaoYiGou.getKey())) {
			this.logger.info("未开好易购的对接");
			return -1;
		}
		
		long lines = createHYGTxtFileAndUpload(flowordertype,hyg);
		
		if(lines == 0){
			this.logger.info("当前没有需要回传给【好易购】的信息!");
		}
		else{
			this.logger.info("当前流程本次生成{}行txt文件记录~请查看！",lines);
		}
		return lines;
	}
	public long createHYGTxtFileAndUpload(long flowordertype,HaoYiGou hyg) {
		long lines = 0;
	
		String b2cids = "";
		try{
			List<B2CData> datalist = b2CDataDAO.getDataListByFlowStatus(flowordertype,hyg.getCustomerid(), hyg.getMaxcount());
			if (datalist == null || datalist.size() == 0) {
				logger.info("当前没有需要反馈给0好易购0的货态信息~");
				return 0;
			}
			long b2cdataid = datalist.get(0).getB2cid();
			String partener = hyg.getPartener(); // 好易购标识
			String filetime = DateTimeUtil.getNowTime("yyyyMMdd");
			String filename_finishps = partener+"S"+"_"+ filetime + "_" + b2cdataid+ ".txt";//配送位置
			String filename_finishth = partener+"R"+"_"+ filetime + "_" + (b2cdataid+"t")+ ".txt";//退货位置
			String uploadPath = hyg.getUploadPath();

			ifInExistsFileDirCreate(uploadPath); // 不存在则创建
			File filePS = new File(uploadPath + filename_finishps); // 创建文件txt配送
			File fileTH = new File(uploadPath + filename_finishth); // 创建文件txt退货
			if (!filePS.exists()) {
				filePS.createNewFile();
			}
			if (!fileTH.exists()) {
				fileTH.createNewFile();
			}
			OutputStreamWriter pwdcps = new OutputStreamWriter(new FileOutputStream(filePS), "UTF-8");
			OutputStreamWriter pwdcth = new OutputStreamWriter(new FileOutputStream(fileTH), "UTF-8");
			BufferedWriter pw_dcps = new BufferedWriter(pwdcps);
			BufferedWriter pw_dcth = new BufferedWriter(pwdcth);
			int peisongflag=0;
			int tuihuoflag=0;
			for (B2CData b2cData : datalist) {
				PeisongAndTuihuoData psthdata = null; 
				try{
					psthdata = getPSObjectsMethod(b2cData.getJsoncontent());
				}catch(Exception e){
					this.logger.error("【好易购】接口异常原因:{}",e);
				}
				if(psthdata != null){
					if(psthdata.getWhichone()==1){//此时抓取的为配送单
						pw_dcps.write(getPS_Strings(b2cData,hyg,psthdata).toString());
						lines ++;
						b2cids += b2cData.getB2cid() + ",";
						pw_dcps.newLine();
						peisongflag++;
					}else{  //此时抓取退货单数据
						pw_dcth.write(getTH_Strings(b2cData,hyg,psthdata).toString());
						lines ++;
						b2cids += b2cData.getB2cid() + ",";
						pw_dcth.newLine();
						tuihuoflag++;
					}
				}
			}
			
			//如果文件生成是空的，则delete
			isDelete(filePS, fileTH, pwdcps, pwdcth, pw_dcps, pw_dcth,peisongflag,tuihuoflag);
			
			
			if(lines>0){
				if(peisongflag!=0){
					pw_dcps.flush();//将缓冲区存储的数据一次性发送出去（配送）
					pw_dcps.close();//========然后关闭流======
				}
				if(tuihuoflag!=0){
					pw_dcth.flush();//将缓冲区存储的数据一次性发送出去（退货）
					pw_dcth.close();//========然后关闭流======
				}
				
				// 文件上传
				HYGFTPUtils ftp = new HYGFTPUtils(hyg.getFtp_host(), hyg.getFtp_username(), hyg.getFtp_password(), hyg.getFtp_port(), hyg.getCharencode(), false);
				ftp.uploadFileToFTPByHYG(uploadPath, hyg.getUploadPath_bak(), filename_finishps+","+filename_finishth, hyg);
				/*this.b2CDataDAO.updateMultiB2cIdSQLResponseStatus_AllSuccess(b2cids.substring(0, b2cids.length() - 1));*/
				//file.delete();//上传完成后删除所在服务器文件
			}else{
				if(peisongflag!=0){
					pw_dcps.flush();//将缓冲区存储的数据一次性发送出去（配送）
					pw_dcps.close();//========然后关闭流======
					filePS.delete();
				}
				if(tuihuoflag!=0){
					pw_dcth.flush();//将缓冲区存储的数据一次性发送出去（退货）
					pw_dcth.close();//========然后关闭流======
					fileTH.delete();
				}
				
			}
			// 修改反馈结果为成功
			if(b2cids.length()>0){
				b2CDataDAO.updateMultiB2cIdSQLResponseStatus_AllSuccess(b2cids.substring(0, b2cids.length() - 1));
				return datalist.size();
			}
		}catch(Exception e){
			this.logger.error("【好易购】处理异常:{}",e);
			if(b2cids.length()>0){
				b2CDataDAO.updateMultiB2cIdSQLResponseStatus_AllFailure(b2cids.substring(0, b2cids.length() - 1));
			}
		}
		return lines;
	}

	private void isDelete(File filePS, File fileTH, OutputStreamWriter pwdcps,
			OutputStreamWriter pwdcth, BufferedWriter pw_dcps,
			BufferedWriter pw_dcth, int peisongflag,int tuihuoflag) throws IOException {
		try {
			if(peisongflag==0){
				if(pw_dcps!=null){
					pw_dcps.flush();
					pw_dcps.close();
				}
				if(pwdcps!=null){
					pwdcps.flush();
					pwdcps.close();
				}
				if(filePS!=null){
					logger.info("删除空文件配送");
					filePS.delete(); // 删除文件
				}
			}
			
			if(tuihuoflag==0){
				
				if(pw_dcth!=null){
					pw_dcth.flush();
					pw_dcth.close();
				}
				if(pwdcth!=null){
					pwdcth.flush();
					pwdcth.close();
				}
				if(fileTH!=null){
					logger.info("删除空文件退货");
					fileTH.delete();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 不存在就创建 文件夹
	 * 
	 * @param uploadPath_bak
	 */
	private void ifInExistsFileDirCreate(String uploadPath_bak) {
		File Fupload_bak = new File(uploadPath_bak);
		if (!Fupload_bak.exists()) {
			Fupload_bak.mkdirs();
		}
	}
	//将配送信息写入txt
	private StringBuffer getPS_Strings(B2CData b2cData, HaoYiGou hyg, PeisongAndTuihuoData xmlnote) throws Exception {
		StringBuffer line = new StringBuffer("");
		line.append(getNeedZeroBackStr(hyg.getSendCode(),PSEnum.DISPATCHERID.getLength(),null));
		line.append(getNeedZeroBackStr("",PSEnum.BLANK1.getLength(),null));
		line.append(getNeedZeroBackStr(hyg.getCustomercode(),PSEnum.CUSTOMERID.getLength(),null));
		line.append(getNeedZeroBackStr(xmlnote.getShiporderno(),PSEnum.SHIPORDERNO.getLength(),null));
		line.append(getNeedZeroBackStr("",PSEnum.BLANK2.getLength(),null));
		line.append(getNeedZeroStr(xmlnote.getDeliveryorderno(),PSEnum.DELIVERYORDERNO.getLength(),null));
		line.append(getNeedZeroBackStr(xmlnote.getReceivername(), PSEnum.RECEIVERNAME.getLength(), null)); 
		line.append(getNeedZeroBackStr("",PSEnum.BLANK3.getLength(),null));
		line.append(getNeedZeroBackStr(xmlnote.getDeliverydate(), PSEnum.DELIVERYDATE.getLength(), null)); 
		line.append(getNeedZeroStr(xmlnote.getNumberofcartons(), PSEnum.NUMBEROFCARTONS.getLength(), null)); // 
		line.append(getNeedZeroBackStr(xmlnote.getDeliverystatusdescription(),PSEnum.DELIVERYSTATUSDESCRIPTION.getLength(), null)); //
		line.append(getNeedZeroBackStr("",PSEnum.BLANK4.getLength(),null));
		line.append(getNeedZeroBackStr(xmlnote.getDeliverystatus(),PSEnum.DELIVERYSTATUS.getLength(), null)); //
		line.append(getNeedZeroBackStr("",PSEnum.BLANK5.getLength(),null));
		line.append(getNeedZeroBackStr(xmlnote.getDeliveryperson(),PSEnum.deliveryperson.getLength(), null));																								// 
		line.append(getNeedZeroBackStr(xmlnote.getDeliverypersonphone(),PSEnum.Deliverypersonphone.getLength(),null)); // 
		line.append(getNeedZeroBackStr(xmlnote.getReceipttime(),PSEnum.receipttime.getLength(),null)); // 
		line.append(getNeedZeroBackStr(xmlnote.getLongitude(),PSEnum.Longitude.getLength(),null)); // 
		line.append(getNeedZeroBackStr(xmlnote.getDimensionvalue(),PSEnum.dimensionvalue.getLength(),null)); // 
		line.append(getNeedZeroBackStr(xmlnote.getUndefinedone(),PSEnum.undefinedone.getLength(),null)); // 
		line.append(getNeedZeroBackStr(xmlnote.getUndefinedtwo(),PSEnum.undefinedtwo.getLength(),null)); // 
		line.append(getNeedZeroBackStr("",PSEnum.BLANK6.getLength(),null));

		return line;
	}
	//将退货信息写入txt文件
	private StringBuffer getTH_Strings(B2CData b2cData, HaoYiGou hyg, PeisongAndTuihuoData xmlnote) throws Exception {
		StringBuffer line = new StringBuffer("");
		line.append(getNeedZeroBackStr("",THEnum.DELIVERYID.getLength(),null));
		line.append(getNeedZeroBackStr(xmlnote.getShiporderno(),THEnum.SHIPORDERNO.getLength(),null));
		line.append(getNeedZeroBackStr("",THEnum.BLANK1.getLength(),null));
		line.append(getNeedZeroBackStr(xmlnote.getCompanyid(),THEnum.COMPANYID.getLength(),null));
		line.append(getNeedZeroBackStr("",THEnum.BLANK2.getLength(),null));
		line.append(getNeedZeroStr(xmlnote.getEdidate(),THEnum.EDIDATE.getLength(),null));
		line.append(getNeedZeroBackStr(xmlnote.getNumberofcartons(), THEnum.NUMBEROFCARTONS.getLength(), null)); 
		line.append(getNeedZeroBackStr("",THEnum.BLANK3.getLength(),null));
		line.append(getNeedZeroBackStr(xmlnote.getStatusupdatedate(), THEnum.STATUSUPDATEDATE.getLength(), null)); 
		line.append(getNeedZeroBackStr("",THEnum.BLANK4.getLength(),null));
		line.append(getNeedZeroStr(xmlnote.getDeliverystatusdescription(), THEnum.DELIVERYSTATUSDESCRIPTION.getLength(), null)); // 
		line.append(getNeedZeroBackStr(xmlnote.getClosuredate(),THEnum.CLOSUREDATE.getLength(), null)); //
		line.append(getNeedZeroBackStr("",THEnum.BLANK5.getLength(),null));
		line.append(getNeedZeroBackStr(xmlnote.getDeliverystatus(),THEnum.DELIVERYSTATUS.getLength(), null)); //
		line.append(getNeedZeroBackStr(xmlnote.getPickupperson(),THEnum.pickupperson.getLength(), null));																								// 
		line.append(getNeedZeroBackStr(xmlnote.getPickuppersonphone(),THEnum.pickuppersonphone.getLength(),null)); // 
		line.append(getNeedZeroBackStr(xmlnote.getLongitude(),THEnum.longitude.getLength(),null)); // 
		line.append(getNeedZeroBackStr(xmlnote.getDimensionvalue(),THEnum.dimensionvalue.getLength(),null)); // 
		line.append(getNeedZeroBackStr(xmlnote.getReturndeliveryorderid(),THEnum.returndeliveryorderid.getLength(),null)); // 
		line.append(getNeedZeroBackStr(xmlnote.getUndefinedone(),THEnum.undefinedone.getLength(),null)); // 
		line.append(getNeedZeroBackStr(xmlnote.getUndefinedtwo(),THEnum.undefinedtwo.getLength(),null)); // 
		line.append(getNeedZeroBackStr("",PSEnum.BLANK6.getLength(),null));
		return line;
	}
	
	// 不足位数前面补0
	private String getNeedZeroStr(String str, int lennum, String feeflag) {
		String zerostr = "";
		if (feeflag != null && str.indexOf(".") > 0) {// 金额字段
			str = str.substring(0, str.indexOf("."));
		}
		str = "          " + str;
		for(int i=0;i<100;i++){
			str = "         "+ str;
			if(str.length()>lennum){
				break;
			}
		}
		zerostr = str.substring(str.length() - lennum, str.length());
		return zerostr;
	}
	
	// 不足位数后面补0 ---feeflag===标识是否为金额字段
	private String getNeedZeroBackStr(String str, int lennum, String feeflag) {
		String zerostr = "";
		if (feeflag != null && str.indexOf(".") > 0) {// 金额字段
			str = str.substring(0, str.indexOf("."));
		}
		str = str + "        ";
		for(int i=0;i<100;i++){
			str += "         "; 
			if(str.length()>lennum){
				break;
			}
		}
		zerostr = str.substring(0,lennum);
		return zerostr;
	}
	
	//测试空格长度
	public static void main(String[] args) throws Exception {
//		DongFangCJFTPUtils ftp = new DongFangCJFTPUtils("ftp.best1.com", "abc_input_qa","abc_input_qa@1", 21, "GBK", false);
		HYGFTPUtils ftp = new HYGFTPUtils("ftp.best1.com", "abc_input_qa", "abc_input_qa@1",21, "GBK", false);
		ftp.uploadFileToFTPByHYG("D:/ftp", "D:/ftpbak", "test"+","+"111", new HaoYiGou());
		
		
	}
	//解析send_b2c_data 存储字段jsoncontent 
	public PeisongAndTuihuoData getPSObjectsMethod(String jsoncontent) throws JsonParseException, JsonMappingException, IOException {
		return new ObjectMapper().readValue(jsoncontent, PeisongAndTuihuoData.class);
	}
	
}
