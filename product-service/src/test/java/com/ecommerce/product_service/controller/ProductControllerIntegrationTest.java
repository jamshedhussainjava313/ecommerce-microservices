package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    void shouldUpdateProduct() throws Exception {

        Product product = new Product();
        product.setName("Laptop");
        product.setDescription("Business laptop");
        product.setPrice(new BigDecimal("75000"));
        product.setStock(10);

        Product savedProduct = productRepository.save(product);

        String request = """
            {
                "name": "Gaming Laptop",
                "description": "Updated gaming laptop",
                "price": 85000,
                "stock": 15
            }
            """;

        mockMvc.perform(put("/products/" + savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedProduct.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Gaming Laptop")))
                .andExpect(jsonPath("$.description", is("Updated gaming laptop")))
                .andExpect(jsonPath("$.price", is(85000)))
                .andExpect(jsonPath("$.stock", is(15)));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        String request = """
            {
                "name": "Gaming Laptop",
                "description": "Updated gaming laptop",
                "price": 85000,
                "stock": 15
            }
            """;

        mockMvc.perform(put("/products/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(content().string(
                        containsString("Product not found with id: 99999")));
    }

    @ParameterizedTest
    @MethodSource("invalidUpdateProductRequests")
    void shouldRejectInvalidUpdateProductRequest(String request) throws Exception {

        Product product = new Product();
        product.setName("Laptop");
        product.setDescription("Business laptop");
        product.setPrice(new BigDecimal("75000"));
        product.setStock(10);

        Product savedProduct = productRepository.save(product);

        mockMvc.perform(put("/products/" + savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    static Stream<String> invalidUpdateProductRequests() {
        return Stream.of(
                """
                {
                    "name": "",
                    "description": "Updated laptop",
                    "price": 85000,
                    "stock": 15
                }
                """,
                """
                {
                    "name": "Gaming Laptop",
                    "description": "Updated laptop",
                    "price": 0,
                    "stock": 15
                }
                """,
                """
                {
                    "name": "Gaming Laptop",
                    "description": "Updated laptop",
                    "price": 85000,
                    "stock": -1
                }
                """
        );
    }
}
