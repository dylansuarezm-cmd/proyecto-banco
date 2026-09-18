package application.domain.services.order;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidProductException;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.models.CartItem;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.PhysicalProduct;
import application.domain.ports.in.CheckoutOrderUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link CheckoutOrderUseCase}.
 *
 * <p>Input: the buyer's identifier and the identifier to assign to the new order.
 * <p>Output: the persisted {@link Order}, in {@code CART} status, with inventory already reserved for every
 * physical product it contains.
 * <p>Exceptions: {@link EntityNotFoundException} when the buyer does not exist or a physical item has no
 * inventory record; {@link application.domain.exceptions.BuyerNotEligibleException} when the buyer cannot
 * purchase; {@link application.domain.exceptions.InvalidCartException} when the cart is empty or has a
 * non-purchasable item; {@link application.domain.exceptions.DamagedInventoryReservationException} or
 * {@link application.domain.exceptions.InsufficientInventoryException} when a physical item cannot be reserved.
 *
 * <p>This service coordinates three aggregates in a single business transaction: the {@link Cart} being
 * consumed, the {@link Order} being created, and the {@link Inventory} being reserved for every physical item.
 */
@Service
@RequiredArgsConstructor
public class CheckoutOrderService implements CheckoutOrderUseCase {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;

    @Override
    public Order checkout(String buyerIdentifier, String orderIdentifier) {
        Buyer buyer = buyerRepositoryPort.findByIdentifier(buyerIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Buyer"));

        Cart cart = buyer.getCart();
        Order order = new Order();
        order.placeFrom(orderIdentifier, buyer, cart);

        reserveInventoryFor(cart);

        cart.clear();
        buyerRepositoryPort.save(buyer);
        return orderRepositoryPort.save(order);
    }

    private void reserveInventoryFor(Cart cart) {
        for (CartItem item : cart.getItems()) {
            if (!(item.getProduct() instanceof PhysicalProduct physicalProduct)) {
                continue;
            }
            Inventory inventory = physicalProduct.getInventory().stream()
                    .findFirst()
                    .orElseThrow(() -> new InvalidProductException(
                            "Product " + physicalProduct.getIdentifier() + " has no inventory to reserve."));
            inventory.reserve(item.getQuantity());
            inventoryRepositoryPort.save(inventory);
        }
    }
}
