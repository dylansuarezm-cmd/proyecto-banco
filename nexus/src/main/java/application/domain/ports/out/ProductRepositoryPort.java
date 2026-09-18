package application.domain.ports.out;

import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import java.util.Optional;

public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findByIdentifier(String identifier);

    Optional<PhysicalProduct> findPhysicalByIdentifier(String identifier);
}
