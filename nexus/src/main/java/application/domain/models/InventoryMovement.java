package application.domain.models;

import application.domain.valueobjects.InventoryMovementType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
