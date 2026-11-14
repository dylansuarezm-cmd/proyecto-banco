package application.adapters.persistence.jpa.converters;

import application.domain.valueobjects.ProductStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProductStatusConverter extends DomainCatalogConverter<ProductStatus> {

    public ProductStatusConverter() {
        super(ProductStatus.class);
    }
}
