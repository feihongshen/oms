package cn.explink.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping(value = "/omsJob")
public class OmsJobController{

	private Logger logger = LoggerFactory.getLogger(OmsJobController.class);
	@Autowired(required=false)
	@Qualifier("schedulerFactory") 
	private StdScheduler stdScheduler;
	
	
	/**
	 * 定时任务管理列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping("/index")
	public ModelAndView timeTask(HttpServletRequest request) {
		return new ModelAndView("omsJobIndex");
	}
			

	@RequestMapping(value = "/findJobList", method = RequestMethod.POST)
	@ResponseBody
	public void findJobList(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		DataGridReturn dgr  = new DataGridReturn();
		List<CronTriggerVo> jobList = new ArrayList<CronTriggerVo>();
		String[] triggersInGroup;
		int j;
		int k=1;

		   triggersInGroup = stdScheduler.getTriggerNames(StdScheduler.DEFAULT_GROUP);
		   for (j = 0; j < triggersInGroup.length; j++) {
			  CronTrigger cronTrigger = (CronTrigger) stdScheduler.getTrigger(triggersInGroup[j], StdScheduler.DEFAULT_GROUP);
		      if(cronTrigger == null){
		      	continue;
		      }
		      CronTriggerVo vo = new CronTriggerVo();
		      int state=stdScheduler.getTriggerState(triggersInGroup[j], StdScheduler.DEFAULT_GROUP);
		      vo.setJobId(k);
		      k=k+1;
		      vo.setJobName(triggersInGroup[j]);
		      vo.setJobDesc(cronTrigger.getDescription());
		      vo.setJobPlan(cronTrigger.getCronExpression());
		      vo.setJobPlanDesc(cronTrigger.getDescription());
		      vo.setPreviousFireTime(cronTrigger.getPreviousFireTime());
		      vo.setNextFireTime(cronTrigger.getNextFireTime());
		      
//		      Trigger.STATE_NORMAL, Trigger.STATE_PAUSED, 
//		      Trigger.STATE_COMPLETE, Trigger.STATE_ERROR, 
//		      Trigger.STATE_BLOCKED, Trigger.STATE_NONE;
		      switch (state) {
	            case Trigger.STATE_NORMAL:
	            	vo.setStatusCode(0);
	            	vo.setStatusName("正常");	       
	                break;
	            case Trigger.STATE_PAUSED:
	            	vo.setStatusCode(1);
	            	vo.setStatusName("暂停");
	                break;
	            case Trigger.STATE_COMPLETE:
	            	vo.setStatusCode(2);
	            	vo.setStatusName("完成");
	                break;
	            case Trigger.STATE_ERROR:
	            	vo.setStatusCode(3);
	            	vo.setStatusName("错误");
	                break;
	            case Trigger.STATE_BLOCKED:
	            	vo.setStatusCode(4);
	            	vo.setStatusName("阻塞");
	                break;
	            case Trigger.STATE_NONE:
	            	vo.setStatusCode(-1);// 如果不存在该trigger,获得的状态是NONE
	            	vo.setStatusName("不存在");	      
	                break;
			}
		      jobList.add(vo);
		   }

		dgr.setRows(jobList);
		dgr.setTotal(jobList.size());
		Tools.outData2Page(Tools.obj2json(dgr), response);
	}

	private String getTriggerGroupName(String triggerName) throws SchedulerException{
		String[] triggerGroups=stdScheduler.getTriggerGroupNames();;
		String[] triggersInGroup;
		int i,j;
		String groupName="";
		for (i = 0; i < triggerGroups.length; i++) {
//			   System.out.println("Group: " + triggerGroups[i] + " contains the following triggers");
			   triggersInGroup = stdScheduler.getTriggerNames(triggerGroups[i]);

			   for (j = 0; j < triggersInGroup.length; j++) {
//			      System.out.println("- " + triggersInGroup[j]);
				  if(triggerName.equals(triggersInGroup[j])){
					  groupName=triggerGroups[i];
				  }
			   }
			}
		return groupName;		
	}
	
	
	/**
	 * 开启任务
	 * 
	 * @param jobName
	 * @since 1.0
	 */
	@RequestMapping(value = "/startJob", method = RequestMethod.POST)
	@ResponseBody
	public AjaxJson startJob(@RequestParam("jobNames[]") String[] jobNames) {
		AjaxJson ajaxJson = new AjaxJson();
		ajaxJson.setStatus(true);
		try {
			for(String jobName : jobNames){
				if(!Tools.isEmpty(jobName)){
					String groupName=getTriggerGroupName(jobName);
					stdScheduler.resumeTrigger(jobName, groupName);
//					schedulerFactory.resumeTrigger(TriggerKey.triggerKey(jobName));
				}
			}
		} catch (SchedulerException e) {
			ajaxJson.setStatus(false);
			ajaxJson.setMsg("系統异常");
			e.printStackTrace();
		}
		return ajaxJson;
	}

	/**
	 * 暂停任务
	 * 
	 * @param jobName
	 * @since 1.0
	 */
	@RequestMapping(value = "/pauseJob", method = RequestMethod.POST)
	@ResponseBody
	public AjaxJson pauseJob(@RequestParam("jobNames[]") String[] jobNames) {
		AjaxJson ajaxJson = new AjaxJson();
		ajaxJson.setStatus(true);
		try {
			for(String jobName : jobNames){
				if(!Tools.isEmpty(jobName)){
					String groupName=getTriggerGroupName(jobName);
					stdScheduler.pauseTrigger(jobName, groupName);
//					schedulerFactory.pauseTrigger(TriggerKey.triggerKey(jobName));
				}
			}
		} catch (SchedulerException e) {
			ajaxJson.setStatus(false);
			ajaxJson.setMsg("系統异常");
			e.printStackTrace();
		}
		return ajaxJson;
	}

	/**
	 * 立即执行指定定时器
	 * <p>
	 * 立即执行指定定时器
	 * </p>
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @since 1.0
	 */
	@RequestMapping(value = "/executeJobNow", method = RequestMethod.POST)
	@ResponseBody
	public AjaxJson executeJobNow(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String jobName = request.getParameter("jobName");
		AjaxJson ajaxJson = new AjaxJson();
		ajaxJson.setStatus(true);
		try {
			String groupName=getTriggerGroupName(jobName);
			CronTrigger trigger = (CronTrigger)stdScheduler.getTrigger(jobName, groupName);
//			CronTrigger trigger = (CronTrigger)schedulerFactory.getTrigger(TriggerKey.triggerKey(jobName));
			if (null != trigger) {// 存在才修改
				String taskJobName=trigger.getJobName();
				String taskGroupName=trigger.getJobGroup();
				stdScheduler.triggerJob(taskJobName, taskGroupName);
//				schedulerFactory.triggerJob(trigger.getJobKey());
			}
		} catch (SchedulerException e) {
			ajaxJson.setStatus(false);
			ajaxJson.setMsg("系統异常");
			e.printStackTrace();
		}
		return ajaxJson;
	}
	
	/**
	 * 修改执行计划
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @since 1.0
	 */
	@RequestMapping(value = "/modifyCronExpression", method = RequestMethod.POST)
	@ResponseBody
	public AjaxJson modifyCronExpression  (HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String jobName = request.getParameter("jobName");
		String cronExpression = request.getParameter("cronExpression");
		AjaxJson ajaxJson = new AjaxJson();
		ajaxJson.setStatus(true);
		try {
//			stdScheduler.getTriggerState("", "");
			if(!StringUtils.isEmpty(jobName)){
//				CronTrigger trigger = (CronTrigger)schedulerFactory.getTrigger(TriggerKey.triggerKey(jobName));
//				TriggerState state = schedulerFactory.getTriggerState(TriggerKey.triggerKey(jobName));
				String groupName=getTriggerGroupName(jobName);
				CronTrigger trigger = (CronTrigger)stdScheduler.getTrigger(jobName, groupName);
				int state=stdScheduler.getTriggerState(jobName, groupName);
				String taskJobName=trigger.getJobName();
				String taskGroupName=trigger.getJobGroup();
				JobDataMap jobdata=trigger.getJobDataMap();
		
				String oldCron = trigger.getCronExpression();
				if(!oldCron.equals(cronExpression) && !StringUtils.isBlank(cronExpression)){
//					 CronTrigger newCronTrigger = TriggerBuilder.newTrigger()
//							 	.withDescription(trigger.getDescription())
//							 	.withIdentity(TriggerKey.triggerKey(jobName))
//							 	.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).forJob(trigger.getJobKey()).build();
					CronTrigger newCronTriggera=new CronTrigger();
					newCronTriggera.setName(trigger.getName());
					newCronTriggera.setDescription(trigger.getDescription());
					newCronTriggera.setCronExpression(cronExpression);
//					CronExpression cronExpressionEP=new CronExpression(cronExpression);
					newCronTriggera.setJobGroup(taskGroupName);
					newCronTriggera.setJobName(taskJobName);
					newCronTriggera.setJobDataMap(jobdata);
			
					stdScheduler.rescheduleJob(jobName, groupName, newCronTriggera);
//					schedulerFactory.rescheduleJob(TriggerKey.triggerKey(jobName), newCronTrigger);
//					if(state == TriggerState.PAUSED){
//						schedulerFactory.pauseTrigger(TriggerKey.triggerKey(jobName));
//					}
//					Trigger.STATE_PAUSED
					if(state == Trigger.STATE_PAUSED){
						stdScheduler.pauseTrigger(jobName, groupName);
					}
				}
				
			}
		} catch (Exception e) {
			ajaxJson.setStatus(false);
			ajaxJson.setMsg("系統异常");
			logger.error("modifyCronExpression  error");
			e.printStackTrace();
		}
		return ajaxJson;
	}
	
}

/**
 * CronTriggerVo
 * @author gordon.zhou
 *
 */
class CronTriggerVo{
	private int jobId;
	/**
	 * 任务名称
	 */
    private String jobName; 
    /**
     * 任务描述
     */
    private String jobDesc;
    /**
     * 执行计划
     */
    private String jobPlan;
    /**
     * 执行计划描述
     */
    private String jobPlanDesc;
    /**
     * 上次执行时间
     */
    private Date previousFireTime;
    /**
     * 下次执行时间
     */
    private Date nextFireTime;
    /**
     * 任务状态编码
     */
    private int statusCode;
    /**
     * 任务状态
     */
    private String statusName;
    
	public int getJobId() {
		return jobId;
	}
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getJobDesc() {
		return jobDesc;
	}
	public void setJobDesc(String jobDesc) {
		this.jobDesc = jobDesc;
	}
	public String getJobPlan() {
		return jobPlan;
	}
	public void setJobPlan(String jobPlan) {
		this.jobPlan = jobPlan;
	}
	public String getJobPlanDesc() {
		return jobPlanDesc;
	}
	public void setJobPlanDesc(String jobPlanDesc) {
		this.jobPlanDesc = jobPlanDesc;
	}
	public Date getPreviousFireTime() {
		return previousFireTime;
	}
	public void setPreviousFireTime(Date previousFireTime) {
		this.previousFireTime = previousFireTime;
	}
	public Date getNextFireTime() {
		return nextFireTime;
	}
	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public String getStatusName() {
		return statusName;
	}
	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
	
    
}

 class AjaxJson {
	/**
	 * 操作成功标识
	 */
	private boolean status = true;
	/**
	 * 提示信息
	 */
	private String msg = "";
	/**
	 * 动态对象
	 */
	private Object obj = null;
	/**
	 * 拓展属性
	 */
	private Map map = new HashMap();
	
	private List<Object> objList=new ArrayList<Object>();
	
	public List<Object> getObjList() {
		return objList;
	}
	public void setObjList(List<Object> objList) {
		this.objList = objList;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}
	public Map getMap() {
		return map;
	}
	public void setMap(Map map) {
		this.map = map;
	}
	
}
 
 class DataGridReturn {

		public DataGridReturn(Integer total, List rows) {
			this.total = total;
			this.rows = rows;
		}

		private Integer total;// 总记录数
		private List rows;// 每行记录
		private List footer;

		public Integer getTotal() {
			return total;
		}

		public void setTotal(Integer total) {
			this.total = total;
		}

		public List getRows() {
			return rows;
		}

		public void setRows(List rows) {
			this.rows = rows;
		}

		public List getFooter() {
			return footer;
		}

		public void setFooter(List footer) {
			this.footer = footer;
		}
		public DataGridReturn() {
			// TODO Auto-generated constructor stub
		}
	}
 
  class Tools {
	  static ObjectMapper mapper = new ObjectMapper();
		public static String obj2json(Object obj) {
			try {
				if (obj == null) {
					return "{}";
				}
				//mapper.setVisibility(JsonMethod.FIELD, Visibility.ANY);
				mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

				return mapper.writeValueAsString(obj);
			} catch (Exception e) {
				System.out.println("==>> Object to JSON occer error: " + e);
			}
			return "{}";
		}
	  
	  
	  public static void outData2Page(String s, HttpServletResponse response) throws IOException {
			outData2Page(s, response, "text/html; charset=UTF-8");
		}

		public static void outData2Page(String s, HttpServletResponse response, String contentType)
				throws IOException {
			response.setContentType(contentType);
			PrintWriter printwriter = response.getWriter();
			printwriter.print(s);
			printwriter.close();
		}
		
		public static boolean isEmpty(String source) {
			return (source == null || source.trim().equals(""));
		} 
 }
 
 
