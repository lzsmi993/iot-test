# DB Change

## 修改 order 表

ALTER TABLE `order`
ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态' AFTER `warehouse_id`,
ADD INDEX `idx_status` (`status`),
ADD INDEX `idx_created_at` (`created_at`);

## 完整 order 表结构

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户ID |
| total_price | decimal(10,2) | 订单金额 |
| warehouse_id | bigint | 仓库ID |
| status | varchar(20) | 订单状态 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

## 订单状态枚举

- PENDING: 待支付
- PAID: 已支付
- SHIPPED: 已发货
- COMPLETED: 已完成
- CANCELLED: 已取消
