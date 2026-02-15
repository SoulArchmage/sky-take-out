package com.sky.service.impl;

import com.sky.service.TokenRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TokenRedisServiceImpl implements TokenRedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "user:token:";
    private static final long TOKEN_EXPIRE_DAYS = 7;

    /**
     * 保存用户Token（按设备类型）
     */
    public void saveUserToken(Long userId, String deviceType, String token) {
        try {
            String key = TOKEN_PREFIX + userId + ":" + deviceType;
            redisTemplate.opsForValue().set(key, token, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
            log.info("保存token成功, userId={}, deviceType={}", userId, deviceType);
        } catch (Exception e) {
            log.error("保存token失败", e);
            throw new RuntimeException("保存token失败", e);
        }
    }

    /**
     * 获取用户Token（按设备类型）
     */
    public String getUserToken(Long userId, String deviceType) {
        try {
            String key = TOKEN_PREFIX + userId + ":" + deviceType;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取token失败", e);
            return null;
        }
    }

    /**
     * 删除用户Token
     */
    public void deleteUserToken(Long userId, String deviceType) {
        try {
            String key = TOKEN_PREFIX + userId + ":" + deviceType;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除token失败", e);
        }
    }

    /**
     * 验证是否为最新Token
     */
    public boolean isLatestToken(Long userId, String deviceType, String token) {
        if (userId == null || token == null) {
            return false;
        }
        String latestToken = getUserToken(userId, deviceType);
        return token.equals(latestToken);
    }

    /**
     * 删除用户所有设备Token（完全登出）
     */
    public void deleteAllUserTokens(Long userId) {
        try {
            String pattern = TOKEN_PREFIX + userId + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("删除所有token失败", e);
        }
    }
}
