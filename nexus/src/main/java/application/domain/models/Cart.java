package application.domain.models;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Cart {
    private String identifier;
    private Buyer owner;
    // Empty by default; populated as the buyer selects products.
    private List<CartItem> items = new ArrayList<>();
}
