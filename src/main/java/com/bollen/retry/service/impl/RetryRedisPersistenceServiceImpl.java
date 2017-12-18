package com.bollen.retry.service.impl;

import java.util.List;
import java.util.concurrent.DelayQueue;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.bollen.retry.config.RedisTemplateFactory;
import com.bollen.retry.model.RetryTask.AssignTask;
import com.bollen.retry.service.RetryPersistenceService;

import lombok.extern.slf4j.Slf4j;

/**
 * 持久化充实任务到Redis
 */
@Slf4j
@Service("retryRedisPersistenceService")
public class RetryRedisPersistenceServiceImpl implements RetryPersistenceService<AssignTask> {

	private final static String KEY = "CUSTOMER_RETRY_ASSIGN_KEY";

	private HashOperations<String, String, AssignTask> hashOperations;
	private final DelayQueue<AssignTask> assignMessageDelayQueue;
	private final RedisTemplateFactory<AssignTask> redisTemplateFactory;

	@Autowired
	public RetryRedisPersistenceServiceImpl(DelayQueue<AssignTask> assignMessageDelayQueue,
			RedisTemplateFactory<AssignTask> redisTemplateFactory) {
		this.assignMessageDelayQueue = assignMessageDelayQueue;
		this.redisTemplateFactory = redisTemplateFactory;
	}

	@PostConstruct
	public void init() {
		RedisTemplate<String, AssignTask> jacksonStringTemplate = redisTemplateFactory.getJacksonStringTemplate(AssignTask.class);
		this.hashOperations = jacksonStringTemplate.opsForHash();
	}

	@Override
	public void save(AssignTask task) {
		hashOperations.put(KEY, task.getUuid(), task);
	}

	@Override
	public List<AssignTask> getAll() {
		return hashOperations.values(KEY);
	}

	@Override
	public void delete(AssignTask entity) {
		hashOperations.delete(KEY, entity.getUuid());
	}

	@Override
	public Long size() {
		return hashOperations.size(KEY);
	}

	@Override
	public AssignTask take() throws InterruptedException {
		List<AssignTask> retryTasks = this.getAll();
		if (CollectionUtils.isNotEmpty(retryTasks)) {
			for (AssignTask task : retryTasks) {
				if (!assignMessageDelayQueue.contains(task)) {
					assignMessageDelayQueue.offer(task);
				}
				//延时任务过期时重新设置重试时间
				if (task.getRetryTime() < System.currentTimeMillis()) {
					log.info("Assign task {} has expired,restart to execute task", JSON.toJSONString(task));
					task.setRetryTime(System.currentTimeMillis() + task.getInterval());
					this.save(task);
				}
			}
		}
		return assignMessageDelayQueue.take();
	}
}
