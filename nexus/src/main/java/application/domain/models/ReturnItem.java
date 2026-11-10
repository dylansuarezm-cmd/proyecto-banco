package application.domain.models;

import application.domain.exceptions.InvalidQuantityException;
import application.domain.exceptions.InvalidReturnException;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Line of a {@link Return}: a product of the returned order and the number of units sent back.
 * The unit price is the one the buyer actually paid, copied from the {@link OrderItem}.
 */
@Getter
@NoArgsConstructor
public class ReturnItem {

    private Product product;
    private Integer quantity;
    private BigDecimal unitPrice;

    /**
     * Defines the line from the purchased item. Returning more units than were bought is rejected.
     */
    public void define(OrderItem purchasedItem, int quantity) {
        if (purchasedItem == null || purchasedItem.getProduct() == null) {
            throw new InvalidReturnException("A returned item must come from a purchased item of the order.");
        }
        if (quantity <= 0) {
            throw new InvalidQuantityException("The returned quantity must be greater than zero.");
        }
        if (quantity > purchasedItem.getQuantity()) {
            throw new InvalidReturnException("Cannot return " + quantity + " units of product "
                    + purchasedItem.getProduct().getIdentifier() + ": only "
                    + purchasedItem.getQuantity() + " were purchased.");
        }
        this.product = purchasedItem.getProduct();
        this.quantity = quantity;
        this.unitPrice = purchasedItem.getUnitPrice();
    }

    public BigDecimal subtotal() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Indicates whether the returned product was sold by the given Seller, that is, who the return is addressed to.
     */
    public boolean isSoldBy(Seller seller) {
        return seller != null && seller.owns(product);
    }
}
