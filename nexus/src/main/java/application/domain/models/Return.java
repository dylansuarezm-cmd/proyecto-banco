package application.domain.models;

import application.domain.exceptions.InvalidReturnException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.valueobjects.ReturnLine;
import application.domain.valueobjects.ReturnStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Buyer request to reverse all or part of a delivered order.
 */
@Getter
@Setter
@NoArgsConstructor
public class Return {

    private String identifier;
    private Order order;
    private Buyer requestedBy;
    private String reason;
    private List<ReturnItem> items = new ArrayList<>();
    private ReturnStatus returnStatus;
    private LocalDateTime requestDate;

    /**
     * Submits a return request. Only a finalized order owned by the buyer can be returned, and the request must
     * state which products of that order are returned and how many units of each.
     */
    public void request(String identifier, Order order, Buyer buyer, String reason, List<ReturnLine> lines) {
        if (identifier == null || identifier.isBlank()) {
            throw new InvalidReturnException("Return identifier must be provided.");
        }
        if (order == null) {
            throw new InvalidReturnException("A return must reference an order.");
        }
        if (buyer == null || !order.belongsTo(buyer)) {
            throw new InvalidReturnException("A buyer can only return its own orders.");
        }
        if (!order.isFinalized()) {
            throw new InvalidReturnException("Only a delivered order can be returned.");
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidReturnException("A return must state a reason.");
        }
        if (lines == null || lines.isEmpty()) {
            throw new InvalidReturnException("A return must state which products and how many units are returned.");
        }
        Map<String, ReturnItem> defined = new LinkedHashMap<>();
        for (ReturnLine line : lines) {
            if (defined.containsKey(line.productIdentifier())) {
                throw new InvalidReturnException(
                        "Product " + line.productIdentifier() + " appears more than once in the return.");
            }
            OrderItem purchased = order.findItemFor(line.productIdentifier())
                    .orElseThrow(() -> new InvalidReturnException(
                            "Product " + line.productIdentifier() + " is not part of order "
                                    + order.getIdentifier() + "."));
            ReturnItem item = new ReturnItem();
            item.define(purchased, line.quantity());
            defined.put(line.productIdentifier(), item);
        }
        this.items = new ArrayList<>(defined.values());
        this.identifier = identifier;
        this.order = order;
        this.requestedBy = buyer;
        this.reason = reason;
        this.returnStatus = ReturnStatus.REQUESTED;
        this.requestDate = LocalDateTime.now();
        buyer.registerReturn(this);
    }

    public void approve() {
        requireStatus(ReturnStatus.REQUESTED, ReturnStatus.APPROVED);
        this.returnStatus = ReturnStatus.APPROVED;
    }

    public void reject() {
        requireStatus(ReturnStatus.REQUESTED, ReturnStatus.REJECTED);
        this.returnStatus = ReturnStatus.REJECTED;
    }

    /**
     * Closes the return once the returned product has been processed.
     */
    public void complete() {
        requireStatus(ReturnStatus.APPROVED, ReturnStatus.COMPLETED);
        this.returnStatus = ReturnStatus.COMPLETED;
    }

    /**
     * Total number of units sent back across all returned products.
     */
    public int totalUnits() {
        return items.stream().mapToInt(ReturnItem::getQuantity).sum();
    }

    /**
     * Value of everything returned, at the price the buyer paid.
     */
    public BigDecimal refundableAmount() {
        return items.stream().map(ReturnItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Value of the returned products sold by the given Seller: the most that Seller may refund.
     */
    public BigDecimal refundableAmountFor(Seller seller) {
        return items.stream().filter(item -> item.isSoldBy(seller))
                .map(ReturnItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Indicates whether at least one returned product was sold by the given Seller.
     */
    public boolean includesProductOf(Seller seller) {
        return items.stream().anyMatch(item -> item.isSoldBy(seller));
    }

    /**
     * Sellers the return is addressed to: the owners of the returned products, without repetition.
     */
    public List<Seller> involvedSellers() {
        Set<String> seen = new HashSet<>();
        List<Seller> sellers = new ArrayList<>();
        for (ReturnItem item : items) {
            Seller owner = item.getProduct().getSeller();
            if (owner != null && seen.add(owner.getIdentifier())) {
                sellers.add(owner);
            }
        }
        return sellers;
    }

    public boolean isApproved() {
        return ReturnStatus.APPROVED.equals(returnStatus);
    }

    private void requireStatus(ReturnStatus expected, ReturnStatus target) {
        if (!expected.equals(returnStatus)) {
            throw new InvalidStatusTransitionException(statusCode(), target.getCode());
        }
    }

    private String statusCode() {
        return returnStatus == null ? "UNKNOWN" : returnStatus.getCode();
    }
}
