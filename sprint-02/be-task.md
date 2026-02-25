# Backend Task

## 新增接口

### 1. 订单列表查询
GET /api/order/list

Query Parameters:
- page: 页码（默认1）
- size: 每页数量（默认10）
- status: 状态筛选（可选）

Response:
{
  code: 200,
  message: "success",
  data: {
    total: 100,
    list: [...]
  }
}

### 2. 订单详情查询
GET /api/order/{id}

### 3. 订单状态更新
PUT /api/order/{id}/status

Request:
{
  status: "PAID"
}

## Service 修改

- OrderService.listOrders(page, size, status)
- OrderService.getOrderById(id)
- OrderService.updateOrderStatus(id, status)
