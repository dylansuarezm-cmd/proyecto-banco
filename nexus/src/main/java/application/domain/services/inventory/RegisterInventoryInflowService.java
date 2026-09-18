package application.domain.services.inventory;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.PhysicalProduct;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.RegisterInventoryInflowUseCase;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link RegisterInventoryInflowUseCase}.
 *
 * <p>Input: the identifier of the user performing the movement, the product and warehouse identifiers, and the
 * incoming quantity.
 * <p>Output: the persisted {@link Inventory} record with its available quantity increased.
 * <p>Exceptions: {@link EntityNotFoundException} when the user, product, or the inventory record for that
 * product/warehouse pair do not exist; {@link application.domain.exceptions.InvalidQuantityException} when the
 * quantity is not positive, enforced by {@link Inventory#registerInflow}.
 */
@Service
@RequiredArgsConstructor
public class RegisterInventoryInflowService implements RegisterInventoryInflowUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    @Override
    public Inventory registerInflow(String performedByIdentifier, String productIdentifier,
                                    String warehouseIdentifier, int quantity) {
        User performedBy = userRepositoryPort.findByIdentifier(performedByIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("User"));
        performedBy.requireActive();

        Inventory inventory = findInventory(productIdentifier, warehouseIdentifier);
        inventory.registerInflow(quantity);
        return inventoryRepositoryPort.save(inventory);
    }

    private Inventory findInventory(String productIdentifier, String warehouseIdentifier) {
        PhysicalProduct product = productRepositoryPort.findPhysicalByIdentifier(productIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("PhysicalProduct"));
        Warehouse warehouse = warehouseRepositoryPort.findByIdentifier(warehouseIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse"));
        return inventoryRepositoryPort.findByProductAndWarehouse(product, warehouse)
                .orElseThrow(() -> new EntityNotFoundException("Inventory"));
    }
}
