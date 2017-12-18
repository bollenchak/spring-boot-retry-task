package com.bollen.retry.service;

import java.util.List;

import com.bollen.retry.model.RetryTask;

/**
 * 持久化重试任务接口
 * 可以持久化到redis、mysql等DB中
 */
public interface RetryPersistenceService<T extends RetryTask> {

	/**
	 *
	 * Save.
	 *
	 * @param task the task
	 */
	void save(T task);

	/**
	 *
	 * Get all list.
	 *
	 * @return the list
	 */
	List<T> getAll();

	/**
	 *
	 * Delete.
	 *
	 * @param task the task
	 */
	void delete(T task);

	/**
	 *
	 *  blocking take retry task.
	 *
	 * @return the retry task
	 * @throws InterruptedException the interrupted exception
	 */
	T take() throws InterruptedException;

	/**
	 *
	 * Size long.
	 *
	 * @return the long
	 */
	Long size();
}
