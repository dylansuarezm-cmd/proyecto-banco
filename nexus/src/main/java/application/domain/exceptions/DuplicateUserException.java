package application.domain.exceptions;

/**
 * Raised when a user is registered with an identifier (identity document) or an email that already
 * belongs to another user. Both must be unique across the whole platform, whatever the role.
 */
public class DuplicateUserException extends DomainException {

    public DuplicateUserException(String message) {
        super(message);
    }
}
