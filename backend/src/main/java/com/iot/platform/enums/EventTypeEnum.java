package com.iot.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 事件类型枚举
 */
@Getter
@AllArgsConstructor
public enum EventTypeEnum {

    LOGIN(1, "登录"),
    LOGOUT(2, "登出"),
    FORCE_OFFLINE(3, "强制下线");

    private final int code;
    private final String desc;

    /**
     * 根据 code 获取枚举实例
     */
    public static EventTypeEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (EventTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
