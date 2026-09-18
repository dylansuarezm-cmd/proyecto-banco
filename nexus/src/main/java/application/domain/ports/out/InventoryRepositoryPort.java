package application.domain.ports.out;

import application.domain.models.Inventory;
import application.domain.models.PhysicalProduct;
import application.domain.models.Warehouse;
import java.util.Optional;

public interface InventoryRepositoryPort {

    Inventory save(Inventory inventory);

    Optional<Inventory> findByIdentifier(String identifier);

    Optional<Inventory> findByProductAndWarehouse(PhysicalProduct product, Warehouse warehouse);
}
