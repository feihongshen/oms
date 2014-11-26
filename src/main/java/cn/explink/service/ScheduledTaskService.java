package cn.explink.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.explink.dao.ScheduledTaskDAO;
import cn.explink.domain.ScheduledTask;
import cn.explink.schedule.Constants;
import cn.explink.schedule.ExecutorManager;
import cn.explink.schedule.Task;
import cn.explink.schedule.Worker;

public class ScheduledTaskService {

	private static Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

	@Autowired
	private ScheduledTaskDAO scheduledTaskDAO;

	/**
	 * cache task type and worker mapping, will be injected by spring
	 */
	private Map<String, Worker> cachedWorkers;

	/**
	 * 
	 */
	public void processTasks() {
		Set<String> taskTypes = cachedWorkers.keySet();
		logger.info("begin processTasks(). all taskTypes = {}", taskTypes);
		for (String taskType : taskTypes) {
			scheduleTasks(taskType);
		}
	}

	/**
	 * process all task for given taskType
	 * 
	 * @param taskType
	 */
	public void scheduleTasks(String taskType) {
		List<Long> taskIds = scheduledTaskDAO.listAllTasksByType(taskType);
		scheduleTasks(taskType, taskIds);
	}

	/**
	 * schedule tasks
	 * 
	 * @param taskType
	 * @param taskIds
	 */
	public void scheduleTasks(String taskType, List<Long> taskIds) {
		for (Long taskId : taskIds) {
			scheduleTask(taskType, taskId);
		}
	}

	/**
	 * schedule task
	 * 
	 * @param taskType
	 * @param taskId
	 */
	public void scheduleTask(String taskType, Long taskId) {
		Task task = new Task();
		task.setTaskId(taskId);
		task.setWorker(cachedWorkers.get(taskType));

		ExecutorService executorService = findExecutorService(taskType);
		logger.debug("putting task to thread pool for taskId = {}", taskId);
		executorService.submit(task);
	}

	protected ExecutorService findExecutorService(String taskType) {
		ExecutorManager executorManager = ExecutorManager.getInstance();
		ExecutorService executorService = executorManager.getExecutorService(taskType);
		if (executorService == null) {
			executorService = executorManager.getExecutorService(Constants.COMMON_TASK_THREAD_POOL_NAME);
		}
		return executorService;
	}

	/**
	 * 根据任务内容项创建任务
	 * 
	 * @param taskType
	 *            任务类型
	 * @param refType
	 *            引用类型
	 * @param refId
	 *            引用编号
	 * @return 任务对象
	 */
	public ScheduledTask createScheduledTask(String taskType, String refType, String refId) {
		return createScheduledTask(taskType, refType, refId, false);
	}

	/**
	 * 根据任务内容项创建任务
	 * 
	 * @param taskType
	 *            任务类型
	 * @param refType
	 *            引用类型
	 * @param refId
	 *            引用编号
	 * @param immediately
	 *            是否立即调度此任务
	 * @return 任务对象
	 */
	public ScheduledTask createScheduledTask(String taskType, String refType, String refId, boolean immediately) {
		ScheduledTask newTask = new ScheduledTask();
		newTask.setTaskType(taskType);
		newTask.setStatus(Constants.TASK_STATUS_INITIALIZED);
		newTask.setReferenceType(refType);
		newTask.setReferenceId(refId);
		newTask.setFireTime(new Date());
		newTask.setCreatedAt(new Date());
		newTask.setTryCount(0);
		scheduledTaskDAO.save(newTask);

		if (immediately) {
			scheduleTask(taskType, newTask.getId());
		}
		return newTask;
	}

	public Map<String, Worker> getCachedWorkers() {
		return cachedWorkers;
	}

	public void setCachedWorkers(Map<String, Worker> cachedWorkers) {
		this.cachedWorkers = cachedWorkers;
	}

	public ScheduledTaskDAO getScheduledTaskDAO() {
		return scheduledTaskDAO;
	}

	public void setScheduledTaskDAO(ScheduledTaskDAO scheduledTaskDAO) {
		this.scheduledTaskDAO = scheduledTaskDAO;
	}

	// /**
	// * 按taskType和referenceId统计任务数
	// *
	// * @param taskType
	// * @return 任务数量（大于0表示有，等于0表示没有)
	// */
	// public int countTasksByTypeAndRefernece(String taskType, String
	// referenceId) {
	// return scheduledTaskDAO.countTasksByTypeAndRefernece(taskType,
	// referenceId);
	// }

	// /**
	// * 按taskType和referenceId 关闭任务
	// *
	// * @param taskType
	// * 任务类型
	// * @param referenceId
	// * 引用编号
	// */
	// @Override
	// public void cancelTaskByTypeAndRefernece(String taskType, String
	// referenceId) {
	// scheduledTaskDAO.cancelTaskByTypeAndRefernece(taskType, referenceId);
	// }

}
