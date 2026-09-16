package application.domain.models;

import application.domain.exceptions.InvalidReturnException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.valueobjects.ReturnStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Buyer request to reverse all or part of a delivered order.
 */
@Getter
@Setter
@NoArgsConstructor
public class Return {

    private String identifier;
    private Order order;
    private Buyer requestedBy;
    private String reason;
    private ReturnStatus returnStatus;
    private LocalDateTime requestDate;

    /**
     * Submits a return request. Only a finalized order owned by the buyer can be returned.
     */
    public void request(String identifier, Order order, Buyer buyer, String reason) {
        if (identifier == null || identifier.isBlank()) {
            throw new InvalidReturnException("Return identifier must be provided.");
        }
        if (order == null) {
            throw new InvalidReturnException("A return must reference an order.");
        }
        if (buyer == null || !order.belongsTo(buyer)) {
            throw new InvalidReturnException("A buyer can only return its own orders.");
        }
        if (!order.isFinalized()) {
            throw new InvalidReturnException("Only a delivered order can be returned.");
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidReturnException("A return must state a reason.");
        }
        this.identifier = identifier;
        this.order = order;
        this.requestedBy = buyer;
        this.reason = reason;
        this.returnStatus = ReturnStatus.REQUESTED;
        this.requestDate = LocalDateTime.now();
        buyer.registerReturn(this);
    }

    public void approve() {
        requireStatus(ReturnStatus.REQUESTED, ReturnStatus.APPROVED);
        this.returnStatus = ReturnStatus.APPROVED;
    }

    public void reject() {
        requireStatus(ReturnStatus.REQUESTED, ReturnStatus.REJECTED);
        this.returnStatus = ReturnStatus.REJECTED;
    }

    /**
     * Closes the return once the returned product has been processed.
     */
    public void complete() {
        requireStatus(ReturnStatus.APPROVED, ReturnStatus.COMPLETED);
        this.returnStatus = ReturnStatus.COMPLETED;
    }

    public boolean isApproved() {
        return ReturnStatus.APPROVED.equals(returnStatus);
    }

    private void requireStatus(ReturnStatus expected, ReturnStatus target) {
        if (!expected.equals(returnStatus)) {
            throw new InvalidStatusTransitionException(statusCode(), target.getCode());
        }
    }

    private String statusCode() {
        return returnStatus == null ? "UNKNOWN" : returnStatus.getCode();
    }
}
