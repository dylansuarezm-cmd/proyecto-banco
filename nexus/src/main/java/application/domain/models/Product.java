package application.domain.models;

import application.domain.exceptions.InvalidProductException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.ProductType;
import application.domain.valueobjects.ProductVariant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Product {

    private String identifier;
    private String name;
    private String description;
    private Seller seller;
    private List<ProductVariant> variants = new ArrayList<>();
    private ProductStatus status;

    public void register(String identifier, String name, String description) {
        requireText(identifier, "Product identifier must be provided.");
        requireText(name, "Product name must not be empty.");
        this.identifier = identifier;
        this.name = name;
        this.description = description;
        this.status = ProductStatus.SUSPENDED;
    }

    /**
     * Distinguishes physical products from digital ones.
     */
    public abstract ProductType type();

    /**
     * Physical products require inventory and dispatch; digital ones do not.
     */
    public abstract boolean requiresShipment();

    void assignSeller(Seller seller) {
        if (seller == null) {
            throw new InvalidProductException("A product must belong to a Seller.");
        }
        this.seller = seller;
    }

    /**
     * Makes the product visible in the public catalog.
     */
    public void publish() {
        if (ProductStatus.DISCONTINUED.equals(status)) {
            throw new InvalidStatusTransitionException(
                    ProductStatus.DISCONTINUED.getCode(), ProductStatus.PUBLISHED.getCode());
        }
        if (seller == null) {
            throw new InvalidProductException("A product cannot be published without a Seller.");
        }
        this.status = ProductStatus.PUBLISHED;
    }

    /**
     * Temporarily hides the product from the catalog.
     */
    public void suspend() {
        if (!ProductStatus.PUBLISHED.equals(status)) {
            throw new InvalidStatusTransitionException(statusCode(), ProductStatus.SUSPENDED.getCode());
        }
        this.status = ProductStatus.SUSPENDED;
    }

    /**
     * Permanently removes the product from sale. This transition is final.
     */
    public void discontinue() {
        if (ProductStatus.DISCONTINUED.equals(status)) {
            throw new InvalidStatusTransitionException(statusCode(), ProductStatus.DISCONTINUED.getCode());
        }
        this.status = ProductStatus.DISCONTINUED;
    }

    public void addVariant(ProductVariant variant) {
        if (variant == null) {
            throw new InvalidProductException("The variant to add must be provided.");
        }
        if (variants.contains(variant)) {
            throw new InvalidProductException("The variant is already registered for this product.");
        }
        this.variants.add(variant);
    }

    public boolean isPublished() {
        return ProductStatus.PUBLISHED.equals(status);
    }

    /**
     * Guarantees the product can take part in a purchase.
     */
    public void requirePurchasable() {
        if (!isPublished()) {
            throw new InvalidProductException(
                    "Product " + identifier + " is not available for purchase.");
        }
    }

    private String statusCode() {
        return status == null ? "UNKNOWN" : status.getCode();
    }

    protected void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidProductException(message);
        }
    }
}
