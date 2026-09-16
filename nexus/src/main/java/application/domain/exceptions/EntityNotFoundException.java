package application.domain.exceptions;

public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entityName) {
        super(entityName + " was not found.");
    }
}
