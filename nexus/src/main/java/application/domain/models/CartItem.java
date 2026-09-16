package application.domain.models;

import application.domain.exceptions.InvalidQuantityException;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartItem {

    private Product product;
    private Integer quantity;
    private BigDecimal unitPrice;

    public void select(Product product, int quantity) {
        requirePositive(quantity);
        this.product = product;
        this.quantity = quantity;
    }

    public void increaseQuantity(int amount) {
        requirePositive(amount);
        this.quantity = currentQuantity() + amount;
    }

    public void changeQuantity(int newQuantity) {
        requirePositive(newQuantity);
        this.quantity = newQuantity;
    }

    public BigDecimal estimatedSubtotal() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(currentQuantity()));
    }

    private int currentQuantity() {
        return quantity == null ? 0 : quantity;
    }

    private void requirePositive(int value) {
        if (value <= 0) {
            throw new InvalidQuantityException("Cart item quantity must be greater than zero.");
        }
    }
}
