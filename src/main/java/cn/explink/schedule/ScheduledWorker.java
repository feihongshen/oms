package cn.explink.schedule;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.explink.dao.ScheduledTaskDAO;
import cn.explink.domain.ScheduledTask;
import cn.explink.util.DateTimeUtil;

public abstract class ScheduledWorker implements Worker {

	private static Logger logger = LoggerFactory.getLogger(ScheduledWorker.class);

	@Autowired
	private ScheduledTaskDAO scheduledTaskDao;

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void executeTask(Task task) {
		ScheduledTask scheduledTask = null;
		try {
			scheduledTask = lockTask(task.getTaskId());
			logger.info("lock task success. task id = {} ", scheduledTask.getId());
			if (Constants.TASK_STATUS_COMPLETED == scheduledTask.getStatus()) {
				logger.info("task is already completed. id = {}, referenceId = {}", new Object[] { scheduledTask.getId(), scheduledTask.getReferenceId() });
				return;
			}
			if (DateTimeUtil.isBefore(new Date(), scheduledTask.getFireTime())) {
				logger.info("task is not readdy to run. id = {}, fireTime = {}", new Object[] { scheduledTask.getId(), scheduledTask.getFireTime() });
				return;
			}
		} catch (Exception e) {
			logger.warn("lock task failed. taskId = {}", task.getTaskId());
		}

		try {
			if (scheduledTask != null) {
				if (doJob(scheduledTask)) {
					logger.info("task success. task id = " + scheduledTask.getId());
					scheduledTask.setStatus(Constants.TASK_STATUS_COMPLETED);
					scheduledTask.setCompletedTime(new Date());
				} else {
					logger.info("task failed. task id = " + scheduledTask.getId());
					handleFailedTask(scheduledTask);
				}
				getScheduledTaskDao().save(scheduledTask);
			}
		} catch (Exception e) {
			logger.error("task exception. taskId = {}", task.getTaskId(), e);
		}
	}

	/**
	 * 处理失败的任务，默认只增加尝试次数，子类可以覆盖此方法做进一步的安排
	 * 
	 * @param scheduledTask
	 */
	protected void handleFailedTask(ScheduledTask scheduledTask) {
		scheduledTask.setTryCount(scheduledTask.getTryCount() + 1);
	}

	/**
	 * 任务对应的处理方法，由子类实现各任务的方法
	 * 
	 * @param scheduledTask
	 * @return 任务是否成功
	 * @throws Exception
	 */
	protected abstract boolean doJob(ScheduledTask scheduledTask) throws Exception;

	private ScheduledTask lockTask(Long taskId) {
		return scheduledTaskDao.lock(taskId);
	}

	/**
	 * for some duplicate tasks, we need scheduled the next task after a task
	 * finished.
	 * 
	 * @param currentTask
	 * @param nextJobTime
	 */
	protected void scheduleNextTask(ScheduledTask currentTask, Date nextJobTime) {
		ScheduledTask newTask = new ScheduledTask();
		BeanUtils.copyProperties(currentTask, newTask, ScheduledTask.class);
		newTask.setId(null);
		newTask.setFireTime(nextJobTime);
		newTask.setCreatedAt(new Date());
		getScheduledTaskDao().save(newTask);
	}

	protected ScheduledTaskDAO getScheduledTaskDao() {
		return scheduledTaskDao;
	}

	protected void setScheduledTaskDao(ScheduledTaskDAO scheduledTaskDao) {
		this.scheduledTaskDao = scheduledTaskDao;
	}

}
