package application.domain.ports.in;

import application.domain.models.Seller;

/** Input Port: Register Seller use case. Sellers are always onboarded by an Administrator. */
public interface RegisterSellerUseCase {

    Seller register(String administratorIdentifier, String identifier, String fullName, String email);
}
