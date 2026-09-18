package application.domain.ports.in;

import application.domain.models.Refund;

/** Input Port: Decide Refund use case. Only an Administrator may decide, and never the initiating Seller. */
public interface DecideRefundUseCase {

    enum Decision { APPROVE, REJECT }

    Refund decide(String administratorIdentifier, String refundIdentifier, Decision decision);
}
