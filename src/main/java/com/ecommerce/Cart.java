package com.ecommerce;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cart class represents a customer's active shopping cart.
 * It manages items selected for purchase before placing an order.
 * Works for both guest sessions and logged-in users.
 */
public class Cart {

    // Internal class representing an individual item entry inside the cart
    public static class CartItem {
        private Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
        // End of CartItem constructor.

        public Product getProduct() {
            return product;
        }
        // End of getProduct getter.

        public int getQuantity() {
            return quantity;
        }
        // End of getQuantity getter.

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
        // End of setQuantity setter.

        public double getSubtotal() {
            return product.getPrice() * quantity;
        }
        // End of getSubtotal - computes item price multiplied by quantity.
    }

    // Map storing productId -> CartItem (preserves insertion order)
    private final Map<Integer, CartItem> items;

    /**
     * Default constructor to initialize an empty cart.
     */
    public Cart() {
        this.items = new LinkedHashMap<>();
    }
    // End of Cart constructor - creates a fresh item bucket.

    /**
     * Adds a product to the cart with the requested quantity.
     * If the item is already present, increases its quantity while checking stock limits.
     *
     * @param product  The product to add
     * @param quantity Quantity to add
     * @return true if successfully added, false if stock insufficient
     */
    public boolean addItem(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return false;
        }

        int currentQtyInCart = 0;
        if (items.containsKey(product.getId())) {
            currentQtyInCart = items.get(product.getId()).getQuantity();
        }

        int requestedTotal = currentQtyInCart + quantity;

        // Validate stock availability
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
    // End of addItem - inserts or increments item in cart after stock validation.

    /**
     * Removes an item completely from the cart by product ID.
     *
     * @param productId The ID of the product to remove
     * @return true if item was found and removed, false otherwise
     */
    public boolean removeItem(int productId) {
        return items.remove(productId) != null;
    }
    // End of removeItem - deletes product entry from active cart.

    /**
     * Calculates the total payable amount for all items in the cart.
     *
     * @return Total price of all cart items
     */
    public double getTotalAmount() {
        double total = 0.0;
        for (CartItem item : items.values()) {
            total += item.getSubtotal();
        }
        return total;
    }
    // End of getTotalAmount - aggregates subtotal of each cart line-item.

    /**
     * Clears all items from the cart (used after checkout or manual reset).
     */
    public void clear() {
        items.clear();
    }
    // End of clear - empties the cart.

    /**
     * Checks if the shopping cart currently has no items.
     *
     * @return true if cart is empty, false otherwise
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }
    // End of isEmpty - determines if cart contains any products.

    /**
     * Returns all items currently in the cart.
     *
     * @return Collection of CartItem objects
     */
    public Collection<CartItem> getItems() {
        return items.values();
    }
    // End of getItems - retrieves all cart items.

    /**
     * Formats and prints the contents of the cart cleanly to the console.
     */
    public void displayCart() {
        if (items.isEmpty()) {
            System.out.println("🛒 Your cart is currently empty.");
            return;
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("                             YOUR SHOPPING CART                                 ");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-6s | %-25s | %-12s | %-8s | %-12s%n",
                "Prod ID", "Product Name", "Unit Price", "Qty", "Subtotal");
        System.out.println("--------------------------------------------------------------------------------");

        for (CartItem item : items.values()) {
            Product p = item.getProduct();
            System.out.printf("%-7d | %-25s | ₹%-10.2f | %-8d | ₹%-10.2f%n",
                    p.getId(), p.getName(), p.getPrice(), item.getQuantity(), item.getSubtotal());
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("Total Cart Value: ₹%.2f%n", getTotalAmount());
        System.out.println("--------------------------------------------------------------------------------");
    }
    // End of displayCart - prints tabular summary of cart products and total price.
}
