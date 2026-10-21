package com.ecommerce.dao;

import com.ecommerce.Cart;
import com.ecommerce.DBConnection;
import com.ecommerce.Order;
import com.ecommerce.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for order operations with MySQL.
 * Interacts with 'orders' and 'order_items' tables with full transaction management.
 */
public class OrderDAO {

    /**
     * Complete transactional database-backed order placement.
     * Process:
     * 1. Verify customer logged in & cart not empty
     * 2. Revalidate stock using the database
     * 3. Start JDBC transaction using setAutoCommit(false)
     * 4. Insert order into orders table and retrieve generated ID
     * 5. Insert order items into order_items table
     * 6. Deduct product stock in products table
     * 7. Clear customer cart in cart table
     * 8. Commit transaction (or rollback on error)
     */
    public Order placeOrder(User user, Cart cart, String address, String paymentMethod) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("Customer must be logged in to place an order.");
        }
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty when placing an order.");
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Step 3: Start JDBC transaction

            // Step 2: Revalidate stock using database
            String stockSql = "SELECT product_id, name, stock_quantity, price FROM products WHERE product_id = ? FOR UPDATE";
            try (PreparedStatement stockPs = con.prepareStatement(stockSql)) {
                for (Cart.CartItem item : cart.getItems()) {
                    stockPs.setInt(1, item.getProduct().getId());
                    try (ResultSet rs = stockPs.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Product with ID " + item.getProduct().getId() + " no longer exists.");
                        }
                        int currentStock = rs.getInt("stock_quantity");
                        String productName = rs.getString("name");
                        if (currentStock < item.getQuantity()) {
                            throw new SQLException("Insufficient quantity for " + productName
                                    + " (Available Quantity: " + currentStock + ", Requested: " + item.getQuantity() + ").");
                        }
                    }
                }
            }

            // Step 4: Insert order into orders table and retrieve its generated ID
            String insertOrderSql = "INSERT INTO orders (customer_id, order_date, total_amount, order_status, delivery_address, payment_method) VALUES (?, ?, ?, ?, ?, ?)";
            int orderId = 0;
            LocalDateTime now = LocalDateTime.now();
            double totalAmount = cart.getTotalAmount();
            String status = "CONFIRMED";
            String finalAddress = (address == null || address.trim().isEmpty()) ? "Standard Delivery" : address.trim();
            String finalPayment = (paymentMethod == null || paymentMethod.trim().isEmpty()) ? "Cash on Delivery" : paymentMethod.trim();

            try (PreparedStatement orderPs = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderPs.setInt(1, user.getUserId());
                orderPs.setTimestamp(2, Timestamp.valueOf(now));
                orderPs.setDouble(3, totalAmount);
                orderPs.setString(4, status);
                orderPs.setString(5, finalAddress);
                orderPs.setString(6, finalPayment);

                int rowsAffected = orderPs.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Creating order failed, no rows affected.");
                }

                try (ResultSet generatedKeys = orderPs.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating order failed, no ID obtained.");
                    }
                }
            }

            // Step 5: Insert order items into order_items table
            String insertItemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
            List<Order.OrderItem> orderItems = new ArrayList<>();
            try (PreparedStatement itemPs = con.prepareStatement(insertItemSql)) {
                for (Cart.CartItem item : cart.getItems()) {
                    itemPs.setInt(1, orderId);
                    itemPs.setInt(2, item.getProduct().getId());
                    itemPs.setInt(3, item.getQuantity());
                    itemPs.setDouble(4, item.getProduct().getPrice());
                    itemPs.addBatch();

                    orderItems.add(new Order.OrderItem(
                            item.getProduct().getId(),
                            item.getProduct().getName(),
                            item.getQuantity(),
                            item.getProduct().getPrice()
                    ));
                }
                itemPs.executeBatch();
            }

            // Step 6: Deduct product stock in products table
            String updateStockSql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
            try (PreparedStatement updateStockPs = con.prepareStatement(updateStockSql)) {
                for (Cart.CartItem item : cart.getItems()) {
                    updateStockPs.setInt(1, item.getQuantity());
                    updateStockPs.setInt(2, item.getProduct().getId());
                    updateStockPs.addBatch();
                }
                updateStockPs.executeBatch();
            }

            // Step 7: Clear customer cart in cart table
            String clearCartSql = "DELETE FROM cart WHERE customer_id = ?";
            try (PreparedStatement clearCartPs = con.prepareStatement(clearCartSql)) {
                clearCartPs.setInt(1, user.getUserId());
                clearCartPs.executeUpdate();
            }

            // Step 8: Commit transaction
            con.commit();

            return new Order(orderId, user.getUserId(), user.getFullName(), finalAddress, finalPayment, now, orderItems, totalAmount, status);

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Retrieve all orders for a specific customer from MySQL
    public List<Order> getOrdersByCustomerId(int customerId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.order_id, o.customer_id, o.order_date, o.total_amount, o.order_status, "
                + "o.delivery_address, o.payment_method, c.name AS customer_name "
                + "FROM orders o "
                + "JOIN customers c ON o.customer_id = c.customer_id "
                + "WHERE o.customer_id = ? "
                + "ORDER BY o.order_date DESC, o.order_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    int userId = rs.getInt("customer_id");
                    String customerName = rs.getString("customer_name");
                    Timestamp ts = rs.getTimestamp("order_date");
                    LocalDateTime orderDate = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                    double totalAmount = rs.getDouble("total_amount");
                    String status = rs.getString("order_status");
                    String address = rs.getString("delivery_address");
                    String payment = rs.getString("payment_method");

                    List<Order.OrderItem> items = getOrderItems(con, orderId);
                    orders.add(new Order(orderId, userId, customerName, address, payment, orderDate, items, totalAmount, status));
                }
            }
        }
        return orders;
    }

    // Retrieve items for a specific order
    public List<Order.OrderItem> getOrderItems(Connection con, int orderId) throws SQLException {
        List<Order.OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.order_item_id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, p.name AS product_name "
                + "FROM order_items oi "
                + "JOIN products p ON oi.product_id = p.product_id "
                + "WHERE oi.order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new Order.OrderItem(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price")
                    ));
                }
            }
        }
        return items;
    }

    // Retrieve an order by its ID
    public Order getOrderById(int orderId) throws SQLException {
        String sql = "SELECT o.order_id, o.customer_id, o.order_date, o.total_amount, o.order_status, "
                + "o.delivery_address, o.payment_method, c.name AS customer_name "
                + "FROM orders o "
                + "JOIN customers c ON o.customer_id = c.customer_id "
                + "WHERE o.order_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("customer_id");
                    String customerName = rs.getString("customer_name");
                    Timestamp ts = rs.getTimestamp("order_date");
                    LocalDateTime orderDate = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                    double totalAmount = rs.getDouble("total_amount");
                    String status = rs.getString("order_status");
                    String address = rs.getString("delivery_address");
                    String payment = rs.getString("payment_method");

                    List<Order.OrderItem> items = getOrderItems(con, orderId);
                    return new Order(orderId, userId, customerName, address, payment, orderDate, items, totalAmount, status);
                }
            }
        }
        return null;
    }

    // Retrieve all orders in the system
    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.order_id, o.customer_id, o.order_date, o.total_amount, o.order_status, "
                + "o.delivery_address, o.payment_method, c.name AS customer_name "
                + "FROM orders o "
                + "JOIN customers c ON o.customer_id = c.customer_id "
                + "ORDER BY o.order_date DESC, o.order_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                int userId = rs.getInt("customer_id");
                String customerName = rs.getString("customer_name");
                Timestamp ts = rs.getTimestamp("order_date");
                LocalDateTime orderDate = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                double totalAmount = rs.getDouble("total_amount");
                String status = rs.getString("order_status");
                String address = rs.getString("delivery_address");
                String payment = rs.getString("payment_method");

                List<Order.OrderItem> items = getOrderItems(con, orderId);
                orders.add(new Order(orderId, userId, customerName, address, payment, orderDate, items, totalAmount, status));
            }
        }
        return orders;
    }
}
