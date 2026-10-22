# Mini E-Commerce Order System

A robust console-based E-Commerce application built using **Core Java (Java 17), JDBC, Maven, and MySQL**. The project demonstrates enterprise-style product management, customer authentication with PBKDF2 hashing & OTP simulation, dual guest/database-backed shopping cart operations, ACID transactional order placement, and inventory management with pessimistic locking.

---

## 1. Project Overview

The **Mini E-Commerce Order System** simulates an online shopping platform directly in your terminal:
- **Guest Experience**: Unregistered or unauthenticated users can browse products, search items, sort by price or rating, and maintain an in-memory shopping cart.
- **Customer Experience**: Logged-in customers enjoy a persistent MySQL-backed cart. Guest cart items automatically merge upon login.
- **Reliable Checkout**: Orders are processed with ACID transaction guarantees via JDBC (`setAutoCommit(false)`, `commit()`, and `rollback()`), row-level stock locks (`SELECT ... FOR UPDATE`), and inventory deductions.
- **Security & Integrity**: Passwords are securely hashed with salted PBKDF2. Login supports username, email, or phone number, along with OTP verification.

---

## 2. Technology Stack

| Component | Technology / Specification |
|---|---|
| **Language** | Java 17 (LTS) |
| **Database** | MySQL 8.0 / 9.x |
| **Connectivity** | JDBC (Java Database Connectivity) |
| **Driver** | `mysql-connector-j` (v9.6.0) |
| **Build Tool** | Apache Maven |
| **Testing** | JUnit 5 & Mockito |
| **Interface** | Interactive Console (CLI) |

---

## 3. Key Features

### 🛍️ Product Management
- **Catalog Browsing**: View all available products with stock quantity, pricing, ratings, and review counts.
- **Dynamic Search**: Search products by partial, case-insensitive names.
- **Sorting Options**: Sort the catalog dynamically:
  - Price: Low to High / High to Low
  - Customer Rating: High to Low
- **Real-Time Database Sync**: Fetches active product details and stock levels from MySQL.

### 🔐 Customer Registration & Authentication
- **Multi-Identifier Login**: Authenticate using username, email address, or phone number.
- **Dual Login Methods**:
  - Secure password verification.
  - One-Time Password (OTP) verification simulation.
- **Salted PBKDF2 Hashing**: Passwords stored safely using `PBKDF2WithHmacSHA512` with unique salts.
- **Data Validation & Uniqueness**: Duplicate checking for username, email, and phone number to preserve account uniqueness.

### 🛒 Dual Shopping Cart Architecture
- **Guest Cart (In-Memory)**: Non-logged-in visitors can add, update, and review items in memory.
- **Database Cart (Persistent)**: Logged-in customers' cart items are synced and saved to the `cart` MySQL table.
- **Seamless Merge**: Automatically migrates guest cart items to the database cart upon customer login.
- **Stock Guard**: Validates quantity limits against available inventory before adding or updating items.

### 📦 Order Placement & Checkout
- **Strict Verification**: Ensures customer authentication and non-empty cart before checkout.
- **Delivery & Payment**: Collects delivery address and payment method (e.g., Cash on Delivery, Card, UPI).
- **Invoice Generation**: Formats and prints an order summary and invoice in the console.

### 🛡️ Transaction Management (ACID)
- **Atomic Checkout**: Uses manual JDBC transaction demarcation (`con.setAutoCommit(false)`).
- **Concurrency & Stock Locking**: Executes `SELECT ... FOR UPDATE` during validation to prevent race conditions and overselling.
- **Batch Processing**: Employs `PreparedStatement.addBatch()` and `executeBatch()` for efficient insertion of order items and stock updates.
- **Rollback Guarantee**: Automatically rolls back on any failure (insufficient inventory, database error), keeping stock and records consistent.

### 📜 Order History
- **Customer History**: View past orders tied to the authenticated user.
- **Detailed Invoices**: Displays order date, total amount, order status, delivery address, payment method, and individual line items with quantities and unit prices.

---

## 4. Database Schema & Design

The database schema consists of **5 relational tables**:

```text
  ┌──────────────┐          ┌──────────────┐
  │  customers   │1        *│    orders    │
  │──────────────│──────────│──────────────│
  │ customer_id  │          │ order_id     │
  │ username     │          │ customer_id  │
  │ password_hash│          │ total_amount │
  └──────────────┘          └──────┬───────┘
         │ 1                       │ 1
         │                         │
         │ *                       │ *
  ┌──────┴───────┐          ┌──────┴───────┐          ┌──────────────┐
  │     cart     │*        1│ order_items  │*        1│   products   │
  │──────────────│──────────│──────────────│──────────│──────────────│
  │ customer_id  │          │ order_id     │          │ product_id   │
  │ product_id   │          │ product_id   │          │ name, price  │
  └──────────────┘          └──────────────┘          │stock_quantity│
                                                      └──────────────┘
```

### Table Details

| Table | Purpose | Key Columns |
|---|---|---|
| `customers` | Stores registered customer profiles and password hashes | `customer_id` (PK), `username`, `email`, `password_hash`, `phone_number` |
| `products` | Stores item catalog, inventory stock, and ratings | `product_id` (PK), `name`, `price`, `stock_quantity`, `rating`, `review_count` |
| `cart` | Stores persistent shopping cart items for registered users | `cart_id` (PK), `customer_id` (FK), `product_id` (FK), `quantity` |
| `orders` | Stores confirmed order header details | `order_id` (PK), `customer_id` (FK), `order_date`, `total_amount`, `order_status` |
| `order_items` | Stores line items for each confirmed order | `order_item_id` (PK), `order_id` (FK), `product_id` (FK), `quantity`, `unit_price` |

### SQL Setup Script

You can run the following script in MySQL to create the database, tables, and sample data:

```sql
CREATE DATABASE IF NOT EXISTS mini_ecommerce_order_system;
USE mini_ecommerce_order_system;

-- 1. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) UNIQUE
);

-- 2. Products Table
CREATE TABLE IF NOT EXISTS products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    rating DECIMAL(3, 2) DEFAULT 0.0,
    review_count INT DEFAULT 0
);

-- 3. Cart Table
CREATE TABLE IF NOT EXISTS cart (
    cart_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE KEY uq_customer_product (customer_id, product_id)
);

-- 4. Orders Table
CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    order_date DATETIME NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    order_status VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED',
    delivery_address VARCHAR(255),
    payment_method VARCHAR(50),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- 5. Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Seed Sample Products
INSERT INTO products (name, price, stock_quantity, rating, review_count) VALUES
('Apple iPhone 15 Pro (128GB)', 119999.00, 15, 4.8, 120),
('Samsung Galaxy S24 Ultra', 129999.00, 10, 4.7, 95),
('Sony WH-1000XM5 Noise-Cancelling Headphones', 29990.00, 25, 4.6, 210),
('Apple MacBook Air M2 (16GB, 512GB)', 114900.00, 8, 4.9, 80),
('Logitech MX Master 3S Wireless Mouse', 8995.00, 40, 4.8, 350),
('Dell UltraSharp 27" 4K Monitor', 45500.00, 12, 4.5, 65);
```

---

## 5. Project Structure

```text
MiniE-CommerceOrderSystem/
├── pom.xml                                    # Maven configuration & dependencies
├── README.md                                  # Project documentation
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── ecommerce/
    │               ├── Main.java              # Interactive console UI & menu loop
    │               ├── DBConnection.java      # MySQL JDBC connection manager
    │               ├── Product.java           # Product entity model
    │               ├── User.java              # Customer entity model
    │               ├── Cart.java              # Cart model & business logic
    │               ├── CartItem.java          # Cart line-item model
    │               ├── Order.java             # Order & OrderItem models
    │               ├── EcommerceService.java  # Business logic coordinator
    │               ├── OrderService.java      # Order processing service
    │               ├── OtpService.java        # OTP generation & validation
    │               ├── PasswordUtil.java      # PBKDF2 hashing & salt utility
    │               └── dao/
    │                   ├── CustomerDAO.java   # Customer database operations
    │                   ├── ProductDAO.java    # Product queries & stock management
    │                   ├── CartDAO.java       # Persistent cart database operations
    │                   └── OrderDAO.java      # Transactional order placement
    └── test/
        └── java/
            └── com/
                └── ecommerce/
                    ├── CartItemTest.java
                    ├── CartTest.java
                    ├── EcommerceServiceTest.java
                    ├── OrderServiceTest.java
                    ├── OrderTest.java
                    ├── OtpServiceTest.java
                    ├── PasswordUtilTest.java
                    ├── ProductTest.java
                    └── UserTest.java
```

---

## 6. Setup & Installation Guide

### Prerequisites
- **JDK 17** or higher installed and configured in your `JAVA_HOME` / `PATH`.
- **MySQL 8.0+** running locally on port `3306`.
- **Apache Maven 3.8+** or IDE bundled Maven.

---

### Step 1: Start MySQL Server

#### Windows (Command Prompt as Administrator):
```cmd
net start MySQL80
```

#### macOS / Linux:
```bash
sudo systemctl start mysql
# or with brew:
brew services start mysql
```

---

### Step 2: Create Database & Tables

Log in to MySQL:
```bash
mysql -u root -p
```

Run the commands from the [SQL Setup Script](#sql-setup-script) above to create `mini_ecommerce_order_system` and seed initial products.

---

### Step 3: Configure Database Credentials

Open `src/main/java/com/ecommerce/DBConnection.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/mini_ecommerce_order_system";
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password"; // Set your local password
```

You can test database connectivity directly by running `DBConnection.main()`:
```bash
# Output should be: MySQL connected successfully!
```

---

### Step 4: Build and Test the Project

From the project root:

```bash
# Clean, compile, and run tests
mvn clean test

# Package into a JAR
mvn clean package
```

---

### Step 5: Run the Application

#### Option A: Run via IDE
- Open the project in **IntelliJ IDEA**, **Eclipse**, or **VS Code**.
- Navigate to `src/main/java/com/ecommerce/Main.java`.
- Run the `main(String[] args)` method.

#### Option B: Run via Maven Exec Plugin / Terminal
```bash
mvn compile exec:java -Dexec.mainClass="com.ecommerce.Main"
```

---

## 7. Order Placement Workflow

The order checkout workflow is strictly guarded by JDBC transactions:

```text
[Customer clicks Checkout]
            │
            ▼
[Check: Customer logged in?] ──── No ───► [Prompt Login / Register]
            │ Yes
            ▼
[Check: Cart is non-empty?] ───── No ───► [Display empty cart message]
            │ Yes
            ▼
[con.setAutoCommit(false)] ─────────────► (Start JDBC Transaction)
            │
            ▼
[SELECT ... FOR UPDATE] ────────────────► (Lock rows & verify stock)
            │
      Stock sufficient?
       /          \
     Yes           No
     /              \
    ▼                ▼
[INSERT orders]   [con.rollback()] ─────► [Abort with clear error]
    │
    ▼
[Batch INSERT order_items]
    │
    ▼
[Batch UPDATE products stock]
    │
    ▼
[DELETE FROM cart (customer)]
    │
    ▼
[con.commit()] ─────────────────────────► (Commit Transaction)
    │
    ▼
[Generate & Print Order Invoice]
```

---

## 8. Core Java & JDBC Concepts Practiced

- **Object-Oriented Programming**: Encapsulation, inheritance, modular class hierarchies, and Data Access Object (DAO) pattern.
- **Java Collections Framework**: `ArrayList`, `HashMap`, and `LinkedHashMap` for in-memory carts and structured queries.
- **Modern Java Idioms**: `Optional<T>` handling, method chaining, streams, `LocalDateTime`, and `Timestamp` mapping.
- **Functional Programming**: Custom `Comparator` implementations for sorting products by multiple criteria (price ascending/descending, rating).
- **Security Engineering**: Salted PBKDF2 (`PBKDF2WithHmacSHA512`) password hashing and OTP generation.
- **Database Best Practices**:
  - `PreparedStatement` to eliminate SQL injection vulnerabilities.
  - JDBC transactions with `setAutoCommit(false)`, `commit()`, and `rollback()`.
  - Batch execution (`addBatch()` & `executeBatch()`) for optimized multi-row inserts and updates.
  - Pessimistic locking (`FOR UPDATE`) for preventing stock overselling.
- **Unit Testing**: Thorough automated test coverage using **JUnit 5** and **Mockito**.

---

## 9. Future Enhancements

- [ ] External email / SMS gateway integration for real-time OTP delivery.
- [ ] Externalized configuration (e.g., `application.properties` or environment variables for database credentials).
- [ ] Admin CLI module for managing products, restocking inventory, and updating order status.
- [ ] Payment gateway simulation / mock integration (Stripe, Razorpay).
- [ ] Integration tests using Testcontainers and Dockerized MySQL.

---

## 10. License

This project is licensed under the [MIT License](LICENSE) - feel free to use, modify, and distribute for educational or development purposes.
