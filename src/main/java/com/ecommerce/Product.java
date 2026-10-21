package com.ecommerce;

/**
 * Product class represents an item available for purchase in the store.
 * It holds details like product ID, name, price, and current stock level.
 */
public class Product {

    private int id;
    private String name;
    private double price;
    private int stock;

    /**
     * Parameterized constructor to initialize a product.
     *
     * @param id    Unique identifier for the product
     * @param name  Display name of the product
     * @param price Unit price of the product
     * @param stock Available quantity in the warehouse
     */
    public Product(int id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
    // End of Product constructor - sets up initial product state.

    /**
     * Checks if requested quantity is available in stock.
     *
     * @param quantity The quantity customer wants to purchase
     * @return true if stock is sufficient, false otherwise
     */
    public boolean hasSufficientStock(int quantity) {
        return this.stock >= quantity && quantity > 0;
    }
    // End of hasSufficientStock - validates quantity against available stock.

    /**
     * Deducts quantity from available stock when an order is placed.
     *
     * @param quantity Number of units to deduct
     */
    public void reduceStock(int quantity) {
        if (hasSufficientStock(quantity)) {
            this.stock -= quantity;
        } else {
            throw new IllegalArgumentException("Cannot reduce stock: insufficient quantity available.");
        }
    }
    // End of reduceStock - securely reduces product inventory.

    /**
     * Adds units back to stock (e.g. for restock or order cancellation).
     *
     * @param quantity Number of units to add
     */
    public void addStock(int quantity) {
        if (quantity > 0) {
            this.stock += quantity;
        }
    }
    // End of addStock - increases product stock count.

    // --- Standard Getters and Setters ---

    public int getId() {
        return id;
    }
    // End of getId getter.

    public void setId(int id) {
        this.id = id;
    }
    // End of setId setter.

    public String getName() {
        return name;
    }
    // End of getName getter.

    public void setName(String name) {
        this.name = name;
    }
    // End of setName setter.

    public double getPrice() {
        return price;
    }
    // End of getPrice getter.

    public void setPrice(double price) {
        this.price = price;
    }
    // End of setPrice setter.

    public int getStock() {
        return stock;
    }
    // End of getStock getter.

    public void setStock(int stock) {
        this.stock = stock;
    }
    // End of setStock setter.

    /**
     * Formats product info for clean console display.
     */
    @Override
    public String toString() {
        return String.format("[ID: %-3d] %-25s | Price: ₹%-8.2f | Available Stock: %-4d",
                id, name, price, stock);
    }
    // End of toString - formats product details for console printing.
}
