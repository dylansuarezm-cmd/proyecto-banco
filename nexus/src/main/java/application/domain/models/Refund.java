package application.domain.models;

import application.domain.exceptions.InvalidRefundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.exceptions.SelfApprovalNotAllowedException;
import application.domain.valueobjects.RefundStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Monetary reimbursement associated with an approved return.
 * The Seller initiates it and the Administrator authorizes it.
 */
@Getter
@Setter
@NoArgsConstructor
public class Refund {

    private String identifier;
    private Return relatedReturn;
    private Seller initiatedBy;
    private Administrator approvedBy;
    private BigDecimal amount;
    private RefundStatus refundStatus;
    private LocalDateTime processedDate;

    /**
     * The Seller registers the refund for an approved return; it then awaits authorization.
     */
    public void initiate(String identifier, Return relatedReturn, Seller seller, BigDecimal amount) {
        if (identifier == null || identifier.isBlank()) {
            throw new InvalidRefundException("Refund identifier must be provided.");
        }
        if (relatedReturn == null) {
            throw new InvalidRefundException("A refund must originate from a return.");
        }
        if (!relatedReturn.isApproved()) {
            throw new InvalidRefundException("Only an approved return can generate a refund.");
        }
        if (seller == null) {
            throw new InvalidRefundException("A refund must be initiated by a Seller.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidRefundException("Refund amount must be greater than zero.");
        }
        seller.requireActive();
        this.identifier = identifier;
        this.relatedReturn = relatedReturn;
        this.initiatedBy = seller;
        this.amount = amount;
        this.refundStatus = RefundStatus.PENDING;
        seller.registerInitiatedRefund(this);
    }

    /**
     * Authorizes the reimbursement. The initiating Seller can never be the approver.
     */
    public void approve(Administrator administrator) {
        requireDecidable(administrator, RefundStatus.APPROVED);
        this.approvedBy = administrator;
        this.refundStatus = RefundStatus.APPROVED;
    }

    public void reject(Administrator administrator) {
        requireDecidable(administrator, RefundStatus.REJECTED);
        this.approvedBy = administrator;
        this.refundStatus = RefundStatus.REJECTED;
    }

    /**
     * Completes the reimbursement once it has been authorized.
     */
    public void process() {
        if (!RefundStatus.APPROVED.equals(refundStatus)) {
            throw new InvalidStatusTransitionException(statusCode(), RefundStatus.PROCESSED.getCode());
        }
        this.refundStatus = RefundStatus.PROCESSED;
        this.processedDate = LocalDateTime.now();
    }

    public boolean isPending() {
        return RefundStatus.PENDING.equals(refundStatus);
    }

    private void requireDecidable(Administrator administrator, RefundStatus target) {
        if (administrator == null) {
            throw new InvalidRefundException("A refund decision requires an Administrator.");
        }
        if (!isPending()) {
            throw new InvalidStatusTransitionException(statusCode(), target.getCode());
        }
        if (initiatedBy != null && initiatedBy.sharesIdentityWith(administrator)) {
            throw new SelfApprovalNotAllowedException();
        }
        administrator.requireActive();
    }

    private String statusCode() {
        return refundStatus == null ? "UNKNOWN" : refundStatus.getCode();
    }
}
