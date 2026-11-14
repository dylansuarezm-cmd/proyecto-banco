package application.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@NoArgsConstructor
public class InventoryMovement {

    @Id
    private String movementId;
    private InventoryMovementType movementType;
    private Integer quantity;
    private LocalDateTime executionDate;
    /** What caused the movement: an order, a return, a refund, or a manual operation. */
    @jakarta.persistence.Column(name = "cause_reference")
    private String reference;
    /** Available quantity of the inventory right after the movement. */
    private Integer availableAfter;
    @ManyToOne
    private User performedBy;
    @ManyToOne
    private Inventory affectedInventory;

    /**
     * Records a movement, leaving evidence of who changed the stock and by how much.
     */
    public void record(String movementId, InventoryMovementType movementType, int quantity,
                       User performedBy, Inventory affectedInventory, String reference) {
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
        this.reference = reference;
        this.availableAfter = affectedInventory.getAvailableQty();
        this.executionDate = LocalDateTime.now();
    }

    public boolean isOfType(InventoryMovementType expectedType) {
        return movementType != null && movementType.equals(expectedType);
    }
}
