package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.ports.out.ProductRepositoryPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ProductJpaAdapter implements ProductRepositoryPort {

    private final JpaSupport support;

    public ProductJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public Product save(Product product) {
        return support.save(product);
    }

    @Override
    public Optional<Product> findByIdentifier(String identifier) {
        return support.findByIdentifier("Product", Product.class, identifier);
    }

    @Override
    public Optional<PhysicalProduct> findPhysicalByIdentifier(String identifier) {
        return support.findByIdentifier("PhysicalProduct", PhysicalProduct.class, identifier);
    }

    @Override
    public boolean existsByIdentifier(String identifier) {
        return support.existsByIdentifier("Product", identifier);
    }
}
