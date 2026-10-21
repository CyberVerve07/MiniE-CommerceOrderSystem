package com.ecommerce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EcommerceService acts as the core business engine of the application.
 * It manages products, user registration/login, shopping cart operations,
 * stock validation, and order placement with mock transaction handling.
 */
public class EcommerceService {

    // In-memory data storage (simulating database tables)
    private final List<Product> productCatalog;
    private final Map<String, User> userRegistry; // username -> User
    private final List<Order> orderHistory;

    // Session state
    private final Cart cart;
    private User currentUser;

    // Auto-increment counters
    private int nextUserId = 101;
    private int nextOrderId = 1001;

    /**
     * Constructor initializes catalog, registers sample users and products,
     * and sets up a fresh shopping cart.
     */
    public EcommerceService() {
        this.productCatalog = new ArrayList<>();
        this.userRegistry = new HashMap<>();
        this.orderHistory = new ArrayList<>();
        this.cart = new Cart();
        this.currentUser = null;

        // Populate sample data so the application is ready to use immediately
        seedSampleData();
    }
    // End of EcommerceService constructor - initializes in-memory state and dummy data.

    /**
     * Pre-populates default products and a default demo user for easy testing.
     */
    private void seedSampleData() {
        // Sample Products (Electronics & Gadgets)
        productCatalog.add(new Product(1, "Dell Inspiron 15 Laptop", 54999.00, 10));
        productCatalog.add(new Product(2, "Samsung Galaxy M34 5G", 18999.00, 15));
        productCatalog.add(new Product(3, "Sony WH-1000XM4 Headphones", 22990.00, 8));
        productCatalog.add(new Product(4, "Apple Watch SE", 29900.00, 5));
        productCatalog.add(new Product(5, "Logitech MX Master 3S Mouse", 8495.00, 20));
        productCatalog.add(new Product(6, "Keychron Mechanical Keyboard", 7499.00, 12));

        // Sample Demo User (username: rahul, password: password123)
        User demoUser = new User(100, "Rahul Sharma", "rahul", "password123", "rahul@example.com");
        userRegistry.put(demoUser.getUsername().toLowerCase(), demoUser);
    }
    // End of seedSampleData - inserts initial mock catalog and user.

    // =========================================================================
    // PRODUCT CATALOG OPERATIONS
    // =========================================================================

    /**
     * Displays all available products in a clean, readable tabular format.
     */
    public void displayAllProducts() {
        System.out.println("================================================================================");
        System.out.println("                             AVAILABLE PRODUCTS                                 ");
        System.out.println("================================================================================");
        System.out.printf("%-5s | %-32s | %-14s | %-10s%n", "ID", "Product Name", "Price (₹)", "Stock");
        System.out.println("--------------------------------------------------------------------------------");

        for (Product product : productCatalog) {
            System.out.printf("%-5d | %-32s | ₹%-13.2f | %-10d%n",
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getStock());
        }

        System.out.println("================================================================================");
    }
    // End of displayAllProducts - lists all items in catalog with prices and inventory.

    /**
     * Finds a product by its unique integer identifier.
     *
     * @param productId The ID to look up
     * @return Product if found, or null if not found
     */
    public Product findProductById(int productId) {
        for (Product product : productCatalog) {
            if (product.getId() == productId) {
                return product;
            }
        }
        return null;
    }
    // End of findProductById - searches catalog for matching product ID.

    // =========================================================================
    // USER AUTHENTICATION OPERATIONS
    // =========================================================================

    /**
     * Registers a new customer into the system.
     *
     * @param fullName Customer's full name
     * @param username Desired unique username
     * @param password Account password
     * @param email    Customer contact email
     * @return true if registration succeeded, false if username already taken
     */
    public boolean registerUser(String fullName, String username, String password, String email) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            System.out.println(" Registration failed: Username and password cannot be empty.");
            return false;
        }

        String key = username.trim().toLowerCase();
        if (userRegistry.containsKey(key)) {
            System.out.println(" Registration failed: Username '" + username + "' is already registered.");
            return false;
        }

        User newUser = new User(nextUserId++, fullName.trim(), username.trim(), password, email.trim());
        userRegistry.put(key, newUser);
        System.out.println(" Registration successful! You can now log in with username: " + username);
        return true;
    }
    // End of registerUser - creates a new user profile and stores in registry.

    /**
     * Authenticates a user with username and password.
     *
     * @param username Login username
     * @param password Login password
     * @return true if login succeeded, false otherwise
     */
    public boolean loginUser(String username, String password) {
        if (username == null || password == null) {
            System.out.println("❌ Login failed: Invalid credentials.");
            return false;
        }

        String key = username.trim().toLowerCase();
        User user = userRegistry.get(key);

        if (user == null || !user.checkPassword(password)) {
            System.out.println("❌ Login failed: Incorrect username or password.");
            return false;
        }

        this.currentUser = user;
        System.out.println("✅ Welcome back, " + user.getFullName() + "! You are now logged in.");
        return true;
    }
    // End of loginUser - validates credentials and sets currentUser session.

    /**
     * Logs out the currently active user session.
     */
    public void logoutUser() {
        if (this.currentUser != null) {
            System.out.println("👋 Logged out successfully. Goodbye, " + currentUser.getFullName() + "!");
            this.currentUser = null;
        } else {
            System.out.println("ℹ️ No active user is currently logged in.");
        }
    }
    // End of logoutUser - clears current active user session.

    /**
     * Checks if a user is currently logged in.
     *
     * @return true if logged in, false if browsing as guest
     */
    public boolean isLoggedIn() {
        return this.currentUser != null;
    }
    // End of isLoggedIn - returns login status.

    /**
     * Gets the currently logged in user.
     *
     * @return Current User or null if guest
     */
    public User getCurrentUser() {
        return currentUser;
    }
    // End of getCurrentUser - returns session user object.

    // =========================================================================
    // CART OPERATIONS (Works for guests as well as logged-in users)
    // =========================================================================

    /**
     * Adds an item to the shopping cart after validating stock.
     *
     * @param productId ID of product to add
     * @param quantity  Number of items to add
     * @return true if added, false if product not found or insufficient stock
     */
    public boolean addProductToCart(int productId, int quantity) {
        if (quantity <= 0) {
            System.out.println(" Invalid quantity! Please enter a quantity greater than zero.");
            return false;
        }

        Product product = findProductById(productId);
        if (product == null) {
            System.out.println(" Product with ID " + productId + " does not exist.");
            return false;
        }

        boolean success = cart.addItem(product, quantity);
        if (success) {
            System.out.println(" Added " + quantity + " x " + product.getName() + " to your cart.");
            return true;
        } else {
            System.out.println(" Cannot add to cart: Requested quantity exceeds available stock ("
                    + product.getStock() + " available).");
            return false;
        }
    }
    // End of addProductToCart - checks stock and adds line item to cart.

    /**
     * Removes an item from the cart.
     *
     * @param productId Product ID to remove
     */
    public void removeProductFromCart(int productId) {
        if (cart.isEmpty()) {
            System.out.println("🛒 Your cart is already empty.");
            return;
        }

        boolean removed = cart.removeItem(productId);
        if (removed) {
            System.out.println("Product removed from your cart.");
        } else {
            System.out.println("Product ID " + productId + " was not found in your cart.");
        }
    }
    // End of removeProductFromCart - removes product from cart if present.

    /**
     * Displays the current contents and total value of the shopping cart.
     */
    public void viewCart() {
        cart.displayCart();
    }
    // End of viewCart - renders shopping cart contents.

    /**
     * Retrieves the internal cart reference.
     */
    public Cart getCart() {
        return cart;
    }
    // End of getCart - returns active cart instance.

    // =========================================================================
    // ORDER PLACEMENT & HISTORY (Requires Login)
    // =========================================================================

    /**
     * Places an order for the items in the cart.
     *
     * Rules enforced:
     * 1. Customer MUST be logged in.
     * 2. Cart MUST NOT be empty.
     * 3. All items in the cart must have sufficient stock (all-or-nothing check).
     * 4. Once validated, stock is deducted and order receipt is produced.
     *
     * @return Confirmed Order object, or null if placement fails
     */
    public Order placeOrder() {
        // Step 1: Ensure user is logged in
        if (!isLoggedIn()) {
            System.out.println("You must log in or register before placing an order!");
            return null;
        }

        // Step 2: Ensure cart is not empty
        if (cart.isEmpty()) {
            System.out.println("🛒 Your cart is empty. Add some products before checking out!");
            return null;
        }

        // Step 3: Stock validation (Pre-check all items to ensure atomic behavior)
        for (Cart.CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.hasSufficientStock(item.getQuantity())) {
                System.out.println("Order Failed! Stock ran out for: " + product.getName()
                        + " (Available: " + product.getStock() + ", In Cart: " + item.getQuantity() + ").");
                System.out.println("Transaction Rolled Back: No changes made to inventory or cart.");
                return null;
            }
        }

        // Step 4: Deduct stock from products (Commit changes)
        List<Order.OrderItem> orderItems = new ArrayList<>();
        for (Cart.CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            product.reduceStock(item.getQuantity());

            orderItems.add(new Order.OrderItem(
                    product.getId(),
                    product.getName(),
                    item.getQuantity(),
                    product.getPrice()
            ));
        }

        // Step 5: Create and store the order
        double totalBill = cart.getTotalAmount();
        Order newOrder = new Order(nextOrderId++, currentUser.getUserId(), currentUser.getFullName(), orderItems, totalBill);
        orderHistory.add(newOrder);

        // Step 6: Clear the cart after successful order placement
        cart.clear();

        System.out.println("Order placed successfully! Thank you for shopping with us.");
        newOrder.printOrderSummary();
        return newOrder;
    }
    // End of placeOrder - validates inventory, commits stock deduction, and generates confirmed order.

    /**
     * Displays all past orders placed by the currently logged-in user.
     */
    public void displayUserOrderHistory() {
        if (!isLoggedIn()) {
            System.out.println("🔒 Please log in to view your order history.");
            return;
        }

        List<Order> userOrders = new ArrayList<>();
        for (Order order : orderHistory) {
            if (order.getUserId() == currentUser.getUserId()) {
                userOrders.add(order);
            }
        }

        if (userOrders.isEmpty()) {
            System.out.println("You have not placed any orders yet.");
            return;
        }

        System.out.println("================================================================================");
        System.out.println("                        ORDER HISTORY FOR " + currentUser.getFullName().toUpperCase());
        System.out.println("================================================================================");

        for (Order order : userOrders) {
            order.printOrderSummary();
            System.out.println();
        }
    }
    // End of displayUserOrderHistory - fetches and prints all previous orders of logged-in user.
}
