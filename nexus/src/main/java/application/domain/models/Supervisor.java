package application.domain.models;

import application.domain.exceptions.UnauthorizedDomainAccessException;
import application.domain.valueobjects.SystemRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Supervisor extends User {

    /**
     * A Supervisor is a consultation-only profile; it never modifies business information.
     */
    public boolean canConsult() {
        return isActive() && hasRole(SystemRole.SUPERVISOR);
    }

    public void requireConsultationRights() {
        if (!canConsult()) {
            throw new UnauthorizedDomainAccessException(
                    "Only an active Supervisor can consult operational information.");
        }
    }
}
