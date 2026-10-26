package application.domain.services.user;

import application.domain.exceptions.DuplicateUserException;
import application.domain.models.Buyer;
import application.domain.ports.in.RegisterBuyerUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link RegisterBuyerUseCase}.
 *
 * <p>Input: identifier, full name, email and primary delivery address for the new buyer.
 * <p>Output: the persisted {@link Buyer}, already commercially enabled with an open {@link application.domain.models.Cart}.
 * <p>Exceptions: none of its own; delegates identity and address validation to {@link Buyer}'s own behavior.
 * <p>Uniqueness: {@link application.domain.exceptions.DuplicateUserException} when the identifier or email already belongs to another user, of any role.
 */
@Service
@RequiredArgsConstructor
public class RegisterBuyerService implements RegisterBuyerUseCase {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    public Buyer register(String identifier, String fullName, String email, String primaryAddress) {
        requireUniqueIdentity(identifier, email);

        Buyer buyer = new Buyer();
        buyer.register(identifier, fullName, email, SystemRole.BUYER);
        buyer.registerCommercialProfile(primaryAddress);
        return buyerRepositoryPort.save(buyer);
    }

    private void requireUniqueIdentity(String identifier, String email) {
        if (identifier != null && userRepositoryPort.existsByIdentifier(identifier.trim())) {
            throw new DuplicateUserException("A user with identifier " + identifier.trim() + " already exists.");
        }
        if (email != null && userRepositoryPort.existsByEmail(email.trim().toLowerCase())) {
            throw new DuplicateUserException("A user with email " + email.trim().toLowerCase() + " already exists.");
        }
    }
}
