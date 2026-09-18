package application.domain.services.refund;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Refund;
import application.domain.ports.in.ProcessRefundUseCase;
import application.domain.ports.out.RefundRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link ProcessRefundUseCase}.
 *
 * <p>Input: the refund's identifier.
 * <p>Output: the persisted {@link Refund}, now {@code PROCESSED}.
 * <p>Exceptions: {@link EntityNotFoundException} when the refund does not exist;
 * {@link application.domain.exceptions.InvalidStatusTransitionException} when the refund is not {@code APPROVED},
 * enforced by {@link Refund#process}.
 */
@Service
@RequiredArgsConstructor
public class ProcessRefundService implements ProcessRefundUseCase {

    private final RefundRepositoryPort refundRepositoryPort;

    @Override
    public Refund process(String refundIdentifier) {
        Refund refund = refundRepositoryPort.findByIdentifier(refundIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Refund"));
        refund.process();
        return refundRepositoryPort.save(refund);
    }
}
