package application.domain.ports.out;

import application.domain.models.Order;
import java.util.Optional;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findByIdentifier(String identifier);
}
