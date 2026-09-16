package application.domain.exceptions;

public class InvalidShipmentException extends DomainException {

    public InvalidShipmentException(String message) {
        super(message);
    }
}
