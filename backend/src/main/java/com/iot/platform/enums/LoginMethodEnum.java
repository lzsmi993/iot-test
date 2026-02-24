package com.iot.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录方式枚举
 */
@Getter
@AllArgsConstructor
public enum LoginMethodEnum {

    PASSWORD(1, "密码登录"),
    SSO(2, "SSO 单点登录"),
    TOKEN(3, "Token 登录"),
    SMS(4, "短信验证码登录");

    private final int code;
    private final String desc;

    /**
     * 根据 code 获取枚举实例
     */
    public static LoginMethodEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (LoginMethodEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
