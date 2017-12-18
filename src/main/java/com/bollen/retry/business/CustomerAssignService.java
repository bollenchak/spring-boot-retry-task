package com.bollen.retry.business;

import com.bollen.retry.model.RetryTask.AssignTask;

/**
 * 客户派工服务
 */
public interface CustomerAssignService {

	/**
	 * 派工案例
	 * @param customerId 客户ID
	 * @param salesId 用户ID
	 * @param unionId 微信的unionId
	 * @param retryTask 重试内容
	 */
	void assignByWxUnionId(Long customerId, String salesId, String unionId, AssignTask retryTask);
}
