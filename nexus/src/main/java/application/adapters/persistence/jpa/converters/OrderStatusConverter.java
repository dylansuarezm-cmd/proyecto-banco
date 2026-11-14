package application.adapters.persistence.jpa.converters;

import application.domain.valueobjects.OrderStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderStatusConverter extends DomainCatalogConverter<OrderStatus> {

    public OrderStatusConverter() {
        super(OrderStatus.class);
    }
}
