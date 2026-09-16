package application.domain.models;

import application.domain.enums.WarehouseOwnership;
import application.domain.exceptions.InvalidWarehouseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Warehouse {

    private String identifier;
    private String name;
    private String location;
    private List<Inventory> inventory = new ArrayList<>();

    public void register(String identifier, String name, String location) {
        requireText(identifier, "Warehouse identifier must be provided.");
        requireText(name, "Warehouse name must not be empty.");
        requireText(location, "Warehouse location must be provided.");
        this.identifier = identifier;
        this.name = name;
        this.location = location;
    }

    /**
     * Identifies whether the warehouse belongs to the Marketplace or to a Seller.
     */
    public abstract WarehouseOwnership ownership();

    /**
     * Links a stock record to this warehouse. Inventory is always distributed per warehouse.
     */
    public void addInventory(Inventory stock) {
        if (stock == null) {
            throw new InvalidWarehouseException("The inventory record must be provided.");
        }
        if (findInventoryFor(stock.getProduct()).isPresent()) {
            throw new InvalidWarehouseException(
                    "This warehouse already holds inventory for the given product.");
        }
        stock.assignWarehouse(this);
        this.inventory.add(stock);
    }

    public Optional<Inventory> findInventoryFor(PhysicalProduct product) {
        if (product == null) {
            return Optional.empty();
        }
        return inventory.stream()
                .filter(record -> record.getProduct() != null
                        && product.getIdentifier() != null
                        && product.getIdentifier().equals(record.getProduct().getIdentifier()))
                .findFirst();
    }

    public int totalAvailableFor(PhysicalProduct product) {
        return findInventoryFor(product).map(Inventory::getAvailableQty).orElse(0);
    }

    protected void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidWarehouseException(message);
        }
    }
}
