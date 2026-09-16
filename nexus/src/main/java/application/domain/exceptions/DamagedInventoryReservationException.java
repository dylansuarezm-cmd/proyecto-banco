package application.domain.exceptions;

/**
 * Raised when stock marked as damaged is used to fulfil a reservation.
 */
public class DamagedInventoryReservationException extends DomainException {

    public DamagedInventoryReservationException() {
        super("Inventory marked as damaged cannot be reserved.");
    }
}
