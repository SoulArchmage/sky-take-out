package com.sky.service;

import com.sky.entity.LoginDevice;

import java.util.List;

public interface LoginDeviceService {
    void saveLoginDevice(Long userId, String token, String deviceType, String ip, String userAgent);

    List<LoginDevice> getUserDevices(Long userId);

    void kickoutDevice(Long userId, String deviceType);


}
