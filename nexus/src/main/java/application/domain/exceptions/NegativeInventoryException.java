package application.domain.exceptions;

/**
 * Raised when an operation would leave inventory below zero.
 * Negative stock is never permitted under any circumstance.
 */
public class NegativeInventoryException extends DomainException {

    public NegativeInventoryException() {
        super("Inventory quantities must never become negative.");
    }
}
