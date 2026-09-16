package application.domain.models;

import application.domain.exceptions.InvalidShipmentException;
import application.domain.exceptions.UnauthorizedDomainAccessException;
import application.domain.valueobjects.SystemRole;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LogisticsOperator extends User {

    /**
     * Performs the physical dispatch of an order from a warehouse.
     */
    public Shipment dispatch(Order order, Warehouse originWarehouse, LocalDateTime dispatchDate) {
        requireOperatorRole();
        if (order == null || originWarehouse == null) {
            throw new InvalidShipmentException("Order and origin warehouse must be provided.");
        }
        Shipment shipment = new Shipment();
        shipment.prepare(order, this, originWarehouse, dispatchDate);
        order.dispatch(shipment);
        return shipment;
    }

    private void requireOperatorRole() {
        requireActive();
        if (!hasRole(SystemRole.LOGISTICS_OPERATOR)) {
            throw new UnauthorizedDomainAccessException(
                    "Only a Logistics Operator can perform this operation.");
        }
    }
}
