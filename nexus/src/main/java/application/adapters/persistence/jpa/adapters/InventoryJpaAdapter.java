package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.Inventory;
import application.domain.models.PhysicalProduct;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryRepositoryPort;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class InventoryJpaAdapter implements InventoryRepositoryPort {

    private final JpaSupport support;

    public InventoryJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public Inventory save(Inventory inventory) {
        return support.save(inventory);
    }

    @Override
    public Optional<Inventory> findByIdentifier(String identifier) {
        return support.findByIdentifier("Inventory", Inventory.class, identifier);
    }

    @Override
    public Optional<Inventory> findByProductAndWarehouse(PhysicalProduct product, Warehouse warehouse) {
        return support.findOne("select i from Inventory i where i.product = :p and i.warehouse = :w",
                Inventory.class, Map.of("p", product, "w", warehouse));
    }
}
