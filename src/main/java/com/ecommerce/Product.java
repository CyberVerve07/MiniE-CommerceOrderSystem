package com.ecommerce;

// Product entity class representing store inventory
public class Product {

    private int id;
    private String name;
    private double price;
    private int quantity;
    private double rating;
    private int reviewCount;

    // Constructor with rating and review count
    public Product(int id, String name, double price, int quantity, double rating, int reviewCount) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    // Overloaded constructor with default review count
    public Product(int id, String name, double price, int quantity, double rating) {
        this(id, name, price, quantity, rating, 50);
    }

    // Overloaded constructor with default rating
    public Product(int id, String name, double price, int quantity) {
        this(id, name, price, quantity, 4.0, 20);
    }

    // Check if sufficient quantity is in stock
    public boolean hasSufficientStock(int reqQty) {
        return this.quantity >= reqQty && reqQty > 0;
    }

    // Reduce product stock after order is placed
    public void reduceStock(int reqQty) {
        if (hasSufficientStock(reqQty)) {
            this.quantity -= reqQty;
        } else {
            throw new IllegalArgumentException("Insufficient quantity available.");
        }
    }

    // Restock product quantity
    public void addStock(int reqQty) {
        if (reqQty > 0) {
            this.quantity += reqQty;
        }
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getStock() {
        return quantity;
    }

    public void setStock(int stock) {
        this.quantity = stock;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    // Formatted rating string for display
    public String getRatingDisplay() {

        return String.format("%.1f/5.0 (%d reviews)", rating, reviewCount);
    }

    @Override
    public String toString() {

        return String.format("%-4d %-18s %-12.2f %-10d %s", id, name, price, quantity, getRatingDisplay());
    }
}
