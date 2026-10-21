package com.ecommerce;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product(101, "Gaming Mouse", 1500.0, 10, 4.5, 80);
    }

    @Test
    @DisplayName("Constructor with all fields initializes correctly")
    void testFullConstructor() {
        assertEquals(101, product.getId());
        assertEquals("Gaming Mouse", product.getName());
        assertEquals(1500.0, product.getPrice());
        assertEquals(10, product.getQuantity());
        assertEquals(10, product.getStock());
        assertEquals(4.5, product.getRating());
        assertEquals(80, product.getReviewCount());
    }

    @Test
    @DisplayName("Constructor with default review count initializes review count to 50")
    void testConstructorWithDefaultReviewCount() {
        Product p = new Product(102, "Mechanical Keyboard", 3500.0, 5, 4.8);
        assertEquals(102, p.getId());
        assertEquals("Mechanical Keyboard", p.getName());
        assertEquals(3500.0, p.getPrice());
        assertEquals(5, p.getQuantity());
        assertEquals(4.8, p.getRating());
        assertEquals(50, p.getReviewCount());
    }

    @Test
    @DisplayName("Constructor with default rating and review count initializes defaults (4.0, 20)")
    void testConstructorWithDefaultRatingAndReviews() {
        Product p = new Product(103, "USB Cable", 250.0, 50);
        assertEquals(103, p.getId());
        assertEquals("USB Cable", p.getName());
        assertEquals(250.0, p.getPrice());
        assertEquals(50, p.getQuantity());
        assertEquals(4.0, p.getRating());
        assertEquals(20, p.getReviewCount());
    }

    @Test
    @DisplayName("hasSufficientStock returns true for valid requested quantity within stock")
    void testHasSufficientStockValid() {
        assertTrue(product.hasSufficientStock(5));
        assertTrue(product.hasSufficientStock(10)); // exactly in stock
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5, 11, 100})
    @DisplayName("hasSufficientStock returns false for zero, negative, or excessive quantities")
    void testHasSufficientStockInvalid(int reqQty) {
        assertFalse(product.hasSufficientStock(reqQty));
    }

    @Test
    @DisplayName("reduceStock decreases quantity when sufficient stock is available")
    void testReduceStockSuccess() {
        product.reduceStock(4);
        assertEquals(6, product.getQuantity());
        assertEquals(6, product.getStock());
    }

    @Test
    @DisplayName("reduceStock throws IllegalArgumentException when stock is insufficient")
    void testReduceStockInsufficientThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            product.reduceStock(15);
        });
        assertEquals("Insufficient quantity available.", exception.getMessage());
    }

    @Test
    @DisplayName("reduceStock throws IllegalArgumentException when quantity is zero or negative")
    void testReduceStockZeroOrNegativeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> product.reduceStock(0));
        assertThrows(IllegalArgumentException.class, () -> product.reduceStock(-2));
    }

    @Test
    @DisplayName("addStock increases quantity for positive numbers")
    void testAddStockPositive() {
        product.addStock(5);
        assertEquals(15, product.getQuantity());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    @DisplayName("addStock ignores non-positive quantities")
    void testAddStockNonPositive(int qty) {
        product.addStock(qty);
        assertEquals(10, product.getQuantity());
    }

    @Test
    @DisplayName("Setters update product attributes correctly")
    void testSetters() {
        product.setId(201);
        product.setName("Wireless Mouse");
        product.setPrice(1800.0);
        product.setQuantity(25);
        product.setStock(30);
        product.setRating(4.9);
        product.setReviewCount(120);

        assertEquals(201, product.getId());
        assertEquals("Wireless Mouse", product.getName());
        assertEquals(1800.0, product.getPrice());
        assertEquals(30, product.getQuantity());
        assertEquals(30, product.getStock());
        assertEquals(4.9, product.getRating());
        assertEquals(120, product.getReviewCount());
    }

    @Test
    @DisplayName("getRatingDisplay formats rating and review count correctly")
    void testGetRatingDisplay() {
        assertEquals("4.5/5.0 (80 reviews)", product.getRatingDisplay());
    }

    @Test
    @DisplayName("toString contains product ID, name, price, quantity, and rating display")
    void testToString() {
        String result = product.toString();
        assertTrue(result.contains("101"));
        assertTrue(result.contains("Gaming Mouse"));
        assertTrue(result.contains("1500.00"));
        assertTrue(result.contains("10"));
        assertTrue(result.contains("4.5/5.0 (80 reviews)"));
    }
}
