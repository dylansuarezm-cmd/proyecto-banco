package application.domain.valueobjects;

public final class ProductType extends DomainCatalog {

    public static final ProductType PHYSICAL = new ProductType(
            "PHYSICAL", "Physical", "Tangible good requiring inventory tracking and physical dispatch.");
    public static final ProductType DIGITAL = new ProductType(
            "DIGITAL", "Digital", "Intangible good delivered immediately after payment confirmation.");

    private ProductType(String code, String name, String description) {
        super(code, name, description);
    }
}
