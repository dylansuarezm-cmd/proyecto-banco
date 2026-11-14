package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.Order;
import application.domain.ports.out.OrderRepositoryPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class OrderJpaAdapter implements OrderRepositoryPort {

    private final JpaSupport support;

    public OrderJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public Order save(Order order) {
        return support.save(order);
    }

    @Override
    public Optional<Order> findByIdentifier(String identifier) {
        return support.findByIdentifier("PurchaseOrder", Order.class, identifier);
    }

    @Override
    public boolean existsByIdentifier(String identifier) {
        return support.existsByIdentifier("PurchaseOrder", identifier);
    }
}
