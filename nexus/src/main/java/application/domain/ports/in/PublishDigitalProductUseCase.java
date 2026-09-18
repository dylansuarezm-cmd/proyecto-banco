package application.domain.ports.in;

import application.domain.models.DigitalProduct;

/** Input Port: Publish Digital Product use case. */
public interface PublishDigitalProductUseCase {

    DigitalProduct publish(String sellerIdentifier, String productIdentifier, String name, String description,
                           String deliveryAsset);
}
