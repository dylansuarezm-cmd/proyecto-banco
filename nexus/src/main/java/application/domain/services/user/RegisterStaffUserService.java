package application.domain.services.user;

import application.domain.exceptions.DuplicateUserException;
import application.domain.exceptions.InvalidUserException;
import application.domain.models.Administrator;
import application.domain.models.LogisticsOperator;
import application.domain.models.Supervisor;
import application.domain.models.User;
import application.domain.ports.in.RegisterStaffUserUseCase;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link RegisterStaffUserUseCase}.
 *
 * <p>Input: identity data and one of {@code LOGISTICS_OPERATOR}, {@code ADMINISTRATOR}, or {@code SUPERVISOR}.
 * <p>Output: the persisted {@link User} of the concrete subtype matching the role.
 * <p>Exceptions: {@link InvalidUserException} when the role is not a staff role (a Buyer or Seller must use
 * their own registration use case).
 * <p>Uniqueness: {@link application.domain.exceptions.DuplicateUserException} when the identifier or email already belongs to another user, of any role.
 */
@Service
@RequiredArgsConstructor
public class RegisterStaffUserService implements RegisterStaffUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public User register(String identifier, String fullName, String email, SystemRole role) {
        User user = newInstanceFor(role);
        requireUniqueIdentity(identifier, email);
        user.register(identifier, fullName, email, role);
        return userRepositoryPort.save(user);
    }

    private void requireUniqueIdentity(String identifier, String email) {
        if (identifier != null && userRepositoryPort.existsByIdentifier(identifier.trim())) {
            throw new DuplicateUserException("A user with identifier " + identifier.trim() + " already exists.");
        }
        if (email != null && userRepositoryPort.existsByEmail(email.trim().toLowerCase())) {
            throw new DuplicateUserException("A user with email " + email.trim().toLowerCase() + " already exists.");
        }
    }

    private User newInstanceFor(SystemRole role) {
        if (SystemRole.LOGISTICS_OPERATOR.equals(role)) {
            return new LogisticsOperator();
        }
        if (SystemRole.ADMINISTRATOR.equals(role)) {
            return new Administrator();
        }
        if (SystemRole.SUPERVISOR.equals(role)) {
            return new Supervisor();
        }
        throw new InvalidUserException(
                "Role " + role + " is not a staff role; use RegisterBuyerUseCase or RegisterSellerUseCase instead.");
    }
}
