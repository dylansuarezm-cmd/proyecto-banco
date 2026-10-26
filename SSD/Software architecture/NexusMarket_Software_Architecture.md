# Software Architecture — NexusMarket

## Overview

NexusMarket follows a **Hexagonal Architecture (Ports and Adapters)** combined with **Domain-Driven Design (DDD)** principles.

The goal of this architecture is to keep the marketplace business domain — users, sellers, buyers, warehouses, catalog, inventory, cart, orders, invoicing, logistics, returns and refunds — isolated from frameworks, databases, and communication protocols. Business rules live in the domain and never depend on external technology.

This document describes the architecture **as it is implemented in the repository**, and clearly separates what already exists from what is planned for later stages.

## Implementation Status

| Component | Status |
| --- | --- |
| Domain models (22 entities with behavior) | Implemented |
| Domain value objects (12) and enums (3) | Implemented |
| Domain exceptions (23) | Implemented |
| Input ports (22 use cases) | Implemented |
| Output ports (14 repository contracts) | Implemented |
| Domain services (22) | Implemented |
| Input adapters (REST) | Planned |
| Output adapters (persistence, reporting) | Planned |
| Infrastructure (configuration, security) | Planned |

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
├── Adapters          (planned)
│
├── Domain            (implemented)
│
└── Infrastructure    (planned)
```

---

# Package Structure

The root package is `application`. Entries marked *(planned)* do not exist yet.

```text
src/
└── main/
    └── java/
        └── application/
            │
            ├── NexusApplication.java
            │
            ├── adapters/                          (planned)
            │   ├── in/
            │   │   └── rest/
            │   │       ├── controllers/
            │   │       ├── requests/
            │   │       ├── responses/
            │   │       └── mappers/
            │   └── out/
            │       └── persistence/
            │           ├── relational/            (transactional data)
            │           └── reporting/             (consolidated read models)
            │
            ├── domain/
            │   ├── models/                        22 entities
            │   ├── valueobjects/                  12 value objects
            │   ├── enums/                         3 enumerations
            │   ├── exceptions/                    25 business exceptions
            │   ├── ports/
            │   │   ├── in/                        22 use case interfaces
            │   │   └── out/                       14 repository interfaces
            │   └── services/
            │       ├── user/                      4 services
            │       ├── warehouse/                 2 services
            │       ├── catalog/                   3 services
            │       ├── inventory/                 3 services
            │       ├── order/                     5 services
            │       └── refund/                    5 services
            │
            └── infrastructure/                    (planned)
                ├── config/
                ├── database/
                └── security/
```

---

# Layer Responsibilities

## Application

`NexusApplication.java` is the Spring Boot entry point (`@SpringBootApplication`). It starts the application and wires the components together.

---

# Adapters (planned)

Adapters connect external technologies with the domain. They translate external requests into calls to the input ports and translate domain objects into technology-specific representations. The domain never communicates with an external system directly.

## Input Adapters

REST controllers will receive HTTP requests from the five participants (Buyer, Seller, Logistics Operator, Administrator, Supervisor), convert request DTOs into domain values, invoke the corresponding input port, and convert the result into a response DTO. Controllers must never contain business rules.

## Output Adapters

- **Relational persistence adapter:** will implement the output ports for the transactional data (users, catalog, inventory, carts, orders, invoices, shipments, returns, refunds).
- **Reporting adapter:** will support the consolidated administrative consultation (OBJ-12), which is intentionally not a domain service because it does not protect any business rule.

Each adapter will map between domain models and its own persistence representation.

---

# Domain

The domain is the core of the application. It contains the business rules of the functional specification and does not depend on REST, JSON, SQL, or any persistence technology. It does use Lombok annotations to reduce boilerplate, and Spring's `@Service` stereotype on the service classes.

## Models

The 22 entities are organized in four hierarchies plus standalone entities:

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
Return, Refund
```

Entities are not anemic: state changes happen through methods that validate the rule and raise a domain exception when it is broken (for example `Inventory.reserve()`, `Order.markAsPaid()`, `Refund.approve()`). Every method is documented in the **Behavior** section of the Domain Model document.

## Value Objects

Controlled business concepts are immutable value objects inheriting from `DomainCatalog`, which provides `code`, `name`, and `description`:

`SystemRole`, `UserStatus`, `CommercialStatus`, `ProductType`, `ProductStatus`, `InventoryMovementType`, `OrderStatus`, `ReturnStatus`, `RefundStatus`, and `Currency` (which adds `isoCode` and `symbol`).

`ProductVariant` is a lightweight structural value object (variant type and value) that does not extend `DomainCatalog`, because its values are defined by each seller rather than by the domain.

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

There are 14 output ports in `domain/ports/out/`, all repository contracts:

`UserRepositoryPort`, `BuyerRepositoryPort`, `SellerRepositoryPort`, `AdministratorRepositoryPort`, `LogisticsOperatorRepositoryPort`, `WarehouseRepositoryPort`, `ProductRepositoryPort`, `InventoryRepositoryPort`, `CartRepositoryPort`, `OrderRepositoryPort`, `InvoiceRepositoryPort`, `ShipmentRepositoryPort`, `ReturnRepositoryPort`, and `RefundRepositoryPort`.

Notification and payment-gateway ports are not defined yet; payment confirmation is currently modeled inside `ProcessPaymentService`.

## Services

A domain service exists only when an operation coordinates several aggregates. It retrieves the entities through output ports, invokes the behavior each entity already exposes, and persists the result. Logic that affects a single entity stays in the entity.

| Subdomain | Services |
| --- | --- |
| `services/user` | `RegisterBuyerService`, `RegisterSellerService`, `RegisterStaffUserService`, `ChangeUserStatusService` |
| `services/warehouse` | `RegisterMarketplaceWarehouseService`, `RegisterSellerWarehouseService` |
| `services/catalog` | `PublishPhysicalProductService`, `PublishDigitalProductService`, `ManageProductLifecycleService` |
| `services/inventory` | `RegisterInventoryInflowService`, `AdjustInventoryService`, `MarkInventoryAsDamagedService` |
| `services/order` | `ManageCartService`, `CheckoutOrderService`, `ProcessPaymentService`, `DispatchOrderService`, `ConfirmDeliveryService` |
| `services/refund` | `RequestReturnService`, `ReviewReturnService`, `InitiateRefundService`, `DecideRefundService`, `ProcessRefundService` |

The three inventory services (`RegisterInventoryInflowService`, `AdjustInventoryService`, `MarkInventoryAsDamagedService`) delegate the role and ownership check to `Product.requireInventoryManagedBy(user)` before touching any stock. `InitiateRefundService` delegates the ownership check to `Refund.initiate(...)`, which calls `Order.includesProductOf(seller)`.

The input, output, and exception details of each service are documented as Javadoc on the service class.

---

# Infrastructure (planned)

Infrastructure will hold technical configuration and no business logic:

- **Config:** application and serialization configuration.
- **Database:** connection and initialization settings.
- **Security:** authentication and authorization plumbing. Technical authentication is outside the functional scope of the specification; this layer only identifies the authenticated user so the domain can enforce RG-01 to RG-03.

---

# Dependency Flow

Dependencies always point toward the domain.

```text
REST Controller (planned)
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
Persistence Adapter (planned)
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
| A finalized order cannot be modified | `Order.requireModifiable()` raises `OrderAlreadyFinalizedException` |
| Order lifecycle: Cart → Pending Payment → Paid → Dispatched → Delivered/Finalized | Transition checks in `Order`; `CheckoutOrderService` calls `Order.submitForPayment()` so a confirmed order waits in Pending Payment |
| A refund can only be initiated by a Seller who sold a product of the returned order | `Order.includesProductOf(seller)`, called from `Refund.initiate()` |
| The seller who initiates a refund cannot approve it | `Refund.approve()` and `Refund.reject()` raise `SelfApprovalNotAllowedException` |

Mapping each use case to the roles allowed to invoke it (Responsibility Matrix, Section 12 of the specification) is otherwise the responsibility of the input adapter layer, which is planned.

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
| OBJ-11 Returns and refunds | `Return`, `Refund`, `ReturnStatus`, `RefundStatus`, return and refund services |
| OBJ-12 Administrative reports | Reporting adapter and `Supervisor` role (planned; not a domain service) |

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
