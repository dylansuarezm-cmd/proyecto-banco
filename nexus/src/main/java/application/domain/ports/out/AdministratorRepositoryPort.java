package application.domain.ports.out;

import application.domain.models.Administrator;
import java.util.Optional;

public interface AdministratorRepositoryPort {

    Optional<Administrator> findByIdentifier(String identifier);
}
