# DB Change

## 修改 order 表

ALTER TABLE order
ADD COLUMN warehouse_id BIGINT NOT NULL;

## 完整 order 表结构

| 字段 | 类型 |
|------|------|
| id | bigint |
| user_id | bigint |
| total_price | decimal |
| warehouse_id | bigint |
