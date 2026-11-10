package application.domain.services.refund;

import application.domain.exceptions.DuplicateEntityException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Return;
import application.domain.ports.in.RequestReturnUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ReturnRepositoryPort;
import application.domain.valueobjects.ReturnLine;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link RequestReturnUseCase}.
 *
 * <p>Input: the requesting buyer's identifier, the order's identifier, the identifier to assign to the return,
 * the reason for the request, and the lines of the return (which products of the order and how many units of
 * each).
 * <p>Output: the persisted {@link Return}, in {@code REQUESTED} status.
 * <p>Exceptions: {@link EntityNotFoundException} when the buyer or order do not exist;
 * {@link application.domain.exceptions.InvalidReturnException} when the order does not belong to the buyer or
 * is not yet finalized, when no lines are given, when a product is not part of the order or is repeated, or when
 * more units than were purchased are returned, enforced by {@link Return#request}.
 * <p>Uniqueness: {@link application.domain.exceptions.DuplicateEntityException} when the return identifier is in use or the order already has a return.
 */
@Service
@RequiredArgsConstructor
public class RequestReturnService implements RequestReturnUseCase {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ReturnRepositoryPort returnRepositoryPort;

    @Override
    public Return request(String buyerIdentifier, String orderIdentifier, String returnIdentifier, String reason,
                          List<ReturnLine> lines) {
        Buyer buyer = buyerRepositoryPort.findByIdentifier(buyerIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Buyer"));
        Order order = orderRepositoryPort.findByIdentifier(orderIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Order"));

        if (returnRepositoryPort.existsByIdentifier(returnIdentifier)) {
            throw new DuplicateEntityException("Return " + returnIdentifier + " is already registered.");
        }
        if (returnRepositoryPort.existsByOrder(order)) {
            throw new DuplicateEntityException("Order " + orderIdentifier + " already has a return request.");
        }

        Return returnRequest = new Return();
        returnRequest.request(returnIdentifier, order, buyer, reason, lines);
        return returnRepositoryPort.save(returnRequest);
    }
}
