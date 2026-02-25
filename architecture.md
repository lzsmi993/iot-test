# Architecture

## order 表（完整结构）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户ID |
| total_price | decimal(10,2) | 订单金额 |
| warehouse_id | bigint | 仓库ID |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

## warehouse 表（完整结构）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| name | varchar(100) | 仓库名称 |
| location | varchar(200) | 仓库地址 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

