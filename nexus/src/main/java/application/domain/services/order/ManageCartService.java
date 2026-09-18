package application.domain.services.order;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.models.Product;
import application.domain.ports.in.ManageCartUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link ManageCartUseCase}.
 *
 * <p>Input: the buyer's identifier, the product's identifier, and (depending on the operation) the quantity.
 * <p>Output: the persisted {@link Cart} reflecting the change.
 * <p>Exceptions: {@link EntityNotFoundException} when the buyer or product do not exist;
 * {@link application.domain.exceptions.InvalidProductException} when the product is not published, enforced by
 * {@link Product#requirePurchasable}; {@link application.domain.exceptions.InvalidCartException} when removing
 * or updating a product that is not in the cart.
 */
@Service
@RequiredArgsConstructor
public class ManageCartService implements ManageCartUseCase {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final CartRepositoryPort cartRepositoryPort;

    @Override
    public Cart addItem(String buyerIdentifier, String productIdentifier, int quantity) {
        Buyer buyer = findBuyer(buyerIdentifier);
        Product product = findProduct(productIdentifier);
        buyer.getCart().addItem(product, quantity);
        return cartRepositoryPort.save(buyer.getCart());
    }

    @Override
    public Cart updateItem(String buyerIdentifier, String productIdentifier, int newQuantity) {
        Buyer buyer = findBuyer(buyerIdentifier);
        Product product = findProduct(productIdentifier);
        buyer.getCart().updateQuantity(product, newQuantity);
        return cartRepositoryPort.save(buyer.getCart());
    }

    @Override
    public Cart removeItem(String buyerIdentifier, String productIdentifier) {
        Buyer buyer = findBuyer(buyerIdentifier);
        Product product = findProduct(productIdentifier);
        buyer.getCart().removeItem(product);
        return cartRepositoryPort.save(buyer.getCart());
    }

    private Buyer findBuyer(String buyerIdentifier) {
        Buyer buyer = buyerRepositoryPort.findByIdentifier(buyerIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Buyer"));
        buyer.requireActive();
        return buyer;
    }

    private Product findProduct(String productIdentifier) {
        return productRepositoryPort.findByIdentifier(productIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Product"));
    }
}
