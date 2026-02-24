package com.iot.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志实体
 *
 * @see com.iot.platform.enums.LoginMethodEnum
 * @see com.iot.platform.enums.EventTypeEnum
 * @see com.iot.platform.enums.LoginResultEnum
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("login_log")
public class LoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 租户 ID */
    private Long tenantId;

    /** 登录时间 */
    private LocalDateTime loginTime;

    /** 客户端 IP 地址 */
    private String ip;

    /** User-Agent */
    private String userAgent;

    /** 登录方式: 1=密码 2=SSO 3=Token 4=短信 */
    private Integer loginMethod;

    /** 事件类型: 1=登录 2=登出 3=强制下线 */
    private Integer eventType;

    /** 结果: 1=成功 0=失败 */
    private Integer result;

    /** 失败原因 */
    private String failReason;

    /** 设备类型 */
    private String deviceType;

    /** 登录地点 */
    private String location;

    /** 会话 ID */
    private String sessionId;

    /** 请求 ID */
    private String requestId;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
