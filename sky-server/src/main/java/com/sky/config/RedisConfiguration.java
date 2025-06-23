package com.sky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始配置Redis序列化方式...");

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 创建ObjectMapper并配置
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java8时间模块
        objectMapper.registerModule(new JavaTimeModule());
        // 配置序列化时间格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 创建JSON序列化工具
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);
        // 创建字符串序列化工具
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 设置key序列化方式
        redisTemplate.setKeySerializer(stringRedisSerializer);
        // 设置value序列化方式
        redisTemplate.setValueSerializer(serializer);
        // 设置hash的key序列化方式
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        // 设置hash的value序列化方式
        redisTemplate.setHashValueSerializer(serializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}