package application.domain.exceptions;

/**
 * Raised when a buyer's commercial status does not allow placing orders.
 */
public class BuyerNotEligibleException extends DomainException {

    public BuyerNotEligibleException(String message) {
        super(message);
    }
}
