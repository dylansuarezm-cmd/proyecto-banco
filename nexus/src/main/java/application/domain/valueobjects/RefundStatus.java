package application.domain.valueobjects;

public final class RefundStatus extends DomainCatalog {

    public static final RefundStatus PENDING = new RefundStatus(
            "PENDING", "Pending", "Refund has been initiated by the Seller and awaits Administrator decision.");
    public static final RefundStatus APPROVED = new RefundStatus(
            "APPROVED", "Approved", "Refund has been approved by an Administrator.");
    public static final RefundStatus REJECTED = new RefundStatus(
            "REJECTED", "Rejected", "Refund request has been denied by an Administrator.");
    public static final RefundStatus PROCESSED = new RefundStatus(
            "PROCESSED", "Processed", "Reimbursement has been completed.");

    private RefundStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
