package com.ecommerce;

import java.util.Scanner;

/**
 * Main class provides an interactive console interface for the Mini E-Commerce System.
 * Users can browse products, manage their cart, register, login, and place orders.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final EcommerceService service = new EcommerceService();

    /**
     * Application entry point. Drives the main interactive command loop.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        System.out.println("********************************************************************************");
        System.out.println("                   WELCOME TO MINI E-COMMERCE STORE                             ");
        System.out.println("********************************************************************************");

        boolean running = true;
        while (running) {
            displayMenu();
            int choice = readIntegerInput("Enter your choice: ");

            if (service.isLoggedIn()) {
                running = handleLoggedInMenuChoice(choice);
            } else {
                running = handleGuestMenuChoice(choice);
            }
            System.out.println();
        }

        System.out.println("================================================================================");
        System.out.println("            Thank you for visiting Mini E-Commerce Store! Have a great day!     ");
        System.out.println("================================================================================");
        scanner.close();
    }
    // End of main - runs application console loop until user exits.

    /**
     * Displays contextual menu options depending on whether user is logged in or guest.
     */
    private static void displayMenu() {
        System.out.println("--------------------------------------------------------------------------------");
        if (service.isLoggedIn()) {
            User current = service.getCurrentUser();
            System.out.println("👤 Active User: " + current.getFullName() + " (@" + current.getUsername() + ")");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("1. Browse Available Products");
            System.out.println("2. View Shopping Cart");
            System.out.println("3. Add Product to Cart");
            System.out.println("4. Remove Product from Cart");
            System.out.println("5. Place Order (Checkout)");
            System.out.println("6. View Order History");
            System.out.println("7. Logout");
            System.out.println("8. Exit Application");
        } else {
            System.out.println("👤 Browsing as: GUEST (Log in to checkout)");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("1. Browse Available Products");
            System.out.println("2. View Shopping Cart");
            System.out.println("3. Add Product to Cart");
            System.out.println("4. Remove Product from Cart");
            System.out.println("5. Login to Your Account");
            System.out.println("6. Register as New Customer");
            System.out.println("7. Exit Application");
        }
        System.out.println("--------------------------------------------------------------------------------");
    }
    // End of displayMenu - prints dynamic menu based on authentication state.

    /**
     * Handles choices made by an unauthenticated / guest customer.
     *
     * @param choice Numeric menu selection
     * @return true to continue loop, false to exit
     */
    private static boolean handleGuestMenuChoice(int choice) {
        switch (choice) {
            case 1:
                service.displayAllProducts();
                break;
            case 2:
                service.viewCart();
                break;
            case 3:
                handleAddToCart();
                break;
            case 4:
                handleRemoveFromCart();
                break;
            case 5:
                handleLogin();
                break;
            case 6:
                handleRegister();
                break;
            case 7:
                return false;
            default:
                System.out.println(" Invalid option! Please choose a number between 1 and 7.");
        }
        return true;
    }
    // End of handleGuestMenuChoice - routes user inputs for guest visitors.

    /**
     * Handles choices made by an authenticated logged-in customer.
     *
     * @param choice Numeric menu selection
     * @return true to continue loop, false to exit
     */
    private static boolean handleLoggedInMenuChoice(int choice) {
        switch (choice) {
            case 1:
                service.displayAllProducts();
                break;
            case 2:
                service.viewCart();
                break;
            case 3:
                handleAddToCart();
                break;
            case 4:
                handleRemoveFromCart();
                break;
            case 5:
                handlePlaceOrder();
                break;
            case 6:
                service.displayUserOrderHistory();
                break;
            case 7:
                service.logoutUser();
                break;
            case 8:
                return false;
            default:
                System.out.println("⚠ Invalid option! Please choose a number between 1 and 8.");
        }
        return true;
    }
    // End of handleLoggedInMenuChoice - routes user inputs for authenticated members.

    /**
     * Prompts user for product ID and quantity, then adds to cart.
     */
    private static void handleAddToCart() {
        service.displayAllProducts();
        int productId = readIntegerInput("Enter Product ID to add: ");
        int quantity = readIntegerInput("Enter Quantity: ");
        service.addProductToCart(productId, quantity);
    }
    // End of handleAddToCart - captures input and invokes cart addition.

    /**
     * Prompts user for product ID to remove from cart.
     */
    private static void handleRemoveFromCart() {
        service.viewCart();
        int productId = readIntegerInput("Enter Product ID to remove: ");
        service.removeProductFromCart(productId);
    }
    // End of handleRemoveFromCart - captures product ID and removes from cart.

    /**
     * Handles login workflow by reading username and password from console.
     */
    private static void handleLogin() {
        System.out.println("--- Login ---");
        System.out.print("Enter username (or demo: rahul): ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password (demo: password123): ");
        String password = scanner.nextLine().trim();

        service.loginUser(username, password);
    }
    // End of handleLogin - collects login inputs and initiates auth check.

    /**
     * Handles customer registration workflow.
     */
    private static void handleRegister() {
        System.out.println("--- Customer Registration ---");
        System.out.print("Enter your full name: ");
        String fullName = scanner.nextLine().trim();
        System.out.print("Enter preferred username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter secure password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Enter email address: ");
        String email = scanner.nextLine().trim();

        service.registerUser(fullName, username, password, email);
    }
    // End of handleRegister - collects sign-up data and registers new user.

    /**
     * Confirms and places an order for the logged-in customer's cart.
     */
    private static void handlePlaceOrder() {
        service.viewCart();
        if (service.getCart().isEmpty()) {
            return;
        }

        System.out.print("Are you sure you want to place this order? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("yes") || confirm.equals("y")) {
            service.placeOrder();
        } else {
            System.out.println("ℹ Order placement was cancelled.");
        }
    }
    // End of handlePlaceOrder - confirms intent and triggers checkout logic.

    /**
     * Helper method to safely read integer inputs without throwing InputMismatchException.
     *
     * @param prompt Message prompt to display to user
     * @return Valid integer entered by user
     */
    private static int readIntegerInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(" Invalid input! Please enter a valid whole number.");
            }
        }
    }
    // End of readIntegerInput - validates console integer parsing to avoid crashes.
}
