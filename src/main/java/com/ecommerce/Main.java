package com.ecommerce;

import java.util.List;
import java.util.Scanner;

// Main interactive console driver
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final EcommerceService service = new EcommerceService();

    // Application entry point
    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            displayMenu();
            int choice = readIntegerInput("SELECT Option:");

            if (service.isLoggedIn()) {
                running = handleLoggedInMenuChoice(choice);
            } else {
                running = handleGuestMenuChoice(choice);
            }
            System.out.println();
        }

        System.out.println("Thank you for using E-Commerce Management System!");
        scanner.close();
    }

    // Display menu matching the application interface
    private static void displayMenu() {
        System.out.println("================================");
        System.out.println("JDBC E-COMMERCE MANAGEMENT SYSTEM");
        System.out.println("================================");

        if (service.isLoggedIn()) {
            User current = service.getCurrentUser();
            System.out.println("Current User : " + current.getFullName());
            System.out.println("--------------------------------");
            System.out.println("1. View All Products");
            System.out.println("2. View Product Details");
            System.out.println("3. Search Product");
            System.out.println("4. Add Product To Cart");
            System.out.println("5. Remove Product From Cart");
            System.out.println("6. View Cart - [" + current.getFullName() + "]");
            System.out.println("7. Place Order");
            System.out.println("8. View Order History");
            System.out.println("9. Logout");
            System.out.println("10. Exit");
        } else {
            System.out.println("Current User : Guest");
            System.out.println("--------------------------------");
            System.out.println("1. View All Products");
            System.out.println("2. View Product Details");
            System.out.println("3. Search Product");
            System.out.println("4. Add Product To Cart");
            System.out.println("5. Remove Product From Cart");
            System.out.println("6. View Cart - [Guest]");
            System.out.println("7. Login");
            System.out.println("8. Register");
            System.out.println("9. Exit");
        }
        System.out.println("--------------------------------");
    }

    // Handle options when user is guest
    private static boolean handleGuestMenuChoice(int choice) {
        switch (choice) {
            case 1:
                handleViewAllProducts();
                break;
            case 2:
                handleViewProductDetails();
                break;
            case 3:
                handleSearchProduct();
                break;
            case 4:
                handleAddToCart();
                break;
            case 5:
                handleRemoveFromCart();
                break;
            case 6:
                service.viewCart();
                break;
            case 7:
                handleLogin();
                break;
            case 8:
                handleRegister();
                break;
            case 9:
                return false;
            default:
                System.out.println("Invalid option! Please choose between 1 and 9.");
        }
        return true;
    }

    // Handle options when user is logged in
    private static boolean handleLoggedInMenuChoice(int choice) {
        switch (choice) {
            case 1:
                handleViewAllProducts();
                break;
            case 2:
                handleViewProductDetails();
                break;
            case 3:
                handleSearchProduct();
                break;
            case 4:
                handleAddToCart();
                break;
            case 5:
                handleRemoveFromCart();
                break;
            case 6:
                service.viewCart();
                break;
            case 7:
                handlePlaceOrder();
                break;
            case 8:
                service.displayUserOrderHistory();
                break;
            case 9:
                service.logoutUser();
                break;
            case 10:
                return false;
            default:
                System.out.println("Invalid option! Please choose between 1 and 10.");
        }
        return true;
    }

    // View products with Comparator sorting option
    private static void handleViewAllProducts() {
        service.displayAllProducts();
        System.out.print("Sort products? [1] Top Rated (High to Low), [2] Lowest Price, [Enter] Skip: ");
        String sortChoice = scanner.nextLine().trim();
        if (sortChoice.equals("1")) {
            service.displayProductsSortedByRating();
        } else if (sortChoice.equals("2")) {
            service.displayProductsSortedByPrice();
        }
    }

    // View specific product details
    private static void handleViewProductDetails() {
        System.out.print("Enter Product Name or ID: ");
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            service.displayProductDetails(input);
        }
    }

    // Search product by name
    private static void handleSearchProduct() {
        System.out.print("Enter Product Name to search: ");
        String query = scanner.nextLine().trim();

        if (query.isEmpty()) {
            System.out.println("Search keyword cannot be empty.");
            return;
        }

        List<Product> matches = service.searchProductsByName(query);
        service.displayProducts(matches, "SEARCH RESULTS FOR: " + query.toUpperCase());

        if (!matches.isEmpty()) {
            System.out.print("Do you want to add an item to your cart? (yes/no): ");
            String answer = scanner.nextLine().trim().toLowerCase();
            if (answer.equals("yes") || answer.equals("y")) {
                handleAddToCart();
            }
        }
    }

    // Add product to cart by name or ID
    private static void handleAddToCart() {
        System.out.print("Enter Product Name or ID to add: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("No product specified.");
            return;
        }

        Product selectedProduct = null;

        // Try numeric ID first
        try {
            int productId = Integer.parseInt(input);
            selectedProduct = service.findProductById(productId);
        } catch (NumberFormatException ignored) {
            // Non-numeric, search by name
        }

        // If not found by ID, search by name
        if (selectedProduct == null) {
            List<Product> matches = service.searchProductsByName(input);
            if (matches.isEmpty()) {
                System.out.println("No product found matching '" + input + "'.");
                return;
            } else if (matches.size() == 1) {
                selectedProduct = matches.get(0);
                System.out.println("Selected: " + selectedProduct.getName());
            } else {
                service.displayProducts(matches, "MATCHING PRODUCTS");
                int id = readIntegerInput("Multiple products match. Enter Product ID: ");
                selectedProduct = service.findProductById(id);
                if (selectedProduct == null) {
                    System.out.println("Invalid Product ID selected.");
                    return;
                }
            }
        }

        int quantity = readIntegerInput("Enter Quantity for " + selectedProduct.getName() + ": ");
        service.addProductToCart(selectedProduct, quantity);
    }

    // Remove product from cart
    private static void handleRemoveFromCart() {
        service.viewCart();
        if (service.getCart().isEmpty()) {
            return;
        }

        System.out.print("Enter Product Name or ID to remove: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return;
        }

        int productId = -1;
        try {
            productId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Product p = service.findProductByName(input);
            if (p != null) {
                productId = p.getId();
            }
        }

        if (productId != -1) {
            service.removeProductFromCart(productId);
        } else {
            System.out.println("Product not recognized: " + input);
        }
    }

    // Login workflow with username, phone number, or email (supports password or OTP)
    private static void handleLogin() {
        System.out.println("--- Login ---");
        System.out.print("Enter username, phone number, or email: ");
        String identifier = scanner.nextLine().trim();

        if (identifier.isEmpty()) {
            System.out.println("Input cannot be empty.");
            return;
        }

        if (!service.isUserRegistered(identifier)) {
            System.out.println("User '" + identifier + "' is not registered yet.");
            System.out.print("Do you want to register now? (yes/no): ");
            String regChoice = scanner.nextLine().trim().toLowerCase();
            if (regChoice.equals("yes") || regChoice.equals("y")) {
                handleRegisterWithIdentifier(identifier);
            }
            return;
        }

        // 10-digit mobile number login supports OTP
        if (identifier.matches("\\d{10}")) {
            System.out.println("Choose login option:");
            System.out.println("1. Login with Password");
            System.out.println("2. Login with OTP on Mobile");
            int loginChoice = readIntegerInput("Select (1 or 2): ");

            if (loginChoice == 2) {
                String otp = service.getOtpService().generateOtp();
                service.getOtpService().sendOtp(identifier, otp);

                System.out.print("Enter the 4-digit OTP: ");
                String enteredOtp = scanner.nextLine().trim();

                if (service.getOtpService().verifyOtp(enteredOtp, otp)) {
                    User user = service.findUserByIdentifier(identifier);
                    if (user != null) {
                        service.loginUserWithOtp(user);
                    } else {
                        System.out.println("Login failed: User not found.");
                    }
                    return;
                } else {
                    System.out.println("Login failed: Incorrect OTP entered.");
                    return;
                }
            }
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        service.loginUser(identifier, password);
    }

    // Registration workflow with OTP verification for mobile number
    private static void handleRegister() {
        System.out.println("--- Customer Registration ---");
        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        if (service.isUserRegistered(username)) {
            System.out.println("Username '" + username + "' is already taken. Please choose another username.");
            return;
        }

        String password = readValidPassword();

        System.out.print("Enter phone number (press Enter to skip): ");
        String phoneNumber = scanner.nextLine().trim();

        if (!phoneNumber.isEmpty()) {
            if (!service.getOtpService().validatePhoneNumber(phoneNumber)) {
                System.out.println("Invalid mobile number! Mobile number must be 10 digits.");
                return;
            }

            String otp = service.getOtpService().generateOtp();
            service.getOtpService().sendOtp(phoneNumber, otp);

            System.out.print("Enter the 4-digit OTP sent to your mobile: ");
            String enteredOtp = scanner.nextLine().trim();

            if (!service.getOtpService().verifyOtp(enteredOtp, otp)) {
                System.out.println("OTP verification failed! Registration aborted.");
                return;
            }
            System.out.println("Mobile number verified successfully!");
        }

        System.out.print("Enter email (press Enter to skip): ");
        String email = scanner.nextLine().trim();

        boolean success = service.registerUser(fullName, username, password, email, phoneNumber);
        if (success) {
            System.out.print("Do you want to log in now? (yes/no): ");
            String loginNow = scanner.nextLine().trim().toLowerCase();
            if (loginNow.equals("yes") || loginNow.equals("y")) {
                service.loginUser(username, password);
            }
        }
    }

    // Quick registration helper from login prompt
    private static void handleRegisterWithIdentifier(String identifier) {
        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine().trim();

        String username = identifier;
        String phoneNumber = "";
        String email = "";

        if (identifier.matches("\\d{7,15}")) {
            phoneNumber = identifier;
            System.out.print("Enter preferred username: ");
            String customUser = scanner.nextLine().trim();
            if (!customUser.isEmpty()) {
                username = customUser;
            }
        } else if (identifier.contains("@")) {
            email = identifier;
            System.out.print("Enter preferred username: ");
            String customUser = scanner.nextLine().trim();
            if (!customUser.isEmpty()) {
                username = customUser;
            }
        }

        if (fullName.isEmpty()) {
            fullName = username;
        }

        String password = readValidPassword();

        if (phoneNumber.isEmpty()) {
            System.out.print("Enter phone number (press Enter to skip): ");
            phoneNumber = scanner.nextLine().trim();
        }

        if (!phoneNumber.isEmpty()) {
            if (service.getOtpService().validatePhoneNumber(phoneNumber)) {
                String otp = service.getOtpService().generateOtp();
                service.getOtpService().sendOtp(phoneNumber, otp);

                System.out.print("Enter the 4-digit OTP: ");
                String enteredOtp = scanner.nextLine().trim();

                if (!service.getOtpService().verifyOtp(enteredOtp, otp)) {
                    System.out.println("Incorrect OTP! Registration cancelled.");
                    return;
                }
                System.out.println("Mobile number verified successfully!");
            }
        }

        if (email.isEmpty()) {
            System.out.print("Enter email (press Enter to skip): ");
            email = scanner.nextLine().trim();
        }

        boolean success = service.registerUser(fullName, username, password, email, phoneNumber);
        if (success) {
            service.loginUser(username, password);
        }
    }

    // Password validation allowing letters, numbers, and symbols (minimum 6 characters)
    private static String readValidPassword() {
        while (true) {
            System.out.print("Enter password (minimum 6 characters - letters, numbers, symbols allowed): ");
            String password = scanner.nextLine().trim();
            if (password.length() >= 6) {
                return password;
            }
            System.out.println("Invalid password! Password must be at least 6 characters long (numbers and symbols like @, #, $, . are allowed).");
        }
    }

    // Place order workflow
    private static void handlePlaceOrder() {
        if (!service.isLoggedIn()) {
            System.out.println("You must be logged in to place an order!");
            return;
        }

        service.viewCart();
        if (service.getCart().isEmpty()) {
            System.out.println("Your cart is empty. Please add products first.");
            return;
        }

        System.out.print("Enter Delivery Address: ");
        String address = scanner.nextLine().trim();
        if (address.isEmpty()) {
            address = "Standard Delivery Address";
        }

        System.out.println("Select Payment Method:");
        System.out.println("1. Cash on Delivery (COD)");
        System.out.println("2. UPI / Net Banking");
        System.out.println("3. Credit / Debit Card");
        int payChoice = readIntegerInput("Choose payment method (1-3): ");

        String paymentMethod;
        switch (payChoice) {
            case 2:
                paymentMethod = "UPI / Net Banking";
                break;
            case 3:
                paymentMethod = "Credit / Debit Card";
                break;
            default:
                paymentMethod = "Cash on Delivery (COD)";
                break;
        }

        System.out.print("Are you sure you want to confirm this order? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("yes") || confirm.equals("y")) {
            service.placeOrder(address, paymentMethod);
        } else {
            System.out.println("Order placement was cancelled.");
        }
    }

    // Helper to read safe integer input
    private static int readIntegerInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number.");
            }
        }
    }
}
