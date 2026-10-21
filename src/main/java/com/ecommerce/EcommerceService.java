package com.ecommerce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Core service class managing catalog, users, cart, and order service
public class EcommerceService {

    private final List<Product> productCatalog;
    private final Map<String, User> userRegistry;
    private final OrderService orderService;
    private final OtpService otpService;
    private final Cart cart;
    private User currentUser;
    private int nextUserId = 101;

    // Constructor initializes in-memory data
    public EcommerceService() {
        this.productCatalog = new ArrayList<>();
        this.userRegistry = new HashMap<>();
        this.orderService = new OrderService();
        this.otpService = new OtpService();
        this.cart = new Cart();
        this.currentUser = null;

        seedSampleData();
    }

    // Populate initial sample products with ratings and review counts
    private void seedSampleData() {
        productCatalog.add(new Product(1, "Laptop", 50000.00, 6, 4.8, 142));
        productCatalog.add(new Product(2, "Mouse", 500.00, 50, 4.5, 280));
        productCatalog.add(new Product(3, "Keyboard", 1200.00, 30, 4.6, 195));
        productCatalog.add(new Product(4, "Monitor", 15000.00, 15, 4.7, 88));
        productCatalog.add(new Product(5, "Headphones", 2500.00, 20, 4.4, 310));
        productCatalog.add(new Product(6, "Webcam", 1800.00, 12, 4.2, 64));
        productCatalog.add(new Product(7, "USB Cable", 250.00, 100, 4.1, 520));
        productCatalog.add(new Product(8, "SSD 1TB", 6500.00, 8, 4.9, 415));
    }

    // Display all products
    public void displayAllProducts() {
        displayProducts(productCatalog, "PRODUCT LIST");
    }

    // Sort products by rating using Comparator (Highest to Lowest)
    public void displayProductsSortedByRating() {
        List<Product> sortedList = new ArrayList<>(productCatalog);
        sortedList.sort(Comparator.comparingDouble(Product::getRating).reversed());
        displayProducts(sortedList, "TOP RATED PRODUCTS (BY RATING)");
    }

    // Sort products by price using Comparator (Lowest to Highest)
    public void displayProductsSortedByPrice() {
        List<Product> sortedList = new ArrayList<>(productCatalog);
        sortedList.sort(Comparator.comparingDouble(Product::getPrice));
        displayProducts(sortedList, "PRODUCTS BY PRICE (LOW TO HIGH)");
    }

    // Search products by name or keyword
    public List<Product> searchProductsByName(String query) {
        List<Product> matches = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return matches;
        }

        String lowerQuery = query.trim().toLowerCase();
        for (Product product : productCatalog) {
            if (product.getName().toLowerCase().contains(lowerQuery)) {
                matches.add(product);
            }
        }
        return matches;
    }

    // Find product by name
    public Product findProductByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String trimmed = name.trim();
        for (Product product : productCatalog) {
            if (product.getName().equalsIgnoreCase(trimmed)) {
                return product;
            }
        }
        for (Product product : productCatalog) {
            if (product.getName().toLowerCase().contains(trimmed.toLowerCase())) {
                return product;
            }
        }
        return null;
    }

    // Find product by ID
    public Product findProductById(int productId) {
        for (Product product : productCatalog) {
            if (product.getId() == productId) {
                return product;
            }
        }
        return null;
    }

    // Display specific product details by Name or ID
    public void displayProductDetails(String input) {
        Product p = null;
        try {
            int id = Integer.parseInt(input.trim());
            p = findProductById(id);
        } catch (NumberFormatException e) {
            p = findProductByName(input);
        }

        if (p == null) {
            System.out.println("Product not found for: " + input);
            return;
        }

        System.out.println("----------------------------------------");
        System.out.println("           PRODUCT DETAILS              ");
        System.out.println("----------------------------------------");
        System.out.println("ID                : " + p.getId());
        System.out.println("Product Name      : " + p.getName());
        System.out.printf("Price             : Rs %.2f%n", p.getPrice());
        System.out.println("Quantity in Stock : " + p.getQuantity());
        System.out.printf("Customer Rating   : %.1f / 5.0 (%d verified reviews)%n", p.getRating(), p.getReviewCount());
        System.out.println("Status            : " + (p.getQuantity() > 0 ? "IN STOCK" : "OUT OF STOCK"));
        System.out.println("----------------------------------------");
    }

    // Display product table with Quantity and Rating details
    public void displayProducts(List<Product> products, String title) {
        System.out.println("============================== " + title + " ==============================");
        System.out.printf("%-5s %-18s %-12s %-10s %s%n", "ID", "Product Name", "Price (Rs)", "Quantity", "Rating");
        System.out.println("----------------------------------------------------------------------------");

        if (products.isEmpty()) {
            System.out.println("No products found.");
        } else {
            for (Product product : products) {
                System.out.printf("%-5d %-18s %-12.2f %-10d %s%n",
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getQuantity(),
                        product.getRatingDisplay());
            }
        }
        System.out.println("============================================================================");
    }

    // Get catalog list
    public List<Product> getProductCatalog() {
        return Collections.unmodifiableList(productCatalog);
    }

    // Find user by username, phone number, or email
    public User findUserByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }

        String searchKey = identifier.trim().toLowerCase();

        // 1. Direct username lookup
        if (userRegistry.containsKey(searchKey)) {
            return userRegistry.get(searchKey);
        }

        // 2. Lookup by phone number or email
        for (User user : userRegistry.values()) {
            if (user.getPhoneNumber() != null && user.getPhoneNumber().trim().equalsIgnoreCase(identifier.trim())) {
                return user;
            }
            if (user.getEmail() != null && user.getEmail().trim().equalsIgnoreCase(searchKey)) {
                return user;
            }
        }

        return null;
    }

    // Check if a user is registered by username, phone, or email
    public boolean isUserRegistered(String identifier) {
        return findUserByIdentifier(identifier) != null;
    }

    // Register user with phone number and email (either can be skipped)
    public boolean registerUser(String fullName, String username, String password, String email, String phoneNumber) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Registration failed: Username cannot be empty.");
            return false;
        }

        if (password == null || password.trim().length() < 6) {
            System.out.println("Registration failed: Password must be at least 6 characters long.");
            return false;
        }

        String key = username.trim().toLowerCase();
        if (userRegistry.containsKey(key)) {
            System.out.println("Registration failed: Username '" + username + "' is already registered.");
            return false;
        }

        String name = (fullName == null || fullName.trim().isEmpty()) ? username.trim() : fullName.trim();
        String mail = (email == null) ? "" : email.trim();
        String phone = (phoneNumber == null) ? "" : phoneNumber.trim();

        User newUser = new User(nextUserId++, name, username.trim(), password.trim(), mail, phone);
        userRegistry.put(key, newUser);
        System.out.println("Registration successful! Welcome, " + name + ".");
        return true;
    }

    // Login user with username, phone number, or email
    public boolean loginUser(String identifier, String password) {
        if (identifier == null || password == null) {
            System.out.println("Login failed: Username/Phone/Email and password cannot be empty.");
            return false;
        }

        User user = findUserByIdentifier(identifier);

        if (user == null) {
            System.out.println("Login failed: User '" + identifier + "' is not registered.");
            return false;
        }

        if (!user.checkPassword(password)) {
            System.out.println("Login failed: Incorrect password.");
            return false;
        }

        this.currentUser = user;
        System.out.println("Welcome back, " + user.getFullName() + "! You are now logged in.");
        return true;
    }

    // Logout user
    public void logoutUser() {
        if (this.currentUser != null) {
            System.out.println("Logged out successfully. Goodbye, " + currentUser.getFullName() + "!");
            this.currentUser = null;
        } else {
            System.out.println("No active user is currently logged in.");
        }
    }

    public boolean isLoggedIn() {
        return this.currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // Add product to cart by product ID
    public boolean addProductToCart(int productId, int quantity) {
        Product product = findProductById(productId);
        if (product == null) {
            System.out.println("Product with ID " + productId + " does not exist.");
            return false;
        }
        return addProductToCart(product, quantity);
    }

    // Add product to cart by Product object
    public boolean addProductToCart(Product product, int quantity) {
        if (product == null) {
            System.out.println("Product does not exist.");
            return false;
        }

        if (quantity <= 0) {
            System.out.println("Invalid quantity! Please enter a quantity greater than zero.");
            return false;
        }

        boolean success = cart.addItem(product, quantity);
        if (success) {
            System.out.println("Added " + quantity + " x " + product.getName() + " to your cart.");
            return true;
        } else {
            System.out.println("Cannot add to cart: Requested quantity exceeds available stock ("
                    + product.getQuantity() + " available).");
            return false;
        }
    }

    // Remove product from cart
    public void removeProductFromCart(int productId) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is already empty.");
            return;
        }

        boolean removed = cart.removeItem(productId);
        if (removed) {
            System.out.println("Product removed from your cart.");
        } else {
            System.out.println("Product ID " + productId + " was not found in your cart.");
        }
    }

    // View cart
    public void viewCart() {
        String name = (currentUser != null) ? currentUser.getFullName() : "Guest";
        cart.displayCart(name);
    }

    public Cart getCart() {
        return cart;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public OtpService getOtpService() {
        return otpService;
    }

    // Place order with address and payment method
    public Order placeOrder(String address, String paymentMethod) {
        return orderService.placeOrder(currentUser, cart, address, paymentMethod);
    }

    // Display order history
    public void displayUserOrderHistory() {
        orderService.displayUserOrderHistory(currentUser);
    }
}
