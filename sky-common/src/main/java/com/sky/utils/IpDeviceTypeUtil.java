package com.sky.utils;

import javax.servlet.http.HttpServletRequest;

public class IpDeviceTypeUtil {
    /**
     * 解析设备类型
     */
    public static String parseDeviceType(String userAgent) {
        if (userAgent == null) return "PC";

        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile") || userAgent.contains("android") ||
                userAgent.contains("iphone")) {
            return "MOBILE";
        }
        if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "TABLET";
        }
        return "PC";
    }

    /**
     * 获取真实IP
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
