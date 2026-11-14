package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.Shipment;
import application.domain.ports.out.ShipmentRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class ShipmentJpaAdapter implements ShipmentRepositoryPort {

    private final JpaSupport support;

    public ShipmentJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public Shipment save(Shipment shipment) {
        return support.save(shipment);
    }

    @Override
    public boolean existsByIdentifier(String identifier) {
        return support.existsByIdentifier("Shipment", identifier);
    }
}
