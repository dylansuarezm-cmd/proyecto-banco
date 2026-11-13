package application.domain.models;

import application.domain.exceptions.InvalidCartException;
import application.domain.exceptions.InvalidQuantityException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Provisional product selection made by a buyer before confirming an order.
 */
@Getter
@Setter
@NoArgsConstructor
public class Cart {

    private String identifier;
    private Buyer owner;
    private List<CartItem> items = new ArrayList<>();

    void assignTo(Buyer buyer) {
        if (buyer == null) {
            throw new InvalidCartException("A cart must belong to a buyer.");
        }
        this.owner = buyer;
    }

    /**
     * Adds a published product to the selection, merging quantities when already present.
     */
    public void addItem(Product product, int quantity) {
        if (product == null) {
            throw new InvalidCartException("The product to add must be provided.");
        }
        if (quantity <= 0) {
            throw new InvalidQuantityException("Cart item quantity must be greater than zero.");
        }
        product.requirePurchasable();
        Optional<CartItem> existing = findItem(product);
        int alreadyInCart = existing.map(CartItem::getQuantity).orElse(0);
        product.requireAvailableFor(alreadyInCart + quantity);
        if (existing.isPresent()) {
            existing.get().increaseQuantity(quantity);
            existing.get().setUnitPrice(product.getPrice());
            return;
        }
        CartItem item = new CartItem();
        item.select(product, quantity);
        item.setUnitPrice(product.getPrice());
        this.items.add(item);
    }

    public void removeItem(Product product) {
        CartItem item = findItem(product)
                .orElseThrow(() -> new InvalidCartException("The product is not present in the cart."));
        this.items.remove(item);
    }

    public void updateQuantity(Product product, int newQuantity) {
        CartItem item = findItem(product)
                .orElseThrow(() -> new InvalidCartException("The product is not present in the cart."));
        if (newQuantity <= 0) {
            throw new InvalidQuantityException("Cart item quantity must be greater than zero.");
        }
        product.requireAvailableFor(newQuantity);
        item.changeQuantity(newQuantity);
        item.setUnitPrice(product.getPrice());
    }

    public void clear() {
        this.items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Indicates whether the selection will require a physical dispatch.
     */
    public boolean containsPhysicalProducts() {
        return items.stream().anyMatch(item -> item.getProduct() != null
                && item.getProduct().requiresShipment());
    }

    public BigDecimal estimatedTotal() {
        return items.stream()
                .map(CartItem::estimatedSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Guarantees the cart can be converted into a formal order.
     */
    public void requireReadyForCheckout() {
        if (isEmpty()) {
            throw new InvalidCartException("An empty cart cannot be converted into an order.");
        }
        items.forEach(item -> item.getProduct().requirePurchasable());
    }

    private Optional<CartItem> findItem(Product product) {
        if (product == null || product.getIdentifier() == null) {
            return Optional.empty();
        }
        return items.stream()
                .filter(item -> item.getProduct() != null
                        && product.getIdentifier().equals(item.getProduct().getIdentifier()))
                .findFirst();
    }
}
