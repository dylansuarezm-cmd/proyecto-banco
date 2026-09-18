package application.domain.ports.in;

import application.domain.models.User;
import application.domain.valueobjects.SystemRole;

/** Input Port: Register Staff User use case (LogisticsOperator, Administrator, or Supervisor). */
public interface RegisterStaffUserUseCase {

    User register(String identifier, String fullName, String email, SystemRole role);
}
