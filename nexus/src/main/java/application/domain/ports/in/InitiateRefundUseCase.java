package application.domain.ports.in;

import application.domain.models.Refund;
import java.math.BigDecimal;

/** Input Port: Initiate Refund use case. Only the Seller responsible for the product may initiate it. */
public interface InitiateRefundUseCase {

    Refund initiate(String sellerIdentifier, String returnIdentifier, String refundIdentifier, BigDecimal amount);
}
