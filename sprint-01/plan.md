# Sprint 01 - Multi Warehouse

## 目标
支持订单绑定仓库

## 任务拆解

### DB
- 新建 warehouse 表
- order 表增加 warehouse_id

### Backend
- 创建订单接口增加 warehouse_id
- 增加仓库合法性校验

### Frontend
- 订单创建页增加仓库下拉框

### Test
- 增加单元测试
- 增加接口测试

## 验收标准

- 可创建带仓库的订单
- warehouse_id 不可为空
