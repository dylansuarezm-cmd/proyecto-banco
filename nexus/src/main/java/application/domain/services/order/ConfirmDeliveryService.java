package application.domain.services.order;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.ports.in.ConfirmDeliveryUseCase;
import application.domain.ports.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link ConfirmDeliveryUseCase}.
 *
 * <p>Input: the order's identifier.
 * <p>Output: the persisted {@link Order}, now {@code DELIVERED_FINALIZED} and immutable.
 * <p>Exceptions: {@link EntityNotFoundException} when the order does not exist;
 * {@link application.domain.exceptions.InvalidStatusTransitionException} when the order is not dispatched (or,
 * for orders without physical items, not yet paid), enforced by {@link Order#confirmDelivery}.
 */
@Service
@RequiredArgsConstructor
public class ConfirmDeliveryService implements ConfirmDeliveryUseCase {

    private final OrderRepositoryPort orderRepositoryPort;

    @Override
    public Order confirmDelivery(String orderIdentifier) {
        Order order = orderRepositoryPort.findByIdentifier(orderIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        order.confirmDelivery();
        return orderRepositoryPort.save(order);
    }
}
