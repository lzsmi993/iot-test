package com.iot.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录结果枚举
 */
@Getter
@AllArgsConstructor
public enum LoginResultEnum {

    FAIL(0, "失败"),
    SUCCESS(1, "成功");

    private final int code;
    private final String desc;

    /**
     * 根据 code 获取枚举实例
     */
    public static LoginResultEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (LoginResultEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
