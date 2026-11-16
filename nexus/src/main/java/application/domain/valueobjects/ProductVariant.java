package application.domain.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import application.domain.exceptions.InvalidProductException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a single differentiating characteristic of a product,
 * such as color, size, or model (Dominio 5).
 *
 * Unlike {@link DomainCatalog} entries, a variant is a lightweight
 * structural Value Object rather than a controlled business catalog,
 * since its possible values are defined per product by the Seller
 * rather than by the domain itself.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class ProductVariant {

    private String variantType;
    @Column(name = "variant_value")
    private String value;

    public ProductVariant(String variantType, String value) {
        if (variantType == null || variantType.isBlank() || value == null || value.isBlank()) {
            throw new InvalidProductException("A variant needs a type (color, size, model...) and a value.");
        }
        this.variantType = variantType.trim();
        this.value = value.trim();
    }
}
