package com.iot.platform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志返回 VO
 */
@Data
public class LoginLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String username;

    private Long tenantId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    private String ip;

    private String userAgent;

    /** 登录方式 code */
    private Integer loginMethod;

    /** 登录方式描述 */
    private String loginMethodDesc;

    /** 事件类型 code */
    private Integer eventType;

    /** 事件类型描述 */
    private String eventTypeDesc;

    /** 结果 code */
    private Integer result;

    /** 结果描述 */
    private String resultDesc;

    private String failReason;

    private String deviceType;

    private String location;

    private String sessionId;

    private String requestId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
