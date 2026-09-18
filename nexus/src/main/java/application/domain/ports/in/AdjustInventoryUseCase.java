package application.domain.ports.in;

import application.domain.models.Inventory;

/** Input Port: Adjust Inventory use case. */
public interface AdjustInventoryUseCase {

    Inventory adjust(String performedByIdentifier, String productIdentifier, String warehouseIdentifier,
                     int newQuantity);
}
