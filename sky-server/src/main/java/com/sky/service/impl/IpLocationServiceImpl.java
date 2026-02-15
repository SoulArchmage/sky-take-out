package com.sky.service.impl;

import com.sky.service.IpLocationService;
import org.springframework.stereotype.Service;

@Service
public class IpLocationServiceImpl implements IpLocationService {
    @Override
    public String getLocation(String ip) {
        return "中国";
    }
}
