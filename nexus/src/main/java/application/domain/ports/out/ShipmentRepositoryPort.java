package application.domain.ports.out;

import application.domain.models.Shipment;

public interface ShipmentRepositoryPort {

    Shipment save(Shipment shipment);
}
