package application.domain.ports.in;

import application.domain.models.Inventory;

/** Input Port: Mark Inventory As Damaged use case. */
public interface MarkInventoryAsDamagedUseCase {

    Inventory markAsDamaged(String performedByIdentifier, String productIdentifier, String warehouseIdentifier,
                            int quantity);
}
