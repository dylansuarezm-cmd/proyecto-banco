package application.domain.services.refund;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Administrator;
import application.domain.models.Refund;
import application.domain.ports.in.DecideRefundUseCase;
import application.domain.ports.out.AdministratorRepositoryPort;
import application.domain.ports.out.RefundRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link DecideRefundUseCase}.
 *
 * <p>Input: the deciding administrator's identifier, the refund's identifier, and the {@link Decision}.
 * <p>Output: the persisted {@link Refund} with its updated {@link application.domain.valueobjects.RefundStatus}.
 * <p>Exceptions: {@link EntityNotFoundException} when the administrator or refund do not exist;
 * {@link application.domain.exceptions.SelfApprovalNotAllowedException} when the administrator is the same
 * participant who initiated the refund, enforced by {@link Refund#approve} / {@link Refund#reject};
 * {@link application.domain.exceptions.InvalidStatusTransitionException} when the refund is not {@code PENDING}.
 */
@Service
@RequiredArgsConstructor
public class DecideRefundService implements DecideRefundUseCase {

    private final AdministratorRepositoryPort administratorRepositoryPort;
    private final RefundRepositoryPort refundRepositoryPort;

    @Override
    public Refund decide(String administratorIdentifier, String refundIdentifier, Decision decision) {
        Administrator administrator = administratorRepositoryPort.findByIdentifier(administratorIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Administrator"));
        Refund refund = refundRepositoryPort.findByIdentifier(refundIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Refund"));

        switch (decision) {
            case APPROVE -> refund.approve(administrator);
            case REJECT -> refund.reject(administrator);
        }
        return refundRepositoryPort.save(refund);
    }
}
