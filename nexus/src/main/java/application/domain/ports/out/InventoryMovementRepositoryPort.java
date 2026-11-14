package application.domain.ports.out;

import application.domain.models.InventoryMovement;

/** Output Port: persistence of the stock movement trail. */
public interface InventoryMovementRepositoryPort {

    InventoryMovement save(InventoryMovement movement);

    boolean existsByIdentifier(String identifier);

    java.util.Optional<InventoryMovement> findByIdentifier(String identifier);
}
