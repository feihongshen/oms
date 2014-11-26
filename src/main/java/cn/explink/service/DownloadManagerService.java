package cn.explink.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.sf.json.JSONObject;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.explink.dao.ChukuDataResultDAO;
import cn.explink.dao.DownloadManagerDAO;
import cn.explink.dao.DownloadResultDAO;
import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.ChukuDataResult;
import cn.explink.domain.Customer;
import cn.explink.domain.DownloadManager;
import cn.explink.domain.DownloadResult;
import cn.explink.domain.SystemInstall;
import cn.explink.enumutil.DownloadState;
import cn.explink.enumutil.ModelEnum;
import cn.explink.exception.ExplinkRuntimeException;
import cn.explink.schedule.Constants;
import cn.explink.util.DateDayUtil;
import cn.explink.util.JsonUtil;
import cn.explink.vo.delivery.DeliveryRateAggregation;
import cn.explink.vo.delivery.DeliveryRateRequest;

@Service
public class DownloadManagerService {
	@Autowired
	DownloadManagerDAO downloadManagerDAO;
	@Autowired
	GetDmpDAO getDmpDAO;
	@Autowired
	DataStatisticService dataStatisticService;
	@Autowired
	ChukuDataResultDAO chukuDataResultDAO;

	@Autowired
	private DownloadResultDAO downloadResultDAO;

	@Autowired
	private ScheduledTaskService scheduledTaskService;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	final Map<Long, Integer> mapA = new HashMap<Long, Integer>();

	@PostConstruct
	public void init() throws Exception {
		try {
			// 重启的tomcat时，将所有正下下载的都置成中断下载
			downloadManagerDAO.updateXiazaiToZhongzhi();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("初始化正在下载的列表出异常：", e);
			e.printStackTrace();
		}
	}

	public void saveXiazaitoMap(long id, int state) {
		mapA.put(id, state);
	}

	public void removeXiazaitoMap(long id) {
		mapA.remove(id);
	}

	public boolean checkMap(long id) {
		return (mapA != null && mapA.get(id) != null && mapA.get(id) == 0);
	}

	/**
	 * 导出任务
	 */
	public void down_task() {
		SystemInstall sysInstall = getDmpDAO.getSystemInstallByName("downThreadCount");
		if (sysInstall != null) {
			int theadCount = Integer.parseInt(sysInstall.getValue());
			int count = downloadManagerDAO.getDownloadManagerCountByState(0);
			if (count >= theadCount) {
				logger.info("下载进程已满");
				return;
			}
			List<DownloadManager> downlist = downloadManagerDAO.getDownloadManagerListByState(-1);

			if (downlist != null && downlist.size() > 0) {
				for (DownloadManager down : downlist) {
					if (mapA.get(down.getId()) != null && mapA.get(down.getId()) == 0) {
						logger.info("已导出中");
						return;
					}
					mapA.put(down.getId(), 0);
					downloadManagerDAO.updateStateById(0, down.getId(), "");// 更新下载状态为
					dataStatisticService.repeatExportExcelMethod(down);
					theadCount = Integer.parseInt(sysInstall.getValue());
					count = downloadManagerDAO.getDownloadManagerCountByState(0);
					if (count > theadCount) {
						logger.info("下载进程已满");
						return;
					}
				}
			} else {
				return;
			}
		}

	}

	/**
	 * 定时器删除文件
	 */
	public void deleteFile_Task() {
		SystemInstall sysInstall = getDmpDAO.getSystemInstallByName("fileEffectiveDay");
		int day = 7;
		if (sysInstall != null) {
			try {
				day = Integer.parseInt(sysInstall.getValue());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				day = 7;
			}
		}
		String createtime = DateDayUtil.getDateBefore(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), day) + " 00:00:00";
		List<DownloadManager> downlist = downloadManagerDAO.getAllListByCreateTime(createtime);
		if (downlist != null && downlist.size() > 0) {
			for (DownloadManager down : downlist) {
				if (down.getState() == 1 || down.getState() == 3) {// 如果导出了文件，将文件删掉
					try {
						File myFilePath = new File(down.getFileurl() + down.getFilename());
						if (myFilePath.exists()) {// 文件是否存在
							myFilePath.delete(); // 删除文件
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					downloadManagerDAO.delStateById(down.getId());
					removeXiazaitoMap(down.getId());
					logger.info("已删除文件：id:{},文件名：{}", down.getId(), down.getFilename());
				}
			}

		}
	}

	/**
	 * 库房出库汇总导出
	 * 
	 * @param down
	 */
	public void KuFangChuKuHuiZongExport(DownloadManager down) {
		JSONObject json = JSONObject.fromObject(down.getDatajson());
		// 供货商
		String customerid = json.getString("customerid");
		List<Customer> customerlist = new ArrayList<Customer>();
		// 供货商列表
		if (customerid.length() > 0) {
			customerlist = getDmpDAO.getCustomerByIds(customerid);
		} else {
			customerlist = getDmpDAO.getAllCustomers();
		}
		String nextbranchid = json.getString("nextbranchid");
		// 下一站列表
		long userid = json.getLong("userid");

		List<Branch> branchList = new ArrayList<Branch>();
		if (nextbranchid.length() > 0) {
			branchList = getDmpDAO.getBranchByBranchids(nextbranchid);
		} else {
			branchList = getDmpDAO.getBranchListByUser(userid);
		}
		// 对应下载列表的汇总表记录
		List<ChukuDataResult> chukuDataResultList = chukuDataResultDAO.getChukuDataResultByExportid(down.getId());

		// 站点和供货商对应的map
		Map<String, Long> branchAndCustomerMap = dataStatisticService.getBranchAndCustomerMap(chukuDataResultList);
		// 以供货商为key的map
		Map<Long, Long> customerAllMap = dataStatisticService.getcustomerAllMap(chukuDataResultList);
		// 以站点为key的map
		Map<Long, Long> branchAllMap = dataStatisticService.getbranchAllMap(chukuDataResultList);

		dataStatisticService.kufangchukuexportFormExcel(down.getId(), down.getFilename(), down.getFileurl(), branchList, customerlist, branchAndCustomerMap, customerAllMap, branchAllMap);
	}

	/**
	 * 列表下载请求
	 * 
	 * @param userId
	 * @param modelId
	 * @return
	 */
	public List<DownloadManager> listDownloadRequest(long userId, ModelEnum modelId) {
		return downloadManagerDAO.getDownloadManager(userId, modelId, DownloadState.values());
	}

	/**
	 * 创建下载请求
	 * 
	 * @param userId
	 * @param modelId
	 * @return
	 */
	public DownloadManager createDownloadRequest(String datajson, String filename, String cnfilename, String fileurl, ModelEnum model, long userid) {
		DownloadManager downloadManager = new DownloadManager();
		downloadManager.setCreatetime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		downloadManager.setDatajson(datajson);
		downloadManager.setFilename(filename);
		downloadManager.setCnfilename(cnfilename);
		downloadManager.setFileurl(fileurl);
		downloadManager.setModelid(model.getValue());
		downloadManager.setState(DownloadState.pending.getValue());
		downloadManager.setTimeout(20);
		downloadManager.setUserid(userid);
		long id = downloadManagerDAO.creDownloadManager(downloadManager);
		downloadManager.setId(id);
		return downloadManager;
	}

	/**
	 * 创建妥投率的查询请求
	 * 
	 * @param drRequest
	 * @param userId
	 */
	@Transactional
	public void createDeliveryRateRequest(DeliveryRateRequest drRequest, long userId) {
		String requestJson = null;
		try {
			requestJson = JsonUtil.translateToJson(drRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DownloadManager downloadManager = createDownloadRequest(requestJson, null, null, null, ModelEnum.deliveryRate, userId);
		scheduledTaskService.createScheduledTask(Constants.TASK_TYPE_DELIVERY_RATE, Constants.REFERENCE_TYPE_DOWNLOAD_MANAGER_ID, String.valueOf(downloadManager.getId()), true);
	}

	public DeliveryRateAggregation getDeliveryRateResult(Integer downloadRequestId) {
		DownloadResult downloadResult = downloadResultDAO.getDownloadResultByDownloadRequestId(downloadRequestId);
		try {
			return JsonUtil.readValue(downloadResult.getResult(), DeliveryRateAggregation.class);
		} catch (Exception e) {
			throw new ExplinkRuntimeException("read DeliveryRateAggregation from jason failed. json = " + downloadResult.getResult(), e);
		}
	}

	public void deleteDownloadManager(long userId, Long downloadRequestId) {
		// TODO verify user
		downloadManagerDAO.delStateById(downloadRequestId);
	}
}
