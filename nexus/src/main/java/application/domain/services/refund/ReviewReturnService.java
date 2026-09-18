package application.domain.services.refund;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Return;
import application.domain.ports.in.ReviewReturnUseCase;
import application.domain.ports.out.ReturnRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link ReviewReturnUseCase}.
 *
 * <p>Input: the return's identifier and the {@link Decision} (approve or reject).
 * <p>Output: the persisted {@link Return} with its updated {@link application.domain.valueobjects.ReturnStatus}.
 * <p>Exceptions: {@link EntityNotFoundException} when the return does not exist;
 * {@link application.domain.exceptions.InvalidStatusTransitionException} when the return is not {@code REQUESTED},
 * enforced by {@link Return#approve} / {@link Return#reject}.
 */
@Service
@RequiredArgsConstructor
public class ReviewReturnService implements ReviewReturnUseCase {

    private final ReturnRepositoryPort returnRepositoryPort;

    @Override
    public Return review(String returnIdentifier, Decision decision) {
        Return returnRequest = returnRepositoryPort.findByIdentifier(returnIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Return"));

        switch (decision) {
            case APPROVE -> returnRequest.approve();
            case REJECT -> returnRequest.reject();
        }
        return returnRepositoryPort.save(returnRequest);
    }
}
