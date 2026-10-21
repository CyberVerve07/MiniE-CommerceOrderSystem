package com.ecommerce;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product(1, "Laptop Stand", 999.0, 15, 4.3, 30);
    }

    @Test
    @DisplayName("CartItem constructor succeeds with valid product and positive quantity")
    void testConstructorSuccess() {
        CartItem item = new CartItem(sampleProduct, 3);
        assertEquals(sampleProduct, item.getProduct());
        assertEquals(3, item.getQuantity());
        assertEquals(2997.0, item.getSubtotal(), 0.001);
    }

    @Test
    @DisplayName("CartItem constructor throws IllegalArgumentException if product is null")
    void testConstructorNullProductThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new CartItem(null, 2);
        });
        assertEquals("Product cannot be null", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5})
    @DisplayName("CartItem constructor throws IllegalArgumentException if quantity is <= 0")
    void testConstructorInvalidQuantityThrows(int invalidQty) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new CartItem(sampleProduct, invalidQty);
        });
        assertEquals("Quantity must be greater than zero", ex.getMessage());
    }

    @Test
    @DisplayName("setQuantity updates quantity when valid positive integer")
    void testSetQuantityValid() {
        CartItem item = new CartItem(sampleProduct, 2);
        item.setQuantity(5);
        assertEquals(5, item.getQuantity());
        assertEquals(4995.0, item.getSubtotal(), 0.001);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    @DisplayName("setQuantity throws IllegalArgumentException when <= 0")
    void testSetQuantityInvalidThrows(int invalidQty) {
        CartItem item = new CartItem(sampleProduct, 2);
        assertThrows(IllegalArgumentException.class, () -> item.setQuantity(invalidQty));
    }

    @Test
    @DisplayName("Cart.CartItem nested class works accurately for cart operations")
    void testNestedCartItem() {
        Cart.CartItem nested = new Cart.CartItem(sampleProduct, 4);
        assertEquals(sampleProduct, nested.getProduct());
        assertEquals(4, nested.getQuantity());
        assertEquals(3996.0, nested.getSubtotal(), 0.001);

        nested.setQuantity(6);
        assertEquals(6, nested.getQuantity());
        assertEquals(5994.0, nested.getSubtotal(), 0.001);
    }
}
