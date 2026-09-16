package application.domain.models;

import application.domain.exceptions.InvalidRoleAssignmentException;
import application.domain.exceptions.InvalidUserException;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class User {

    private String identifier;
    private String fullName;
    private String email;
    private SystemRole role;
    private UserStatus status;

    /**
     * Registers the user with its identity data and single role (RG-02).
     */
    public void register(String identifier, String fullName, String email, SystemRole role) {
        requireText(identifier, "User identifier must be provided.");
        requireText(fullName, "User full name must not be empty.");
        requireText(email, "User email must be provided.");
        if (role == null) {
            throw new InvalidRoleAssignmentException("A user must be created with exactly one role.");
        }
        this.identifier = identifier;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Restores an inactive or blocked user to an operational state.
     */
    public void activate() {
        if (UserStatus.ACTIVE.equals(status)) {
            throw new InvalidUserException("User is already active.");
        }
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Suspends the user's access to the platform.
     */
    public void block() {
        if (UserStatus.BLOCKED.equals(status)) {
            throw new InvalidUserException("User is already blocked.");
        }
        this.status = UserStatus.BLOCKED;
    }

    /**
     * Leaves the user registered but unable to perform operations.
     */
    public void deactivate() {
        if (UserStatus.INACTIVE.equals(status)) {
            throw new InvalidUserException("User is already inactive.");
        }
        this.status = UserStatus.INACTIVE;
    }

    public boolean isActive() {
        return UserStatus.ACTIVE.equals(status);
    }

    public boolean hasRole(SystemRole expectedRole) {
        return role != null && role.equals(expectedRole);
    }

    /**
     * Guarantees that every operation is executed by an authenticated, active user (RG-01).
     */
    public void requireActive() {
        if (!isActive()) {
            throw new InvalidUserException("User " + identifier + " is not active.");
        }
    }

    public boolean sharesIdentityWith(User other) {
        return other != null && identifier != null && identifier.equals(other.getIdentifier());
    }

    protected void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserException(message);
        }
    }
}
