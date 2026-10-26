package com.ecommerce.order_service.service;

import com.ecommerce.order_service.InsufficientStockException;
import com.ecommerce.order_service.client.*;
import com.ecommerce.order_service.dto.CreateOrderRequest;
import com.ecommerce.order_service.dto.OrderItemRequest;
import com.ecommerce.order_service.dto.OrderItemResponse;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.entity.OrderItem;
import com.ecommerce.order_service.exception.OrderNotFoundException;
import com.ecommerce.order_service.repository.OrderRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;

    public OrderService(
            OrderRepository orderRepository,
            UserClient userClient,
            ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.userClient = userClient;
        this.productClient = productClient;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        // Step 1: Validate the user.
        try {
            userClient.getUserById(request.getUserId());
        } catch (FeignException.NotFound ex) {
            throw new UserNotFoundException(request.getUserId());
        }

        // Step 2: Validate every product before saving the order.
        for (OrderItemRequest itemRequest : request.getItems()) {

            ProductResponse product;

            try {
                product = productClient.getProductById(
                        itemRequest.getProductId());
            } catch (FeignException.NotFound ex) {
                throw new ProductNotFoundException(
                        itemRequest.getProductId());
            } catch (FeignException ex) {
                throw new ProductServiceUnavailableException(
                        "Unable to validate product "
                                + itemRequest.getProductId()
                                + " because Product Service returned an error.",
                        ex);
            }

            if (product.getStock() == null
                    || product.getStock() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        itemRequest.getProductId(),
                        itemRequest.getQuantity(),
                        product.getStock());
            }
        }

        // Step 3: Create and save the order only after validation succeeds.
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(itemRequest.getProductId());
            item.setQuantity(itemRequest.getQuantity());

            // Price calculation is outside this validation step.
            item.setPrice(BigDecimal.ZERO);

            order.getOrderItems().add(item);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(id));

        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> items = order.getOrderItems()
                .stream()
                .map(item -> {
                    OrderItemResponse itemResponse =
                            new OrderItemResponse();

                    itemResponse.setProductId(item.getProductId());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setPrice(item.getPrice());

                    return itemResponse;
                })
                .toList();

        response.setItems(items);

        return response;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistory(Long userId) {
        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

}
