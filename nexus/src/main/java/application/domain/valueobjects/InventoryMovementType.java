package application.domain.valueobjects;

public final class InventoryMovementType extends DomainCatalog {

    public static final InventoryMovementType INFLOW = new InventoryMovementType(
            "INFLOW", "Inflow", "Addition of new stock into a warehouse.");
    public static final InventoryMovementType RESERVATION = new InventoryMovementType(
            "RESERVATION", "Reservation", "Temporary hold of stock for an active cart or pending order.");
    public static final InventoryMovementType SALE_OUTFLOW = new InventoryMovementType(
            "SALE_OUTFLOW", "Sale Outflow", "Reduction of stock resulting from a confirmed sale.");
    public static final InventoryMovementType ADJUSTMENT = new InventoryMovementType(
            "ADJUSTMENT", "Adjustment", "Manual correction of stock quantity.");
    public static final InventoryMovementType RETURN = new InventoryMovementType(
            "RETURN", "Return", "Stock reincorporated as a result of an approved return.");

    private InventoryMovementType(String code, String name, String description) {
        super(code, name, description);
    }
}
