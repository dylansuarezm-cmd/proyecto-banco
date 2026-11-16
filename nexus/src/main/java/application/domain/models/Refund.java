package application.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import application.domain.exceptions.InvalidRefundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.exceptions.SelfApprovalNotAllowedException;
import application.domain.exceptions.UnauthorizedDomainAccessException;
import application.domain.valueobjects.RefundStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Monetary reimbursement associated with an approved return.
 * The Buyer who returned the products requests it and the Administrator authorizes it (Responsibility Matrix,
 * Section 12 of the specification). The Seller(s) involved are identified through the returned items.
 */
@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
public class Refund {

    @Id
    private String identifier;
    @ManyToOne
    private Return relatedReturn;
    @ManyToOne
    private Buyer requestedBy;
    @ManyToOne
    private Administrator approvedBy;
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;
    private RefundStatus refundStatus;
    private LocalDateTime processedDate;

    /**
     * The Buyer who made the return requests the refund for it once approved; it then awaits authorization.
     */
    public void initiate(String identifier, Return relatedReturn, Buyer buyer, BigDecimal amount) {
        if (identifier == null || identifier.isBlank()) {
            throw new InvalidRefundException("Refund identifier must be provided.");
        }
        if (relatedReturn == null) {
            throw new InvalidRefundException("A refund must originate from a return.");
        }
        if (!relatedReturn.isApproved()) {
            throw new InvalidRefundException("Only an approved return can generate a refund.");
        }
        if (buyer == null) {
            throw new InvalidRefundException("A refund must be requested by a Buyer.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidRefundException("Refund amount must be greater than zero.");
        }
        buyer.requireActive();
        if (!relatedReturn.getRequestedBy().sharesIdentityWith(buyer)) {
            throw new UnauthorizedDomainAccessException(
                    "Buyer " + buyer.getIdentifier() + " did not make return " + relatedReturn.getIdentifier() + ".");
        }
        if (amount.compareTo(relatedReturn.refundableAmount()) > 0) {
            throw new InvalidRefundException("Refund amount exceeds the value of the returned products ("
                    + relatedReturn.refundableAmount() + ").");
        }
        this.identifier = identifier;
        this.relatedReturn = relatedReturn;
        this.requestedBy = buyer;
        this.amount = amount;
        this.refundStatus = RefundStatus.PENDING;
    }

    /**
     * Authorizes the reimbursement. Whoever requested it can never be the approver.
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
        if (requestedBy != null && requestedBy.sharesIdentityWith(administrator)) {
            throw new SelfApprovalNotAllowedException();
        }
        administrator.requireActive();
    }

    private String statusCode() {
        return refundStatus == null ? "UNKNOWN" : refundStatus.getCode();
    }
}
