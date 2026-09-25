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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    void shouldGetProduct() throws Exception {
        Product product = new Product();
        product.setName("Laptop");
        product.setDescription("Business laptop");
        product.setPrice(new BigDecimal("75000"));
        product.setStock(10);

        Product savedProduct = productRepository.save(product);

        mockMvc.perform(get("/products/" + savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProduct.getId()))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.description").value("Business laptop"))
                .andExpect(jsonPath("$.price").value(75000.0))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExistForGet() throws Exception {
        mockMvc.perform(get("/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(
                        containsString("Product not found with id: 999999")
                ));
    }

    @Test
    void shouldGetAllProducts() throws Exception {

        Product product1 = new Product();
        product1.setName("Laptop");
        product1.setDescription("Business laptop");
        product1.setPrice(new BigDecimal("75000"));
        product1.setStock(10);

        Product product2 = new Product();
        product2.setName("Keyboard");
        product2.setDescription("Mechanical keyboard");
        product2.setPrice(new BigDecimal("2500"));
        product2.setStock(20);

        productRepository.save(product1);
        productRepository.save(product2);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].description").value("Business laptop"))
                .andExpect(jsonPath("$[0].price").value(75000.0))
                .andExpect(jsonPath("$[0].stock").value(10))
                .andExpect(jsonPath("$[1].name").value("Keyboard"))
                .andExpect(jsonPath("$[1].description").value("Mechanical keyboard"))
                .andExpect(jsonPath("$[1].price").value(2500.0))
                .andExpect(jsonPath("$[1].stock").value(20));
    }

    @Test
    void shouldReturnEmptyListWhenNoProductsExist() throws Exception {

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldDeleteProduct() throws Exception {

        Product product = new Product();
        product.setName("Laptop");
        product.setDescription("Business laptop");
        product.setPrice(new BigDecimal("75000"));
        product.setStock(10);

        Product savedProduct = productRepository.save(product);

        mockMvc.perform(delete("/products/" + savedProduct.getId()))
                .andExpect(status().isNoContent());

        assertTrue(productRepository.findById(savedProduct.getId()).isEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExistForDelete() throws Exception {

        mockMvc.perform(delete("/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(
                        containsString("Product not found with id: 999999")
                ));
    }
}
