package application.domain.ports.in;

import application.domain.models.Order;
import application.domain.valueobjects.Currency;

/** Input Port: Process Payment use case. */
public interface ProcessPaymentUseCase {

    Order processPayment(String orderIdentifier, String invoiceIdentifier, Currency currency);
}
