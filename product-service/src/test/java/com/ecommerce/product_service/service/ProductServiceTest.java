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
import java.util.List;
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

    @Test
    void shouldGetProduct() {
        Long productId = 1L;

        Product product = new Product();
        product.setId(productId);
        product.setName("Laptop");
        product.setDescription("Business laptop");
        product.setPrice(new BigDecimal("75000"));
        product.setStock(10);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        Product result = productService.getProduct(productId);

        assertEquals(productId, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals("Business laptop", result.getDescription());
        assertEquals(new BigDecimal("75000"), result.getPrice());
        assertEquals(10, result.getStock());

        verify(productRepository).findById(productId);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExistForGet() {
        Long productId = 999L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProduct(productId)
        );

        verify(productRepository).findById(productId);
    }

    @Test
    void shouldGetAllProducts() {

        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Laptop");
        product1.setDescription("Business laptop");
        product1.setPrice(new BigDecimal("75000"));
        product1.setStock(10);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Keyboard");
        product2.setDescription("Mechanical keyboard");
        product2.setPrice(new BigDecimal("2500"));
        product2.setStock(20);

        when(productRepository.findAll())
                .thenReturn(List.of(product1, product2));

        List<Product> result = productService.getAllProducts();

        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals("Laptop", result.get(0).getName());

        assertEquals(2L, result.get(1).getId());
        assertEquals("Keyboard", result.get(1).getName());

        verify(productRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoProductsExist() {

        when(productRepository.findAll())
                .thenReturn(List.of());

        List<Product> result = productService.getAllProducts();

        assertTrue(result.isEmpty());

        verify(productRepository).findAll();
    }

    @Test
    void shouldDeleteProduct() {
        Long productId = 1L;

        Product product = new Product();
        product.setId(productId);
        product.setName("Laptop");
        product.setDescription("Business laptop");
        product.setPrice(new BigDecimal("75000"));
        product.setStock(10);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(productId);

        verify(productRepository).findById(productId);
        verify(productRepository).delete(product);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExistForDelete() {
        Long productId = 999L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProduct(productId)
        );

        verify(productRepository).findById(productId);
        verify(productRepository, never()).delete(any(Product.class));
    }

}
