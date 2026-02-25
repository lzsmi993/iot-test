package com.freight.service;

import com.freight.dto.CreateOrderRequest;
import com.freight.dto.PageResult;
import com.freight.entity.Order;
import com.freight.entity.Warehouse;
import com.freight.mapper.OrderMapper;
import com.freight.mapper.WarehouseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private WarehouseMapper warehouseMapper;

    @InjectMocks
    private OrderService orderService;

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

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setName("仓库A");

        when(warehouseMapper.findById(1L)).thenReturn(warehouse);
        doNothing().when(orderMapper).insert(any(Order.class));

        Order order = orderService.createOrder(request);

        assertNotNull(order);
        assertEquals(1L, order.getUserId());
        assertEquals(new BigDecimal("100.00"), order.getTotalPrice());
        assertEquals(1L, order.getWarehouseId());
        assertEquals("PENDING", order.getStatus());
        verify(warehouseMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).insert(any(Order.class));
    }

    @Test
    void testCreateOrder_WarehouseIdNull() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setTotalPrice(new BigDecimal("100.00"));
        request.setWarehouseId(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(request);
        });

        assertEquals("warehouseId 不可为空", exception.getMessage());
    }

    @Test
    void testCreateOrder_WarehouseNotFound() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setTotalPrice(new BigDecimal("100.00"));
        request.setWarehouseId(999L);

        when(warehouseMapper.findById(999L)).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(request);
        });

        assertEquals("仓库不存在: 999", exception.getMessage());
    }

    @Test
    void testListOrders_NormalPagination() {
        Order o1 = new Order();
        o1.setId(1L);
        o1.setStatus("PENDING");

        Order o2 = new Order();
        o2.setId(2L);
        o2.setStatus("PAID");

        List<Order> orders = Arrays.asList(o1, o2);
        when(orderMapper.findAll(0, 10, null)).thenReturn(orders);
        when(orderMapper.count(null)).thenReturn(2L);

        PageResult<Order> result = orderService.listOrders(1, 10, null);

        assertEquals(2, result.getTotal());
        assertEquals(2, result.getList().size());
    }

    @Test
    void testListOrders_StatusFilter() {
        Order o1 = new Order();
        o1.setId(1L);
        o1.setStatus("PAID");

        when(orderMapper.findAll(0, 10, "PAID")).thenReturn(Collections.singletonList(o1));
        when(orderMapper.count("PAID")).thenReturn(1L);

        PageResult<Order> result = orderService.listOrders(1, 10, "PAID");

        assertEquals(1, result.getTotal());
        assertEquals("PAID", result.getList().get(0).getStatus());
    }

    @Test
    void testGetOrderById_Exists() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("PENDING");

        when(orderMapper.findById(1L)).thenReturn(order);

        Order found = orderService.getOrderById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void testGetOrderById_NotExists() {
        when(orderMapper.findById(999L)).thenReturn(null);

        Order found = orderService.getOrderById(999L);

        assertNull(found);
    }

    @Test
    void testUpdateOrderStatus_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("PENDING");

        when(orderMapper.findById(1L)).thenReturn(order);
        when(orderMapper.updateStatus(1L, "PAID")).thenReturn(1);

        boolean result = orderService.updateOrderStatus(1L, "PAID");

        assertTrue(result);
        verify(orderMapper).updateStatus(1L, "PAID");
    }

    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        when(orderMapper.findById(999L)).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.updateOrderStatus(999L, "PAID");
        });

        assertEquals("订单不存在: 999", exception.getMessage());
    }
}
