package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.CreateOrderRequest;
import com.ecommerce.order_service.dto.OrderItemRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.exception.OrderNotFoundException;
import com.ecommerce.order_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderSuccessfully() {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        request.setItems(List.of(itemRequest));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(1L);
                    return order;
                });

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals("CREATED", response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals(1L, response.getItems().get(0).getProductId());
        assertEquals(2, response.getItems().get(0).getQuantity());
    }

    @Test
    void getOrderById_shouldReturnOrder_whenOrderExists() {

        Long orderId = 1L;

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(1L);
        order.setStatus("CREATED");
        order.setTotalAmount(BigDecimal.ZERO);
        order.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(orderId);

        assertNotNull(response);
        assertEquals(orderId, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals("CREATED", response.getStatus());

        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrderById_shouldThrowException_whenOrderDoesNotExist() {

        Long orderId = 999L;

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrderById(orderId)
        );

        assertEquals(
                "Order Not Found with id: 999",
                exception.getMessage()
        );

        verify(orderRepository).findById(orderId);
    }
}
