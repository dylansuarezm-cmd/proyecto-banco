package application.domain.exceptions;

public class InsufficientInventoryException extends DomainException {

    public InsufficientInventoryException(int requested, int available) {
        super("Requested quantity " + requested + " exceeds the available quantity " + available + ".");
    }
}
