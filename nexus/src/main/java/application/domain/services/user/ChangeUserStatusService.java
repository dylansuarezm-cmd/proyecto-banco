package application.domain.services.user;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.ports.in.ChangeUserStatusUseCase;
import application.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link ChangeUserStatusUseCase}.
 *
 * <p>Input: the identifier of any user (Buyer, Seller, or staff) and the desired {@link Action}.
 * <p>Output: the persisted {@link User} with its updated {@link application.domain.valueobjects.UserStatus}.
 * <p>Exceptions: {@link EntityNotFoundException} when the user does not exist;
 * {@link application.domain.exceptions.InvalidUserException} when the user is already in the requested status,
 * enforced by {@link User}'s own behavior.
 */
@Service
@RequiredArgsConstructor
public class ChangeUserStatusService implements ChangeUserStatusUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public User changeStatus(String userIdentifier, Action action) {
        User user = userRepositoryPort.findByIdentifier(userIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("User"));

        switch (action) {
            case ACTIVATE -> user.activate();
            case BLOCK -> user.block();
            case DEACTIVATE -> user.deactivate();
        }
        return userRepositoryPort.save(user);
    }
}
