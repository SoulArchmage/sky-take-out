package com.sky.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LoginDevice {
    private String deviceId;        // 设备唯一标识
    private String deviceType;      // PC/MOBILE/TABLET
    private String deviceName;      // 设备名称
    private String ip;              // 登录IP
    private String location;        // 地理位置
    private String userAgent;       // 浏览器信息
    private LocalDateTime loginTime; // 登录时间
    private String token;           // 当前token
}