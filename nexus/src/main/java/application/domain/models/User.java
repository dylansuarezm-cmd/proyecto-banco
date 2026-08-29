package application.domain.models;

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
}
