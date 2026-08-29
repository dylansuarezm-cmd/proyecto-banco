# Domain Value Objects — NexusMarket

## Introduction

Value Objects represent immutable concepts within the NexusMarket domain.

Unlike Entities, Value Objects do not have their own identity. They are defined entirely by their values and are used to encapsulate controlled business concepts, improve domain expressiveness, and prevent the use of primitive values or scattered string literals throughout the application.

The NexusMarket domain uses Value Objects for business catalogs such as roles, statuses, product types, movement types, and currencies.

All business catalogs inherit from `DomainCatalog`.

---

# Value Object Hierarchy

```text
DomainCatalog (Abstract)
├── SystemRole
├── UserStatus
├── CommercialStatus
├── ProductType
├── ProductStatus
├── InventoryMovementType
├── OrderStatus
├── ReturnStatus
├── RefundStatus
└── Currency
```

---

# DomainCatalog (Abstract)

## Description

Represents a generic business catalog used throughout the NexusMarket domain.

`DomainCatalog` provides a consistent structure for controlled business values that require a code, human-readable name, and business description.

This class cannot be instantiated directly.

## Attributes

| Attribute   | Type   | Description                                           |
| ----------- | ------ | ----------------------------------------------------- |
| code        | String | Unique business identifier of the catalog value.      |
| name        | String | Human-readable name displayed within the application. |
| description | String | Business definition of the catalog value.             |

## Characteristics

* Immutable.
* Equality is determined by value rather than object identity.
* Catalog values are controlled by the domain.
* Catalog values must not be represented by arbitrary strings throughout the application.
* Each catalog value must have a unique `code`.

---

# SystemRole

## Description

Represents the responsibilities and permissions assigned to a user within NexusMarket.

The role is a characteristic of `User` because it represents what the user means within the system. Each user has exactly one role (RG-02), and no participant may operate outside the scope of their role (RG-03).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code                | Name                | Description                                                                 |
| ------------------- | ------------------- | ---------------------------------------------------------------------------- |
| BUYER               | Buyer               | User who purchases products published on the marketplace.                    |
| SELLER               | Seller               | User responsible for registering and administering their own products.       |
| LOGISTICS_OPERATOR  | Logistics Operator  | User responsible for the physical operation of warehouses and dispatches.    |
| ADMINISTRATOR       | Administrator       | User responsible for administering sellers, warehouses, and refund approvals.|
| SUPERVISOR          | Supervisor          | Read-only user responsible for operational consultation and follow-up.       |

---

# UserStatus

## Description

Represents the current operational status of a user's access to NexusMarket.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code     | Name     | Description                                       |
| -------- | -------- | -------------------------------------------------- |
| ACTIVE   | Active   | User can access the system normally.                |
| BLOCKED  | Blocked  | User access has been suspended.                     |
| INACTIVE | Inactive | User exists but cannot currently perform operations.|

---

# CommercialStatus

## Description

Represents the condition of a `Buyer` that determines their ability to make purchases on the marketplace (Dominio 2).

`CommercialStatus` is independent from `UserStatus`: a buyer may have valid system access while temporarily restricted from placing new orders (e.g., due to a pending dispute).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code      | Name      | Description                                                |
| --------- | --------- | ------------------------------------------------------------ |
| ENABLED   | Enabled   | Buyer can place orders normally.                              |
| RESTRICTED| Restricted| Buyer's purchasing ability is temporarily limited.            |
| SUSPENDED | Suspended | Buyer is not allowed to place new orders.                     |

---

# ProductType

## Description

Represents the classification of a product as physical or digital, determining whether it requires inventory and dispatch (Dominio 5).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code     | Name     | Description                                                        |
| -------- | -------- | --------------------------------------------------------------------- |
| PHYSICAL | Physical | Tangible good requiring inventory tracking and physical dispatch.      |
| DIGITAL  | Digital  | Intangible good delivered immediately after payment confirmation.      |

---

# ProductStatus

## Description

Represents the publication state of a product within the catalog (Dominio 5).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code           | Name           | Description                                        |
| -------------- | -------------- | ----------------------------------------------------- |
| PUBLISHED      | Published      | Product is visible in the public catalog.               |
| SUSPENDED      | Suspended      | Product is temporarily hidden from the catalog.          |
| DISCONTINUED   | Discontinued   | Product has been permanently removed from sale.          |

## Lifecycle

```text
PUBLISHED
    │
    ├──────────────> SUSPENDED
    │                     │
    │                     ▼
    │                PUBLISHED
    │
    ▼
DISCONTINUED
```

---

# InventoryMovementType

## Description

Represents the type of stock-affecting action executed over an `Inventory` record (Dominio 6).

Movements are independent from the current stock level: a movement represents an event that occurred, while `Inventory.availableQty` represents the current state.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code           | Name           | Description                                             |
| -------------- | -------------- | ---------------------------------------------------------- |
| INFLOW         | Inflow         | Addition of new stock into a warehouse.                      |
| RESERVATION    | Reservation    | Temporary hold of stock for an active cart or pending order. |
| SALE_OUTFLOW   | Sale Outflow   | Reduction of stock resulting from a confirmed sale.           |
| ADJUSTMENT     | Adjustment     | Manual correction of stock quantity.                          |
| RETURN         | Return         | Stock reincorporated as a result of an approved return.        |

---

# OrderStatus

## Description

Represents the current state of the order lifecycle, the central business process of NexusMarket (Dominio 7).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code                   | Name                    | Description                                          |
| ---------------------- | ----------------------- | ------------------------------------------------------- |
| CART                   | Cart                    | Provisional product selection, not yet a formal order.   |
| PENDING_PAYMENT        | Pending Payment         | Awaiting financial confirmation.                          |
| PAID                   | Paid                    | Payment confirmed; preparation process begins.            |
| DISPATCHED             | Dispatched              | Order has physically left the warehouse.                  |
| DELIVERED_FINALIZED    | Delivered / Finalized   | Delivery confirmed; the order is closed.                   |

## Lifecycle

```text
CART
   │
   ▼
PENDING_PAYMENT
   │
   ├──────────────> (payment failed / abandoned)
   │
   ▼
PAID
   │
   ▼
DISPATCHED
   │
   ▼
DELIVERED_FINALIZED
```

## Business Rule

```text
An Order in status DELIVERED_FINALIZED
cannot be modified under any circumstance.
```

---

# ReturnStatus

## Description

Represents the current state of a return request submitted by a buyer (OBJ-11).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code       | Name       | Description                                     |
| ---------- | ---------- | -------------------------------------------------- |
| REQUESTED  | Requested  | Return has been submitted by the buyer.               |
| APPROVED   | Approved   | Return has been accepted for processing.               |
| REJECTED   | Rejected   | Return request has been denied.                         |
| COMPLETED  | Completed  | Returned product has been processed and closed.          |

## Lifecycle

```text
REQUESTED
    │
    ├──────────────> REJECTED
    │
    ▼
APPROVED
    │
    ▼
COMPLETED
```

---

# RefundStatus

## Description

Represents the current state of a monetary reimbursement associated with a `Return` (OBJ-11).

Reflects the shared responsibility between Seller and Administrator defined in the Responsibility Matrix (Section 12 of the specification): the Seller initiates the refund, and the Administrator authorizes it.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code       | Name       | Description                                                     |
| ---------- | ---------- | ------------------------------------------------------------------ |
| PENDING    | Pending    | Refund has been initiated by the Seller and awaits Administrator decision. |
| APPROVED   | Approved   | Refund has been approved by an Administrator.                        |
| REJECTED   | Rejected   | Refund request has been denied by an Administrator.                   |
| PROCESSED  | Processed  | Reimbursement has been completed.                                      |

## Lifecycle

```text
PENDING
   │
   ├──────────────> REJECTED
   │
   ▼
APPROVED
   │
   ▼
PROCESSED
```

---

# Currency

## Description

Represents the monetary currency used for invoicing and refunds within NexusMarket.

Currency is a business Value Object because its meaning is determined by its controlled values rather than by an independent identity.

## Inherits From

`DomainCatalog`

## Additional Attributes

| Attribute | Type   | Description                       |
| --------- | ------ | ---------------------------------- |
| isoCode   | String | ISO 4217 currency code.             |
| symbol    | String | Currency symbol used for display.   |

## Allowed Values

| ISO Code | Name                 | Symbol |
| -------- | -------------------- | ------ |
| COP      | Colombian Peso       | $      |
| USD      | United States Dollar | $      |
| EUR      | Euro                 | €      |

---

# Primitive Enumerations

The following concepts are represented as primitive enumerations because they contain fixed technical values and do not require business catalog metadata such as `code`, `name`, or `description`.

---

# WarehouseOwnership

## Description

Represents whether a warehouse belongs to the Marketplace or to a Seller (Dominio 4).

## Values

```text
MARKETPLACE
SELLER
```

---

# ApprovalDecision

## Description

Represents the result of an approval process, such as a refund approval performed by an Administrator.

## Values

```text
APPROVED
REJECTED
```

---

# NotificationChannel

## Description

Represents the communication channel used by the system to notify users about order, shipment, or refund events.

## Values

```text
EMAIL
SMS
PUSH_NOTIFICATION
```

---

# ProductVariant

## Description

Represents a single differentiating characteristic of a product, such as color, size, or model (Dominio 5). Unlike `DomainCatalog` entries, a variant is a lightweight structural Value Object rather than a controlled business catalog, since its possible values are defined per product by the Seller rather than by the domain itself.

## Attributes

| Attribute    | Type   | Description                                  |
| ------------ | ------ | ----------------------------------------------- |
| variantType  | String | Category of the variation (e.g., "Color", "Size", "Model"). |
| value        | String | Specific value of the variation (e.g., "Red", "XL").         |

---

# Value Object Design Rules

## Immutability

All Value Objects must be immutable after creation.

Their values cannot be modified after the object has been instantiated.

## Equality

Value Objects are compared according to their values rather than object identity.

Two instances containing the same business values represent the same Value Object.

## Controlled Values

Business catalogs must use controlled values defined by the domain.

The application must avoid replacing these concepts with arbitrary strings such as:

```text
"PUBLISHED"
"BLOCKED"
"APPROVED"
```

throughout the codebase.

Instead, the corresponding Value Object must be used:

```text
UserStatus
CommercialStatus
ProductStatus
OrderStatus
ReturnStatus
RefundStatus
```

## Business Versus Technical Enumerations

A business concept should be modeled as a `DomainCatalog` Value Object when it requires:

* a business code;
* a display name;
* a business description;
* controlled domain evolution.

A simple enumeration should be used when the concept represents a fixed technical value without additional business metadata, or a structural characteristic defined by the seller rather than by the domain (such as `ProductVariant`).

## Relationship With Entities

Entities reference Value Objects rather than primitive strings whenever the referenced value represents a controlled business concept.

Examples:

```text
User.role : SystemRole

User.status : UserStatus

Buyer.commercialStatus : CommercialStatus

Product.status : ProductStatus

InventoryMovement.movementType : InventoryMovementType

Order.orderStatus : OrderStatus

Return.returnStatus : ReturnStatus

Refund.refundStatus : RefundStatus

Invoice.currency : Currency
```

This approach improves type safety, domain expressiveness, maintainability, and consistency with Domain-Driven Design principles.
