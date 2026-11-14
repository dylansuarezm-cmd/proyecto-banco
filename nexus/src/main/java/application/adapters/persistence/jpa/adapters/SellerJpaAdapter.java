package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.Seller;
import application.domain.ports.out.SellerRepositoryPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SellerJpaAdapter implements SellerRepositoryPort {

    private final JpaSupport support;

    public SellerJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public Seller save(Seller seller) {
        return support.save(seller);
    }

    @Override
    public Optional<Seller> findByIdentifier(String identifier) {
        return support.findByIdentifier("Seller", Seller.class, identifier);
    }
}
