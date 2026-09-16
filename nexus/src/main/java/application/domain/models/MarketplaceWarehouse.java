package application.domain.models;

import application.domain.enums.WarehouseOwnership;
import application.domain.exceptions.InvalidWarehouseException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MarketplaceWarehouse extends Warehouse {

    private Administrator registeredBy;

    public void registerBy(Administrator administrator) {
        if (administrator == null) {
            throw new InvalidWarehouseException(
                    "A marketplace warehouse must be registered by an Administrator.");
        }
        this.registeredBy = administrator;
    }

    @Override
    public WarehouseOwnership ownership() {
        return WarehouseOwnership.MARKETPLACE;
    }
}
