package com.sky.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {
    
    /**
     * 发送异常登录通知
     */
    public void sendAbnormalLoginNotice(Long userId, String ip, String location) {
        // 实际项目中可以接入邮件服务、短信服务
        log.info("发送异常登录通知, userId={}, ip={}, location={}", userId, ip, location);
        
        // 示例：发送邮件
        // String message = String.format("您的账号在%s(%s)登录，如非本人操作请及时修改密码", location, ip);
        // emailService.sendEmail(userEmail, "异常登录提醒", message);
    }
}