package application.domain.models;

import application.domain.exceptions.BuyerNotEligibleException;
import application.domain.exceptions.InvalidUserException;
import application.domain.valueobjects.CommercialStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Buyer extends User {

    private String primaryAddress;
    private List<String> additionalAddresses = new ArrayList<>();
    private CommercialStatus commercialStatus;
    private Cart cart;
    private List<Order> orders = new ArrayList<>();
    private List<Return> returns = new ArrayList<>();

    /**
     * Establishes the delivery address and enables the buyer commercially.
     */
    public void registerCommercialProfile(String primaryAddress) {
        if (primaryAddress == null || primaryAddress.isBlank()) {
            throw new InvalidUserException("A buyer must have a primary delivery address.");
        }
        this.primaryAddress = primaryAddress;
        this.commercialStatus = CommercialStatus.ENABLED;
        this.cart = new Cart();
        this.cart.assignTo(this);
    }

    public void addAdditionalAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new InvalidUserException("An additional address must not be empty.");
        }
        if (address.equals(primaryAddress) || additionalAddresses.contains(address)) {
            throw new InvalidUserException("The address is already registered for this buyer.");
        }
        this.additionalAddresses.add(address);
    }

    public void removeAdditionalAddress(String address) {
        if (!additionalAddresses.remove(address)) {
            throw new InvalidUserException("The address is not registered for this buyer.");
        }
    }

    public void restrictCommercially() {
        this.commercialStatus = CommercialStatus.RESTRICTED;
    }

    public void suspendCommercially() {
        this.commercialStatus = CommercialStatus.SUSPENDED;
    }

    public void enableCommercially() {
        this.commercialStatus = CommercialStatus.ENABLED;
    }

    public boolean canPlaceOrders() {
        return isActive() && CommercialStatus.ENABLED.equals(commercialStatus);
    }

    /**
     * Guarantees the buyer may start a purchase before the cart is confirmed.
     */
    public void requireEligibleToPurchase() {
        if (!canPlaceOrders()) {
            throw new BuyerNotEligibleException(
                    "Buyer " + getIdentifier() + " is not allowed to place orders.");
        }
    }

    public void registerOrder(Order order) {
        if (order == null) {
            throw new InvalidUserException("The order to register must be provided.");
        }
        this.orders.add(order);
    }

    public void registerReturn(Return returnRequest) {
        if (returnRequest == null) {
            throw new InvalidUserException("The return to register must be provided.");
        }
        this.returns.add(returnRequest);
    }

    /**
     * A buyer never administers information belonging to another buyer.
     */
    public boolean owns(Order order) {
        return order != null && order.getBuyer() != null && sharesIdentityWith(order.getBuyer());
    }
}
