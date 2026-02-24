package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.service.IpLocationService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

//import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class IpLocationServiceImpl implements IpLocationService {
    /**
     * 根据IP获取地理位置（简化实现，实际可接入高德/腾讯地图API）
     */
    @Override
    public String getLocation(String ip) {
        // 内网IP
        if (ip.startsWith("192.168.") || ip.startsWith("127.0.")) {
            log.info("IP:{}，地理位置:{}", ip, "本地网络");
            return "本地网络";
        }

        // 调用IP定位API
        Map<String, String> params = new HashMap<>();
        params.put("key", "4b5d4f8cebabff1f4498874892c55d58");
        String response = HttpClientUtil.doGet("https://restapi.amap.com/v3/ip?ip=" + ip + "&sig", params);
        JSONObject jsonObject = JSON.parseObject(response);
        String city = jsonObject.getString("province") + "/" + jsonObject.getString("city");
        log.info("IP:{}，地理位置:{}", ip, city);

        return city;
    }
}
