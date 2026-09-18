package application.domain.services.order;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.LogisticsOperator;
import application.domain.models.Order;
import application.domain.models.Shipment;
import application.domain.models.Warehouse;
import application.domain.ports.in.DispatchOrderUseCase;
import application.domain.ports.out.LogisticsOperatorRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link DispatchOrderUseCase}.
 *
 * <p>Input: the dispatching operator's identifier, the order's identifier, the origin warehouse's identifier,
 * and the identifier to assign to the shipment.
 * <p>Output: the persisted {@link Order}, now {@code DISPATCHED}, with its {@link Shipment} attached.
 * <p>Exceptions: {@link EntityNotFoundException} when the operator, order, or warehouse do not exist;
 * {@link application.domain.exceptions.InvalidStatusTransitionException} when the order is not {@code PAID},
 * enforced by {@link Order#dispatch}; {@link application.domain.exceptions.InvalidOrderException} when the
 * order has no physical items to dispatch.
 */
@Service
@RequiredArgsConstructor
public class DispatchOrderService implements DispatchOrderUseCase {

    private final LogisticsOperatorRepositoryPort logisticsOperatorRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final ShipmentRepositoryPort shipmentRepositoryPort;

    @Override
    public Order dispatch(String operatorIdentifier, String orderIdentifier, String warehouseIdentifier,
                          String shipmentIdentifier) {
        LogisticsOperator operator = logisticsOperatorRepositoryPort.findByIdentifier(operatorIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("LogisticsOperator"));
        Order order = orderRepositoryPort.findByIdentifier(orderIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        Warehouse warehouse = warehouseRepositoryPort.findByIdentifier(warehouseIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse"));

        Shipment shipment = operator.dispatch(order, warehouse, LocalDateTime.now());
        shipment.setIdentifier(shipmentIdentifier);
        shipmentRepositoryPort.save(shipment);
        return orderRepositoryPort.save(order);
    }
}
