package application.domain.models;

import application.domain.exceptions.InvalidInventoryException;
import application.domain.exceptions.InvalidQuantityException;
import application.domain.valueobjects.InventoryMovementType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Traceability record of a stock-affecting action executed over an inventory record.
 */
@Getter
@Setter
@NoArgsConstructor
public class InventoryMovement {

    private String movementId;
    private InventoryMovementType movementType;
    private Integer quantity;
    private LocalDateTime executionDate;
    private User performedBy;
    private Inventory affectedInventory;

    /**
     * Records a movement, leaving evidence of who changed the stock and by how much.
     */
    public void record(String movementId, InventoryMovementType movementType, int quantity,
                       User performedBy, Inventory affectedInventory) {
        if (movementId == null || movementId.isBlank()) {
            throw new InvalidInventoryException("Movement identifier must be provided.");
        }
        if (movementType == null) {
            throw new InvalidInventoryException("Movement type must be provided.");
        }
        if (quantity <= 0) {
            throw new InvalidQuantityException("Movement quantity must be greater than zero.");
        }
        if (performedBy == null) {
            throw new InvalidInventoryException("Every movement must be performed by a user.");
        }
        if (affectedInventory == null) {
            throw new InvalidInventoryException("Every movement must affect an inventory record.");
        }
        performedBy.requireActive();
        this.movementId = movementId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.performedBy = performedBy;
        this.affectedInventory = affectedInventory;
        this.executionDate = LocalDateTime.now();
    }

    public boolean isOfType(InventoryMovementType expectedType) {
        return movementType != null && movementType.equals(expectedType);
    }
}
