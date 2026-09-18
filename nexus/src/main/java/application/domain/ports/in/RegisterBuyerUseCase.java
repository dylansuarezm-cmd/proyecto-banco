package application.domain.ports.in;

import application.domain.models.Buyer;

/** Input Port: Register Buyer use case. */
public interface RegisterBuyerUseCase {

    Buyer register(String identifier, String fullName, String email, String primaryAddress);
}
