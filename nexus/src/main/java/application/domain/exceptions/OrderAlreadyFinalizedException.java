package application.domain.exceptions;

/**
 * Raised when a finalized order is modified. A finalized order is immutable.
 */
public class OrderAlreadyFinalizedException extends DomainException {

    public OrderAlreadyFinalizedException() {
        super("A finalized order cannot be modified under any circumstance.");
    }
}
