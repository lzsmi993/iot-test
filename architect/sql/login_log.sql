-- ============================================================
-- IoT 平台 - 登录日志表
-- 数据库: iot_platform
-- 表名:   login_log
-- 版本:   1.0.0
-- 日期:   2026-02-24
-- ============================================================

CREATE DATABASE IF NOT EXISTS `iot_platform`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `iot_platform`;

-- ------------------------------------------------------------
-- 1. 主表：login_log（按月 RANGE 分区）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `login_log` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`       BIGINT       NOT NULL                COMMENT '用户ID',
  `username`      VARCHAR(64)  NOT NULL                COMMENT '用户名（登录账号）',
  `tenant_id`     BIGINT       NOT NULL DEFAULT 0      COMMENT '租户ID（0=平台级）',
  `login_time`    DATETIME(3)  NOT NULL                COMMENT '登录/登出时间（毫秒精度）',
  `ip`            VARCHAR(45)  NOT NULL DEFAULT ''      COMMENT 'IP地址（兼容 IPv6）',
  `user_agent`    VARCHAR(512) NOT NULL DEFAULT ''      COMMENT '浏览器 User-Agent',
  `login_method`  TINYINT      NOT NULL DEFAULT 1       COMMENT '登录方式: 1=密码, 2=SSO, 3=Token刷新, 4=短信验证码',
  `event_type`    TINYINT      NOT NULL DEFAULT 1       COMMENT '事件类型: 1=登录, 2=登出, 3=强制下线',
  `result`        TINYINT      NOT NULL DEFAULT 1       COMMENT '登录结果: 1=成功, 0=失败',
  `fail_reason`   VARCHAR(256) NOT NULL DEFAULT ''      COMMENT '失败原因（成功时为空）',
  `device_type`   VARCHAR(32)  NOT NULL DEFAULT ''      COMMENT '设备类型: PC/Mobile/Tablet/API',
  `location`      VARCHAR(128) NOT NULL DEFAULT ''      COMMENT 'IP归属地（异步反查填充）',
  `session_id`    VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '会话ID（用于关联登录-登出）',
  `request_id`    VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '请求追踪ID（链路追踪）',
  `created_at`    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
  PRIMARY KEY (`id`, `login_time`)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = '用户登录日志表'
-- ============================================================
-- 分区策略：按月 RANGE 分区
-- login_time 作为分区键，必须包含在主键中
-- 保留 180 天（约 6 个月），预建 8 个月分区 + 1 个兜底分区
-- ============================================================
PARTITION BY RANGE (TO_DAYS(`login_time`)) (
  PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')) COMMENT '2026年02月',
  PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')) COMMENT '2026年03月',
  PARTITION p202604 VALUES LESS THAN (TO_DAYS('2026-05-01')) COMMENT '2026年04月',
  PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')) COMMENT '2026年05月',
  PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')) COMMENT '2026年06月',
  PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01')) COMMENT '2026年07月',
  PARTITION p202608 VALUES LESS THAN (TO_DAYS('2026-09-01')) COMMENT '2026年08月',
  PARTITION p202609 VALUES LESS THAN (TO_DAYS('2026-10-01')) COMMENT '2026年09月',
  PARTITION p202610 VALUES LESS THAN (TO_DAYS('2026-11-01')) COMMENT '2026年10月',
  PARTITION p_future VALUES LESS THAN MAXVALUE               COMMENT '兜底分区（定时任务会拆分）'
);

-- ============================================================
-- 2. 索引设计
-- ============================================================

-- 索引 1: 租户 + 登录时间（租户管理员按时间范围查询，分区裁剪友好）
ALTER TABLE `login_log`
  ADD INDEX `idx_tenant_login_time` (`tenant_id`, `login_time`);

-- 索引 2: 用户名 + 登录时间（按用户名搜索 + 时间排序）
ALTER TABLE `login_log`
  ADD INDEX `idx_username_login_time` (`username`, `login_time`);

-- 索引 3: 用户ID + 登录时间（普通用户查看自己的记录）
ALTER TABLE `login_log`
  ADD INDEX `idx_user_id_login_time` (`user_id`, `login_time`);

-- 索引 4: IP + 登录时间（按 IP 追溯查询）
ALTER TABLE `login_log`
  ADD INDEX `idx_ip_login_time` (`ip`, `login_time`);

-- 索引 5: 登录结果 + 用户ID + 登录时间（异常登录检测：查某用户近5分钟失败次数）
ALTER TABLE `login_log`
  ADD INDEX `idx_result_user_time` (`result`, `user_id`, `login_time`);

-- ============================================================
-- 3. 分区维护存储过程
-- ============================================================

DELIMITER $$

-- 3.1 自动创建下月分区（每月1号由定时任务调用）
CREATE PROCEDURE `sp_login_log_add_partition`()
BEGIN
  DECLARE v_part_name VARCHAR(16);
  DECLARE v_boundary  DATE;
  DECLARE v_sql       TEXT;

  -- 计算下下个月1号作为新分区的上界
  SET v_boundary  = DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 2 MONTH), '%Y-%m-01');
  SET v_part_name = CONCAT('p', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 1 MONTH), '%Y%m'));

  -- 将 p_future 重组：先拆出新分区，再保留 MAXVALUE 兜底
  SET v_sql = CONCAT(
    'ALTER TABLE `login_log` REORGANIZE PARTITION `p_future` INTO (',
    'PARTITION `', v_part_name, '` VALUES LESS THAN (TO_DAYS(''', v_boundary, ''')),',
    'PARTITION `p_future` VALUES LESS THAN MAXVALUE)'
  );

  SET @dynamic_sql = v_sql;
  PREPARE stmt FROM @dynamic_sql;
  EXECUTE stmt;
  DEALLOCATE PREPARE stmt;
END$$

-- 3.2 删除超过 180 天的过期分区
CREATE PROCEDURE `sp_login_log_drop_expired_partitions`()
BEGIN
  DECLARE v_cutoff_days INT;
  DECLARE v_part_name   VARCHAR(64);
  DECLARE v_done        INT DEFAULT 0;
  DECLARE cur CURSOR FOR
    SELECT PARTITION_NAME
    FROM   INFORMATION_SCHEMA.PARTITIONS
    WHERE  TABLE_SCHEMA = 'iot_platform'
      AND  TABLE_NAME   = 'login_log'
      AND  PARTITION_NAME NOT IN ('p_future')
      AND  PARTITION_DESCRIPTION != 'MAXVALUE'
      AND  CAST(PARTITION_DESCRIPTION AS UNSIGNED) < TO_DAYS(DATE_SUB(CURDATE(), INTERVAL 180 DAY));
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO v_part_name;
    IF v_done THEN
      LEAVE read_loop;
    END IF;

    SET @drop_sql = CONCAT('ALTER TABLE `login_log` DROP PARTITION `', v_part_name, '`');
    PREPARE stmt FROM @drop_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END LOOP;
  CLOSE cur;
END$$

DELIMITER ;

-- ============================================================
-- 4. 定时事件（MySQL Event Scheduler）
--    需确保 event_scheduler = ON
-- ============================================================
SET GLOBAL event_scheduler = ON;

-- 每月1号凌晨 02:00 自动创建新分区
CREATE EVENT IF NOT EXISTS `evt_login_log_add_partition`
  ON SCHEDULE EVERY 1 MONTH
  STARTS '2026-03-01 02:00:00'
  ON COMPLETION PRESERVE
  ENABLE
  COMMENT '登录日志表：自动创建下月分区'
  DO CALL `sp_login_log_add_partition`();

-- 每天凌晨 03:00 清理过期分区
CREATE EVENT IF NOT EXISTS `evt_login_log_drop_expired`
  ON SCHEDULE EVERY 1 DAY
  STARTS '2026-02-25 03:00:00'
  ON COMPLETION PRESERVE
  ENABLE
  COMMENT '登录日志表：清理超过180天的过期分区'
  DO CALL `sp_login_log_drop_expired_partitions`();
