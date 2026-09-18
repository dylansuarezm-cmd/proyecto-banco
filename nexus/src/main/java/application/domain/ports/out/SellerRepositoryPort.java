package application.domain.ports.out;

import application.domain.models.Seller;
import java.util.Optional;

public interface SellerRepositoryPort {

    Seller save(Seller seller);

    Optional<Seller> findByIdentifier(String identifier);
}
