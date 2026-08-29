package application.domain.models;

import application.domain.valueobjects.OrderStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Order {
    private String identifier;
    private Buyer buyer;
    private List<OrderItem> items = new ArrayList<>();
    private OrderStatus orderStatus;
    private LocalDateTime creationDate;
    private Invoice invoice;
    // Populated only when the order contains at least one PhysicalProduct item.
    private Shipment shipment;
}
