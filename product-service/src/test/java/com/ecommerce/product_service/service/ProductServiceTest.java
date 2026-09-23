package com.ecommerce.product_service.service;

import com.ecommerce.product_service.dto.CreateProductRequest;
import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct(){
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Laptop");
        request.setDescription("Business laptop");
        request.setPrice(new BigDecimal("75000"));
        request.setStock(10);

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("Laptop");
        savedProduct.setDescription("Business laptop");
        savedProduct.setPrice(new BigDecimal("75000"));
        savedProduct.setStock(10);

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        Product result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals("Business laptop", result.getDescription());
        assertEquals(new BigDecimal("75000"), result.getPrice());
        assertEquals(10, result.getStock());

        verify(productRepository).save(any(Product.class));
    }

}
