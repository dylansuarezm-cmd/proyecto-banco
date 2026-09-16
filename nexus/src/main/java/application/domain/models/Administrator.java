package application.domain.models;

import application.domain.exceptions.InvalidRefundException;
import application.domain.exceptions.InvalidUserException;
import application.domain.exceptions.UnauthorizedDomainAccessException;
import application.domain.valueobjects.SystemRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Administrator extends User {

    /**
     * Onboards a seller into the marketplace. Sellers cannot self-register.
     */
    public void registerSeller(Seller seller) {
        requireAdministratorRole();
        if (seller == null) {
            throw new InvalidUserException("The seller to register must be provided.");
        }
        seller.onboardedBy(this);
    }

    /**
     * Registers a warehouse operated directly by the marketplace.
     */
    public void registerWarehouse(MarketplaceWarehouse warehouse) {
        requireAdministratorRole();
        if (warehouse == null) {
            throw new InvalidUserException("The warehouse to register must be provided.");
        }
        warehouse.registerBy(this);
    }

    /**
     * Authorizes a refund previously initiated by a Seller.
     */
    public void approveRefund(Refund refund) {
        requireAdministratorRole();
        if (refund == null) {
            throw new InvalidRefundException("The refund to approve must be provided.");
        }
        refund.approve(this);
    }

    public void rejectRefund(Refund refund) {
        requireAdministratorRole();
        if (refund == null) {
            throw new InvalidRefundException("The refund to reject must be provided.");
        }
        refund.reject(this);
    }

    private void requireAdministratorRole() {
        requireActive();
        if (!hasRole(SystemRole.ADMINISTRATOR)) {
            throw new UnauthorizedDomainAccessException(
                    "Only an Administrator can perform this operation.");
        }
    }
}
