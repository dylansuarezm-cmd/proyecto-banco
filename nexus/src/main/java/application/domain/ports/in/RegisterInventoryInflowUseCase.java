package application.domain.ports.in;

import application.domain.models.Inventory;

/** Input Port: Register Inventory Inflow use case. */
public interface RegisterInventoryInflowUseCase {

    Inventory registerInflow(String performedByIdentifier, String productIdentifier, String warehouseIdentifier,
                             int quantity);
}
