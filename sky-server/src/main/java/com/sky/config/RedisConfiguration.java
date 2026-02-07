package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


// 当前配置类不是必须的，因为 Spring Boot 框架会自动装配 RedisTemplate 对象，
// 但是默认的key序列化器为JdkSerializationRedisSerializer，
// 导致我们存到Redis中后的数据和原始数据有差别，故设置为StringRedisSerializer序列化器。
/*
 * redisConnectionFactory：这个连接工厂对象并不需要自己去创建，因为在
 *      server子模块下的pom.xml文件已经引入了starter依赖,这个starter依赖
 *      会自动的把这个连接工厂对象给我们创建好，并且放到Spring容器中，所以这个地方只需要声明一下
 *      就可以把它注入进来了。
 *   <dependency>
 *        <groupId>org.springframework.boot</groupId>
 *        <artifactId>spring-boot-starter-data-redis</artifactId>
 *   </dependency>
 * */
@Configuration
@Slf4j
public class RedisConfiguration {
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建RedisTemplate对象...");
        RedisTemplate redisTemplate = new RedisTemplate();
        // 设置Redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置RedisTemplate的Key序列化方式
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //设置RedisTemplate的Value序列化方式
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        log.info("创建RedisTemplate对象成功！");
        return redisTemplate;
    }
}
