package com.ecommerce.product_service.service;

import com.ecommerce.product_service.dto.CreateProductRequest;
import com.ecommerce.product_service.dto.UpdateProductRequest;
import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.exception.ProductNotFoundException;
import com.ecommerce.product_service.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    void shouldUpdateProduct() {

        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setName("Laptop");
        existingProduct.setDescription("Business laptop");
        existingProduct.setPrice(new BigDecimal("75000"));
        existingProduct.setStock(10);

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Gaming Laptop");
        request.setDescription("Updated gaming laptop");
        request.setPrice(new BigDecimal("85000"));
        request.setStock(15);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(any(Product.class)))
                .thenReturn(existingProduct);

        Product result = productService.updateProduct(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Gaming Laptop", result.getName());
        assertEquals("Updated gaming laptop", result.getDescription());
        assertEquals(new BigDecimal("85000"), result.getPrice());
        assertEquals(15, result.getStock());

        verify(productRepository).findById(1L);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Gaming Laptop");
        request.setDescription("Updated gaming laptop");
        request.setPrice(new BigDecimal("85000"));
        request.setStock(15);

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProduct(999L, request)
        );

        verify(productRepository).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

}
