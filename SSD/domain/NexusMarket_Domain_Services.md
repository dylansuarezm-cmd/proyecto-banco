# Services

## Introduction

This document provides a conceptual overview of the services that compose the NexusMarket Information Management System.

The services described here define the main business capabilities exposed by the system. At this level, each service is described only in terms of its purpose and responsibility within the domain.

The detailed definition of each service — including inputs, outputs, business rules, validations, authorization requirements, domain interactions, exceptions, and persistence considerations — will be documented separately, following the same subdomain organization used here.

Unlike the entities documented in *Domain Model — NexusMarket*, a Domain Service does not hold business state of its own. It exists only to orchestrate an operation that spans more than one aggregate — retrieving the entities involved through repository ports, invoking the behavior each entity already exposes, and persisting the result. Whenever an operation affects a single entity, that behavior lives in the entity itself (see the **Behavior** sections of the Domain Model) rather than in a service.

Consultation of consolidated administrative information (OBJ-12) is intentionally not modeled as a Domain Service: it does not enforce or protect any business rule, so it belongs to the Reporting adapter described in *NexusMarket Software Architecture*, not to the domain layer.

The service documentation is therefore divided conceptually into the following subdomains:

- **User Management**
- **Seller and Warehouse Management**
- **Catalog Management**
- **Inventory Management**
- **Cart and Order Management**
- **Return and Refund Management**

---

# User Management Services

## Register Buyer

Creates a new `Buyer`, establishes its commercial profile and delivery address, and opens the `Cart` associated with it.

## Register Seller

Creates a new `Seller` onboarded by an `Administrator`. A seller can never register itself (RG-02, Dominio 3 business rule).

## Register Staff User

Creates a system user representing an internal marketplace participant — `LogisticsOperator`, `Administrator`, or `Supervisor`.

## Change User Status

Changes the operational status of any user — `Buyer`, `Seller`, or staff — such as activating, blocking, or deactivating it.

---

# Seller and Warehouse Management Services

## Register Marketplace Warehouse

Creates a new warehouse operated directly by NexusMarket, registered by an `Administrator`.

## Register Seller Warehouse

Creates a new warehouse owned and operated by a `Seller`.

---

# Catalog Management Services

## Publish Physical Product

Creates a new physical product for a seller's catalog and opens its initial `Inventory` record in one of the seller's warehouses.

## Publish Digital Product

Creates a new digital product for a seller's catalog, together with the asset delivered immediately after payment.

## Manage Product Lifecycle

Changes the publication status of an existing product — suspending or discontinuing it — enforcing that a discontinued product can never be published again.

---

# Inventory Management Services

## Register Inventory Inflow

Registers newly arrived stock for a product in a specific warehouse and records the corresponding `InventoryMovement`.

## Adjust Inventory

Manually corrects the available quantity of an inventory record, guaranteeing it never becomes negative, and records the corresponding `InventoryMovement`.

## Mark Inventory As Damaged

Moves a quantity of stock into the damaged pool, excluding it from any future reservation, and records the corresponding `InventoryMovement`.

---

# Cart and Order Management Services

## Manage Cart

Adds, updates, or removes products in a buyer's cart, validating that every selected product is currently published.

## Checkout Order

Converts a buyer's cart into a formal `Order` and reserves the corresponding `Inventory` for every physical product included, coordinating the `Cart`, `Order`, and `Inventory` aggregates as a single business transaction.

## Process Payment

Confirms payment for a pending order, issues its `Invoice`, and confirms the sale outflow of the inventory previously reserved for it.

## Dispatch Order

Creates the `Shipment` for a paid order and registers its dispatch from the origin warehouse by a `LogisticsOperator`.

## Confirm Delivery

Confirms the delivery of a dispatched order, closing both the `Order` and its associated `Shipment`.

---

# Return and Refund Management Services

## Request Return

Creates a `Return` request for a finalized order, validating that the requesting buyer is the one who placed it.

## Review Return

Approves or rejects a pending return request.

## Initiate Refund

Registers a `Refund` for an approved return. Only the `Seller` responsible for the returned product may initiate it, and the return must already be approved.

## Decide Refund

Approves or rejects a pending refund. Only an `Administrator` may decide, and the administrator can never be the same participant who initiated the refund (separation of duties).

## Process Refund

Completes an approved refund once the reimbursement has actually been transferred to the buyer.
