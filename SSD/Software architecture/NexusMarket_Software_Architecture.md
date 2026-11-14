# Software Architecture — NexusMarket

## Overview

NexusMarket follows a **Hexagonal Architecture (Ports and Adapters)** combined with **Domain-Driven Design (DDD)** principles.

The goal of this architecture is to keep the marketplace business domain — users, sellers, buyers, warehouses, catalog, inventory, cart, orders, invoicing, logistics, returns and refunds — isolated from frameworks, databases, and communication protocols. Business rules live in the domain and never depend on external technology.

This document describes the architecture **as it is implemented in the repository**.

## Implementation Status

| Component | Status |
| --- | --- |
| Domain models (23 entities with behavior) | Implemented |
| Domain value objects (13) and enums (3) | Implemented |
| Domain exceptions (23) | Implemented |
| Input ports (22 use cases) | Implemented |
| Output ports (15 repository contracts) | Implemented |
| Domain services (22) and one helper component (`InventoryMovementRecorder`) | Implemented |
| Input adapters (REST controllers, DTOs, error handling, Swagger) | Implemented |
| Output adapters (JPA persistence on MySQL) | Implemented |
| Security (login, roles, first administrator) | Implemented |

---

# Architectural Principles

- Domain-first design.
- Separation of concerns.
- Dependency inversion: the domain defines the interfaces, adapters implement them.
- Technology independence.
- High cohesion and low coupling.
- Rich domain model: entities protect their own business rules instead of being simple data holders.
- One use case per input port and one service per use case.

---

# Architecture Layers

```text
Application
│
├── Adapters          (implemented: REST, JPA persistence, security)
│
├── Domain            (implemented)
│
└── Configuration     (application.properties)
```

---

# Package Structure

The root package is `application`.

```text
src/
└── main/
    ├── resources/
    │   └── application.properties             (MySQL, Swagger, first administrator)
    └── java/
        └── application/
            │
            ├── NexusApplication.java
            │
            ├── adapters/
            │   ├── persistence/jpa/
            │   │   ├── JpaSupport.java            (shared helper for the adapters)
            │   │   ├── QueryRepository.java       (read-only listings used by GET endpoints)
            │   │   ├── adapters/                  15 output-port implementations (JPA / MySQL)
            │   │   └── converters/                catalog value objects <-> database columns
            │   ├── rest/
            │   │   ├── controllers/               User, Warehouse, Product, Inventory, Cart,
            │   │   │                              Order, Return, Refund, Report
            │   │   ├── dtos/                      ApiRequests, ApiResponses
            │   │   ├── exception/                 GlobalExceptionHandler (domain error -> HTTP code)
            │   │   └── config/                    OpenApiConfig (Swagger)
            │   └── security/                      SecurityConfig, login, AdminSeed
            │
            └── domain/
                ├── models/                        23 entities
                ├── valueobjects/                  13 value objects
                ├── enums/                         3 enumerations
                ├── exceptions/                    25 business exceptions
                ├── ports/
                │   ├── in/                        22 use case interfaces
                │   └── out/                       15 repository interfaces
                └── services/
                    ├── user/                      4 services
                    ├── warehouse/                 2 services
                    ├── catalog/                   3 services
                    ├── inventory/                 3 services + InventoryMovementRecorder
                    ├── order/                     5 services
                    └── refund/                    5 services
```

---

# Layer Responsibilities

## Application

`NexusApplication.java` is the Spring Boot entry point (`@SpringBootApplication`). It starts the application and wires the components together.

---

# Adapters

Adapters connect external technologies with the domain. They translate external requests into calls to the input ports and translate domain objects into technology-specific representations. The domain never communicates with an external system directly.

## Input Adapters (REST)

REST controllers receive HTTP requests from the five participants (Buyer, Seller, Logistics Operator, Administrator, Supervisor), invoke the corresponding input port and convert the result into a response DTO. Entities are never exposed directly. Controllers contain no business rules: the authenticated user is always taken from the login and passed to the use case as the actor, so nobody can act in the name of another user. The only checks done in controllers are visibility filters on GET endpoints and "is this order mine?" style checks.

`GlobalExceptionHandler` converts domain exceptions into HTTP codes: 404 (not found), 409 (duplicates, invalid status transitions, insufficient stock), 403 (not allowed), 400 (other validation errors).

Swagger UI is available at `/swagger-ui.html` and is used to exercise every endpoint (button **Authorize** = HTTP Basic login).

## Output Adapters (persistence)

- Each of the 15 output ports has a JPA adapter in `adapters/persistence/jpa/adapters`, storing the data in MySQL (`nexus_db`, tables created automatically by Hibernate).
- JPA annotations are placed directly on the domain models (a deliberate, pragmatic choice that avoids duplicating every entity). Catalog value objects (roles, statuses, currencies...) are stored by their code through converters.
- Controllers run inside a transaction, so changes made by the domain to loaded entities are written to MySQL when the request ends.
- `QueryRepository` holds read-only listing queries (users, products, orders, returns, refunds, stock movements...).

---

# Domain

The domain is the core of the application. It contains the business rules of the functional specification and does not depend on REST, JSON, SQL, or any persistence technology. It does use Lombok annotations to reduce boilerplate, and Spring's `@Service` stereotype on the service classes.

## Models

The 23 entities are organized in four hierarchies plus standalone entities:

```text
User (abstract)
├── Buyer
├── Seller
├── LogisticsOperator
├── Administrator
└── Supervisor

Warehouse (abstract)
├── MarketplaceWarehouse
└── SellerWarehouse

Product (abstract)
├── PhysicalProduct
└── DigitalProduct

Inventory, InventoryMovement
Cart, CartItem
Order, OrderItem
Invoice, Shipment
Return, ReturnItem, Refund
```

Entities are not anemic: state changes happen through methods that validate the rule and raise a domain exception when it is broken (for example `Inventory.reserve()`, `Order.markAsPaid()`, `Refund.approve()`). Every method is documented in the **Behavior** section of the Domain Model document.

## Value Objects

Controlled business concepts are immutable value objects inheriting from `DomainCatalog`, which provides `code`, `name`, and `description`:

`SystemRole`, `UserStatus`, `CommercialStatus`, `ProductType`, `ProductStatus`, `InventoryMovementType`, `OrderStatus`, `ReturnStatus`, `RefundStatus`, and `Currency` (which adds `isoCode` and `symbol`).

`ProductVariant` is a lightweight structural value object (variant type and value) that does not extend `DomainCatalog`, because its values are defined by each seller rather than by the domain. `ReturnLine` (product identifier and units) is another structural value object, used to describe what a buyer returns.

## Enums

Fixed technical values without business metadata: `WarehouseOwnership`, `ApprovalDecision`, and `NotificationChannel`.

## Exceptions

All business exceptions extend `DomainException`. They are grouped by the rule they protect:

| Area | Exceptions |
| --- | --- |
| General | `DomainException`, `EntityNotFoundException`, `InvalidStatusTransitionException`, `InvalidQuantityException`, `DuplicateEntityException` |
| Users and access (RG-01 to RG-03) | `InvalidUserException`, `InvalidRoleAssignmentException`, `UnauthorizedDomainAccessException`, `SelfRegistrationNotAllowedException`, `BuyerNotEligibleException`, `DuplicateUserException` |
| Warehouses and catalog | `InvalidWarehouseException`, `InvalidProductException` |
| Inventory | `InvalidInventoryException`, `NegativeInventoryException`, `InsufficientInventoryException`, `DamagedInventoryReservationException` |
| Cart and orders | `InvalidCartException`, `InvalidOrderException`, `OrderAlreadyFinalizedException`, `InvalidInvoiceException`, `InvalidShipmentException` |
| Returns and refunds | `InvalidReturnException`, `InvalidRefundException`, `SelfApprovalNotAllowedException` |

`UnauthorizedDomainAccessException` is shared across subdomains: it protects RG-03 for products, and also protects who may administer inventory and who may initiate a refund (see Business Rule Enforcement below).

## Ports

Ports are the contracts between the domain and the outside world. The domain owns every interface.

### Input Ports

There are 22 input ports, one per use case, in `domain/ports/in/`. Each one is implemented directly by a domain service.

| Subdomain | Input ports |
| --- | --- |
| User management | `RegisterBuyerUseCase`, `RegisterSellerUseCase`, `RegisterStaffUserUseCase`, `ChangeUserStatusUseCase` |
| Warehouses | `RegisterMarketplaceWarehouseUseCase`, `RegisterSellerWarehouseUseCase` |
| Catalog | `PublishPhysicalProductUseCase`, `PublishDigitalProductUseCase`, `ManageProductLifecycleUseCase` |
| Inventory | `RegisterInventoryInflowUseCase`, `AdjustInventoryUseCase`, `MarkInventoryAsDamagedUseCase` |
| Cart and orders | `ManageCartUseCase`, `CheckoutOrderUseCase`, `ProcessPaymentUseCase`, `DispatchOrderUseCase`, `ConfirmDeliveryUseCase` |
| Returns and refunds | `RequestReturnUseCase`, `ReviewReturnUseCase`, `InitiateRefundUseCase`, `DecideRefundUseCase`, `ProcessRefundUseCase` |

### Output Ports

There are 15 output ports in `domain/ports/out/`, all repository contracts:

`UserRepositoryPort`, `BuyerRepositoryPort`, `SellerRepositoryPort`, `AdministratorRepositoryPort`, `LogisticsOperatorRepositoryPort`, `WarehouseRepositoryPort`, `ProductRepositoryPort`, `InventoryRepositoryPort`, `CartRepositoryPort`, `OrderRepositoryPort`, `InvoiceRepositoryPort`, `ShipmentRepositoryPort`, `ReturnRepositoryPort`, `RefundRepositoryPort`, and `InventoryMovementRepositoryPort`.

Notification and payment-gateway ports are not defined yet; payment confirmation is currently modeled inside `ProcessPaymentService`.

## Services

A domain service exists only when an operation coordinates several aggregates. It retrieves the entities through output ports, invokes the behavior each entity already exposes, and persists the result. Logic that affects a single entity stays in the entity.

| Subdomain | Services |
| --- | --- |
| `services/user` | `RegisterBuyerService`, `RegisterSellerService`, `RegisterStaffUserService`, `ChangeUserStatusService` |
| `services/warehouse` | `RegisterMarketplaceWarehouseService`, `RegisterSellerWarehouseService` |
| `services/catalog` | `PublishPhysicalProductService`, `PublishDigitalProductService`, `ManageProductLifecycleService` |
| `services/inventory` | `RegisterInventoryInflowService`, `AdjustInventoryService`, `MarkInventoryAsDamagedService` (plus the helper `InventoryMovementRecorder`) |
| `services/order` | `ManageCartService`, `CheckoutOrderService`, `ProcessPaymentService`, `DispatchOrderService`, `ConfirmDeliveryService` |
| `services/refund` | `RequestReturnService`, `ReviewReturnService`, `InitiateRefundService`, `DecideRefundService`, `ProcessRefundService` |

The three inventory services (`RegisterInventoryInflowService`, `AdjustInventoryService`, `MarkInventoryAsDamagedService`) delegate the role and ownership check to `Product.requireInventoryManagedBy(user)` before touching any stock. `InitiateRefundService` delegates the ownership check to `Refund.initiate(...)`, which calls `Return.includesProductOf(seller)`.

`InventoryMovementRecorder` leaves a trace (`InventoryMovement`) of every stock change: initial stock, inflows, adjustments, damaged units, reservations at checkout, sale outflow at payment and returned units when a return is approved. The seller reports are built from this trace.

The input, output, and exception details of each service are documented as Javadoc on the service class.

---

# Infrastructure: security and configuration

- **Configuration:** `application.properties` defines the MySQL connection (the password comes from the `DB_PASSWORD` environment variable, never from a file), the Swagger path and the first administrator.
- **Security:** Spring Security with HTTP Basic login, stateless. Users and passwords (encrypted with BCrypt) are stored in MySQL. On the first start `AdminSeed` creates the administrator `ADMIN-001` (the only user that cannot self-register). Each endpoint is protected with `@PreAuthorize` by role, and the domain still enforces ownership and state rules (RG-01 to RG-03).

## Roles and endpoints

| Role | Allowed operations |
| --- | --- |
| Public | `POST /api/buyers` (buyer self-registration) |
| Administrator | Register sellers (`/api/sellers`) and staff (`/api/staff`); change user status; marketplace warehouses; review returns; decide and process refunds; read everything |
| Seller | Own warehouses and products (`/api/products/...`); inventory of own products; review returns and initiate refunds for own products; own orders (own lines only) and own reports (`/api/reports/sellers/{id}`) |
| Buyer | Own cart (`/api/cart`), checkout and payment (`/api/orders/...`), return requests (`/api/returns`); sees only own orders, returns and refunds |
| Logistics Operator | Dispatch (`/dispatch`) and delivery confirmation (`/delivery`); inventory operations; reads orders |
| Supervisor | Read-only consultation (users, products, inventory, orders, returns, refunds, reports) |

Seller reports (`/sales`, `/inventory`, `/movements`) are calculated from the database, so they are identical after restarting the application.

---

# Dependency Flow

Dependencies always point toward the domain.

```text
REST Controller
        │
        ▼
Input Port (UseCase interface)
        │
        ▼
Domain Service ──────► Domain Models (behavior and business rules)
        │
        ▼
Output Port (RepositoryPort interface)
        │
        ▼
Persistence Adapter (JPA)
        │
        ▼
Database
```

---

# Main Business Flow

The functional flow of the specification maps to services as follows:

```text
RegisterSellerService / RegisterMarketplaceWarehouseService   1. Onboarding
PublishPhysicalProductService / PublishDigitalProductService  2-4. Catalog, initial stock, publication
ManageCartService                                             5. Product selection
CheckoutOrderService                                          5. Order created, inventory reserved
ProcessPaymentService                                         6. Payment, invoice, sale confirmed
DispatchOrderService                                          7. Dispatch by the logistics operator
ConfirmDeliveryService                                        8. Order finalized (immutable)
RequestReturnService → ReviewReturnService
   → InitiateRefundService → DecideRefundService
   → ProcessRefundService                                     Post-sale
```

`CheckoutOrderService` is the most complex service: it coordinates the cart being consumed, the order being created, and the inventory reserved for every physical product.

---

# Business Rule Enforcement

| Rule from the specification | Where it is enforced |
| --- | --- |
| RG-01: every operation is executed by an active, authenticated user | `User.requireActive()`, called by entities and services |
| RG-02: each user has a single role | `User.register()` requires exactly one `SystemRole` |
| Identity document and email are unique across the platform, whatever the role (Section 11) | `RegisterBuyerService`, `RegisterSellerService` and `RegisterStaffUserService` check `UserRepositoryPort.existsByIdentifier()` and `existsByEmail()` and raise `DuplicateUserException`; `User.register()` normalizes the email (trimmed, lower case) and rejects malformed addresses |
| Identifiers of warehouses, products, orders, invoices, shipments, returns and refunds are unique | Each creating service checks `existsByIdentifier()` on its output port and raises `DuplicateEntityException` |
| An order has at most one return, and a return has at most one refund | `ReturnRepositoryPort.existsByOrder()` in `RequestReturnService`; `RefundRepositoryPort.existsByRelatedReturn()` in `InitiateRefundService` |
| A shipment always carries an identifier | `Shipment.prepare()` requires it; `LogisticsOperator.dispatch()` receives it |
| RG-03: no participant operates outside its role | Role checks in `Administrator`, `LogisticsOperator`, `Supervisor`; ownership check in `Seller.requireOwnershipOf()` |
| Sellers cannot self-register | `Seller.onboardedBy()` and `Administrator.registerSeller()` |
| Inventory is administered only by the owning Seller or a Logistics Operator (Responsibility Matrix, Section 12) | `Product.requireInventoryManagedBy(user)`, called by the three inventory services |
| Inventory can never be negative | `Inventory` methods raise `NegativeInventoryException` |
| Damaged or non-existent stock cannot be reserved | `Inventory.reserve()` |
| A cart can never hold more units of a physical product than the stock available | `Cart.addItem()` / `Cart.updateQuantity()` via `Product.requireAvailableFor()` |
| Every product has a price greater than zero, kept in cart and order lines and totaled in the invoice | `Product.register()`, `Cart`, `OrderItem`, `Invoice.issueFor()` |
| A finalized order cannot be modified | `Order.requireModifiable()` raises `OrderAlreadyFinalizedException` |
| Order lifecycle: Cart → Pending Payment → Paid → Dispatched → Delivered/Finalized | Transition checks in `Order`; `CheckoutOrderService` calls `Order.submitForPayment()` so a confirmed order waits in Pending Payment |
| A return states which products and how many units are returned, never more than were purchased | `Return.request()` with `ReturnLine` and `ReturnItem.define()` |
| A return is addressed to the Seller(s) who sold the returned products | `Return.involvedSellers()` |
| A refund can only be initiated by a Seller who sold a product that was actually returned | `Return.includesProductOf(seller)`, called from `Refund.initiate()` |
| A refund cannot exceed the value of the returned products sold by the seller who initiates it | `Refund.initiate()` compares with `Return.refundableAmountFor(seller)` |
| A return can only be reviewed by an Administrator or by a Seller of one of the returned products | `Return.requireReviewableBy(user)`, called by `ReviewReturnService` |
| An approved return puts the returned physical units back into stock | `ReviewReturnService` (`Inventory.registerReturn()`) and a `RETURN` movement |
| Every stock-affecting action leaves an `InventoryMovement` | `InventoryMovementRecorder`, used by the inventory, catalog, checkout, payment and return services |
| The seller who initiates a refund cannot approve it | `Refund.approve()` and `Refund.reject()` raise `SelfApprovalNotAllowedException` |

The mapping of each use case to the roles allowed to invoke it (Responsibility Matrix, Section 12 of the specification) is done in the input adapter layer with `@PreAuthorize` (see *Roles and endpoints*).

---

# Architectural Mapping to Functional Objectives

| Objective | Components |
| --- | --- |
| OBJ-01 Users | `User`, `SystemRole`, `UserStatus`, `RegisterStaffUserService`, `ChangeUserStatusService` |
| OBJ-02 Sellers | `Seller`, `Administrator`, `RegisterSellerService` |
| OBJ-03 Buyers | `Buyer`, `CommercialStatus`, `RegisterBuyerService` |
| OBJ-04 Warehouses | `Warehouse`, `MarketplaceWarehouse`, `SellerWarehouse`, `WarehouseOwnership`, warehouse services |
| OBJ-05 Catalog | `Product`, `PhysicalProduct`, `DigitalProduct`, `ProductStatus`, `ProductVariant`, catalog services |
| OBJ-06 Inventory | `Inventory`, `InventoryMovement`, `InventoryMovementType`, inventory services |
| OBJ-07 Cart | `Cart`, `CartItem`, `ManageCartService` |
| OBJ-08 Orders | `Order`, `OrderItem`, `OrderStatus`, `CheckoutOrderService`, `ConfirmDeliveryService` |
| OBJ-09 Invoicing | `Invoice`, `Currency`, `ProcessPaymentService` |
| OBJ-10 Logistics | `Shipment`, `DispatchOrderService` |
| OBJ-11 Returns and refunds | `Return`, `ReturnItem`, `ReturnLine`, `Refund`, `ReturnStatus`, `RefundStatus`, return and refund services |
| OBJ-12 Administrative reports | `ReportController` (seller reports) and `Supervisor` role; not a domain service |

---

# Architectural Constraints

1. Business logic belongs to the domain layer.
2. Controllers must not contain business rules.
3. DTOs never enter the domain.
4. Persistence entities are never exposed through the API.
5. Communication between the domain and technology happens only through ports.
6. Adapters implement ports and never define business rules.
7. Infrastructure and adapters depend on the domain, never the opposite.
8. Domain models expose behavior methods; state is not changed from outside the domain by direct assignment.
9. The domain must be testable without infrastructure components.
10. A finalized order is immutable.

---

# Benefits

- Business rules are enforced in one place, the domain, and are reusable from any delivery channel.
- Technology can be replaced (database, framework, delivery mechanism) without touching the business rules.
- The domain can be tested with in-memory implementations of the output ports.
- The design scales by adding new use cases, one input port and one service at a time.
