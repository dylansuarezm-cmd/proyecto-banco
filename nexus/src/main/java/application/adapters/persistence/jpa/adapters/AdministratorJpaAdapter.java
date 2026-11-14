package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.Administrator;
import application.domain.ports.out.AdministratorRepositoryPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AdministratorJpaAdapter implements AdministratorRepositoryPort {

    private final JpaSupport support;

    public AdministratorJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public Optional<Administrator> findByIdentifier(String identifier) {
        return support.findByIdentifier("Administrator", Administrator.class, identifier);
    }
}
