package application.domain.ports.out;

import application.domain.models.Warehouse;
import java.util.Optional;

public interface WarehouseRepositoryPort {

    Warehouse save(Warehouse warehouse);

    Optional<Warehouse> findByIdentifier(String identifier);
}
