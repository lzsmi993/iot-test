package com.freight.controller;

import com.freight.common.Result;
import com.freight.dto.CreateOrderRequest;
import com.freight.dto.PageResult;
import com.freight.dto.UpdateStatusRequest;
import com.freight.entity.Order;
import com.freight.entity.Warehouse;
import com.freight.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setTotalPrice(new BigDecimal("100.00"));
        request.setWarehouseId(1L);

        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setTotalPrice(new BigDecimal("100.00"));
        order.setWarehouseId(1L);
        order.setStatus("PENDING");

        when(orderService.createOrder(request)).thenReturn(order);

        Result<Order> result = orderController.createOrder(request);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
    }

    @Test
    void testCreateOrder_Error() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setTotalPrice(new BigDecimal("100.00"));
        request.setWarehouseId(null);

        when(orderService.createOrder(request))
            .thenThrow(new IllegalArgumentException("warehouseId 不可为空"));

        Result<Order> result = orderController.createOrder(request);

        assertEquals(400, result.getCode());
        assertEquals("warehouseId 不可为空", result.getMessage());
    }

    @Test
    void testListOrders() {
        Order o1 = new Order();
        o1.setId(1L);
        o1.setStatus("PENDING");

        PageResult<Order> page = new PageResult<>(1, List.of(o1));
        when(orderService.listOrders(1, 10, null)).thenReturn(page);

        Result<PageResult<Order>> result = orderController.listOrders(1, 10, null);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().getTotal());
        assertEquals(1, result.getData().getList().size());
    }

    @Test
    void testGetOrder_Exists() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("PENDING");

        when(orderService.getOrderById(1L)).thenReturn(order);

        Result<Order> result = orderController.getOrder(1L);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testGetOrder_NotFound() {
        when(orderService.getOrderById(999L)).thenReturn(null);

        Result<Order> result = orderController.getOrder(999L);

        assertEquals(404, result.getCode());
        assertEquals("订单不存在", result.getMessage());
    }

    @Test
    void testUpdateOrderStatus_Success() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus("PAID");

        when(orderService.updateOrderStatus(1L, "PAID")).thenReturn(true);

        Result<Void> result = orderController.updateOrderStatus(1L, request);

        assertEquals(200, result.getCode());
    }

    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus("PAID");

        when(orderService.updateOrderStatus(999L, "PAID"))
            .thenThrow(new IllegalArgumentException("订单不存在: 999"));

        Result<Void> result = orderController.updateOrderStatus(999L, request);

        assertEquals(400, result.getCode());
        assertEquals("订单不存在: 999", result.getMessage());
    }

    @Test
    void testListWarehouses() {
        Warehouse w1 = new Warehouse();
        w1.setId(1L);
        w1.setName("仓库A");

        Warehouse w2 = new Warehouse();
        w2.setId(2L);
        w2.setName("仓库B");

        List<Warehouse> warehouses = Arrays.asList(w1, w2);
        when(orderService.listWarehouses()).thenReturn(warehouses);

        Result<List<Warehouse>> result = orderController.listWarehouses();

        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
    }
}
