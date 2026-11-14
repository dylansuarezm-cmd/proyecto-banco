package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.ports.out.CartRepositoryPort;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CartJpaAdapter implements CartRepositoryPort {

    private final JpaSupport support;

    public CartJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public Cart save(Cart cart) {
        return support.save(cart);
    }

    @Override
    public Optional<Cart> findByBuyer(Buyer buyer) {
        return support.findOne("select c from Cart c where c.owner = :b", Cart.class, Map.of("b", buyer));
    }
}
