package application.domain.ports.in;

import application.domain.valueobjects.ProductVariant;
import java.math.BigDecimal;
import java.util.List;
import application.domain.models.PhysicalProduct;

/** Input Port: Publish Physical Product use case. Opens the product's initial inventory. */
public interface PublishPhysicalProductUseCase {

    PhysicalProduct publish(String sellerIdentifier, String productIdentifier, String name, String description,
                            BigDecimal price, String warehouseIdentifier, int initialQuantity,
                            List<ProductVariant> variants);
}
