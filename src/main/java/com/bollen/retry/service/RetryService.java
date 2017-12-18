package com.bollen.retry.service;

import com.bollen.retry.model.RetryTask;

/**
 * 重试接口
 */
public interface RetryService<T extends RetryTask> {

	/**
	 * 添加任务
	 * @param task 任务
	 */
	void addRetryTask(T task);

	/**
	 *
	 * 失败重试
	 */
	void failedRetry();
}
