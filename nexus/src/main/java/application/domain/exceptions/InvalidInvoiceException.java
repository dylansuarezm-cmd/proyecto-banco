package application.domain.exceptions;

public class InvalidInvoiceException extends DomainException {

    public InvalidInvoiceException(String message) {
        super(message);
    }
}
