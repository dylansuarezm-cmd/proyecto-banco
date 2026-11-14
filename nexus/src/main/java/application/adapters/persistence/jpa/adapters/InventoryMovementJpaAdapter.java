package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.InventoryMovement;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class InventoryMovementJpaAdapter implements InventoryMovementRepositoryPort {

    private final JpaSupport support;

    public InventoryMovementJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        return support.save(movement);
    }

    @Override
    public boolean existsByIdentifier(String identifier) {
        return support.exists("select count(m) from InventoryMovement m where m.movementId = :id",
                java.util.Map.of("id", identifier));
    }

    @Override
    public Optional<InventoryMovement> findByIdentifier(String identifier) {
        return support.findOne("select m from InventoryMovement m where m.movementId = :id",
                InventoryMovement.class, java.util.Map.of("id", identifier));
    }
}
