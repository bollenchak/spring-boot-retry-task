package com.bollen.retry.business.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

import com.alibaba.fastjson.JSON;
import com.bollen.retry.business.CustomerAssignService;
import com.bollen.retry.config.AsyncConfig;
import com.bollen.retry.model.RetryTask.AssignTask;
import com.bollen.retry.model.RetryTask.RetryStrategy;
import com.bollen.retry.service.RetryPersistenceService;
import com.bollen.retry.service.RetryService;

import lombok.extern.slf4j.Slf4j;

/**
 * 客户派工业务实现
 */
@Slf4j
@Service
public class CustomerAssignServiceImpl implements CustomerAssignService, RetryService<AssignTask> {

	private final RetryPersistenceService<AssignTask> retryPersistenceService;

	private final AsyncConfig asyncConfig;

	@Autowired
	public CustomerAssignServiceImpl(@Qualifier("retryRedisPersistenceService") RetryPersistenceService<AssignTask> retryPersistenceService,
			AsyncConfig asyncConfig) {
		this.retryPersistenceService = retryPersistenceService;
		this.asyncConfig = asyncConfig;
	}

	@PostConstruct
	public void init() {
		List<AssignTask> tasks = retryPersistenceService.getAll();
		if (CollectionUtils.isNotEmpty(tasks)) {
			asyncConfig.getAsyncExecutor().execute(this::failedRetry);
		}
	}

	@Override
	public void assignByWxUnionId(Long customerId, String salesId, String unionId, AssignTask retryTask) {
		if (!StringUtils.isEmpty(unionId)) {
			try {
				//此处为需要重试的方法调用,例如是一个调用外部服务的rest请求

			} catch (RestClientException e) {
				if (retryTask == null) {
					retryTask = new AssignTask(unionId, customerId, salesId);
				}
				this.addRetryTask(retryTask);
			}
		}
	}

	@Override
	public void addRetryTask(AssignTask task) {
		task.refreshRetryInfo(RetryStrategy.FIBONACCI_SEQUENCE);
		if (task.getRetryCount() > AssignTask.MAX_RETRY_COUNT) {
			log.error("Retry assign task {} has retried more than MAX_RETRY_COUNT {}", JSON.toJSONString(task), AssignTask.MAX_RETRY_COUNT);
			return;
		}
		retryPersistenceService.save(task);
		this.failedRetry();
	}

	@Override
	@Async
	public void failedRetry() {
		log.info("assign request crm failed total {}", retryPersistenceService.size());
		try {
			while (retryPersistenceService.size() > 0) {
				AssignTask task = retryPersistenceService.take();
				log.info("The {}th retry assign task {}", task.getRetryCount(), JSON.toJSONString(task));
				this.assignByWxUnionId(task.getCustomerId(), task.getSalesId(), task.getUnionId(), task);
				retryPersistenceService.delete(task);
			}
		} catch (InterruptedException e) {
			log.error("take delay assign message error", e);
		}
	}
}
