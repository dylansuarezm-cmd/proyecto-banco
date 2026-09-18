package application.domain.ports.in;

import application.domain.models.Cart;

/** Input Port: Manage Cart use case (add, update, or remove products). */
public interface ManageCartUseCase {

    Cart addItem(String buyerIdentifier, String productIdentifier, int quantity);

    Cart updateItem(String buyerIdentifier, String productIdentifier, int newQuantity);

    Cart removeItem(String buyerIdentifier, String productIdentifier);
}
