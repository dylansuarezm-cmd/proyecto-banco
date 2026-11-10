package application.domain.ports.in;

import application.domain.models.Return;
import application.domain.valueobjects.ReturnLine;
import java.util.List;

/** Input Port: Request Return use case. */
public interface RequestReturnUseCase {

    Return request(String buyerIdentifier, String orderIdentifier, String returnIdentifier, String reason,
                  List<ReturnLine> lines);
}
