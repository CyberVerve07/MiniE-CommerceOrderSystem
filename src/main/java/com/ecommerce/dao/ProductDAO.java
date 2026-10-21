package com.ecommerce.dao;

import com.ecommerce.DBConnection;
import com.ecommerce.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getAllProducts() throws SQLException {

        List<Product> products = new ArrayList<>();

        String sql = "SELECT product_id, name, price, stock_quantity, rating, review_count FROM products";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Product product = new Product(rs.getInt("product_id"), rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getDouble("rating"),
                        rs.getInt("review_count")
                );

                products.add(product);
            }
        }

        return products;
    }
    // Retrieve a single product by its ID from MySQL
    public Product getProductById(int productId) throws SQLException {
        String sql = "SELECT product_id, name, price, stock_quantity, rating, review_count FROM products WHERE product_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock_quantity"),
                            rs.getDouble("rating"),
                            rs.getInt("review_count")
                    );
                }
            }
        }
        return null;
    }

    // Search products by name (case-insensitive substring)
    public List<Product> searchProductsByName(String query) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, price, stock_quantity, rating, review_count FROM products WHERE LOWER(name) LIKE ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + query.trim().toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(new Product(
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock_quantity"),
                            rs.getDouble("rating"),
                            rs.getInt("review_count")
                    ));
                }
            }
        }
        return products;
    }

    // Reduce stock for a product within an active JDBC transaction
    public boolean reduceStock(Connection con, int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ? AND stock_quantity >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        }
    }

    // Get current stock quantity directly from MySQL
    public int getStock(int productId) throws SQLException {
        String sql = "SELECT stock_quantity FROM products WHERE product_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock_quantity");
                }
            }
        }
        return 0;
    }

    // Update stock quantity for a product
    public boolean updateStock(int productId, int newQuantity) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = ? WHERE product_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    public static void main(String[] args) {

        ProductDAO dao = new ProductDAO();

        try {
            List<Product> products = dao.getAllProducts();

            for (Product product : products) {
                System.out.println(product);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
