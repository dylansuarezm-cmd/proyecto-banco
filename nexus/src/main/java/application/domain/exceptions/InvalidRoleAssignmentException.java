package application.domain.exceptions;

/**
 * Raised when a user does not hold a single, well defined role (RG-02).
 */
public class InvalidRoleAssignmentException extends DomainException {

    public InvalidRoleAssignmentException(String message) {
        super(message);
    }
}
