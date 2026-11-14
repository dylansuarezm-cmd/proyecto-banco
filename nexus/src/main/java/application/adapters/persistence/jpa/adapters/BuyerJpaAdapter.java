package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.Buyer;
import application.domain.ports.out.BuyerRepositoryPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class BuyerJpaAdapter implements BuyerRepositoryPort {

    private final JpaSupport support;

    public BuyerJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public Buyer save(Buyer buyer) {
        return support.save(buyer);
    }

    @Override
    public Optional<Buyer> findByIdentifier(String identifier) {
        return support.findByIdentifier("Buyer", Buyer.class, identifier);
    }
}
