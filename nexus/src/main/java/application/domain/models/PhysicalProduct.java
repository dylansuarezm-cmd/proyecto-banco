package application.domain.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import application.domain.valueobjects.ProductType;
import application.domain.exceptions.InsufficientInventoryException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PhysicalProduct extends Product {

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
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

    /**
     * Rejects a quantity above the stock currently available, so nobody can put in a cart (or buy)
     * more units than exist.
     */
    @Override
    public void requireAvailableFor(int quantity) {
        int available = totalAvailableQuantity();
        if (quantity > available) {
            throw new InsufficientInventoryException(quantity, available);
        }
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
