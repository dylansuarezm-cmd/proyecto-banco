package application.domain.services.refund;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Refund;
import application.domain.models.Return;
import application.domain.models.Seller;
import application.domain.ports.in.InitiateRefundUseCase;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.ReturnRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link InitiateRefundUseCase}.
 *
 * <p>Input: the initiating seller's identifier, the approved return's identifier, the identifier to assign to
 * the refund, and the amount to reimburse.
 * <p>Output: the persisted {@link Refund}, in {@code PENDING} status, awaiting an Administrator's decision.
 * <p>Exceptions: {@link EntityNotFoundException} when the seller or return do not exist;
 * {@link application.domain.exceptions.InvalidRefundException} when the return is not approved or the amount
 * is not positive, enforced by {@link Refund#initiate}.
 */
@Service
@RequiredArgsConstructor
public class InitiateRefundService implements InitiateRefundUseCase {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final ReturnRepositoryPort returnRepositoryPort;
    private final RefundRepositoryPort refundRepositoryPort;

    @Override
    public Refund initiate(String sellerIdentifier, String returnIdentifier, String refundIdentifier,
                           BigDecimal amount) {
        Seller seller = sellerRepositoryPort.findByIdentifier(sellerIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
        Return relatedReturn = returnRepositoryPort.findByIdentifier(returnIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Return"));

        Refund refund = new Refund();
        refund.initiate(refundIdentifier, relatedReturn, seller, amount);
        return refundRepositoryPort.save(refund);
    }
}
