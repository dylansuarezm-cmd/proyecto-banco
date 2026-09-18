package application.domain.ports.in;

import application.domain.models.Order;

/** Input Port: Dispatch Order use case. */
public interface DispatchOrderUseCase {

    Order dispatch(String operatorIdentifier, String orderIdentifier, String warehouseIdentifier,
                   String shipmentIdentifier);
}
