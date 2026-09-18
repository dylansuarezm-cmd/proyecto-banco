package application.domain.services.user;

import application.domain.models.Buyer;
import application.domain.ports.in.RegisterBuyerUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link RegisterBuyerUseCase}.
 *
 * <p>Input: identifier, full name, email and primary delivery address for the new buyer.
 * <p>Output: the persisted {@link Buyer}, already commercially enabled with an open {@link application.domain.models.Cart}.
 * <p>Exceptions: none of its own; delegates identity and address validation to {@link Buyer}'s own behavior.
 */
@Service
@RequiredArgsConstructor
public class RegisterBuyerService implements RegisterBuyerUseCase {

    private final BuyerRepositoryPort buyerRepositoryPort;

    @Override
    public Buyer register(String identifier, String fullName, String email, String primaryAddress) {
        Buyer buyer = new Buyer();
        buyer.register(identifier, fullName, email, SystemRole.BUYER);
        buyer.registerCommercialProfile(primaryAddress);
        return buyerRepositoryPort.save(buyer);
    }
}
