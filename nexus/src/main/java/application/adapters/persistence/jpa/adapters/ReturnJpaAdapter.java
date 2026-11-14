package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.Order;
import application.domain.models.Return;
import application.domain.ports.out.ReturnRepositoryPort;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ReturnJpaAdapter implements ReturnRepositoryPort {

    private final JpaSupport support;

    public ReturnJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public Return save(Return returnRequest) {
        return support.save(returnRequest);
    }

    @Override
    public Optional<Return> findByIdentifier(String identifier) {
        return support.findByIdentifier("ReturnRequest", Return.class, identifier);
    }

    @Override
    public boolean existsByIdentifier(String identifier) {
        return support.existsByIdentifier("ReturnRequest", identifier);
    }

    @Override
    public boolean existsByOrder(Order order) {
        return support.exists("select count(r) from ReturnRequest r where r.order = :o", Map.of("o", order));
    }
}
