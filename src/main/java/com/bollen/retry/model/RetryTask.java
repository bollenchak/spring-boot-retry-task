package com.bollen.retry.model;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 重试任务
 * @author bolong.zhai @baidao.com
 * @since 2017 -12-14 10:59:36
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RetryTask implements Delayed, Serializable {

	private static final long serialVersionUID = -151142192882178642L;

	String uuid;
	Long interval;
	Long retryTime;
	Integer retryCount;

	RetryTask(Long interval) {
		this.uuid = UUID.randomUUID().toString();
		this.interval = interval;
		this.retryTime = System.currentTimeMillis() + interval;
		this.retryCount = 0;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(retryTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	public int compareTo(Delayed other) {
		long diff = getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);
		return (diff == 0 ? 0 : ((diff < 0) ? -1 : 1));
	}

	public void refreshRetryInfo(RetryStrategy strategy) {
		this.retryCount++;
		this.retryTime = System.currentTimeMillis() + strategy.getIntervalTime(this.interval, this.getRetryCount());
	}

	/**
	 * 重试策略
	 */
	public enum RetryStrategy {

		/**
		 * 等量间距时间
		 */
		EQUAL_INTERVAL {
			@Override
			public long getIntervalTime(long initDelayTime, int retryCount) {
				checkArguments(initDelayTime, retryCount);
				return initDelayTime;
			}
		},

		/**
		 * 斐波拉契数列
		 */
		FIBONACCI_SEQUENCE {
			@Override
			public long getIntervalTime(long initDelayTime, int retryCount) {
				checkArguments(initDelayTime, retryCount);
				long retryTime = initDelayTime;
				long previous = initDelayTime;
				long beforePrevious = initDelayTime;
				for (int i = 2; i <= retryCount; i++) {
					retryTime = beforePrevious + previous;
					beforePrevious = previous;
					previous = retryTime;
				}
				return retryTime;
			}
		},
		/**
		 * 间距时间翻倍
		 */
		DOUBLE_INTERVAL {
			@Override
			public long getIntervalTime(long initDelayTime, int retryCount) {
				checkArguments(initDelayTime, retryCount);
				return initDelayTime << retryCount;
			}
		};

		void checkArguments(long initDelayTime, int retryCount) {
			if (initDelayTime < 1 || retryCount < 1) {
				throw new IllegalArgumentException();
			}
		}

		/**
		 * 获取重试间隔时间
		 * @param initDelayTime 初始间隔时间
		 * @param retryCount 重试次数
		 * @return 对应次数的间隔时间
		 */
		public long getIntervalTime(long initDelayTime, int retryCount) {
			throw new AbstractMethodError();
		}

	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	@NoArgsConstructor
	public static class AssignTask extends RetryTask {

		private static final long serialVersionUID = -959250453347169995L;

		private static final long INTERVAL_TIME = 2 * 60 * 1000L;
		public static final int MAX_RETRY_COUNT = 10;

		private String unionId;
		private Long customerId;
		private String salesId;

		public AssignTask(String unionId, Long customerId, String salesId) {
			super(INTERVAL_TIME);
			this.unionId = unionId;
			this.customerId = customerId;
			this.salesId = salesId;
		}
	}
}
