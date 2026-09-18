package application.domain.services.inventory;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.PhysicalProduct;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.AdjustInventoryUseCase;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link AdjustInventoryUseCase}.
 *
 * <p>Input: the identifier of the user performing the adjustment, the product and warehouse identifiers, and
 * the corrected quantity.
 * <p>Output: the persisted {@link Inventory} record with its available quantity corrected.
 * <p>Exceptions: {@link EntityNotFoundException} when the user, product, or inventory record do not exist;
 * {@link application.domain.exceptions.NegativeInventoryException} when the new quantity is negative, enforced
 * by {@link Inventory#adjust}.
 */
@Service
@RequiredArgsConstructor
public class AdjustInventoryService implements AdjustInventoryUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    @Override
    public Inventory adjust(String performedByIdentifier, String productIdentifier, String warehouseIdentifier,
                            int newQuantity) {
        User performedBy = userRepositoryPort.findByIdentifier(performedByIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("User"));
        performedBy.requireActive();

        PhysicalProduct product = productRepositoryPort.findPhysicalByIdentifier(productIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("PhysicalProduct"));
        Warehouse warehouse = warehouseRepositoryPort.findByIdentifier(warehouseIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse"));
        Inventory inventory = inventoryRepositoryPort.findByProductAndWarehouse(product, warehouse)
                .orElseThrow(() -> new EntityNotFoundException("Inventory"));

        inventory.adjust(newQuantity);
        return inventoryRepositoryPort.save(inventory);
    }
}
