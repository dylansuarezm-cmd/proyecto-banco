package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.Refund;
import application.domain.models.Return;
import application.domain.ports.out.RefundRepositoryPort;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RefundJpaAdapter implements RefundRepositoryPort {

    private final JpaSupport support;

    public RefundJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public Refund save(Refund refund) {
        return support.save(refund);
    }

    @Override
    public Optional<Refund> findByIdentifier(String identifier) {
        return support.findByIdentifier("Refund", Refund.class, identifier);
    }

    @Override
    public boolean existsByIdentifier(String identifier) {
        return support.existsByIdentifier("Refund", identifier);
    }

    @Override
    public boolean existsByRelatedReturn(Return relatedReturn) {
        return support.exists("select count(r) from Refund r where r.relatedReturn = :r", Map.of("r", relatedReturn));
    }
}
