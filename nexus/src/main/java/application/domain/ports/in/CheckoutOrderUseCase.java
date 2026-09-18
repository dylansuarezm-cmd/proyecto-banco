package application.domain.ports.in;

import application.domain.models.Order;

/** Input Port: Checkout Order use case. Converts a buyer's cart into a formal order and reserves inventory. */
public interface CheckoutOrderUseCase {

    Order checkout(String buyerIdentifier, String orderIdentifier);
}
