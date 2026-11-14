package application.adapters.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CredentialService {

    private static final int MIN_PASSWORD_LENGTH = 6;

    @PersistenceContext
    private EntityManager entityManager;

    private final PasswordEncoder passwordEncoder;

    public CredentialService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Stores the encrypted password of a newly registered user.
     */
    public void create(String identifier, String rawPassword) {
        requireValid(rawPassword);
        entityManager.persist(new UserCredential(identifier.trim(), passwordEncoder.encode(rawPassword)));
    }

    public static void requireValid(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "The password must have at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
    }

    public Optional<UserCredential> find(String identifier) {
        return Optional.ofNullable(entityManager.find(UserCredential.class, identifier));
    }
}
