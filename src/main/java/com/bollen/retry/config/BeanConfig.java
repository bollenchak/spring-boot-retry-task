package com.bollen.retry.config;

import java.util.concurrent.DelayQueue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bollen.retry.model.RetryTask.AssignTask;

@Configuration
public class BeanConfig {
	/**
	 * 配置处理任务失败的延迟队列
	 * @return DelayQueue
	 */
	@Bean("assignMessageDelayQueue")
	DelayQueue<AssignTask> getAssignMessageDelayQueue() {
		return new DelayQueue<>();
	}
}
