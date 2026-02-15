package com.sky.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.entity.LoginDevice;
import com.sky.service.IpLocationService;
import com.sky.service.LoginDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginDeviceServiceImpl implements LoginDeviceService {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private IpLocationService ipLocationService;
    
    private static final String DEVICE_PREFIX = "user:devices:";
    private static final long DEVICE_EXPIRE_DAYS = 7;
    
    /**
     * 保存登录设备信息
     */
    public void saveLoginDevice(Long userId, String token, String deviceType, 
                                String ip, String userAgent) {
        try {
            LoginDevice device = new LoginDevice();
            device.setDeviceId(generateDeviceId(userAgent));
            device.setDeviceType(deviceType);
            device.setDeviceName(parseDeviceName(userAgent));
            device.setIp(ip);
            device.setLocation(ipLocationService.getLocation(ip));
            device.setUserAgent(userAgent);
            device.setLoginTime(LocalDateTime.now());
            device.setToken(token);
            
            String key = DEVICE_PREFIX + userId + ":" + deviceType;
            String deviceJson = objectMapper.writeValueAsString(device);
            redisTemplate.opsForValue().set(key, deviceJson, DEVICE_EXPIRE_DAYS, TimeUnit.DAYS);
            
            log.info("保存设备信息成功, userId={}, deviceType={}, ip={}", userId, deviceType, ip);
        } catch (Exception e) {
            log.error("保存设备信息失败", e);
        }
    }
    
    /**
     * 获取用户所有登录设备
     */
    public List<LoginDevice> getUserDevices(Long userId) {
        try {
            String pattern = DEVICE_PREFIX + userId + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            
            List<LoginDevice> devices = new ArrayList<>();
            if (keys != null) {
                for (String key : keys) {
                    String deviceJson = redisTemplate.opsForValue().get(key);
                    if (deviceJson != null) {
                        LoginDevice device = objectMapper.readValue(deviceJson, LoginDevice.class);
                        devices.add(device);
                    }
                }
            }
            // 按登录时间倒序排列
            devices.sort((a, b) -> b.getLoginTime().compareTo(a.getLoginTime()));
            return devices;
        } catch (Exception e) {
            log.error("获取设备列表失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 踢出指定设备
     */
    public void kickoutDevice(Long userId, String deviceType) {
        String key = DEVICE_PREFIX + userId + ":" + deviceType;
        redisTemplate.delete(key);
        log.info("踢出设备成功, userId={}, deviceType={}", userId, deviceType);
    }
    
    /**
     * 生成设备ID（基于UserAgent的哈希）
     */
    private String generateDeviceId(String userAgent) {
        return String.valueOf(userAgent.hashCode());
    }
    
    /**
     * 解析设备名称
     */
    private String parseDeviceName(String userAgent) {
        if (userAgent.contains("Windows")) return "Windows电脑";
        if (userAgent.contains("Mac")) return "Mac电脑";
        if (userAgent.contains("iPhone")) return "iPhone";
        if (userAgent.contains("Android")) return "Android手机";
        if (userAgent.contains("iPad")) return "iPad";
        return "未知设备";
    }
}