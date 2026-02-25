package com.freight.controller;

import com.freight.common.Result;
import com.freight.dto.CreateOrderRequest;
import com.freight.dto.PageResult;
import com.freight.dto.UpdateStatusRequest;
import com.freight.entity.Order;
import com.freight.entity.Warehouse;
import com.freight.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/order/create")
    public Result<Order> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(request);
            return Result.success(order);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/order/list")
    public Result<PageResult<Order>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        PageResult<Order> result = orderService.listOrders(page, size, status);
        return Result.success(result);
    }

    @GetMapping("/order/{id}")
    public Result<Order> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        return Result.success(order);
    }

    @PutMapping("/order/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        try {
            orderService.updateOrderStatus(id, request.getStatus());
            return Result.success(null);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/warehouse/list")
    public Result<List<Warehouse>> listWarehouses() {
        List<Warehouse> list = orderService.listWarehouses();
        return Result.success(list);
    }
}
