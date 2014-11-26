package cn.explink.schedule;

public class Constants {

	/**
	 * 任务状态 - 初始化
	 */
	public static final int TASK_STATUS_INITIALIZED = 0;

	/**
	 * 任务状态 - 已完成
	 */
	public static final int TASK_STATUS_COMPLETED = 1;

	/**
	 * 任务状态 - 已取消
	 */
	public static final int TASK_STATUS_CANCELED = 2;

	/**
	 * 执行调度任务的线程池名字
	 */
	public static final String COMMON_TASK_THREAD_POOL_NAME = "scheduledTaskThreadPool";

	/**
	 * 执行调度任务的线程池大小
	 */
	public static final int COMMON_TASK_THREAD_POOL_SIZE = 15;

	/**
	 * 任务类型 - deliveryRateTask
	 */
	public static final String TASK_TYPE_DELIVERY_RATE = "deliveryRateTask";

	/**
	 * 引用数据类型 - downloadManagerId
	 */
	public static final String REFERENCE_TYPE_DOWNLOAD_MANAGER_ID = "downloadManagerId";

}
