package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("ci")
@AutoConfigureMockMvc
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {

        String request = """
                {
                    "userId": 1,
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 2
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectOrderWhenUserIdIsMissing() throws Exception {

        String request = """
                {
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 2
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectOrderWhenItemsAreEmpty() throws Exception {

        String request = """
                {
                    "userId": 1,
                    "items": []
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectOrderWhenQuantityIsInvalid() throws Exception {

        String request = """
                {
                    "userId": 1,
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 0
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrderById_shouldReturn200_whenOrderExists() throws Exception {

        Order order = new Order();
        order.setUserId(1L);
        order.setStatus("CREATED");
        order.setTotalAmount(BigDecimal.ZERO);
        order.setCreatedAt(LocalDateTime.now());

        order = orderRepository.save(order);

        mockMvc.perform(get("/orders/{id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getOrderHistory_shouldReturnOrdersForUser() throws Exception {
        Order order1 = new Order();
        order1.setUserId(1L);
        order1.setTotalAmount(BigDecimal.ZERO);
        order1.setStatus("CREATED");
        order1.setCreatedAt(LocalDateTime.now().minusDays(1));

        Order order2 = new Order();
        order2.setUserId(1L);
        order2.setTotalAmount(BigDecimal.ZERO);
        order2.setStatus("CREATED");
        order2.setCreatedAt(LocalDateTime.now());

        order1 = orderRepository.save(order1);
        order2 = orderRepository.save(order2);

        mockMvc.perform(get("/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(order2.getId()))
                .andExpect(jsonPath("$[1].id").value(order1.getId()));
    }

    @Test
    void getOrderHistory_shouldReturnEmptyListWhenUserHasNoOrders() throws Exception {
        mockMvc.perform(get("/orders/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

}
