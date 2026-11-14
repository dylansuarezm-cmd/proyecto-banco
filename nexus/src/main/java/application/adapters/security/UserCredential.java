package application.adapters.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Login credential of a user (identifier + encrypted password). It lives in the infrastructure layer because
 * passwords are not part of the business domain.
 */
@Entity
@Table(name = "user_credentials")
public class UserCredential {

    @Id
    private String identifier;

    @Column(nullable = false)
    private String passwordHash;

    protected UserCredential() {
    }

    public UserCredential(String identifier, String passwordHash) {
        this.identifier = identifier;
        this.passwordHash = passwordHash;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
