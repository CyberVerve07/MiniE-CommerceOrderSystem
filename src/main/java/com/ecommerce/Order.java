package com.ecommerce;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Order entity class storing placed order details
public class Order {

    // Nested class representing each item inside an order
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

        public int getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public double getSubtotal() {
            return unitPrice * quantity;
        }
    }

    private int orderId;
    private int userId;
    private String customerName;
    private String deliveryAddress;
    private String paymentMethod;
    private LocalDateTime orderDate;
    private List<OrderItem> items;
    private double totalAmount;
    private String status;

    // Constructor with address and payment method
    public Order(int orderId, int userId, String customerName, String deliveryAddress, String paymentMethod, List<OrderItem> items, double totalAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.customerName = customerName;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.orderDate = LocalDateTime.now();
        this.items = new ArrayList<>(items);
        this.totalAmount = totalAmount;
        this.status = "CONFIRMED";
    }

    // Overloaded constructor for default address/payment
    public Order(int orderId, int userId, String customerName, List<OrderItem> items, double totalAmount) {
        this(orderId, userId, customerName, "Standard Delivery", "Cash on Delivery", items, totalAmount);
    }

    // Getters and Setters

    public int getOrderId() {
        return orderId;
    }

    public int getUserId() {
        return userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Print formatted order bill receipt
    public void printOrderSummary() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
        System.out.println("================================================================================");
        System.out.println("                               ORDER INVOICE RECEIPT                            ");
        System.out.println("================================================================================");
        System.out.printf(" Order ID         : #%d%n", orderId);
        System.out.printf(" Customer Name    : %s%n", customerName);
        System.out.printf(" Order Date       : %s%n", orderDate.format(formatter));
        System.out.printf(" Delivery Address : %s%n", deliveryAddress);
        System.out.printf(" Payment Method   : %s%n", paymentMethod);
        System.out.printf(" Order Status     : %s%n", status);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-8s | %-25s | %-12s | %-10s | %-12s%n",
                "Prod ID", "Product Name", "Price (Rs)", "Quantity", "Subtotal");
        System.out.println("--------------------------------------------------------------------------------");

        for (OrderItem item : items) {
            System.out.printf("%-8d | %-25s | %-12.2f | %-10d | %-12.2f%n",
                    item.getProductId(), item.getProductName(), item.getUnitPrice(),
                    item.getQuantity(), item.getSubtotal());
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf(" Grand Total      : Rs %.2f%n", totalAmount);
        System.out.println("================================================================================");
    }
}
