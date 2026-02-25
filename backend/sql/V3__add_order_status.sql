-- Sprint-02: 订单状态字段
ALTER TABLE `order`
ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态' AFTER `warehouse_id`,
ADD INDEX `idx_status` (`status`),
ADD INDEX `idx_created_at` (`created_at`);
