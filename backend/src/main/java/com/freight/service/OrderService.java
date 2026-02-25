package com.freight.service;

import com.freight.dto.CreateOrderRequest;
import com.freight.dto.PageResult;
import com.freight.entity.Order;
import com.freight.entity.Warehouse;
import com.freight.mapper.OrderMapper;
import com.freight.mapper.WarehouseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final WarehouseMapper warehouseMapper;

    public OrderService(OrderMapper orderMapper, WarehouseMapper warehouseMapper) {
        this.orderMapper = orderMapper;
        this.warehouseMapper = warehouseMapper;
    }

    public Order createOrder(CreateOrderRequest request) {
        if (request.getWarehouseId() == null) {
            throw new IllegalArgumentException("warehouseId 不可为空");
        }

        Warehouse warehouse = warehouseMapper.findById(request.getWarehouseId());
        if (warehouse == null) {
            throw new IllegalArgumentException("仓库不存在: " + request.getWarehouseId());
        }

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalPrice(request.getTotalPrice());
        order.setWarehouseId(request.getWarehouseId());
        order.setStatus("PENDING");
        orderMapper.insert(order);
        return order;
    }

    public PageResult<Order> listOrders(int page, int size, String status) {
        int offset = (page - 1) * size;
        List<Order> list = orderMapper.findAll(offset, size, status);
        long total = orderMapper.count(status);
        return new PageResult<>(total, list);
    }

    public Order getOrderById(Long id) {
        return orderMapper.findById(id);
    }

    public boolean updateOrderStatus(Long id, String status) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + id);
        }
        return orderMapper.updateStatus(id, status) > 0;
    }

    public List<Warehouse> listWarehouses() {
        return warehouseMapper.findAll();
    }
}
