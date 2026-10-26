package application.domain.ports.out;

import application.domain.models.Refund;
import application.domain.models.Return;
import java.util.Optional;

public interface RefundRepositoryPort {

    Refund save(Refund refund);

    Optional<Refund> findByIdentifier(String identifier);

    boolean existsByIdentifier(String identifier);

    boolean existsByRelatedReturn(Return relatedReturn);
}
