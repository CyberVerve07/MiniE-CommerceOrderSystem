package com.ecommerce;

import com.ecommerce.dao.OrderDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderDAO orderDAO;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User(1, "Test User", "testuser", "hashedpass", "test@test.com", "9876543210");
        product = new Product(10, "USB Drive", 500.0, 20);
        cart = new Cart();
        cart.addItem(product, 2);
    }

    @Test
    @DisplayName("placeOrder returns null if user is null")
    void testPlaceOrderNullUser() throws SQLException {
        Order result = orderService.placeOrder(null, cart, "Address", "UPI");
        assertNull(result);
        verify(orderDAO, never()).placeOrder(any(), any(), any(), any());
    }

    @Test
    @DisplayName("placeOrder returns null if cart is null or empty")
    void testPlaceOrderEmptyCart() throws SQLException {
        Order result1 = orderService.placeOrder(user, null, "Address", "UPI");
        assertNull(result1);

        Cart emptyCart = new Cart();
        Order result2 = orderService.placeOrder(user, emptyCart, "Address", "UPI");
        assertNull(result2);

        verify(orderDAO, never()).placeOrder(any(), any(), any(), any());
    }

    @Test
    @DisplayName("placeOrder clears cart and returns order on success")
    void testPlaceOrderSuccess() throws SQLException {
        List<Order.OrderItem> items = new ArrayList<>();
        items.add(new Order.OrderItem(10, "USB Drive", 2, 500.0));
        Order mockOrder = new Order(1001, user.getUserId(), user.getFullName(), "123 Street", "UPI", items, 1000.0);

        when(orderDAO.placeOrder(eq(user), eq(cart), eq("123 Street"), eq("UPI"))).thenReturn(mockOrder);

        Order placedOrder = orderService.placeOrder(user, cart, "123 Street", "UPI");

        assertNotNull(placedOrder);
        assertEquals(1001, placedOrder.getOrderId());
        assertEquals(1000.0, placedOrder.getTotalAmount(), 0.001);
        assertTrue(cart.isEmpty()); // Cart should be cleared on success
        verify(orderDAO, times(1)).placeOrder(user, cart, "123 Street", "UPI");
    }

    @Test
    @DisplayName("placeOrder catches SQLException and returns null without throwing")
    void testPlaceOrderSQLException() throws SQLException {
        when(orderDAO.placeOrder(any(), any(), any(), any()))
                .thenThrow(new SQLException("Database connection error"));

        Order result = orderService.placeOrder(user, cart, "Address", "COD");
        assertNull(result);
        assertFalse(cart.isEmpty()); // Cart should not be cleared on failure
    }

    @Test
    @DisplayName("placeOrder overloaded method uses default address and payment")
    void testPlaceOrderOverloaded() throws SQLException {
        List<Order.OrderItem> items = new ArrayList<>();
        Order mockOrder = new Order(1002, user.getUserId(), user.getFullName(), items, 1000.0);

        when(orderDAO.placeOrder(eq(user), eq(cart), eq("Standard Delivery"), eq("Cash on Delivery")))
                .thenReturn(mockOrder);

        Order result = orderService.placeOrder(user, cart);
        assertNotNull(result);
        verify(orderDAO).placeOrder(user, cart, "Standard Delivery", "Cash on Delivery");
    }

    @Test
    @DisplayName("getOrdersByUserId returns list from DAO")
    void testGetOrdersByUserIdSuccess() throws SQLException {
        List<Order> expectedOrders = new ArrayList<>();
        expectedOrders.add(new Order(1, user.getUserId(), user.getFullName(), new ArrayList<>(), 500.0));

        when(orderDAO.getOrdersByCustomerId(1)).thenReturn(expectedOrders);

        List<Order> actualOrders = orderService.getOrdersByUserId(1);
        assertEquals(1, actualOrders.size());
        assertEquals(1, actualOrders.get(0).getOrderId());
    }

    @Test
    @DisplayName("getOrdersByUserId returns empty list on SQLException")
    void testGetOrdersByUserIdSQLException() throws SQLException {
        when(orderDAO.getOrdersByCustomerId(1)).thenThrow(new SQLException("Query failed"));

        List<Order> orders = orderService.getOrdersByUserId(1);
        assertNotNull(orders);
        assertTrue(orders.isEmpty());
    }

    @Test
    @DisplayName("displayUserOrderHistory handles null user, empty orders, and existing orders")
    void testDisplayUserOrderHistory() throws SQLException {
        assertDoesNotThrow(() -> orderService.displayUserOrderHistory(null));

        when(orderDAO.getOrdersByCustomerId(user.getUserId())).thenReturn(new ArrayList<>());
        assertDoesNotThrow(() -> orderService.displayUserOrderHistory(user));

        List<Order> userOrders = new ArrayList<>();
        userOrders.add(new Order(201, user.getUserId(), user.getFullName(), new ArrayList<>(), 250.0));
        when(orderDAO.getOrdersByCustomerId(user.getUserId())).thenReturn(userOrders);
        assertDoesNotThrow(() -> orderService.displayUserOrderHistory(user));
    }

    @Test
    @DisplayName("getAllOrders returns list on success and empty list on SQLException")
    void testGetAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        orders.add(new Order(1, 1, "User", new ArrayList<>(), 100.0));
        when(orderDAO.getAllOrders()).thenReturn(orders);

        assertEquals(1, orderService.getAllOrders().size());

        when(orderDAO.getAllOrders()).thenThrow(new SQLException("Error"));
        assertTrue(orderService.getAllOrders().isEmpty());
    }

    @Test
    @DisplayName("findOrderById returns order on success and null on SQLException")
    void testFindOrderById() throws SQLException {
        Order mockOrder = new Order(55, 1, "User", new ArrayList<>(), 500.0);
        when(orderDAO.getOrderById(55)).thenReturn(mockOrder);

        assertEquals(mockOrder, orderService.findOrderById(55));

        when(orderDAO.getOrderById(99)).thenThrow(new SQLException("Error"));
        assertNull(orderService.findOrderById(99));
    }
}
