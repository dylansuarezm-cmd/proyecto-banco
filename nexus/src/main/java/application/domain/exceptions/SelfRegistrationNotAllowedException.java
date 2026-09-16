package application.domain.exceptions;

/**
 * Raised when a Seller attempts to self-register. Sellers are onboarded by an Administrator.
 */
public class SelfRegistrationNotAllowedException extends DomainException {

    public SelfRegistrationNotAllowedException() {
        super("Sellers cannot self-register; they must be registered by an Administrator.");
    }
}
