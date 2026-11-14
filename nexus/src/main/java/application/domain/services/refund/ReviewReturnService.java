package application.domain.services.refund;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidProductException;
import application.domain.models.Inventory;
import application.domain.models.PhysicalProduct;
import application.domain.models.Return;
import application.domain.models.ReturnItem;
import application.domain.models.User;
import application.domain.ports.in.ReviewReturnUseCase;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ReturnRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.inventory.InventoryMovementRecorder;
import application.domain.valueobjects.InventoryMovementType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link ReviewReturnUseCase}.
 *
 * <p>Input: the reviewer's identifier, the return's identifier and the {@link Decision} (approve or reject).
 * <p>Output: the persisted {@link Return} with its updated {@link application.domain.valueobjects.ReturnStatus}.
 * When a return is approved, the returned physical units go back to the available stock of their product and the
 * movement is recorded as a {@code RETURN}. Digital products have no stock, so nothing changes for them.
 * <p>Exceptions: {@link EntityNotFoundException} when the reviewer or the return do not exist;
 * {@link application.domain.exceptions.UnauthorizedDomainAccessException} when the reviewer is neither an
 * Administrator nor a Seller of one of the returned products, enforced by {@link Return#requireReviewableBy};
 * {@link application.domain.exceptions.InvalidStatusTransitionException} when the return is not {@code REQUESTED},
 * enforced by {@link Return#approve} / {@link Return#reject}.
 */
@Service
@RequiredArgsConstructor
public class ReviewReturnService implements ReviewReturnUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final ReturnRepositoryPort returnRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final InventoryMovementRecorder movementRecorder;

    @Override
    public Return review(String reviewerIdentifier, String returnIdentifier, Decision decision) {
        User reviewer = userRepositoryPort.findByIdentifier(reviewerIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("User"));
        Return returnRequest = returnRepositoryPort.findByIdentifier(returnIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Return"));
        returnRequest.requireReviewableBy(reviewer);

        switch (decision) {
            case APPROVE -> {
                returnRequest.approve();
                restock(returnRequest, reviewer);
            }
            case REJECT -> returnRequest.reject();
        }
        return returnRepositoryPort.save(returnRequest);
    }

    private void restock(Return returnRequest, User reviewer) {
        for (ReturnItem item : returnRequest.getItems()) {
            if (!(item.getProduct() instanceof PhysicalProduct physicalProduct)) {
                continue;
            }
            Inventory inventory = physicalProduct.getInventory().stream()
                    .findFirst()
                    .orElseThrow(() -> new InvalidProductException(
                            "Product " + physicalProduct.getIdentifier() + " has no inventory to receive the return."));
            inventory.registerReturn(item.getQuantity());
            inventoryRepositoryPort.save(inventory);
            movementRecorder.record(InventoryMovementType.RETURN, item.getQuantity(), reviewer, inventory,
                    returnRequest.getIdentifier());
        }
    }
}
