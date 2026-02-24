package com.iot.platform.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志查询参数 DTO
 */
@Data
public class LoginLogQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户名（模糊匹配） */
    private String username;

    /** IP 地址（精确匹配） */
    private String ip;

    /** 登录结果: 1=成功 0=失败 */
    private Integer result;

    /** 事件类型: 1=登录 2=登出 3=强制下线 */
    private Integer eventType;

    /** 登录方式: 1=密码 2=SSO 3=Token 4=短信 */
    private Integer loginMethod;

    /** 开始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 结束时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 当前页码（默认 1） */
    private Integer pageNum = 1;

    /** 每页大小（默认 20） */
    private Integer pageSize = 20;
}
