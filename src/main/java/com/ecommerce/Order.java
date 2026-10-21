package com.ecommerce;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Order class represents a confirmed purchase by a registered user.
 * It stores order metadata, line items purchased, total bill, and placement date.
 */
public class Order {

    // Nested class representing an individual ordered line item
    public static class OrderItem {
        private int productId;
        private String productName;
        private int quantity;
        private double unitPrice;

        public OrderItem(int productId, String productName, int quantity, double unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        // End of OrderItem constructor.

        public int getProductId() {
            return productId;
        }
        // End of getProductId getter.

        public String getProductName() {
            return productName;
        }
        // End of getProductName getter.

        public int getQuantity() {
            return quantity;
        }
        // End of getQuantity getter.

        public double getUnitPrice() {
            return unitPrice;
        }
        // End of getUnitPrice getter.

        public double getSubtotal() {
            return unitPrice * quantity;
        }
        // End of getSubtotal - calculates total cost for this order item.
    }

    private int orderId;
    private int userId;
    private String customerName;
    private LocalDateTime orderDate;
    private List<OrderItem> items;
    private double totalAmount;
    private String status;

    /**
     * Parameterized constructor for generating a complete order.
     *
     * @param orderId      Unique tracking ID for the order
     * @param userId       ID of the customer who placed the order
     * @param customerName Full name of the customer
     * @param items        List of ordered items
     * @param totalAmount  Total bill amount
     */
    public Order(int orderId, int userId, String customerName, List<OrderItem> items, double totalAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.customerName = customerName;
        this.orderDate = LocalDateTime.now();
        this.items = new ArrayList<>(items);
        this.totalAmount = totalAmount;
        this.status = "CONFIRMED";
    }
    // End of Order constructor - initializes placed order with timestamp and items.

    // --- Standard Getters and Setters ---

    public int getOrderId() {
        return orderId;
    }
    // End of getOrderId getter.

    public int getUserId() {
        return userId;
    }
    // End of getUserId getter.

    public String getCustomerName() {
        return customerName;
    }
    // End of getCustomerName getter.

    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    // End of getOrderDate getter.

    public List<OrderItem> getItems() {
        return items;
    }
    // End of getItems getter.

    public double getTotalAmount() {
        return totalAmount;
    }
    // End of getTotalAmount getter.

    public String getStatus() {
        return status;
    }
    // End of getStatus getter.

    public void setStatus(String status) {
        this.status = status;
    }
    // End of setStatus setter.

    /**
     * Formats and prints a detailed order receipt to the console.
     */
    public void printOrderSummary() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
        System.out.println("================================================================================");
        System.out.printf(" ORDER #%d | Customer: %s | Status: %s%n", orderId, customerName, status);
        System.out.printf(" Placed On: %s%n", orderDate.format(formatter));
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-8s | %-25s | %-12s | %-8s | %-12s%n",
                "Prod ID", "Item Name", "Unit Price", "Qty", "Subtotal");
        System.out.println("--------------------------------------------------------------------------------");

        for (OrderItem item : items) {
            System.out.printf("%-8d | %-25s | ₹%-10.2f | %-8d | ₹%-10.2f%n",
                    item.getProductId(), item.getProductName(), item.getUnitPrice(),
                    item.getQuantity(), item.getSubtotal());
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf(" Total Paid: ₹%.2f%n", totalAmount);
        System.out.println("================================================================================");
    }
    // End of printOrderSummary - outputs printable receipt for customer order.
}
