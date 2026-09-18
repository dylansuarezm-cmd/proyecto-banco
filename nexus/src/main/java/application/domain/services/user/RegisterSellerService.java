package application.domain.services.user;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Administrator;
import application.domain.models.Seller;
import application.domain.ports.in.RegisterSellerUseCase;
import application.domain.ports.out.AdministratorRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link RegisterSellerUseCase}.
 *
 * <p>Input: identifier of the administrator performing the registration, plus the new seller's identity data.
 * <p>Output: the persisted {@link Seller}, linked to the administrator that onboarded it.
 * <p>Exceptions: {@link EntityNotFoundException} when the administrator does not exist;
 * {@link application.domain.exceptions.SelfRegistrationNotAllowedException} is enforced by {@link Seller#onboardedBy}.
 */
@Service
@RequiredArgsConstructor
public class RegisterSellerService implements RegisterSellerUseCase {

    private final AdministratorRepositoryPort administratorRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;

    @Override
    public Seller register(String administratorIdentifier, String identifier, String fullName, String email) {
        Administrator administrator = administratorRepositoryPort.findByIdentifier(administratorIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Administrator"));
        administrator.requireActive();

        Seller seller = new Seller();
        seller.register(identifier, fullName, email, SystemRole.SELLER);
        administrator.registerSeller(seller);
        return sellerRepositoryPort.save(seller);
    }
}
