package application.domain.ports.in;

import application.domain.valueobjects.ProductVariant;
import java.math.BigDecimal;
import java.util.List;
import application.domain.models.DigitalProduct;

/** Input Port: Publish Digital Product use case. */
public interface PublishDigitalProductUseCase {

    DigitalProduct publish(String sellerIdentifier, String productIdentifier, String name, String description,
                           BigDecimal price, String deliveryAsset, List<ProductVariant> variants);
}
