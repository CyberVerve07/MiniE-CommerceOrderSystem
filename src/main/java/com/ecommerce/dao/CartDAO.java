package com.ecommerce.dao;

import com.ecommerce.Cart;
import com.ecommerce.DBConnection;
import com.ecommerce.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for cart operations with MySQL.
 * Interacts with the 'cart' table (cart_id, customer_id, product_id, quantity).
 */
public class CartDAO {

    // Retrieve all cart items for a given customer joined with product details
    public List<Cart.CartItem> getCartByCustomerId(int customerId) throws SQLException {
        List<Cart.CartItem> items = new ArrayList<>();
        String sql = "SELECT c.cart_id, c.customer_id, c.product_id, c.quantity, " + "p.name, p.price, p.stock_quantity, p.rating, p.review_count "
                + "FROM cart c "
                + "JOIN products p ON c.product_id = p.product_id "
                + "WHERE c.customer_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product(rs.getInt("product_id"), rs.getString("name"), rs.getDouble("price"),
                            rs.getInt("stock_quantity"),
                            rs.getDouble("rating"),
                            rs.getInt("review_count")
                    );
                    items.add(new Cart.CartItem(product, rs.getInt("quantity")));}
            }}return items;
    }
    // Get current quantity of a specific product in a customer's cart
    public int getCartItemQuantity(int customerId, int productId) throws SQLException {
        String sql = "SELECT quantity FROM cart WHERE customer_id = ? AND product_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");}}}
        return 0;
    }

    // Add product to cart or increase quantity if already present
    public boolean addToCart(int customerId, int productId, int quantity) throws SQLException {
        int existingQuantity = getCartItemQuantity(customerId, productId);
        if (existingQuantity > 0) {
            return updateQuantity(customerId, productId, existingQuantity + quantity);
        }

        String sql = "INSERT INTO cart (customer_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        }
    }

    // Update quantity for a specific product in customer's cart
    public boolean updateQuantity(int customerId, int productId, int quantity) throws SQLException {
        if (quantity <= 0) {
            return removeFromCart(customerId, productId);
        }

        String sql = "UPDATE cart SET quantity = ? WHERE customer_id = ? AND product_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, customerId);
            ps.setInt(3, productId);
            return ps.executeUpdate() > 0;
        }
    }

    // Remove a single product from customer's cart
    public boolean removeFromCart(int customerId, int productId) throws SQLException {
        String sql = "DELETE FROM cart WHERE customer_id = ? AND product_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    // Clear all items from a customer's cart
    public boolean clearCart(int customerId) throws SQLException {
        String sql = "DELETE FROM cart WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() >= 0;
        }
    }

    // Clear all items from a customer's cart within an active transaction connection
    public boolean clearCart(Connection con, int customerId) throws SQLException {
        String sql = "DELETE FROM cart WHERE customer_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() >= 0;
        }
    }

    // Merge in-memory guest cart into the persistent database cart upon login
    public void mergeGuestCart(int customerId, Cart guestCart, ProductDAO productDAO) throws SQLException {
        if (guestCart == null || guestCart.isEmpty()) {
            return;
        }

        for (Cart.CartItem guestItem : guestCart.getItems()) {
            int productId = guestItem.getProduct().getId();
            int guestQty = guestItem.getQuantity();

            Product liveProduct = productDAO.getProductById(productId);
            if (liveProduct == null) {
                continue;
            }

            int availableStock = liveProduct.getQuantity();
            int currentDbQty = getCartItemQuantity(customerId, productId);
            int newTotalQty = currentDbQty + guestQty;

            if (newTotalQty > availableStock) {
                newTotalQty = availableStock;
                System.out.println("Note: Quantity for " + liveProduct.getName()
                        + " merged up to maximum available stock (" + availableStock + ").");
            }

            if (newTotalQty > 0) {
                if (currentDbQty > 0) {
                    updateQuantity(customerId, productId, newTotalQty);
                } else {
                    String insertSql = "INSERT INTO cart (customer_id, product_id, quantity) VALUES (?, ?, ?)";
                    try (Connection con = DBConnection.getConnection();
                         PreparedStatement ps = con.prepareStatement(insertSql)) {
                        ps.setInt(1, customerId);
                        ps.setInt(2, productId);
                        ps.setInt(3, newTotalQty);
                        ps.executeUpdate();}}
            }}
    }}
