package application.adapters.rest.dtos;

import application.domain.models.Cart;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Refund;
import application.domain.models.Return;
import application.domain.models.Seller;
import application.domain.models.Shipment;
import application.domain.models.MarketplaceWarehouse;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.SellerWarehouse;
import application.domain.models.User;
import application.domain.models.Warehouse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Response bodies of the REST API. Entities are never exposed directly. */
public final class ApiResponses {

    private ApiResponses() {
    }

    public record UserResponse(String identifier, String fullName, String email, String role, String status) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getIdentifier(), user.getFullName(), user.getEmail(),
                    user.getRole().getCode(), user.getStatus().getCode());
        }
    }

    public record WarehouseResponse(String identifier, String name, String location, String type,
                                    String ownerIdentifier) {
        public static WarehouseResponse from(Warehouse warehouse) {
            if (warehouse instanceof SellerWarehouse sellerWarehouse) {
                return new WarehouseResponse(warehouse.getIdentifier(), warehouse.getName(),
                        warehouse.getLocation(), "SELLER", sellerWarehouse.getOwner().getIdentifier());
            }
            String registeredBy = warehouse instanceof MarketplaceWarehouse marketplace
                    && marketplace.getRegisteredBy() != null ? marketplace.getRegisteredBy().getIdentifier() : null;
            return new WarehouseResponse(warehouse.getIdentifier(), warehouse.getName(),
                    warehouse.getLocation(), "MARKETPLACE", registeredBy);
        }
    }

    public record ProductResponse(String identifier, String name, String description, BigDecimal price,
                                  String type, String status, String sellerIdentifier,
                                  Integer availableQuantity) {
        public static ProductResponse from(Product product) {
            Integer available = product instanceof PhysicalProduct physical
                    ? physical.totalAvailableQuantity() : null;
            return new ProductResponse(product.getIdentifier(), product.getName(), product.getDescription(),
                    product.getPrice(), product.type().getCode(), product.getStatus().getCode(),
                    product.getSeller() == null ? null : product.getSeller().getIdentifier(), available);
        }
    }

    public record InventoryResponse(String identifier, String productIdentifier, String warehouseIdentifier,
                                    int availableQuantity, int reservedQuantity, int damagedQuantity) {
        public static InventoryResponse from(Inventory inventory) {
            return new InventoryResponse(inventory.getIdentifier(), inventory.getProduct().getIdentifier(),
                    inventory.getWarehouse() == null ? null : inventory.getWarehouse().getIdentifier(),
                    value(inventory.getAvailableQty()), value(inventory.getReservedQty()),
                    value(inventory.getDamagedQty()));
        }

        private static int value(Integer number) {
            return number == null ? 0 : number;
        }
    }

    public record ProductLine(String productIdentifier, String productName, String type, String sellerIdentifier,
                              int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        static ProductLine of(Product product, Integer quantity, BigDecimal unitPrice) {
            int units = quantity == null ? 0 : quantity;
            BigDecimal price = unitPrice == null ? BigDecimal.ZERO : unitPrice;
            return new ProductLine(product.getIdentifier(), product.getName(), product.type().getCode(),
                    product.getSeller() == null ? null : product.getSeller().getIdentifier(), units, price,
                    price.multiply(BigDecimal.valueOf(units)));
        }
    }

    public record CartResponse(String buyerIdentifier, List<ProductLine> items, BigDecimal estimatedTotal) {
        public static CartResponse from(Cart cart) {
            List<ProductLine> lines = cart.getItems().stream()
                    .map(i -> ProductLine.of(i.getProduct(), i.getQuantity(), i.getUnitPrice())).toList();
            return new CartResponse(cart.getOwner().getIdentifier(), lines, cart.estimatedTotal());
        }
    }

    public record InvoiceResponse(String identifier, BigDecimal totalAmount, String currency,
                                  LocalDateTime issueDate) {
    }

    public record ShipmentResponse(String identifier, String dispatchedBy, String originWarehouse,
                                   LocalDateTime dispatchDate, LocalDateTime deliveryDate) {
    }

    /**
     * When a seller consults an order, only the lines of that seller are shown (and the total is the total of
     * those lines); the rest of the purchase belongs to other sellers.
     */
    public record OrderResponse(String identifier, String buyerIdentifier, String status,
                                LocalDateTime creationDate, List<ProductLine> items, BigDecimal total,
                                InvoiceResponse invoice, ShipmentResponse shipment) {
        public static OrderResponse from(Order order, Seller onlyFor) {
            List<ProductLine> lines = order.getItems().stream()
                    .filter(i -> onlyFor == null || onlyFor.owns(i.getProduct()))
                    .map(i -> ProductLine.of(i.getProduct(), i.getQuantity(), i.getUnitPrice())).toList();
            BigDecimal total = lines.stream().map(ProductLine::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            Invoice invoice = order.getInvoice();
            Shipment shipment = order.getShipment();
            return new OrderResponse(order.getIdentifier(), order.getBuyer().getIdentifier(),
                    order.getOrderStatus().getCode(), order.getCreationDate(), lines, total,
                    invoice == null || onlyFor != null ? null : new InvoiceResponse(invoice.getIdentifier(),
                            invoice.getTotalAmount(), invoice.getCurrency().getCode(), invoice.getIssueDate()),
                    shipment == null ? null : new ShipmentResponse(shipment.getIdentifier(),
                            shipment.getDispatchedBy().getIdentifier(),
                            shipment.getOriginWarehouse().getIdentifier(), shipment.getDispatchDate(),
                            shipment.getDeliveryDate()));
        }
    }

    public record ReturnResponse(String identifier, String orderIdentifier, String buyerIdentifier, String reason,
                                 String status, LocalDateTime requestDate, int totalUnits,
                                 BigDecimal refundableAmount, List<ProductLine> items,
                                 List<String> involvedSellers) {
        public static ReturnResponse from(Return returnRequest) {
            List<ProductLine> lines = returnRequest.getItems().stream()
                    .map(i -> ProductLine.of(i.getProduct(), i.getQuantity(), i.getUnitPrice())).toList();
            return new ReturnResponse(returnRequest.getIdentifier(), returnRequest.getOrder().getIdentifier(),
                    returnRequest.getRequestedBy().getIdentifier(), returnRequest.getReason(),
                    returnRequest.getReturnStatus().getCode(), returnRequest.getRequestDate(),
                    returnRequest.totalUnits(), returnRequest.refundableAmount(), lines,
                    returnRequest.involvedSellers().stream().map(Seller::getIdentifier).toList());
        }
    }

    public record RefundResponse(String identifier, String returnIdentifier, String orderIdentifier,
                                 String sellerIdentifier, String approvedBy, BigDecimal amount, String status,
                                 LocalDateTime processedDate) {
        public static RefundResponse from(Refund refund) {
            return new RefundResponse(refund.getIdentifier(), refund.getRelatedReturn().getIdentifier(),
                    refund.getRelatedReturn().getOrder().getIdentifier(), refund.getInitiatedBy().getIdentifier(),
                    refund.getApprovedBy() == null ? null : refund.getApprovedBy().getIdentifier(),
                    refund.getAmount(), refund.getRefundStatus().getCode(), refund.getProcessedDate());
        }
    }

    public record MovementResponse(String movementId, String type, int quantity, String reference,
                                   String performedBy, String productIdentifier, String warehouseIdentifier,
                                   Integer availableAfter, LocalDateTime executionDate) {
        public static MovementResponse from(InventoryMovement movement) {
            Inventory inventory = movement.getAffectedInventory();
            return new MovementResponse(movement.getMovementId(), movement.getMovementType().getCode(),
                    movement.getQuantity(), movement.getReference(), movement.getPerformedBy().getIdentifier(),
                    inventory.getProduct().getIdentifier(),
                    inventory.getWarehouse() == null ? null : inventory.getWarehouse().getIdentifier(),
                    movement.getAvailableAfter(), movement.getExecutionDate());
        }
    }

    // ---- seller reports ----

    public record SaleLine(String orderIdentifier, String orderStatus, String buyerIdentifier,
                           String productIdentifier, String productName, String type, int quantity,
                           BigDecimal unitPrice, BigDecimal subtotal) {
    }

    /** Sales are the lines of paid orders; refunds already processed are subtracted to get the net sales. */
    public record SalesReport(String sellerIdentifier, List<SaleLine> sales, int unitsSold, BigDecimal grossSales,
                              BigDecimal refundedAmount, BigDecimal netSales, List<RefundResponse> refunds) {
    }

    public record StockLine(String productIdentifier, String productName, String warehouseIdentifier,
                            int availableQuantity, int reservedQuantity, int damagedQuantity) {
    }

    public record InventoryReport(String sellerIdentifier, List<StockLine> stock, int totalAvailable,
                                  int totalReserved, int totalDamaged) {
    }

    /** Stock changes caused by purchases (RESERVATION, SALE_OUTFLOW), returns (RETURN) and manual operations. */
    public record MovementsReport(String sellerIdentifier, List<MovementResponse> movements,
                                  Map<String, Integer> unitsByType) {
    }

    public record SellerReport(String sellerIdentifier, SalesReport sales, InventoryReport inventory,
                               MovementsReport movements) {
    }
}
