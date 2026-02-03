package com.sky.utils;

import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;


// 密码工具类（PBKDF2加密）
public class PasswordUtils {

    private static final Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder();
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}