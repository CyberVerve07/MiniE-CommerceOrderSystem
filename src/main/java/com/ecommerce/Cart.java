package com.ecommerce;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

// Cart class managing items selected by user before checkout
public class Cart {

    // Internal class representing cart item
    public static class CartItem {
        private Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getSubtotal() {
            return product.getPrice() * quantity;
        }
    }

    private final Map<Integer, CartItem> items;

    // Default constructor
    public Cart() {
        this.items = new LinkedHashMap<>();
    }

    // Add product to cart or increase quantity
    public boolean addItem(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return false;
        }

        int currentQtyInCart = 0;
        if (items.containsKey(product.getId())) {
            currentQtyInCart = items.get(product.getId()).getQuantity();
        }

        int requestedTotal = currentQtyInCart + quantity;

        // Check product stock
        if (!product.hasSufficientStock(requestedTotal)) {
            return false;
        }

        if (items.containsKey(product.getId())) {
            items.get(product.getId()).setQuantity(requestedTotal);
        } else {
            items.put(product.getId(), new CartItem(product, quantity));
        }

        return true;
    }

    // Remove item from cart by product ID
    public boolean removeItem(int productId) {
        return items.remove(productId) != null;
    }

    // Calculate total amount of cart
    public double getTotalAmount() {
        double total = 0.0;
        for (CartItem item : items.values()) {
            total += item.getSubtotal();
        }
        return total;
    }

    // Clear cart items
    public void clear() {
        items.clear();
    }

    // Check if cart is empty
    public boolean isEmpty() {
        return items.isEmpty();
    }

    // Get all cart items
    public Collection<CartItem> getItems()
    {
        return items.values();
    }

    // Display cart contents in formatted table
    public void displayCart(String userName) {
        if (items.isEmpty()) {
            System.out.println("Your cart is currently empty.");
            return;
        }
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("                         SHOPPING CART - [" + userName + "]");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-7s | %-25s | %-12s | %-10s | %-12s%n",
                "Prod ID", "Product Name", "Price (Rs)", "Quantity", "Subtotal");
        System.out.println("--------------------------------------------------------------------------------");

        for (CartItem item : items.values()) {
            Product p = item.getProduct();
            System.out.printf("%-7d | %-25s | %-12.2f | %-10d | %-12.2f%n",
                    p.getId(), p.getName(), p.getPrice(), item.getQuantity(), item.getSubtotal());
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("Total Cart Value: Rs %.2f%n", getTotalAmount());
        System.out.println("--------------------------------------------------------------------------------");
    }

    // Overloaded displayCart without username
    public void displayCart()
    {
        displayCart("Guest");
    }
}
