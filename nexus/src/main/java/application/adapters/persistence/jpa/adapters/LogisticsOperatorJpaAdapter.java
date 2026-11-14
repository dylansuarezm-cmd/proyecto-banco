package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.LogisticsOperator;
import application.domain.ports.out.LogisticsOperatorRepositoryPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class LogisticsOperatorJpaAdapter implements LogisticsOperatorRepositoryPort {

    private final JpaSupport support;

    public LogisticsOperatorJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public Optional<LogisticsOperator> findByIdentifier(String identifier) {
        return support.findByIdentifier("LogisticsOperator", LogisticsOperator.class, identifier);
    }
}
