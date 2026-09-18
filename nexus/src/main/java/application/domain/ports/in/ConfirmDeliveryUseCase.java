package application.domain.ports.in;

import application.domain.models.Order;

/** Input Port: Confirm Delivery use case. */
public interface ConfirmDeliveryUseCase {

    Order confirmDelivery(String orderIdentifier);
}
