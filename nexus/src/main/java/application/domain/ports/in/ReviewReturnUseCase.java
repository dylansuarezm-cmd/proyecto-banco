package application.domain.ports.in;

import application.domain.models.Return;

/** Input Port: Review Return use case (approve or reject a pending return). */
public interface ReviewReturnUseCase {

    enum Decision { APPROVE, REJECT }

    Return review(String returnIdentifier, Decision decision);
}
