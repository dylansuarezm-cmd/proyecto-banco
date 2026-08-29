# Software Architecture - NexusMarket

## Overview

NexusMarket is a centralized digital marketplace that acts as a commercial intermediary between buyers and sellers. The system manages the complete business operation, including user management, seller administration, product catalog management, distributed inventory, shopping carts, orders, billing, logistics, returns, refunds, and administrative reporting.

Based on the functional specification, NexusMarket will use a **Hexagonal Architecture (Ports and Adapters)** combined with **Domain-Driven Design (DDD)** principles.

The main objective of this architecture is to keep the business rules independent from external technologies such as databases, REST communication, frameworks, and infrastructure components.

This approach promotes maintainability, testability, scalability, low coupling, and a clear separation of responsibilities.

---

# Architectural Principles

The architecture is based on the following principles:

- Domain-first design.
- Separation of concerns.
- Dependency inversion.
- Technology independence.
- High cohesion.
- Low coupling.
- Explicit boundaries between layers.
- Business rules centralized in the Domain.
- External technologies connected through Ports and Adapters.

The Domain must contain the core NexusMarket business rules and must not depend directly on infrastructure or communication technologies.

---

# Architecture Layers

The application is organized into four major components:

```text
Application
│
├── Adapters
│
├── Domain
│
└── Infrastructure
```

Each component has a clearly defined responsibility.

---

# Package Structure

The following structure is proposed for the NexusMarket implementation:

```text
src/
└── main/
    └── java/
        └── nexusmarket/
            │
            ├── App.java
            │
            ├── adapters/
            │   │
            │   ├── in/
            │   │   └── rest/
            │   │       ├── controllers/
            │   │       ├── requests/
            │   │       ├── responses/
            │   │       └── mappers/
            │   │
            │   └── out/
            │       └── persistence/
            │           ├── mysql/
            │           │   ├── adapters/
            │           │   ├── entities/
            │           │   ├── repositories/
            │           │   └── mappers/
            │           │
            │           └── mongodb/
            │               ├── adapters/
            │               ├── documents/
            │               ├── repositories/
            │               └── mappers/
            │
            ├── domain/
            │   ├── models/
            │   ├── valueobjects/
            │   ├── enums/
            │   ├── services/
            │   ├── exceptions/
            │   └── ports/
            │       ├── in/
            │       └── out/
            │
            └── infrastructure/
                ├── config/
                ├── database/
                └── security/
```

---

# Layer Responsibilities

## Application

The `application` package represents the root of the NexusMarket application.

### Responsibilities

- Application bootstrap.
- Component organization.
- Dependency composition.
- Initialization of the application environment.

## App.java

`App.java` is the application entry point.

### Responsibilities

- Initialize the application.
- Load infrastructure configuration.
- Configure dependencies.
- Start the application.

---

# Adapters

Adapters connect NexusMarket with external technologies.

They translate external requests into application operations and transform domain results into technology-specific representations.

The Domain must never communicate directly with external systems.

---

## Input Adapters

Input adapters expose the application to external clients.

Current proposed implementation:

```text
adapters/in/rest
```

### Responsibilities

- Receive HTTP requests.
- Validate incoming data.
- Convert Request DTOs into application/domain objects.
- Execute application use cases.
- Convert domain results into Response DTOs.

---

## Controllers

Controllers expose REST endpoints for the main NexusMarket operations.

Possible controllers include:

- `UserController`
- `BuyerController`
- `SellerController`
- `WarehouseController`
- `ProductController`
- `InventoryController`
- `CartController`
- `OrderController`
- `InvoiceController`
- `ShipmentController`
- `ReturnController`
- `RefundController`
- `ReportController`

### Responsibilities

- Receive HTTP requests.
- Delegate execution to the appropriate input port.
- Return HTTP responses.

Controllers must not contain business rules.

---

## Requests

Request DTOs represent incoming API payloads.

Examples:

- `CreateBuyerRequest`
- `CreateSellerRequest`
- `CreateProductRequest`
- `AddToCartRequest`
- `CreateOrderRequest`
- `CreateWarehouseRequest`

### Responsibilities

- Receive client data.
- Validate basic input.
- Transport data into the application layer.

These objects must not contain business logic.

---

## Responses

Response DTOs represent information returned by the API.

Examples:

- `UserResponse`
- `ProductResponse`
- `InventoryResponse`
- `OrderResponse`
- `InvoiceResponse`
- `ShipmentResponse`
- `RefundResponse`
- `ReportResponse`

### Responsibilities

- Return processed information.
- Hide internal domain implementation.
- Standardize API responses.

---

## Mappers

Mappers convert between external representations and domain/application objects.

Main conversions:

```text
Request DTO ↔ Domain Model
Domain Model ↔ Response DTO
```

This prevents REST-specific objects from entering the Domain layer.

---

# Output Adapters

Output adapters connect the Domain with external resources.

Possible external resources include:

- Relational databases.
- Document databases for audit information.
- Payment services.
- Logistics services.
- Notification services.

The exact technologies can be selected during implementation without changing the business Domain.

---

## MySQL Adapter

A relational database can be used for transactional information such as:

- Users.
- Buyers.
- Sellers.
- Warehouses.
- Products.
- Inventory.
- Shopping carts.
- Orders.
- Invoices.
- Shipments.
- Returns.
- Refunds.

### Components

#### Entities

Represent relational database tables.

#### Repositories

Implement persistence operations.

#### Mappers

Convert Domain Models into database entities and vice versa.

#### Adapters

Implement the Domain output ports.

---

## MongoDB Adapter

A document-oriented database can be used for information such as audit records or operational traceability if required by the implementation.

### Components

#### Documents

Represent MongoDB collections.

#### Repositories

Provide document persistence.

#### Mappers

Convert Domain objects into MongoDB documents.

#### Adapters

Implement the corresponding persistence ports.

---

# Domain

The Domain layer is the core of NexusMarket.

It contains the business entities, business rules, validations, domain services, value objects, and communication contracts required by the application.

The Domain must remain independent from external technologies.

No class inside the Domain should directly depend on:

- Spring.
- JPA.
- MySQL.
- MongoDB.
- HTTP.
- REST.
- JSON.
- SQL.

---

# Domain Models

The Domain Models represent the main business concepts identified in the NexusMarket functional specification.

## User

Represents a person authorized to interact with the system.

Relevant concepts include:

- Identifier.
- Full name.
- Email.
- Role.
- Status.

Business rules include:

- The user identifier must be unique.
- The email must be unique.
- Each user has a single role.
- A user can only operate according to the permissions of that role.

---

## Buyer

Represents a customer who purchases published products.

Relevant concepts include:

- Main address.
- Additional addresses.
- Commercial status.

The Buyer must not manage information belonging to other buyers or inventory.

---

## Seller

Represents a provider responsible for registering and managing products.

A Seller cannot self-register. Sellers are incorporated into the marketplace by an Administrator.

---

## Warehouse

Represents a physical storage location.

The marketplace distinguishes between:

- Marketplace warehouses.
- Seller warehouses.

---

## Product

Represents a physical or digital product offered through the marketplace.

Relevant concepts include:

- Product type.
- Variants.
- Publication status.

Product types:

- Physical.
- Digital.

Physical products require inventory and shipment. Digital products can be delivered immediately after payment.

---

## Inventory

Represents available product stock associated with a specific product and warehouse.

Inventory movements include:

- Entry.
- Reservation.
- Sale output.
- Adjustment.
- Return.

Critical rule:

> Inventory quantities must never become negative.

Inventory cannot be reserved if it does not exist or if it is marked as damaged.

---

## Shopping Cart

Represents the buyer's provisional selection of products before creating an order.

---

## Order

Represents the formal commercial commitment between the buyer and the marketplace.

The order lifecycle is:

```text
Cart
  ↓
Pending Payment
  ↓
Paid
  ↓
Dispatched
  ↓
Delivered / Finalized
```

A finalized order cannot be modified.

---

## Invoice

Represents the commercial billing information associated with a purchase.

---

## Shipment

Represents the logistics process required to deliver physical products.

The logistics process includes:

- Preparation.
- Packaging.
- Dispatch.
- Transportation.
- Delivery confirmation.

---

## Return

Represents a product return process initiated according to the business rules.

---

## Refund

Represents the reimbursement associated with an approved return or other valid business condition.

---

# Value Objects

Value Objects represent immutable business concepts that are identified by their value rather than by a unique identity.

Possible NexusMarket Value Objects include:

- `UserId`
- `Email`
- `Address`
- `ProductId`
- `OrderId`
- `WarehouseId`
- `Money`
- `Quantity`

These objects can encapsulate validation rules and prevent invalid values from entering the Domain.

---

# Enums

Enums represent controlled values that do not require complex independent behavior.

Possible NexusMarket enums include:

- `UserRole`
- `UserStatus`
- `ProductType`
- `ProductStatus`
- `InventoryMovementType`
- `OrderStatus`
- `WarehouseType`
- `ReturnStatus`
- `RefundStatus`

Examples of `UserRole` values:

```text
BUYER
SELLER
LOGISTICS_OPERATOR
ADMINISTRATOR
SUPERVISOR
```

Examples of `OrderStatus` values:

```text
CART
PENDING_PAYMENT
PAID
DISPATCHED
DELIVERED
FINALIZED
```

---

# Domain Services

Domain Services contain business logic that does not naturally belong to a single entity.

Possible NexusMarket services include:

## InventoryService

Responsible for operations such as:

- Reserve inventory.
- Register inventory entry.
- Register sale output.
- Register adjustments.
- Process returned inventory.
- Prevent negative inventory.

## OrderService

Responsible for:

- Creating orders.
- Validating order transitions.
- Confirming payment-related transitions.
- Preventing changes to finalized orders.

## SellerRegistrationService

Responsible for:

- Registering sellers through the Administrator.
- Associating the seller's first warehouse.

## LogisticsService

Responsible for coordinating:

- Order preparation.
- Packaging.
- Dispatch.
- Delivery confirmation.

## RefundService

Responsible for:

- Validating refund requests.
- Coordinating approved refunds.
- Maintaining refund business rules.

---

# Ports

Ports define the communication contracts between the Domain and external components.

The Domain owns these interfaces.

---

## Input Ports

Input Ports represent the use cases that NexusMarket supports.

Possible Input Ports include:

- `RegisterBuyerUseCase`
- `RegisterSellerUseCase`
- `CreateWarehouseUseCase`
- `CreateProductUseCase`
- `ManageInventoryUseCase`
- `ManageCartUseCase`
- `CreateOrderUseCase`
- `ProcessPaymentUseCase`
- `ManageShipmentUseCase`
- `ManageReturnUseCase`
- `ProcessRefundUseCase`
- `GenerateAdministrativeReportUseCase`

Input Ports define what the system can do.

---

## Output Ports

Output Ports represent dependencies required by the Domain.

Possible Output Ports include:

- `UserRepository`
- `BuyerRepository`
- `SellerRepository`
- `WarehouseRepository`
- `ProductRepository`
- `InventoryRepository`
- `CartRepository`
- `OrderRepository`
- `InvoiceRepository`
- `ShipmentRepository`
- `ReturnRepository`
- `RefundRepository`
- `ReportRepository`
- `PaymentGateway`
- `NotificationService`

Output Ports define what the Domain needs from external systems.

---

# Domain Exceptions

Business exceptions belong exclusively to the Domain.

Possible NexusMarket exceptions include:

- `DuplicateUserException`
- `InvalidRoleException`
- `SellerSelfRegistrationException`
- `InsufficientInventoryException`
- `NegativeInventoryException`
- `InvalidOrderStateException`
- `FinalizedOrderModificationException`
- `InvalidProductStatusException`
- `InvalidRefundException`

These exceptions represent violations of NexusMarket business rules.

---

# Infrastructure

Infrastructure contains the technical configuration required by the application.

It must not contain business logic.

---

## Config

Responsible for application configuration.

Examples:

- REST configuration.
- Serialization.
- Environment variables.
- Dependency injection configuration.

---

## Database

Responsible for database configuration.

Examples:

- MySQL configuration.
- MongoDB configuration.
- Database connections.
- Connection pools.
- Database initialization.

---

## Security

Responsible for technical authentication and authorization mechanisms.

Examples:

- JWT configuration.
- Password encoding.
- Authentication filters.
- Authorization configuration.

The functional specification requires every operation to be executed by an authenticated user, while the technical authentication mechanism is outside the scope of that specification.

---

# Dependency Flow

Dependencies must always point toward the Domain.

```text
REST Controller
       │
       ▼
Input Port / Use Case
       │
       ▼
Domain Service / Domain Model
       │
       ▼
Output Port
       │
       ▼
Persistence Adapter / External Adapter
       │
       ▼
Database / External System
```

The Domain never depends on Adapters or Infrastructure.

---

# NexusMarket Main Business Flow

The architecture supports the business flow defined in the functional specification:

```text
Administrator
     │
     ▼
Register Seller
     │
     ▼
Create Seller Warehouse
     │
     ▼
Seller Registers Products
     │
     ▼
Register Initial Inventory
     │
     ▼
Publish Products
     │
     ▼
Buyer Selects Products
     │
     ▼
Shopping Cart
     │
     ▼
Create Order
     │
     ▼
Payment Validation
     │
     ▼
Order Paid
     │
     ▼
Preparation and Packaging
     │
     ▼
Dispatch
     │
     ▼
Transportation
     │
     ▼
Delivery Confirmation
     │
     ▼
Order Finalized
```

---

# Role-Based Access

NexusMarket defines five business participants:

| Role | Main Responsibility |
|---|---|
| Buyer | Purchases published products. |
| Seller | Registers and manages products. |
| Logistics Operator | Operates warehouses and dispatch processes. |
| Administrator | Manages sellers and warehouses. |
| Supervisor | Performs operational consultation and monitoring. |

Each user has a single role and may only manage information corresponding to that role.

---

# Architectural Mapping to Functional Domains

| Functional Domain | Main Domain Components |
|---|---|
| User Management | User, UserId, Email, UserRole, UserStatus, UserRepository |
| Buyer Management | Buyer, Address, BuyerRepository |
| Seller Management | Seller, SellerRegistrationService, SellerRepository |
| Warehouse Management | Warehouse, WarehouseType, WarehouseRepository |
| Catalog Management | Product, ProductType, ProductStatus, ProductRepository |
| Inventory Management | Inventory, Quantity, InventoryMovementType, InventoryService |
| Shopping Cart | ShoppingCart, CartItem, CartRepository |
| Order Management | Order, OrderStatus, OrderService, OrderRepository |
| Billing | Invoice, InvoiceRepository |
| Logistics | Shipment, LogisticsService, ShipmentRepository |
| Returns | Return, ReturnStatus, ReturnRepository |
| Refunds | Refund, RefundStatus, RefundService, RefundRepository |
| Administrative Reports | ReportService, ReportRepository |

---

# Architectural Constraints

The following rules must always be respected:

1. Business logic belongs exclusively to the Domain layer.
2. Controllers must not contain business rules.
3. DTOs must never enter the Domain layer.
4. Persistence entities must never be exposed directly through the API.
5. Communication between external technologies and the Domain must occur through Ports.
6. Adapters implement Ports but do not define business rules.
7. Infrastructure depends on the Domain, never the opposite.
8. Dependencies must point toward the Domain.
9. Business entities must remain framework-independent.
10. The Domain must be testable without requiring infrastructure components.
11. Inventory must never become negative.
12. A finalized order cannot be modified.
13. Sellers cannot self-register.
14. Each user must have exactly one role.
15. Users cannot manage information outside their assigned role.
16. User identification data and email must be unique.

---

# Non-Functional Architectural Considerations

## Maintainability

The separation between Domain, Application, Adapters, and Infrastructure makes the system easier to modify without affecting unrelated components.

## Testability

Domain rules can be tested independently from databases, REST APIs, and infrastructure.

## Scalability

The architecture allows NexusMarket to evolve as the number of users, products, orders, warehouses, and transactions increases.

## Technology Independence

The business rules are not tied to a particular framework, database, or communication protocol.

## Security

Authentication and authorization mechanisms are isolated in the Infrastructure layer while business-level role restrictions remain part of the application and Domain rules.

## Traceability

The architecture maps the functional domains from the NexusMarket specification to explicit Domain Models, Services, Ports, and Adapters.

---

# Benefits

This architecture provides:

- Technology independence.
- High maintainability.
- Clear separation of concerns.
- Improved testability.
- Easier scalability.
- Better support for Domain-Driven Design.
- Easy replacement of frameworks or databases.
- Reusable business logic.
- Clear business boundaries.
- Strong control over business rules.
- Better traceability between requirements and implementation.

---

# Conclusion

The proposed NexusMarket architecture applies the same architectural direction used by the reference project: Hexagonal Architecture with Domain-Driven Design.

The Domain is the center of the solution and contains the business concepts and rules of the marketplace. Input Adapters expose use cases to external clients, Output Adapters connect the system with persistence and external services, and Infrastructure contains technical configuration.

This structure provides a clear foundation for implementing the NexusMarket functional requirements while keeping the core business logic independent from implementation technologies.
