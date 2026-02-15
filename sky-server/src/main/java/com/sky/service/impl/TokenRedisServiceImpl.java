package com.sky.service.impl;

import com.sky.service.TokenRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenRedisServiceImpl implements TokenRedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "user:token:";
    private static final long TOKEN_EXPIRE_DAYS = 7;

    /**
     * 保存用户Token到Redis
     */
    public void saveUserToken(Long userId, String token) {
        String key = TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 获取用户的当前Token
     */
    public String getUserToken(Long userId) {
        String key = TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除用户Token（用户主动登出）
     */
    public void deleteUserToken(Long userId) {
        String key = TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }

    /**
     * 验证Token是否为最新的
     */
    public boolean isLatestToken(Long userId, String token) {
        String latestToken = getUserToken(userId);
        return token.equals(latestToken);
    }
}
