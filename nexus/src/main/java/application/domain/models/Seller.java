package application.domain.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import application.domain.exceptions.InvalidProductException;
import application.domain.exceptions.SelfRegistrationNotAllowedException;
import application.domain.exceptions.UnauthorizedDomainAccessException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Seller extends User {

    @ManyToOne
    private Administrator registeredBy;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<SellerWarehouse> warehouses = new ArrayList<>();
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Product> catalog = new ArrayList<>();
    @OneToMany(mappedBy = "initiatedBy", cascade = CascadeType.ALL)
    private List<Refund> refundsInitiated = new ArrayList<>();

    /**
     * Records the Administrator that onboarded this seller.
     * Sellers can never self-register.
     */
    public void onboardedBy(Administrator administrator) {
        if (administrator == null) {
            throw new SelfRegistrationNotAllowedException();
        }
        this.registeredBy = administrator;
    }

    public void addWarehouse(SellerWarehouse warehouse) {
        if (warehouse == null) {
            throw new UnauthorizedDomainAccessException("The warehouse to add must be provided.");
        }
        warehouse.assignOwner(this);
        this.warehouses.add(warehouse);
    }

    /**
     * Adds a product to this seller's catalog and publishes it.
     */
    public void publish(Product product) {
        if (product == null) {
            throw new InvalidProductException("The product to publish must be provided.");
        }
        product.assignSeller(this);
        product.publish();
        this.catalog.add(product);
    }

    public void registerInitiatedRefund(Refund refund) {
        if (refund == null) {
            throw new InvalidProductException("The refund to register must be provided.");
        }
        this.refundsInitiated.add(refund);
    }

    /**
     * A seller only administers its own products (RG-03).
     */
    public boolean owns(Product product) {
        return product != null && product.getSeller() != null && sharesIdentityWith(product.getSeller());
    }

    public void requireOwnershipOf(Product product) {
        if (!owns(product)) {
            throw new UnauthorizedDomainAccessException(
                    "Seller " + getIdentifier() + " cannot administer products of another seller.");
        }
    }
}
