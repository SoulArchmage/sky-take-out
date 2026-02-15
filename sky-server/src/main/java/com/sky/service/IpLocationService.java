package com.sky.service;

public interface IpLocationService {

    /**
     * 根据IP获取地理位置（可接入高德/腾讯地图API）
     */
    public String getLocation(String ip);
}
