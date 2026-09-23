package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
public class ProductControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void shouldCreateProduct() throws Exception {

        String request = """
                {
                    "name": "Laptop",
                    "description": "Business laptop",
                    "price": 75000,
                    "stock": 10
                }
                """;

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Laptop")))
                .andExpect(jsonPath("$.description", is("Business laptop")))
                .andExpect(jsonPath("$.price", is(75000)))
                .andExpect(jsonPath("$.stock", is(10)));
    }

    @Test
    void shouldRejectProductWhenNameIsBlank() throws Exception {

        String request = """
            {
                "name": "",
                "description": "Business laptop",
                "price": 75000,
                "stock": 10
            }
            """;

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectProductWhenPriceIsInvalid() throws Exception {

        String request = """
            {
                "name": "Laptop",
                "description": "Business laptop",
                "price": 0,
                "stock": 10
            }
            """;

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectProductWhenStockIsNegative() throws Exception {

        String request = """
            {
                "name": "Laptop",
                "description": "Business laptop",
                "price": 75000,
                "stock": -1
            }
            """;

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}
