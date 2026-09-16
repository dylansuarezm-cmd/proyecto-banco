package application.domain.exceptions;

/**
 * Raised when the Seller who initiated a refund also attempts to approve it,
 * breaking the separation between execution and authorization.
 */
public class SelfApprovalNotAllowedException extends DomainException {

    public SelfApprovalNotAllowedException() {
        super("The participant who initiated a refund cannot also approve it.");
    }
}
