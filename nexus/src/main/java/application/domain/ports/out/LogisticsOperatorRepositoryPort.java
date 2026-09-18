package application.domain.ports.out;

import application.domain.models.LogisticsOperator;
import java.util.Optional;

public interface LogisticsOperatorRepositoryPort {

    Optional<LogisticsOperator> findByIdentifier(String identifier);
}
