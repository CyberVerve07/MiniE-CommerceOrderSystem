package com.ecommerce;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private List<Order.OrderItem> items;
    private Order.OrderItem item1;
    private Order.OrderItem item2;

    @BeforeEach
    void setUp() {
        item1 = new Order.OrderItem(1, "Wireless Mouse", 2, 750.0);
        item2 = new Order.OrderItem(2, "Mechanical Keyboard", 1, 3500.0);

        items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
    }

    @Test
    @DisplayName("OrderItem constructor and getters calculate subtotal correctly")
    void testOrderItem() {
        assertEquals(1, item1.getProductId());
        assertEquals("Wireless Mouse", item1.getProductName());
        assertEquals(2, item1.getQuantity());
        assertEquals(750.0, item1.getUnitPrice());
        assertEquals(1500.0, item1.getSubtotal(), 0.001);

        assertEquals(3500.0, item2.getSubtotal(), 0.001);
    }

    @Test
    @DisplayName("Order constructor with all 9 fields sets attributes properly")
    void testFullOrderConstructor() {
        LocalDateTime date = LocalDateTime.of(2025, 5, 20, 14, 30);
        Order order = new Order(101, 1, "Alice", "123 Main St", "Credit Card", date, items, 5000.0, "DELIVERED");

        assertEquals(101, order.getOrderId());
        assertEquals(1, order.getUserId());
        assertEquals("Alice", order.getCustomerName());
        assertEquals("123 Main St", order.getDeliveryAddress());
        assertEquals("Credit Card", order.getPaymentMethod());
        assertEquals(date, order.getOrderDate());
        assertEquals(2, order.getItems().size());
        assertEquals(5000.0, order.getTotalAmount(), 0.001);
        assertEquals("DELIVERED", order.getStatus());
    }

    @Test
    @DisplayName("Order constructor with custom address and payment method sets defaults for date and status")
    void testOrderConstructorWithAddressAndPayment() {
        Order order = new Order(102, 2, "Bob", "456 Park Ave", "UPI", items, 5000.0);

        assertEquals(102, order.getOrderId());
        assertEquals(2, order.getUserId());
        assertEquals("Bob", order.getCustomerName());
        assertEquals("456 Park Ave", order.getDeliveryAddress());
        assertEquals("UPI", order.getPaymentMethod());
        assertNotNull(order.getOrderDate());
        assertEquals("CONFIRMED", order.getStatus());
    }

    @Test
    @DisplayName("Order constructor with minimal fields defaults to Standard Delivery and Cash on Delivery")
    void testOrderConstructorMinimalDefaults() {
        Order order = new Order(103, 3, "Charlie", items, 5000.0);

        assertEquals(103, order.getOrderId());
        assertEquals(3, order.getUserId());
        assertEquals("Charlie", order.getCustomerName());
        assertEquals("Standard Delivery", order.getDeliveryAddress());
        assertEquals("Cash on Delivery", order.getPaymentMethod());
        assertEquals("CONFIRMED", order.getStatus());
    }

    @Test
    @DisplayName("Order setters update delivery address, payment method, and status")
    void testOrderSetters() {
        Order order = new Order(104, 4, "David", items, 5000.0);

        order.setDeliveryAddress("789 Pine Road");
        order.setPaymentMethod("Net Banking");
        order.setStatus("SHIPPED");

        assertEquals("789 Pine Road", order.getDeliveryAddress());
        assertEquals("Net Banking", order.getPaymentMethod());
        assertEquals("SHIPPED", order.getStatus());
    }

    @Test
    @DisplayName("printOrderSummary prints receipt without errors")
    void testPrintOrderSummary() {
        Order order = new Order(105, 5, "Eve", "Baker St", "Cash on Delivery", items, 5000.0);
        assertDoesNotThrow(order::printOrderSummary);
    }
}
