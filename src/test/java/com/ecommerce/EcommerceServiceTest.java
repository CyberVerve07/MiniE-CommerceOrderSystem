package com.ecommerce;

import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.CustomerDAO;
import com.ecommerce.dao.ProductDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EcommerceServiceTest {

    @Mock
    private ProductDAO productDAO;

    @Mock
    private CustomerDAO customerDAO;

    @Mock
    private CartDAO cartDAO;

    @Mock
    private OrderService orderService;

    @Mock
    private OtpService otpService;

    private EcommerceService ecommerceService;
    private Product p1;
    private Product p2;
    private Product p3;

    @BeforeEach
    void setUp() throws SQLException {
        // Return empty list on initial catalog load
        when(productDAO.getAllProducts()).thenReturn(new ArrayList<>());

        ecommerceService = new EcommerceService(productDAO, customerDAO, cartDAO, orderService, otpService);

        p1 = new Product(1, "Wireless Headphones", 2500.0, 10, 4.5, 100);
        p2 = new Product(2, "Bluetooth Speaker", 1500.0, 5, 4.0, 50);
        p3 = new Product(3, "USB-C Fast Charger", 800.0, 20, 4.8, 200);

        ecommerceService.addProductToCatalog(p1);
        ecommerceService.addProductToCatalog(p2);
        ecommerceService.addProductToCatalog(p3);
    }

    @Test
    @DisplayName("Catalog initializes with added products")
    void testCatalogInitialization() {
        assertEquals(3, ecommerceService.getProductCatalog().size());
    }

    @Test
    @DisplayName("searchProductsByName finds products case-insensitively and returns empty for blank query")
    void testSearchProductsByName() throws SQLException {
        when(productDAO.getAllProducts()).thenReturn(List.of(p1, p2, p3));

        List<Product> results = ecommerceService.searchProductsByName("bluetooth");
        assertEquals(1, results.size());
        assertEquals(2, results.get(0).getId());

        List<Product> partialResults = ecommerceService.searchProductsByName("less");
        assertEquals(1, partialResults.size());
        assertEquals("Wireless Headphones", partialResults.get(0).getName());

        assertTrue(ecommerceService.searchProductsByName("").isEmpty());
        assertTrue(ecommerceService.searchProductsByName(null).isEmpty());
        assertTrue(ecommerceService.searchProductsByName("NonExistentProduct").isEmpty());
    }

    @Test
    @DisplayName("findProductByName matches exact and partial name")
    void testFindProductByName() throws SQLException {
        when(productDAO.getAllProducts()).thenReturn(List.of(p1, p2, p3));

        assertNull(ecommerceService.findProductByName(null));
        assertNull(ecommerceService.findProductByName("   "));

        Product exact = ecommerceService.findProductByName("Bluetooth Speaker");
        assertNotNull(exact);
        assertEquals(2, exact.getId());

        Product partial = ecommerceService.findProductByName("Headphones");
        assertNotNull(partial);
        assertEquals(1, partial.getId());

        assertNull(ecommerceService.findProductByName("Smart Watch"));
    }

    @Test
    @DisplayName("findProductById finds from catalog and falls back to DAO if not in memory")
    void testFindProductById() throws SQLException {
        assertEquals(p1, ecommerceService.findProductById(1));

        Product p4 = new Product(4, "Webcam", 3000.0, 8, 4.3, 40);
        when(productDAO.getProductById(4)).thenReturn(p4);

        Product found = ecommerceService.findProductById(4);
        assertNotNull(found);
        assertEquals(4, found.getId());
        assertEquals("Webcam", found.getName());

        // Searching unknown product returns null
        when(productDAO.getProductById(999)).thenReturn(null);
        assertNull(ecommerceService.findProductById(999));
    }

    @Test
    @DisplayName("displayProductDetails and display methods run cleanly")
    void testDisplayMethods() throws SQLException {
        when(productDAO.getAllProducts()).thenReturn(List.of(p1, p2, p3));

        assertDoesNotThrow(() -> ecommerceService.displayProductDetails("1"));
        assertDoesNotThrow(() -> ecommerceService.displayProductDetails("Bluetooth"));
        assertDoesNotThrow(() -> ecommerceService.displayProductDetails("Unknown"));

        assertDoesNotThrow(() -> ecommerceService.displayAllProducts());
        assertDoesNotThrow(() -> ecommerceService.displayProductsSortedByRating());
        assertDoesNotThrow(() -> ecommerceService.displayProductsSortedByPrice());
    }

    @Test
    @DisplayName("registerUser validates empty username and password length")
    void testRegisterUserValidation() {
        assertFalse(ecommerceService.registerUser("Test User", "", "123456", "test@test.com", "9876543210"));
        assertFalse(ecommerceService.registerUser("Test User", "testuser", "123", "test@test.com", "9876543210"));
    }

    @Test
    @DisplayName("registerUser fails if username, email, or phone is already taken")
    void testRegisterUserTakenFields() throws SQLException {
        when(customerDAO.isUsernameTaken("existinguser")).thenReturn(true);
        assertFalse(ecommerceService.registerUser("Existing", "existinguser", "password123", "new@test.com", "9876543210"));

        when(customerDAO.isUsernameTaken("newuser")).thenReturn(false);
        when(customerDAO.isEmailTaken("taken@test.com")).thenReturn(true);
        assertFalse(ecommerceService.registerUser("New", "newuser", "password123", "taken@test.com", "9876543210"));

        when(customerDAO.isEmailTaken("unique@test.com")).thenReturn(false);
        when(customerDAO.isPhoneTaken("9999999999")).thenReturn(true);
        assertFalse(ecommerceService.registerUser("New", "newuser", "password123", "unique@test.com", "9999999999"));
    }

    @Test
    @DisplayName("registerUser succeeds with valid unique details")
    void testRegisterUserSuccess() throws SQLException {
        when(customerDAO.isUsernameTaken("validuser")).thenReturn(false);
        when(customerDAO.isEmailTaken("valid@test.com")).thenReturn(false);
        when(customerDAO.isPhoneTaken("9876543210")).thenReturn(false);
        when(customerDAO.registerCustomer(any(User.class))).thenReturn(true);

        boolean registered = ecommerceService.registerUser("Valid User", "validuser", "validpass123", "valid@test.com", "9876543210");
        assertTrue(registered);
        assertTrue(ecommerceService.isUserRegistered("validuser"));
    }

    @Test
    @DisplayName("loginUser fails on invalid arguments, unknown user, or incorrect password")
    void testLoginUserFailures() throws SQLException {
        assertFalse(ecommerceService.loginUser(null, "pass"));
        assertFalse(ecommerceService.loginUser("user", null));

        when(customerDAO.findByIdentifier("unknown")).thenReturn(null);
        assertFalse(ecommerceService.loginUser("unknown", "anyPass"));

        String hashed = PasswordUtil.hashPassword("CorrectPass123");
        User registeredUser = new User(10, "Test", "knownuser", hashed, "known@test.com", "9123456780");
        when(customerDAO.findByIdentifier("knownuser")).thenReturn(registeredUser);

        assertFalse(ecommerceService.loginUser("knownuser", "WrongPass"));
        assertFalse(ecommerceService.isLoggedIn());
    }

    @Test
    @DisplayName("loginUser succeeds on valid credentials and sets current user")
    void testLoginUserSuccess() throws SQLException {
        String hashed = PasswordUtil.hashPassword("Secret123");
        User user = new User(15, "Alice Brown", "alice", hashed, "alice@test.com", "9876501234");
        when(customerDAO.findByIdentifier("alice")).thenReturn(user);

        boolean loggedIn = ecommerceService.loginUser("alice", "Secret123");
        assertTrue(loggedIn);
        assertTrue(ecommerceService.isLoggedIn());
        assertEquals("Alice Brown", ecommerceService.getCurrentUser().getFullName());
    }

    @Test
    @DisplayName("loginUserWithOtp logs user in directly")
    void testLoginUserWithOtp() {
        assertFalse(ecommerceService.loginUserWithOtp(null));

        User user = new User(16, "Bob Green", "bobg", "hash", "bob@test.com", "9988776655");
        assertTrue(ecommerceService.loginUserWithOtp(user));
        assertTrue(ecommerceService.isLoggedIn());
        assertEquals(user, ecommerceService.getCurrentUser());
    }

    @Test
    @DisplayName("logoutUser resets current user and clears cart")
    void testLogoutUser() {
        ecommerceService.logoutUser(); // logged out when no one is logged in

        User user = new User(17, "Charlie", "charlie", "hash", "c@test.com", "9988001122");
        ecommerceService.loginUserWithOtp(user);
        ecommerceService.getCart().addItem(p1, 1);

        assertTrue(ecommerceService.isLoggedIn());
        assertFalse(ecommerceService.getCart().isEmpty());

        ecommerceService.logoutUser();
        assertFalse(ecommerceService.isLoggedIn());
        assertNull(ecommerceService.getCurrentUser());
        assertTrue(ecommerceService.getCart().isEmpty());
    }

    @Test
    @DisplayName("addProductToCart validates product existence, quantity, and stock")
    void testAddProductToCart() throws SQLException {
        when(productDAO.getAllProducts()).thenReturn(List.of(p1, p2, p3));
        when(productDAO.getStock(p1.getId())).thenReturn(10);

        assertFalse(ecommerceService.addProductToCart(999, 1));
        assertFalse(ecommerceService.addProductToCart(p1, 0));
        assertFalse(ecommerceService.addProductToCart(p1, -2));
        assertFalse(ecommerceService.addProductToCart(p1, 15)); // exceeds stock of 10

        assertTrue(ecommerceService.addProductToCart(p1, 2));
        assertEquals(2, ecommerceService.getCart().getQuantity(p1.getId()));
    }

    @Test
    @DisplayName("removeProductFromCart removes product and handles empty cart cleanly")
    void testRemoveProductFromCart() {
        ecommerceService.removeProductFromCart(1); // empty cart

        ecommerceService.addProductToCart(p1, 2);
        assertEquals(2, ecommerceService.getCart().getQuantity(p1.getId()));

        ecommerceService.removeProductFromCart(p1.getId());
        assertEquals(0, ecommerceService.getCart().getQuantity(p1.getId()));
    }

    @Test
    @DisplayName("viewCart displays cart without error")
    void testViewCart() {
        assertDoesNotThrow(() -> ecommerceService.viewCart());
        ecommerceService.addProductToCart(p1, 1);
        assertDoesNotThrow(() -> ecommerceService.viewCart());
    }

    @Test
    @DisplayName("placeOrder delegates to OrderService and refreshes catalog when order succeeds")
    void testPlaceOrder() throws SQLException {
        when(productDAO.getAllProducts()).thenReturn(List.of(p1, p2, p3));
        User user = new User(20, "Dave", "dave", "hash", "d@test.com", "9112233445");
        ecommerceService.loginUserWithOtp(user);
        ecommerceService.addProductToCart(p1, 1);

        Order placed = new Order(500, user.getUserId(), user.getFullName(), new ArrayList<>(), 2500.0);
        when(orderService.placeOrder(eq(user), any(Cart.class), eq("123 St"), eq("Card"))).thenReturn(placed);

        Order result = ecommerceService.placeOrder("123 St", "Card");
        assertNotNull(result);
        assertEquals(500, result.getOrderId());
        verify(orderService).placeOrder(user, ecommerceService.getCart(), "123 St", "Card");
    }

    @Test
    @DisplayName("displayUserOrderHistory delegates to OrderService")
    void testDisplayUserOrderHistory() {
        User user = new User(21, "Emma", "emma", "hash", "e@test.com", "9123412345");
        ecommerceService.loginUserWithOtp(user);

        ecommerceService.displayUserOrderHistory();
        verify(orderService).displayUserOrderHistory(user);
    }

    @Test
    @DisplayName("Getters for dependencies return non-null instances")
    void testGetters() {
        assertNotNull(ecommerceService.getCart());
        assertNotNull(ecommerceService.getOrderService());
        assertNotNull(ecommerceService.getOtpService());
        assertNotNull(ecommerceService.getProductDAO());
        assertNotNull(ecommerceService.getCustomerDAO());
        assertNotNull(ecommerceService.getCartDAO());
    }
}
