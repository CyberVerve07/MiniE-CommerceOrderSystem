package com.ecommerce;

import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.CustomerDAO;
import com.ecommerce.dao.ProductDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//Fully Business Logic Layer (BLL) for the e-commerce application, managing products, users, cart, and orders. It interacts with DAOs for database operations and provides methods for user registration
// , login, product management, cart operations, and order placement.
// Core service class managing catalog, users, cart, and order service
public class EcommerceService {

    private final List<Product> productCatalog;
    private final Map<String, User> userRegistry;
    private final OrderService orderService;
    private final OtpService otpService;
    private final Cart cart;
    private User currentUser;
    private final ProductDAO productDAO;
    private final CustomerDAO customerDAO;
    private final CartDAO cartDAO;

    // Constructor initializes database DAOs and loads products from MySQL
    public EcommerceService() {
        this(new ProductDAO(), new CustomerDAO(), new CartDAO(), new OrderService(), new OtpService());
    }

    // Overloaded constructor for dependency injection and testing
    public EcommerceService(ProductDAO productDAO, CustomerDAO customerDAO, CartDAO cartDAO,
                            OrderService orderService, OtpService otpService) {
        this.productCatalog = new ArrayList<>();
        this.userRegistry = new HashMap<>();
        this.orderService = (orderService != null) ? orderService : new OrderService();
        this.otpService = (otpService != null) ? otpService : new OtpService();
        this.cart = new Cart();
        this.currentUser = null;
        this.productDAO = productDAO;
        this.customerDAO = customerDAO;
        this.cartDAO = cartDAO;

        if (this.productDAO != null) {
            loadProductsFromDatabase(); // Load products from MySQL database
        }
    }

    // Helper method to add product to in-memory catalog
    public void addProductToCatalog(Product product) {
        if (product != null) {
            this.productCatalog.add(product);
        }
    }

    // Refresh products in memory from the MySQL database
    public void refreshProductCatalog() {
        if (productDAO == null) {
            return;
        }
        try {
            List<Product> products = productDAO.getAllProducts();
            productCatalog.clear();
            productCatalog.addAll(products);
        } catch (SQLException e) {
            System.out.println("Failed to load products from MySQL.");
            e.printStackTrace();
        }
    }

    // Display all products
    public void displayAllProducts() {
        refreshProductCatalog();
        displayProducts(productCatalog, "PRODUCT LIST");
    }

    // Sort products by rating using Comparator (Highest to Lowest)
    public void displayProductsSortedByRating() {
        refreshProductCatalog();
        List<Product> sortedList = new ArrayList<>(productCatalog);
        sortedList.sort(Comparator.comparingDouble(Product::getRating).reversed());
        displayProducts(sortedList, "TOP RATED PRODUCTS (BY RATING)");
    }

    // Sort products by price using Comparator (Lowest to Highest)
    public void displayProductsSortedByPrice() {
        refreshProductCatalog();
        List<Product> sortedList = new ArrayList<>(productCatalog);
        sortedList.sort(Comparator.comparingDouble(Product::getPrice));
        displayProducts(sortedList, "PRODUCTS BY PRICE (LOW TO HIGH)");
    }

    // Search products by name or keyword
    public List<Product> searchProductsByName(String query) {
        refreshProductCatalog();
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

        refreshProductCatalog();
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
        if (productDAO != null) {
            try {
                Product p = productDAO.getProductById(productId);
                if (p != null) {
                    productCatalog.add(p);
                    return p;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Display specific product details by Name or ID
    public void displayProductDetails(String input) {
        refreshProductCatalog();
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
                System.out.printf("%-5d %-18s %-12.2f %-10d %s%n", product.getId(), product.getName(), product.getPrice(), product.getQuantity(), product.getRatingDisplay());
            }
        }
        System.out.println("============================================================================");
    }

    // Get catalog list
    public List<Product> getProductCatalog() {
        return Collections.unmodifiableList(productCatalog);
    }

    // Finds a customer by username, phone number, or email.
    public User findUserByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }

        String searchKey = identifier.trim().toLowerCase();

        // First, search users already loaded in Java memory.
        if (userRegistry.containsKey(searchKey)) {
            return userRegistry.get(searchKey);
        }

        for (User user : userRegistry.values()) {
            if (user.getPhoneNumber() != null
                    && user.getPhoneNumber().equalsIgnoreCase(identifier.trim())) {
                return user;
            }

            if (user.getEmail() != null
                    && user.getEmail().equalsIgnoreCase(identifier.trim())) {
                return user;
            }
        }

        // If the user is not in memory, search MySQL.
        if (customerDAO != null) {
            try {
                User user = customerDAO.findByIdentifier(identifier.trim());

                if (user != null) {
                    userRegistry.put(user.getUsername().toLowerCase(), user);
                }

                return user;

            } catch (SQLException e) {
                System.out.println("Unable to find customer in database.");
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }

    // Check if a user is registered by username, phone, or email
    public boolean isUserRegistered(String identifier) {
        return findUserByIdentifier(identifier) != null;
    }

    // Registers a customer and saves their details in MySQL.
    public boolean registerUser(String fullName, String username, String password, String email, String phoneNumber) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username cannot be empty.");
            return false;
        }
        if (password == null || password.trim().length() < 6) {
            System.out.println("Password must be at least 6 characters.");
            return false;
        }

        String trimmedUser = username.trim();
        String trimmedEmail = (email != null && !email.trim().isEmpty()) ? email.trim() : null;
        String trimmedPhone = (phoneNumber != null && !phoneNumber.trim().isEmpty()) ? phoneNumber.trim() : null;

        try {
            if (customerDAO.isUsernameTaken(trimmedUser)) {
                System.out.println("Registration failed: Username '" + trimmedUser + "' is already taken. Please choose another username.");
                return false;
            }
            if (trimmedEmail != null && customerDAO.isEmailTaken(trimmedEmail)) {
                System.out.println("Registration failed: Email '" + trimmedEmail + "' is already registered.");
                return false;
            }
            if (trimmedPhone != null && customerDAO.isPhoneTaken(trimmedPhone)) {
                System.out.println("Registration failed: Phone number '" + trimmedPhone + "' is already registered.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Database error during registration validation.");
            e.printStackTrace();
            return false;
        }

        String name = (fullName == null || fullName.trim().isEmpty()) ? trimmedUser : fullName.trim();
        User newUser = new User(0, name, trimmedUser, password.trim(), trimmedEmail, trimmedPhone);

        try {
            if (customerDAO.registerCustomer(newUser)) {
                userRegistry.put(trimmedUser.toLowerCase(), newUser);
                System.out.println("Registration successful! Welcome, " + name + ".");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Registration failed. Username, email, or phone number may already exist.");
            e.printStackTrace();
        }

        return false;
    }

    // Login user with username, phone number, or email and password
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
        synchronizeCartOnLogin(user);
        System.out.println("Welcome back, " + user.getFullName() + "! You are now logged in.");
        return true;
    }

    // Login user directly via verified OTP without password hash comparison
    public boolean loginUserWithOtp(User user) {
        if (user == null) {
            System.out.println("Login failed: User not found.");
            return false;
        }

        this.currentUser = user;
        synchronizeCartOnLogin(user);
        System.out.println("Welcome back, " + user.getFullName() + "! You are now logged in.");
        return true;
    }

    // Synchronize guest cart items and database cart upon login
    private void synchronizeCartOnLogin(User user) {
        if (cartDAO == null) {
            return;
        }
        try {
            if (!cart.isEmpty()) {
                cartDAO.mergeGuestCart(user.getUserId(), cart, productDAO);
            }
            loadCartFromDatabase(user.getUserId());
        } catch (SQLException e) {
            System.out.println("Failed to synchronize cart with database.");
            e.printStackTrace();
        }
    }

    // Load persistent cart items from MySQL into the in-memory cart
    public void loadCartFromDatabase(int customerId) {
        cart.clear();
        if (cartDAO == null) {
            return;
        }
        try {
            List<Cart.CartItem> dbItems = cartDAO.getCartByCustomerId(customerId);
            for (Cart.CartItem item : dbItems) {
                int stock = item.getProduct().getQuantity();
                int qty = item.getQuantity();
                if (qty > stock) {
                    qty = stock;
                    if (qty > 0) {
                        cartDAO.updateQuantity(customerId, item.getProduct().getId(), qty);
                    } else {
                        cartDAO.removeFromCart(customerId, item.getProduct().getId());
                        continue;
                    }
                }
                cart.setItem(item.getProduct(), qty);
            }
        } catch (SQLException e) {
            System.out.println("Failed to load cart from database.");
            e.printStackTrace();
        }
    }

    // Logout user and reset in-memory cart for the guest session
    public void logoutUser() {
        if (this.currentUser != null) {
            System.out.println("Logged out successfully. Goodbye, " + currentUser.getFullName() + "!");
            this.currentUser = null;
            this.cart.clear();
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
        refreshProductCatalog();
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

        // Get latest stock from database if available
        int availableStock = product.getQuantity();
        if (productDAO != null) {
            try {
                int liveStock = productDAO.getStock(product.getId());
                if (liveStock >= 0) {
                    availableStock = liveStock;
                    product.setStock(liveStock);
                }
            } catch (SQLException ignored) {
            }
        }

        int currentQtyInCart = cart.getQuantity(product.getId());
        int requestedTotal = currentQtyInCart + quantity;

        if (requestedTotal > availableStock) {
            System.out.println("Cannot add to cart: Requested quantity exceeds available stock ("
                    + availableStock + " available).");
            return false;
        }

        if (isLoggedIn() && cartDAO != null) {
            try {
                cartDAO.addToCart(currentUser.getUserId(), product.getId(), quantity);
            } catch (SQLException e) {
                System.out.println("Failed to save cart item to database.");
                e.printStackTrace();
                return false;
            }
        }

        boolean success = cart.addItem(product, quantity);
        if (success) {
            System.out.println("Added " + quantity + " x " + product.getName() + " to your cart.");
            return true;
        } else {
            System.out.println("Cannot add to cart: Requested quantity exceeds available stock ("
                    + availableStock + " available).");
            return false;
        }
    }

    // Remove product from cart
    public void removeProductFromCart(int productId) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is already empty.");
            return;
        }

        if (isLoggedIn() && cartDAO != null) {
            try {
                cartDAO.removeFromCart(currentUser.getUserId(), productId);
            } catch (SQLException e) {
                System.out.println("Failed to remove cart item from database.");
                e.printStackTrace();
            }
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
        if (isLoggedIn()) {
            loadCartFromDatabase(currentUser.getUserId());
        }
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

    public CartDAO getCartDAO() {
        return cartDAO;
    }

    public ProductDAO getProductDAO() {
        return productDAO;
    }

    public CustomerDAO getCustomerDAO() {
        return customerDAO;
    }

    // Place order with address and payment method
    public Order placeOrder(String address, String paymentMethod) {
        Order order = orderService.placeOrder(currentUser, cart, address, paymentMethod);
        if (order != null) {
            refreshProductCatalog();
        }
        return order;
    }

    // Display order history
    public void displayUserOrderHistory() {
        orderService.displayUserOrderHistory(currentUser);
    }

    private void loadProductsFromDatabase() {
        try {
            productCatalog.clear();
            productCatalog.addAll(productDAO.getAllProducts());
            System.out.println("Products loaded from MySQL.");
        } catch (SQLException e) {
            System.out.println("Failed to load products from MySQL.");
            e.printStackTrace();
        }
    }
}
