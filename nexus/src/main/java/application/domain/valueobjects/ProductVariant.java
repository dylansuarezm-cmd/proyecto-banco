package application.domain.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents a single differentiating characteristic of a product,
 * such as color, size, or model (Dominio 5).
 *
 * Unlike {@link DomainCatalog} entries, a variant is a lightweight
 * structural Value Object rather than a controlled business catalog,
 * since its possible values are defined per product by the Seller
 * rather than by the domain itself.
 */
@Getter
@EqualsAndHashCode
public final class ProductVariant {

    private final String variantType;
    private final String value;

    public ProductVariant(String variantType, String value) {
        this.variantType = variantType;
        this.value = value;
    }
}
