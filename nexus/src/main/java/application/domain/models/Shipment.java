package application.domain.models;

import application.domain.exceptions.InvalidShipmentException;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Logistics process required to deliver the physical products of an order.
 */
@Getter
@Setter
@NoArgsConstructor
public class Shipment {

    private String identifier;
    private Order order;
    private LogisticsOperator dispatchedBy;
    private Warehouse originWarehouse;
    private LocalDateTime dispatchDate;
    private LocalDateTime deliveryDate;

    /**
     * Prepares the dispatch of an order from a warehouse.
     */
    public void prepare(Order order, LogisticsOperator operator, Warehouse originWarehouse,
                        LocalDateTime dispatchDate) {
        if (order == null) {
            throw new InvalidShipmentException("A shipment must belong to an order.");
        }
        if (!order.requiresShipment()) {
            throw new InvalidShipmentException("Only orders with physical products can be shipped.");
        }
        if (operator == null) {
            throw new InvalidShipmentException("A shipment must be dispatched by a Logistics Operator.");
        }
        if (originWarehouse == null) {
            throw new InvalidShipmentException("A shipment must depart from a warehouse.");
        }
        this.order = order;
        this.dispatchedBy = operator;
        this.originWarehouse = originWarehouse;
        this.dispatchDate = dispatchDate == null ? LocalDateTime.now() : dispatchDate;
    }

    /**
     * Registers the confirmed delivery of the shipment.
     */
    public void confirmDelivery() {
        if (dispatchDate == null) {
            throw new InvalidShipmentException("A shipment that was never dispatched cannot be delivered.");
        }
        if (isDelivered()) {
            throw new InvalidShipmentException("The shipment has already been delivered.");
        }
        this.deliveryDate = LocalDateTime.now();
    }

    public boolean isDelivered() {
        return deliveryDate != null;
    }
}
