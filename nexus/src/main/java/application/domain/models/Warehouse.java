package application.domain.models;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Warehouse {
    private String identifier;
    private String name;
    private String location;
    // Empty by default; populated on demand by application services.
    private List<Inventory> inventory = new ArrayList<>();
}
