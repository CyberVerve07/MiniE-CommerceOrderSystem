package com.ecommerce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Service class handling order placement and history
public class OrderService {

    private final List<Order> orderHistory;
    private int nextOrderId;

    // Constructor initializes order storage and starting order ID
    public OrderService() {
        this.orderHistory = new ArrayList<>();
        this.nextOrderId = 1001;
    }

    // Place an order with delivery address and payment method
    public Order placeOrder(User user, Cart cart, String address, String paymentMethod) {
        if (user == null) {
            System.out.println("You must log in or register before placing an order!");
            return null;
        }

        if (cart == null || cart.isEmpty()) {
            System.out.println("Your cart is empty. Add some products before checking out!");
            return null;
        }

        // Validate stock availability for all items before placing order
        for (Cart.CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.hasSufficientStock(item.getQuantity())) {
                System.out.println("Order failed: Insufficient quantity for " + product.getName()
                        + " (Available Quantity: " + product.getQuantity() + ", Requested: " + item.getQuantity() + ").");
                System.out.println("Transaction cancelled: No changes made to inventory or cart.");
                return null;
            }
        }

        // Deduct inventory and prepare order items
        List<Order.OrderItem> orderItems = new ArrayList<>();
        for (Cart.CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            product.reduceStock(item.getQuantity());
            orderItems.add(new Order.OrderItem(product.getId(), product.getName(), item.getQuantity(), product.getPrice()
            ));
        }

        // Create and save confirmed order
        double totalBill = cart.getTotalAmount();
        String finalAddress = (address == null || address.trim().isEmpty()) ? "Standard Delivery" : address.trim();
        String finalPayment = (paymentMethod == null || paymentMethod.trim().isEmpty()) ? "Cash on Delivery" : paymentMethod.trim();

        Order newOrder = new Order(nextOrderId++, user.getUserId(), user.getFullName(), finalAddress, finalPayment, orderItems, totalBill);
        orderHistory.add(newOrder);

        // Clear cart after checkout
        cart.clear();

        System.out.println("Order placed successfully! Thank you for shopping with us.");
        newOrder.printOrderSummary();
        return newOrder;
    }

    // Overloaded placeOrder without custom address/payment
    public Order placeOrder(User user, Cart cart) {
        return placeOrder(user, cart, "Standard Address", "Cash on Delivery");
    }

    // Get list of orders for a user ID
    public List<Order> getOrdersByUserId(int userId) {
        List<Order> result = new ArrayList<>();
        for (Order order : orderHistory) {
            if (order.getUserId() == userId) {result.add(order);
            }
        }
        return result;
    }

    // Display order history of user
    public void displayUserOrderHistory(User user) {
        if (user == null) {
            System.out.println("Please log in to view your order history.");
            return;
        }

        List<Order> userOrders = getOrdersByUserId(user.getUserId());
        if (userOrders.isEmpty()) {
            System.out.println("You have not placed any orders yet.");
            return;
        }

        System.out.println("================================================================================");
        System.out.println("                        ORDER HISTORY FOR " + user.getFullName().toUpperCase());
        System.out.println("================================================================================");

        for (Order order : userOrders) {
            order.printOrderSummary();
            System.out.println();
        }
    }
    // Get all orders in system
    public List<Order> getAllOrders() {
        return Collections.unmodifiableList(orderHistory);
    }

    // Find order by order ID
    public Order findOrderById(int orderId) {
        for (Order order : orderHistory) {
            if (order.getOrderId() == orderId) {
                return order;
            }
        }
        return null;
    }
}
