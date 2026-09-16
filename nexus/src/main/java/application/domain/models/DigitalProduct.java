package application.domain.models;

import application.domain.exceptions.InvalidProductException;
import application.domain.valueobjects.ProductType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DigitalProduct extends Product {

    private String deliveryAsset;

    /**
     * Digital products are delivered immediately after payment confirmation.
     */
    public String deliver() {
        if (deliveryAsset == null || deliveryAsset.isBlank()) {
            throw new InvalidProductException(
                    "Digital product " + getIdentifier() + " has no deliverable asset.");
        }
        return deliveryAsset;
    }

    @Override
    public ProductType type() {
        return ProductType.DIGITAL;
    }

    @Override
    public boolean requiresShipment() {
        return false;
    }
}
