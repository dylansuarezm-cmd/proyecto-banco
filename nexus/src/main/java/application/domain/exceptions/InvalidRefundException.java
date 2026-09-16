package application.domain.exceptions;

public class InvalidRefundException extends DomainException {

    public InvalidRefundException(String message) {
        super(message);
    }
}
