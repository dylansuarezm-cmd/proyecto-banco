package application.domain.ports.in;

import application.domain.models.Return;

/** Input Port: Request Return use case. */
public interface RequestReturnUseCase {

    Return request(String buyerIdentifier, String orderIdentifier, String returnIdentifier, String reason);
}
