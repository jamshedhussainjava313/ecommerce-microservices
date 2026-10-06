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

}
