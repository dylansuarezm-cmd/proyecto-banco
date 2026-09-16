package application.domain.models;

import application.domain.exceptions.InvalidOrderException;
import application.domain.exceptions.InvalidQuantityException;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    private Product product;
    private Integer quantity;
    private BigDecimal unitPrice;

    /**
     * Freezes the product, quantity and price at the moment of purchase.
     */
    public void confirm(Product product, int quantity, BigDecimal unitPrice) {
        if (product == null) {
            throw new InvalidOrderException("An order item must reference a product.");
        }
        if (quantity <= 0) {
            throw new InvalidQuantityException("Order item quantity must be greater than zero.");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new InvalidOrderException("Order item price must not be negative.");
        }
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal subtotal() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean requiresShipment() {
        return product != null && product.requiresShipment();
    }
}
