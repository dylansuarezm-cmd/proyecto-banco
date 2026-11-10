package application.domain.valueobjects;

import application.domain.exceptions.InvalidQuantityException;
import application.domain.exceptions.InvalidReturnException;

/**
 * Value object describing one line of a return request: which product of the order is returned and how many
 * units of it. Immutable; two lines with the same product and quantity are interchangeable.
 */
public record ReturnLine(String productIdentifier, int quantity) {

    public ReturnLine {
        if (productIdentifier == null || productIdentifier.isBlank()) {
            throw new InvalidReturnException("A return line must identify the product being returned.");
        }
        if (quantity <= 0) {
            throw new InvalidQuantityException("The returned quantity must be greater than zero.");
        }
        productIdentifier = productIdentifier.trim();
    }
}
