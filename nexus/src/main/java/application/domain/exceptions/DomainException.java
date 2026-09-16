package application.domain.exceptions;

/**
 * Base class for every business rule violation raised by the NexusMarket domain.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
