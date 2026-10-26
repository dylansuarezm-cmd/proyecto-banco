package application.domain.exceptions;

/**
 * Raised when a business entity is created with an identifier that is already in use, or when a second
 * record is created for something that admits only one (a second return for the same order, a second
 * refund for the same return).
 */
public class DuplicateEntityException extends DomainException {

    public DuplicateEntityException(String message) {
        super(message);
    }
}
