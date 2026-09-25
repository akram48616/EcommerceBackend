# Ecommerce Backend

A comprehensive Spring Boot REST API backend for e-commerce applications with user authentication, product management, shopping cart, orders, payments, and analytics.

## 📋 Overview

This project is a full-featured e-commerce backend built with Spring Boot 3.5.4 and Java 17. It provides RESTful APIs for managing products, users, orders, payments, shopping carts, wishlists, reviews, and more. The application includes enterprise-level features like JWT authentication, caching, async processing, and integrated payment processing via Razorpay.

## ✨ Features

### User & Authentication
- User registration and login
- JWT-based authentication with refresh tokens
- Secure password management with bcrypt
- Token blacklisting for logout functionality
- User profile management

### Product Management
- Product CRUD operations
- Product categorization
- Stock management with low-stock alerts
- Product search and filtering
- Pagination support
- Product reviews and ratings

### Shopping & Orders
- Shopping cart management
- Order creation and tracking
- Order history and status management
- Order item management
- Multiple payment methods support

### Payment Processing
- Razorpay payment integration
- Payment verification and confirmation
- Payment order creation
- Transaction tracking

### Wishlist
- Add/remove products from wishlist
- Wishlist management per user
- Wishlist persistence

### Analytics & Reporting
- Dashboard summary with key metrics
- Sales analytics with chart data
- Low-stock product alerts
- Top products tracking
- Comprehensive analytics endpoints

### Additional Features
- Caching with Redis
- Asynchronous processing
- Email notifications
- Global exception handling
- OpenAPI/Swagger documentation
- Data validation with Bean Validation

## 🛠️ Technology Stack

### Core Framework
- **Spring Boot** 3.5.4
- **Java** 17
- **Maven** (Build tool)

### Database
- **MySQL** (Production database)
- **H2** (Testing database)
- **Spring Data JPA** (ORM)

### Authentication & Security
- **Spring Security** 
- **JWT (JSON Web Tokens)** using JJWT
- **Bcrypt** (Password encryption)

### Caching & Performance
- **Redis** (Distributed caching)
- **Spring Caching** (Cache abstraction)

### APIs & Documentation
- **Spring MVC** (REST APIs)
- **OpenAPI 3.0** (API documentation)
- **Swagger UI** (Interactive documentation)

### Utilities
- **Lombok** (Code generation)
- **ModelMapper** (Object mapping)
- **Razorpay SDK** (Payment integration)

### Testing
- **JUnit 5**
- **Mockito** (Mocking framework)

## 📁 Project Structure

```
src/main/java/org/example/ecommercebackend/
├── Controller/           # REST API endpoints
│   ├── AuthController.java
│   ├── UserController.java
│   ├── ProductController.java
│   ├── CartController.java
│   ├── OrderController.java
│   ├── PaymentController.java
│   ├── ReviewController.java
│   ├── WishlistController.java
│   ├── CategoryController.java
│   └── AnalyticsController.java
├── Service/              # Business logic
│   ├── AuthService.java
│   ├── UserService.java
│   ├── ProductService.java
│   ├── CartService.java
│   ├── OrderService.java
│   ├── PaymentService.java
│   ├── ReviewService.java
│   ├── WishlistService.java
│   ├── CategoryService.java
│   ├── EmailService.java
│   ├── AnalyticsService.java
│   └── RefreshTokenService.java
├── Entity/               # JPA entities
│   ├── User.java
│   ├── Product.java
│   ├── Category.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Payment.java
│   ├── Review.java
│   ├── Wishlist.java
│   ├── WishlistItem.java
│   ├── RefreshToken.java
│   └── TokenBlacklist.java
├── Repository/           # Data access layer
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   ├── CategoryRepository.java
│   ├── CartRepository.java
│   ├── CartItemRepository.java
│   ├── OrderRepository.java
│   ├── OrderItemRepository.java
│   ├── PaymentRepository.java
│   ├── ReviewRepository.java
│   ├── WishlistRepository.java
│   ├── WishlistItemRepository.java
│   ├── RefreshTokenRepository.java
│   └── TokenBlacklistRepository.java
├── DTO/                  # Data Transfer Objects
│   ├── RequestDTO/       # Incoming request objects
│   └── ResponseDTO/      # Outgoing response objects
├── Config/               # Configuration classes
│   ├── SecurityConfig.java
│   ├── RedisConfig.java
│   ├── RazorpayConfig.java
│   ├── OpenApiConfig.java
│   ├── AsyncConfig.java
│   └── UserDetailsConfig.java
├── Security/             # Security utilities
│   ├── JwtUtil.java      # JWT token generation/validation
│   └── JwtFilter.java    # JWT authentication filter
├── Exception/            # Custom exception classes
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   ├── DuplicateEmailException.java
│   ├── InsufficientStockException.java
│   ├── EmptyCartException.java
│   └── ErrorResponse.java
└── EcommerceBackendApplication.java  # Main application class
```

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.6** or higher
- **MySQL 8.0** or higher (for production)
- **Redis** (optional, for caching)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd EcommerceBackend
   ```

2. **Configure the database**
   
   Create a MySQL database:
   ```sql
   CREATE DATABASE ecommerce_db;
   ```

   Update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
   spring.datasource.username=root
   spring.datasource.password=your_password
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=false
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
   ```

3. **Configure Redis** (optional)
   ```properties
   spring.redis.host=localhost
   spring.redis.port=6379
   ```

4. **Configure Razorpay** (for payment integration)
   ```properties
   razorpay.key.id=your_razorpay_key_id
   razorpay.key.secret=your_razorpay_key_secret
   ```

5. **Build the project**
   ```bash
   mvn clean install
   ```

6. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## 📚 API Documentation

Once the application is running, you can access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🔐 Authentication

The API uses JWT (JSON Web Tokens) for authentication:

1. **Register/Login** to get an access token and refresh token
2. **Include the token** in the Authorization header:
   ```
   Authorization: Bearer <your_jwt_token>
   ```
3. **Refresh token** when access token expires using the refresh endpoint

## 📝 Key Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/logout` - Logout user

### Products
- `GET /api/products` - Get all products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product (Admin)
- `PUT /api/products/{id}` - Update product (Admin)
- `DELETE /api/products/{id}` - Delete product (Admin)

### Shopping Cart
- `GET /api/cart` - Get user's cart
- `POST /api/cart/items` - Add item to cart
- `PUT /api/cart/items/{id}` - Update cart item
- `DELETE /api/cart/items/{id}` - Remove from cart
- `DELETE /api/cart` - Clear cart

### Orders
- `POST /api/orders` - Create order
- `GET /api/orders` - Get user's orders
- `GET /api/orders/{id}` - Get order details
- `PUT /api/orders/{id}/status` - Update order status

### Payments
- `POST /api/payments/create-order` - Create payment order
- `POST /api/payments/verify` - Verify payment

### Wishlist
- `GET /api/wishlist` - Get user's wishlist
- `POST /api/wishlist/items` - Add to wishlist
- `DELETE /api/wishlist/items/{id}` - Remove from wishlist

### Reviews
- `POST /api/reviews` - Create product review
- `GET /api/products/{id}/reviews` - Get product reviews

### Analytics
- `GET /api/analytics/dashboard` - Get dashboard summary
- `GET /api/analytics/sales` - Get sales data
- `GET /api/analytics/low-stock` - Get low-stock products

## 🧪 Testing

Run the test suite:
```bash
mvn test
```

## 🔧 Configuration

### Application Properties
Key configurations in `application.properties`:

- **Database**: MySQL connection settings
- **JPA**: Hibernate DDL strategy and dialect
- **JWT**: Token secret and expiration time
- **Redis**: Cache configuration
- **Razorpay**: Payment gateway credentials
- **Email**: SMTP configuration for notifications
- **Async**: Thread pool configuration for async tasks

## 🚢 Deployment

### Building for Production
```bash
mvn clean package
```

This creates a JAR file in the `target` directory.

### Running the JAR
```bash
java -jar target/EcommerceBackend-0.0.1-SNAPSHOT.jar
```

### Docker Deployment (Optional)
Create a `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/EcommerceBackend-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:
```bash
docker build -t ecommerce-backend .
docker run -p 8080:8080 ecommerce-backend
```

## 📊 Database Schema

The application uses the following main entities:
- **User**: User account information
- **Product**: Product catalog
- **Category**: Product categories
- **Cart**: Shopping carts
- **CartItem**: Items in cart
- **Order**: Customer orders
- **OrderItem**: Order details
- **Payment**: Payment transactions
- **Review**: Product reviews
- **Wishlist**: User wishlists
- **RefreshToken**: Token management

## 🐛 Exception Handling

The application includes global exception handling with custom exceptions:
- `ResourceNotFoundException` - Resource not found
- `BadRequestException` - Invalid request
- `DuplicateEmailException` - Email already exists
- `InsufficientStockException` - Product out of stock
- `EmptyCartException` - Cart is empty

## 🔄 Caching Strategy

Redis caching is implemented for:
- Product data
- Category information
- User profiles
- Cart data

Cache is invalidated on updates/deletions.

## 📧 Email Integration

The `EmailService` sends notifications for:
- Order confirmations
- Payment status updates
- Shipping updates

## 🎯 Future Enhancements

- [ ] Multi-vendor support
- [ ] Coupon and discount system
- [ ] Advanced inventory management
- [ ] Mobile app API versioning
- [ ] Real-time notifications (WebSocket)
- [ ] Elasticsearch integration for search
- [ ] Machine learning for recommendations

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Contributing

Contributions are welcome! Please follow these steps:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Support

For support, email support@example.com or open an issue in the repository.

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT Guide](https://jwt.io/)
- [Razorpay Documentation](https://razorpay.com/docs/)

---

**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: 2024
