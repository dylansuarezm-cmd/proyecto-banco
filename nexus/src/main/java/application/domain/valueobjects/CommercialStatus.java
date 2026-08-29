package application.domain.valueobjects;

public final class CommercialStatus extends DomainCatalog {

    public static final CommercialStatus ENABLED = new CommercialStatus(
            "ENABLED", "Enabled", "Buyer can place orders normally.");
    public static final CommercialStatus RESTRICTED = new CommercialStatus(
            "RESTRICTED", "Restricted", "Buyer's purchasing ability is temporarily limited.");
    public static final CommercialStatus SUSPENDED = new CommercialStatus(
            "SUSPENDED", "Suspended", "Buyer is not allowed to place new orders.");

    private CommercialStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
