package application.domain.valueobjects;

public final class SystemRole extends DomainCatalog {

    public static final SystemRole BUYER = new SystemRole(
            "BUYER", "Buyer", "User who purchases products published on the marketplace.");
    public static final SystemRole SELLER = new SystemRole(
            "SELLER", "Seller", "User responsible for registering and administering their own products.");
    public static final SystemRole LOGISTICS_OPERATOR = new SystemRole(
            "LOGISTICS_OPERATOR", "Logistics Operator", "User responsible for the physical operation of warehouses and dispatches.");
    public static final SystemRole ADMINISTRATOR = new SystemRole(
            "ADMINISTRATOR", "Administrator", "User responsible for administering sellers, warehouses, and refund approvals.");
    public static final SystemRole SUPERVISOR = new SystemRole(
            "SUPERVISOR", "Supervisor", "Read-only user responsible for operational consultation and follow-up.");

    private SystemRole(String code, String name, String description) {
        super(code, name, description);
    }
}
