package com.bollen.retry.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Component
public class RedisTemplateFactory<V> {

	private final RedisConnectionFactory redisConnectionFactory;
	private final StringRedisSerializer stringRedisSerializer;

	@Autowired
	public RedisTemplateFactory(RedisConnectionFactory redisConnectionFactory, StringRedisSerializer stringRedisSerializer) {
		this.redisConnectionFactory = redisConnectionFactory;
		this.stringRedisSerializer = stringRedisSerializer;
	}


	public RedisTemplate<String, V> getJacksonStringTemplate(Class<V> clazz) {
		RedisTemplate<String, V> redisTemplate = new RedisTemplate<>();
		Jackson2JsonRedisSerializer<V> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(clazz);
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(stringRedisSerializer);
		redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
		redisTemplate.setHashKeySerializer(stringRedisSerializer);
		redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
		// 不是注入方法的话，必须调用它。Spring注入的话，会在注入时调用
		redisTemplate.afterPropertiesSet();

		return redisTemplate;
	}
}
