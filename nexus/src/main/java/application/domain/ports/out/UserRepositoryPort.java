package application.domain.ports.out;

import application.domain.models.User;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByIdentifier(String identifier);

    boolean existsByEmail(String email);
}
