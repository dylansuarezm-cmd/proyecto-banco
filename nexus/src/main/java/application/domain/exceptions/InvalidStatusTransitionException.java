package application.domain.exceptions;

public class InvalidStatusTransitionException extends DomainException {

    public InvalidStatusTransitionException(String currentStatus, String targetStatus) {
        super("Invalid status transition from " + currentStatus + " to " + targetStatus + ".");
    }
}
