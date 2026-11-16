package application.domain.ports.in;

import application.domain.models.Refund;
import java.math.BigDecimal;

/** Input Port: Request Refund use case. Only the Buyer who made the approved return may request it. */
public interface InitiateRefundUseCase {

    Refund initiate(String buyerIdentifier, String returnIdentifier, String refundIdentifier, BigDecimal amount);
}
