package application.domain.models;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Seller extends User {
    private Administrator registeredBy;
    // Empty by default; populated on demand by application services.
    private List<SellerWarehouse> warehouses = new ArrayList<>();
    private List<Product> catalog = new ArrayList<>();
    private List<Refund> refundsInitiated = new ArrayList<>();
}
