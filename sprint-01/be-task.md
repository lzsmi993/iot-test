# Backend Task

## 接口修改

POST /api/order/create

Request:
{
  userId,
  totalPrice,
  warehouseId
}

校验逻辑：
- warehouseId 必须存在
- 用户必须有权限

## Service 修改

createOrder():
  - 查询 warehouse
  - 保存 order
