package application.domain.models;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Shipment {
    private String identifier;
    private Order order;
    private LogisticsOperator dispatchedBy;
    private Warehouse originWarehouse;
    private LocalDateTime dispatchDate;
    // Null until delivery is confirmed.
    private LocalDateTime deliveryDate;
}
