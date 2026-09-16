package application.domain.exceptions;

/**
 * Raised when a participant operates outside the scope of its own role (RG-03).
 */
public class UnauthorizedDomainAccessException extends DomainException {

    public UnauthorizedDomainAccessException(String message) {
        super(message);
    }
}
