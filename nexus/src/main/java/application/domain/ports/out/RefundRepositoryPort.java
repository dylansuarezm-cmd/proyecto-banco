package application.domain.ports.out;

import application.domain.models.Refund;
import java.util.Optional;

public interface RefundRepositoryPort {

    Refund save(Refund refund);

    Optional<Refund> findByIdentifier(String identifier);
}
