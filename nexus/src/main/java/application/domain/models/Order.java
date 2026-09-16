package application.domain.models;

import application.domain.exceptions.InvalidOrderException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.exceptions.OrderAlreadyFinalizedException;
import application.domain.valueobjects.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Formal commercial commitment between a buyer and the marketplace.
 * Its lifecycle is the central process of NexusMarket.
 */
@Getter
@Setter
@NoArgsConstructor
public class Order {

    private String identifier;
    private Buyer buyer;
    private List<OrderItem> items = new ArrayList<>();
    private OrderStatus orderStatus;
    private LocalDateTime creationDate;
    private Invoice invoice;
    private Shipment shipment;

    /**
     * Converts a buyer's cart into a formal order awaiting payment.
     */
    public void placeFrom(String identifier, Buyer buyer, Cart cart) {
        if (identifier == null || identifier.isBlank()) {
            throw new InvalidOrderException("Order identifier must be provided.");
        }
        if (buyer == null) {
            throw new InvalidOrderException("An order must be placed by a buyer.");
        }
        if (cart == null) {
            throw new InvalidOrderException("The originating cart must be provided.");
        }
        buyer.requireEligibleToPurchase();
        cart.requireReadyForCheckout();

        this.identifier = identifier;
        this.buyer = buyer;
        this.creationDate = LocalDateTime.now();
        this.orderStatus = OrderStatus.CART;
        cart.getItems().forEach(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.confirm(cartItem.getProduct(), cartItem.getQuantity(),
                    cartItem.getUnitPrice() == null ? BigDecimal.ZERO : cartItem.getUnitPrice());
            this.items.add(orderItem);
        });
        buyer.registerOrder(this);
    }

    /**
     * Submits the order for financial confirmation.
     */
    public void submitForPayment() {
        requireModifiable();
        if (!OrderStatus.CART.equals(orderStatus)) {
            throw new InvalidStatusTransitionException(statusCode(), OrderStatus.PENDING_PAYMENT.getCode());
        }
        if (items.isEmpty()) {
            throw new InvalidOrderException("An order without items cannot be submitted for payment.");
        }
        this.orderStatus = OrderStatus.PENDING_PAYMENT;
    }

    /**
     * Confirms payment and starts the preparation process.
     */
    public void markAsPaid(Invoice invoice) {
        requireModifiable();
        if (!OrderStatus.PENDING_PAYMENT.equals(orderStatus)) {
            throw new InvalidStatusTransitionException(statusCode(), OrderStatus.PAID.getCode());
        }
        if (invoice == null) {
            throw new InvalidOrderException("A paid order must generate an invoice.");
        }
        this.invoice = invoice;
        this.orderStatus = OrderStatus.PAID;
    }

    /**
     * Registers the physical departure of the order from the warehouse.
     */
    public void dispatch(Shipment shipment) {
        requireModifiable();
        if (!OrderStatus.PAID.equals(orderStatus)) {
            throw new InvalidStatusTransitionException(statusCode(), OrderStatus.DISPATCHED.getCode());
        }
        if (!requiresShipment()) {
            throw new InvalidOrderException("An order without physical products cannot be dispatched.");
        }
        if (shipment == null) {
            throw new InvalidOrderException("A dispatched order must have a shipment.");
        }
        this.shipment = shipment;
        this.orderStatus = OrderStatus.DISPATCHED;
    }

    /**
     * Closes the order after delivery has been confirmed.
     */
    public void confirmDelivery() {
        requireModifiable();
        boolean deliverable = OrderStatus.DISPATCHED.equals(orderStatus)
                || (OrderStatus.PAID.equals(orderStatus) && !requiresShipment());
        if (!deliverable) {
            throw new InvalidStatusTransitionException(statusCode(), OrderStatus.DELIVERED_FINALIZED.getCode());
        }
        if (shipment != null) {
            shipment.confirmDelivery();
        }
        this.orderStatus = OrderStatus.DELIVERED_FINALIZED;
    }

    public boolean isFinalized() {
        return OrderStatus.DELIVERED_FINALIZED.equals(orderStatus);
    }

    /**
     * A finalized order cannot be modified under any circumstance.
     */
    public void requireModifiable() {
        if (isFinalized()) {
            throw new OrderAlreadyFinalizedException();
        }
    }

    /**
     * Only orders containing physical products generate a shipment.
     */
    public boolean requiresShipment() {
        return items.stream().anyMatch(OrderItem::requiresShipment);
    }

    public BigDecimal totalAmount() {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean belongsTo(Buyer candidate) {
        return buyer != null && candidate != null && buyer.sharesIdentityWith(candidate);
    }

    private String statusCode() {
        return orderStatus == null ? "UNKNOWN" : orderStatus.getCode();
    }
}
