package application.domain.services.catalog;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.DigitalProduct;
import application.domain.models.Seller;
import application.domain.ports.in.PublishDigitalProductUseCase;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link PublishDigitalProductUseCase}.
 *
 * <p>Input: the seller's identifier, the product's identity data, and the asset delivered upon payment.
 * <p>Output: the persisted, published {@link DigitalProduct}.
 * <p>Exceptions: {@link EntityNotFoundException} when the seller does not exist.
 */
@Service
@RequiredArgsConstructor
public class PublishDigitalProductService implements PublishDigitalProductUseCase {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;

    @Override
    public DigitalProduct publish(String sellerIdentifier, String productIdentifier, String name,
                                  String description, String deliveryAsset) {
        Seller seller = sellerRepositoryPort.findByIdentifier(sellerIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
        seller.requireActive();

        DigitalProduct product = new DigitalProduct();
        product.register(productIdentifier, name, description);
        product.setDeliveryAsset(deliveryAsset);
        seller.publish(product);
        return (DigitalProduct) productRepositoryPort.save(product);
    }
}
