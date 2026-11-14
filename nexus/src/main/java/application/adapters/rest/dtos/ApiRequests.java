package application.adapters.rest.dtos;

import java.math.BigDecimal;

/** Request bodies of the REST API. */
public final class ApiRequests {

    private ApiRequests() {
    }

    public record RegisterBuyer(String identifier, String fullName, String email, String password,
                                String primaryAddress) {
    }

    public record RegisterSeller(String identifier, String fullName, String email, String password) {
    }

    public record RegisterStaff(String identifier, String fullName, String email, String password, String role) {
    }

    public record ChangeStatus(String action) {
    }

    public record RegisterWarehouse(String identifier, String name, String location) {
    }

    public record PublishPhysicalProduct(String identifier, String name, String description, BigDecimal price,
                                         String warehouseIdentifier, int initialQuantity) {
    }

    public record PublishDigitalProduct(String identifier, String name, String description, BigDecimal price,
                                        String deliveryAsset) {
    }

    public record ChangeLifecycle(String action) {
    }

    public record InventoryMovement(String productIdentifier, String warehouseIdentifier, int quantity) {
    }

    public record CartItemRequest(String productIdentifier, int quantity) {
    }

    public record Checkout(String orderIdentifier) {
    }

    public record Payment(String invoiceIdentifier, String currency) {
    }

    public record Dispatch(String warehouseIdentifier, String shipmentIdentifier) {
    }

    public record ReturnLineRequest(String productIdentifier, int quantity) {
    }

    public record RequestReturn(String returnIdentifier, String orderIdentifier, String reason,
                                java.util.List<ReturnLineRequest> lines) {
    }

    /** decision: APPROVE or REJECT. */
    public record Decision(String decision) {
    }

    public record InitiateRefund(String refundIdentifier, String returnIdentifier, BigDecimal amount) {
    }
}
