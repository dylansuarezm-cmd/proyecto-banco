package application.domain.models;

import application.domain.valueobjects.ProductType;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PhysicalProduct extends Product {

    private List<Inventory> inventory = new ArrayList<>();

    void registerStock(Inventory stock) {
        this.inventory.add(stock);
    }

    /**
     * Total quantity available across every warehouse holding this product.
     */
    public int totalAvailableQuantity() {
        return inventory.stream()
                .mapToInt(record -> record.getAvailableQty() == null ? 0 : record.getAvailableQty())
                .sum();
    }

    @Override
    public ProductType type() {
        return ProductType.PHYSICAL;
    }

    @Override
    public boolean requiresShipment() {
        return true;
    }
}
