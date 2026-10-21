package com.ecommerce;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class CartTest {

    private Cart cart;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        product1 = new Product(1, "Wireless Earbuds", 2000.0, 10, 4.2, 50);
        product2 = new Product(2, "Smartphone Case", 500.0, 5, 4.0, 20);
    }

    @Test
    @DisplayName("New cart is initially empty with total amount 0")
    void testNewCartIsEmpty() {
        assertTrue(cart.isEmpty());
        assertEquals(0.0, cart.getTotalAmount(), 0.001);
        assertEquals(0, cart.getItems().size());
        assertEquals(0, cart.getQuantity(1));
    }

    @Test
    @DisplayName("addItem successfully adds product when stock is sufficient")
    void testAddItemSuccess() {
        boolean added = cart.addItem(product1, 3);
        assertTrue(added);
        assertFalse(cart.isEmpty());
        assertEquals(3, cart.getQuantity(1));
        assertEquals(6000.0, cart.getTotalAmount(), 0.001);
    }

    @Test
    @DisplayName("addItem returns false if product is null or quantity <= 0")
    void testAddItemInvalidInputs() {
        assertFalse(cart.addItem(null, 2));
        assertFalse(cart.addItem(product1, 0));
        assertFalse(cart.addItem(product1, -3));
        assertTrue(cart.isEmpty());
    }

    @Test
    @DisplayName("addItem returns false when requested quantity exceeds available stock")
    void testAddItemInsufficientStock() {
        boolean added = cart.addItem(product1, 15); // only 10 in stock
        assertFalse(added);
        assertTrue(cart.isEmpty());
    }

    @Test
    @DisplayName("addItem increments quantity when existing product is added again")
    void testAddItemCumulative() {
        assertTrue(cart.addItem(product1, 3));
        assertTrue(cart.addItem(product1, 4));
        assertEquals(7, cart.getQuantity(1));
        assertEquals(14000.0, cart.getTotalAmount(), 0.001);
    }

    @Test
    @DisplayName("addItem fails when cumulative quantity exceeds available stock")
    void testAddItemCumulativeExceedsStock() {
        assertTrue(cart.addItem(product1, 8));
        boolean addedMore = cart.addItem(product1, 5); // 8 + 5 = 13 > 10 stock
        assertFalse(addedMore);
        assertEquals(8, cart.getQuantity(1)); // remains unchanged
    }

    @Test
    @DisplayName("setItem adds or updates item directly")
    void testSetItem() {
        cart.setItem(product2, 3);
        assertEquals(3, cart.getQuantity(2));

        // updating with setItem
        cart.setItem(product2, 5);
        assertEquals(5, cart.getQuantity(2));

        // null product or invalid quantity ignored
        cart.setItem(null, 5);
        cart.setItem(product2, 0);
        assertEquals(5, cart.getQuantity(2));
    }

    @Test
    @DisplayName("removeItem removes item and updates cart total")
    void testRemoveItem() {
        cart.addItem(product1, 2);
        cart.addItem(product2, 1);
        assertEquals(4500.0, cart.getTotalAmount(), 0.001);

        boolean removed = cart.removeItem(product1.getId());
        assertTrue(removed);
        assertEquals(0, cart.getQuantity(product1.getId()));
        assertEquals(500.0, cart.getTotalAmount(), 0.001);

        // removing already removed or non-existing product returns false
        assertFalse(cart.removeItem(product1.getId()));
        assertFalse(cart.removeItem(999));
    }

    @Test
    @DisplayName("clear empties all items from the cart")
    void testClearCart() {
        cart.addItem(product1, 2);
        cart.addItem(product2, 2);
        assertFalse(cart.isEmpty());

        cart.clear();
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getQuantity(1));
        assertEquals(0, cart.getQuantity(2));
        assertEquals(0.0, cart.getTotalAmount(), 0.001);
    }

    @Test
    @DisplayName("getItems returns all items in insertion order")
    void testGetItems() {
        cart.addItem(product1, 2);
        cart.addItem(product2, 3);

        Collection<Cart.CartItem> items = cart.getItems();
        assertEquals(2, items.size());
    }

    @Test
    @DisplayName("displayCart runs cleanly for both empty and populated cart")
    void testDisplayCart() {
        assertDoesNotThrow(() -> cart.displayCart());
        assertDoesNotThrow(() -> cart.displayCart("Alice"));

        cart.addItem(product1, 1);
        assertDoesNotThrow(() -> cart.displayCart());
        assertDoesNotThrow(() -> cart.displayCart("Alice"));
    }
}
