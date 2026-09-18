package application.domain.ports.in;

import application.domain.models.PhysicalProduct;

/** Input Port: Publish Physical Product use case. Opens the product's initial inventory. */
public interface PublishPhysicalProductUseCase {

    PhysicalProduct publish(String sellerIdentifier, String productIdentifier, String name, String description,
                            String warehouseIdentifier, int initialQuantity);
}
