package application.domain.services.refund;

import application.domain.exceptions.DuplicateEntityException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Refund;
import application.domain.models.Return;
import application.domain.models.Buyer;
import application.domain.ports.in.InitiateRefundUseCase;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.ReturnRepositoryPort;
import application.domain.ports.out.BuyerRepositoryPort;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link InitiateRefundUseCase}.
 *
 * <p>Input: the requesting buyer's identifier, the approved return's identifier, the identifier to assign to
 * the refund, and the amount to reimburse.
 * <p>Output: the persisted {@link Refund}, in {@code PENDING} status, awaiting an Administrator's decision.
 * <p>Exceptions: {@link EntityNotFoundException} when the buyer or return do not exist;
 * {@link application.domain.exceptions.InvalidRefundException} when the return is not approved, the amount
 * is not positive or it exceeds the value of the returned products, and
 * {@link application.domain.exceptions.UnauthorizedDomainAccessException} when the buyer is not the one who made
 * the return, both enforced by {@link Refund#initiate}.
 * <p>Uniqueness: {@link application.domain.exceptions.DuplicateEntityException} when the refund identifier is in use or the return already has a refund.
 */
@Service
@RequiredArgsConstructor
public class InitiateRefundService implements InitiateRefundUseCase {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final ReturnRepositoryPort returnRepositoryPort;
    private final RefundRepositoryPort refundRepositoryPort;

    @Override
    public Refund initiate(String buyerIdentifier, String returnIdentifier, String refundIdentifier,
                           BigDecimal amount) {
        Buyer buyer = buyerRepositoryPort.findByIdentifier(buyerIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Buyer"));
        Return relatedReturn = returnRepositoryPort.findByIdentifier(returnIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Return"));

        if (refundRepositoryPort.existsByIdentifier(refundIdentifier)) {
            throw new DuplicateEntityException("Refund " + refundIdentifier + " is already registered.");
        }
        if (refundRepositoryPort.existsByRelatedReturn(relatedReturn)) {
            throw new DuplicateEntityException("Return " + returnIdentifier + " already has a refund.");
        }

        Refund refund = new Refund();
        refund.initiate(refundIdentifier, relatedReturn, buyer, amount);
        return refundRepositoryPort.save(refund);
    }
}
