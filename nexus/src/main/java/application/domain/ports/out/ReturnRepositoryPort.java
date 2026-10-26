package application.domain.ports.out;

import application.domain.models.Order;
import application.domain.models.Return;
import java.util.Optional;

public interface ReturnRepositoryPort {

    Return save(Return returnRequest);

    Optional<Return> findByIdentifier(String identifier);

    boolean existsByIdentifier(String identifier);

    boolean existsByOrder(Order order);
}
