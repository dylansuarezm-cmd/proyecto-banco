package application.domain.valueobjects;

public final class ProductStatus extends DomainCatalog {

    public static final ProductStatus PUBLISHED = new ProductStatus(
            "PUBLISHED", "Published", "Product is visible in the public catalog.");
    public static final ProductStatus SUSPENDED = new ProductStatus(
            "SUSPENDED", "Suspended", "Product is temporarily hidden from the catalog.");
    public static final ProductStatus DISCONTINUED = new ProductStatus(
            "DISCONTINUED", "Discontinued", "Product has been permanently removed from sale.");

    private ProductStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
