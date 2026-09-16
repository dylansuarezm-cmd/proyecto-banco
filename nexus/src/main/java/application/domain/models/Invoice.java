package application.domain.models;

import application.domain.exceptions.InvalidInvoiceException;
import application.domain.valueobjects.Currency;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Commercial billing information associated with a purchase.
 */
@Getter
@Setter
@NoArgsConstructor
public class Invoice {

    private String identifier;
    private Order order;
    private BigDecimal totalAmount;
    private Currency currency;
    private LocalDateTime issueDate;

    /**
     * Issues the invoice for a given order, taking its total from the order items.
     */
    public void issueFor(String identifier, Order order, Currency currency) {
        if (identifier == null || identifier.isBlank()) {
            throw new InvalidInvoiceException("Invoice identifier must be provided.");
        }
        if (order == null) {
            throw new InvalidInvoiceException("An invoice must belong to an order.");
        }
        if (currency == null) {
            throw new InvalidInvoiceException("An invoice must declare its currency.");
        }
        this.identifier = identifier;
        this.order = order;
        this.currency = currency;
        this.totalAmount = order.totalAmount();
        this.issueDate = LocalDateTime.now();
    }

    public boolean isIssued() {
        return issueDate != null;
    }
}
