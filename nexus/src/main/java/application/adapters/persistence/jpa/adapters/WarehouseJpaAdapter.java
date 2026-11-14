package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepositoryPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class WarehouseJpaAdapter implements WarehouseRepositoryPort {

    private final JpaSupport support;

    public WarehouseJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        return support.save(warehouse);
    }

    @Override
    public Optional<Warehouse> findByIdentifier(String identifier) {
        return support.findByIdentifier("Warehouse", Warehouse.class, identifier);
    }

    @Override
    public boolean existsByIdentifier(String identifier) {
        return support.existsByIdentifier("Warehouse", identifier);
    }
}
