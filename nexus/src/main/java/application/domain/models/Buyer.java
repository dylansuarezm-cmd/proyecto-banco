package application.domain.models;

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
    // Empty by default; populated on demand by application services.
    private List<Order> orders = new ArrayList<>();
    private List<Return> returns = new ArrayList<>();
}
