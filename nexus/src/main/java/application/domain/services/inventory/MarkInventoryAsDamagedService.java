package application.domain.services.inventory;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.PhysicalProduct;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.MarkInventoryAsDamagedUseCase;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link MarkInventoryAsDamagedUseCase}.
 *
 * <p>Input: the identifier of the user performing the operation, the product and warehouse identifiers, and
 * the quantity to mark as damaged.
 * <p>Output: the persisted {@link Inventory} record, with the given quantity moved from available to damaged.
 * <p>Exceptions: {@link EntityNotFoundException} when the user, product, or inventory record do not exist;
 * {@link application.domain.exceptions.InsufficientInventoryException} when the quantity exceeds what is
 * available, enforced by {@link Inventory#markAsDamaged}.
 */
@Service
@RequiredArgsConstructor
public class MarkInventoryAsDamagedService implements MarkInventoryAsDamagedUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    @Override
    public Inventory markAsDamaged(String performedByIdentifier, String productIdentifier,
                                   String warehouseIdentifier, int quantity) {
        User performedBy = userRepositoryPort.findByIdentifier(performedByIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("User"));
        performedBy.requireActive();

        PhysicalProduct product = productRepositoryPort.findPhysicalByIdentifier(productIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("PhysicalProduct"));
        Warehouse warehouse = warehouseRepositoryPort.findByIdentifier(warehouseIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse"));
        Inventory inventory = inventoryRepositoryPort.findByProductAndWarehouse(product, warehouse)
                .orElseThrow(() -> new EntityNotFoundException("Inventory"));

        inventory.markAsDamaged(quantity);
        return inventoryRepositoryPort.save(inventory);
    }
}
