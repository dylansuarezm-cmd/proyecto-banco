package application.domain.services.catalog;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.ports.in.ManageProductLifecycleUseCase;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link ManageProductLifecycleUseCase}.
 *
 * <p>Input: the seller's identifier, the product's identifier, and the desired {@link Action}.
 * <p>Output: the persisted {@link Product} with its updated {@link application.domain.valueobjects.ProductStatus}.
 * <p>Exceptions: {@link EntityNotFoundException} when the product or seller do not exist;
 * {@link application.domain.exceptions.UnauthorizedDomainAccessException} when the seller does not own the
 * product (RG-03), enforced by {@link Seller#requireOwnershipOf}; {@link application.domain.exceptions.InvalidStatusTransitionException}
 * when discontinuing an already discontinued product (that transition is final).
 */
@Service
@RequiredArgsConstructor
public class ManageProductLifecycleService implements ManageProductLifecycleUseCase {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;

    @Override
    public Product changeLifecycle(String sellerIdentifier, String productIdentifier, Action action) {
        Seller seller = sellerRepositoryPort.findByIdentifier(sellerIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
        Product product = productRepositoryPort.findByIdentifier(productIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Product"));
        seller.requireOwnershipOf(product);

        switch (action) {
            case SUSPEND -> product.suspend();
            case DISCONTINUE -> product.discontinue();
        }
        return productRepositoryPort.save(product);
    }
}
