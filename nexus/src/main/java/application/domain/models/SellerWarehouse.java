package application.domain.models;

import application.domain.enums.WarehouseOwnership;
import application.domain.exceptions.InvalidWarehouseException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerWarehouse extends Warehouse {

    private Seller owner;

    public void assignOwner(Seller seller) {
        if (seller == null) {
            throw new InvalidWarehouseException("A seller warehouse must have an owning Seller.");
        }
        this.owner = seller;
    }

    public boolean isOwnedBy(Seller seller) {
        return owner != null && seller != null && owner.sharesIdentityWith(seller);
    }

    @Override
    public WarehouseOwnership ownership() {
        return WarehouseOwnership.SELLER;
    }
}
