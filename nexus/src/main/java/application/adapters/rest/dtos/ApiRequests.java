package application.adapters.rest.dtos;

import java.math.BigDecimal;
import java.util.List;

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

    /** Differentiating characteristic of a product, such as color: RED, size: M, model: Pro. */
    public record VariantRequest(String variantType, String value) {
    }

    public record PublishPhysicalProduct(String identifier, String name, String description, BigDecimal price,
                                         String warehouseIdentifier, int initialQuantity,
                                         List<VariantRequest> variants) {
    }

    public record PublishDigitalProduct(String identifier, String name, String description, BigDecimal price,
                                        String deliveryAsset, List<VariantRequest> variants) {
    }

    public record ChangeLifecycle(String action) {
    }

    public record InventoryMovement(String productIdentifier, String warehouseIdentifier, int quantity) {
    }

    /** For a seller's warehouse: the Administrator must say which seller owns it; a seller leaves it empty. */
    public record RegisterSellerWarehouse(String sellerIdentifier, String identifier, String name, String location) {
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
