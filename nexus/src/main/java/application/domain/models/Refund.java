package application.domain.models;

import application.domain.valueobjects.RefundStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Refund {
    private String identifier;
    private Return relatedReturn;
    private Seller initiatedBy;
    // Null while refundStatus = PENDING.
    private Administrator approvedBy;
    private BigDecimal amount;
    private RefundStatus refundStatus;
    // Null until the refund has been processed.
    private LocalDateTime processedDate;
}
