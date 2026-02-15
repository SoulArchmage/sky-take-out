package com.sky.service;

public interface TokenRedisService {
    
    /**
     * 保存用户Token到Redis
     * @param userId 用户ID
     * @param token JWT令牌
     */
    void saveUserToken(Long userId, String deviceType, String token);
    
    /**
     * 获取用户的当前Token
     * @param userId 用户ID
     * @return 用户的JWT令牌
     */
    String getUserToken(Long userId, String deviceType);
    
    /**
     * 删除用户Token（用户主动登出）
     * @param userId 用户ID
     */
    void deleteUserToken(Long userId, String deviceType);
    
    /**
     * 验证Token是否为最新的
     * @param userId 用户ID
     * @param token 待验证的JWT令牌
     * @return true表示是最新的token，false表示token已失效
     */
    boolean isLatestToken(Long userId, String deviceType, String token);
}
