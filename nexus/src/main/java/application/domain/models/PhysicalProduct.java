package application.domain.models;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PhysicalProduct extends Product {
    // Empty by default; populated on demand by application services.
    private List<Inventory> inventory = new ArrayList<>();
}
