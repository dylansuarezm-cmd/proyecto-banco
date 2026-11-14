package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserJpaAdapter implements UserRepositoryPort {

    private final JpaSupport support;

    public UserJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public User save(User user) {
        return support.save(user);
    }

    @Override
    public Optional<User> findByIdentifier(String identifier) {
        return support.findByIdentifier("UserAccount", User.class, identifier);
    }

    @Override
    public boolean existsByEmail(String email) {
        return support.exists("select count(u) from UserAccount u where u.email = :e", Map.of("e", email));
    }

    @Override
    public boolean existsByIdentifier(String identifier) {
        return support.existsByIdentifier("UserAccount", identifier);
    }
}
