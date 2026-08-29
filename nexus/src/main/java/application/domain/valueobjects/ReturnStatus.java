package application.domain.valueobjects;

public final class ReturnStatus extends DomainCatalog {

    public static final ReturnStatus REQUESTED = new ReturnStatus(
            "REQUESTED", "Requested", "Return has been submitted by the buyer.");
    public static final ReturnStatus APPROVED = new ReturnStatus(
            "APPROVED", "Approved", "Return has been accepted for processing.");
    public static final ReturnStatus REJECTED = new ReturnStatus(
            "REJECTED", "Rejected", "Return request has been denied.");
    public static final ReturnStatus COMPLETED = new ReturnStatus(
            "COMPLETED", "Completed", "Returned product has been processed and closed.");

    private ReturnStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
