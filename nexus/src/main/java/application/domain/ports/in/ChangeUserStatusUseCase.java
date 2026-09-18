package application.domain.ports.in;

import application.domain.models.User;

/** Input Port: Change User Status use case. */
public interface ChangeUserStatusUseCase {

    enum Action { ACTIVATE, BLOCK, DEACTIVATE }

    User changeStatus(String userIdentifier, Action action);
}
