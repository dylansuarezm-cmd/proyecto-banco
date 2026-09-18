package application.domain.ports.in;

import application.domain.models.Refund;

/** Input Port: Process Refund use case. */
public interface ProcessRefundUseCase {

    Refund process(String refundIdentifier);
}
