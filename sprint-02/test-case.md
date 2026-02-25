# Test Case

## 单元测试

1. 订单列表查询 - 正常分页
2. 订单列表查询 - 状态筛选
3. 订单详情查询 - 订单存在
4. 订单详情查询 - 订单不存在
5. 订单状态更新 - 正常流转

## 接口测试

1. GET /api/order/list - 返回分页数据
2. GET /api/order/list?status=PAID - 返回筛选结果
3. GET /api/order/999 - 返回404
4. PUT /api/order/1/status - 更新成功
