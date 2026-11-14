package application.domain.services.inventory;

import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.User;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import application.domain.valueobjects.InventoryMovementType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Leaves evidence of every stock change (who, how many, why). The seller reports are built from this trail.
 * A movement of zero units is not recorded, because nothing changed.
 */
@Component
@RequiredArgsConstructor
public class InventoryMovementRecorder {

    private final InventoryMovementRepositoryPort movementRepositoryPort;

    public void record(InventoryMovementType type, int quantity, User performedBy, Inventory inventory,
                       String reference) {
        if (quantity <= 0) {
            return;
        }
        InventoryMovement movement = new InventoryMovement();
        movement.record("MOV-" + UUID.randomUUID(), type, quantity, performedBy, inventory, reference);
        movementRepositoryPort.save(movement);
    }
}
