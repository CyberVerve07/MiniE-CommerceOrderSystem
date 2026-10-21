package com.ecommerce;

import com.ecommerce.dao.OrderDAO;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

// Service class handling order placement and history backed by MySQL
public class OrderService {

    private final OrderDAO orderDAO;

    // Constructor initializes OrderDAO
    public OrderService() {
        this.orderDAO = new OrderDAO();
    }

    public OrderService(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    // Place an order with delivery address and payment method using database transaction
    public Order placeOrder(User user, Cart cart, String address, String paymentMethod) {
        if (user == null) {
            System.out.println("You must log in or register before placing an order!");
            return null;
        }
        if (cart == null || cart.isEmpty()) {
            System.out.println("Your cart is empty. Add some products before checking out!");
            return null;
        }

        try {
            Order newOrder = orderDAO.placeOrder(user, cart, address, paymentMethod);
            if (newOrder != null) {
                cart.clear();
                System.out.println("Order placed successfully! Thank you for shopping with us.");
                newOrder.printOrderSummary();
                return newOrder;
            }
        } catch (SQLException e) {
            System.out.println("Order failed: " + e.getMessage());
            System.out.println("Transaction cancelled: No changes made to inventory or cart.");
        }
        return null;
    }

    // Overloaded placeOrder without custom address/payment
    public Order placeOrder(User user, Cart cart) {
        return placeOrder(user, cart, "Standard Delivery", "Cash on Delivery");
    }

    // Get list of orders for a user ID from MySQL
    public List<Order> getOrdersByUserId(int userId) {
        try {
            return orderDAO.getOrdersByCustomerId(userId);
        } catch (SQLException e) {
            System.out.println("Unable to fetch order history from database.");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Display order history of user from MySQL
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
        try {
            return orderDAO.getAllOrders();
        } catch (SQLException e) {
            System.out.println("Unable to fetch all orders from database.");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Find order by order ID
    public Order findOrderById(int orderId) {
        try {
            return orderDAO.getOrderById(orderId);
        } catch (SQLException e) {
            System.out.println("Unable to fetch order from database.");
            e.printStackTrace();
            return null;
        }
    }
}
