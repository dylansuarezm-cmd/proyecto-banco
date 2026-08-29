package application.domain.models;

import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.ProductVariant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Product {
    private String identifier;
    private String name;
    private String description;
    private Seller seller;
    // Empty by default; populated as variants are defined by the seller.
    private List<ProductVariant> variants = new ArrayList<>();
    private ProductStatus status;
}
