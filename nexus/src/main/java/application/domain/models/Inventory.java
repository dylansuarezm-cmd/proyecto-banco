package application.domain.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Inventory {
    private String identifier;
    private PhysicalProduct product;
    private Warehouse warehouse;
    private Integer availableQty;
    private Integer reservedQty;
    private Integer damagedQty;
}
