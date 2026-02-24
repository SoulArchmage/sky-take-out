package com.sky.service.impl;

import com.sky.service.IpLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginSecurityService {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private IpLocationService ipLocationService;
    
    @Autowired
    private NotificationService notificationService;
    
    private static final String LAST_LOGIN_IP_PREFIX = "user:last_login_ip:";
    private static final String LOGIN_FAIL_COUNT_PREFIX = "user:login_fail:";
    
    /**
     * 检测异常登录
     */
    public boolean detectAbnormalLogin(Long userId, String currentIp) {
        try {
            // 获取上次登录IP
            String lastIpKey = LAST_LOGIN_IP_PREFIX + userId;
            String lastIp = redisTemplate.opsForValue().get(lastIpKey);
            log.info("用户上次登录IP:{}", lastIp);
            
            if (lastIp != null && !lastIp.equals(currentIp)) {
                // IP变化，检测是否异地登录
                String lastLocation = ipLocationService.getLocation(lastIp);
                String currentLocation = ipLocationService.getLocation(currentIp);
                
                if (!lastLocation.equals(currentLocation) && 
                    !currentLocation.equals("本地网络")) {
                    log.warn("检测到异地登录, userId={}, lastIp={}, currentIp={}, lastLocation={}, currentLocation={}", 
                            userId, lastIp, currentIp, lastLocation, currentLocation);
                    
                    // 发送通知（邮件/短信）
                    notificationService.sendAbnormalLoginNotice(userId, currentIp, currentLocation);
                    
                    return true;
                }
            }
            
            // 更新最后登录IP
            redisTemplate.opsForValue().set(lastIpKey, currentIp, 30, TimeUnit.DAYS);
            
            return false;
        } catch (Exception e) {
            log.error("异常登录检测失败", e);
            return false;
        }
    }
    
    /**
     * 记录登录失败次数（防暴力破解）
     */
    public void recordLoginFailure(String username, String ip) {
        String key = LOGIN_FAIL_COUNT_PREFIX + username + ":" + ip;
        Long count = redisTemplate.opsForValue().increment(key);
        
        // 首次失败设置过期时间（1小时）
        if (count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        }
        
        // 失败5次后锁定账号10分钟
        if (count >= 5) {
            log.warn("登录失败次数过多, username={}, ip={}, count={}", username, ip, count);
            // 可以实现账号临时锁定逻辑
        }
    }
    
    /**
     * 检查是否被锁定
     */
    public boolean isLocked(String username, String ip) {
        String key = LOGIN_FAIL_COUNT_PREFIX + username + ":" + ip;
        String countStr = redisTemplate.opsForValue().get(key);
        if (countStr != null) {
            return Integer.parseInt(countStr) >= 5;
        }
        return false;
    }
    
    /**
     * 清除失败记录（登录成功后）
     */
    public void clearLoginFailure(String username, String ip) {
        String key = LOGIN_FAIL_COUNT_PREFIX + username + ":" + ip;
        redisTemplate.delete(key);
    }
}