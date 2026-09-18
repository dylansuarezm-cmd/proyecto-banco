package application.domain.services.refund;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Return;
import application.domain.ports.in.RequestReturnUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ReturnRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link RequestReturnUseCase}.
 *
 * <p>Input: the requesting buyer's identifier, the order's identifier, the identifier to assign to the return,
 * and the reason for the request.
 * <p>Output: the persisted {@link Return}, in {@code REQUESTED} status.
 * <p>Exceptions: {@link EntityNotFoundException} when the buyer or order do not exist;
 * {@link application.domain.exceptions.InvalidReturnException} when the order does not belong to the buyer or
 * is not yet finalized, enforced by {@link Return#request}.
 */
@Service
@RequiredArgsConstructor
public class RequestReturnService implements RequestReturnUseCase {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ReturnRepositoryPort returnRepositoryPort;

    @Override
    public Return request(String buyerIdentifier, String orderIdentifier, String returnIdentifier, String reason) {
        Buyer buyer = buyerRepositoryPort.findByIdentifier(buyerIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Buyer"));
        Order order = orderRepositoryPort.findByIdentifier(orderIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Order"));

        Return returnRequest = new Return();
        returnRequest.request(returnIdentifier, order, buyer, reason);
        return returnRepositoryPort.save(returnRequest);
    }
}
