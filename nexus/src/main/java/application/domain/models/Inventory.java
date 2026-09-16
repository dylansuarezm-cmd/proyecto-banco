package application.domain.models;

import application.domain.exceptions.DamagedInventoryReservationException;
import application.domain.exceptions.InsufficientInventoryException;
import application.domain.exceptions.InvalidInventoryException;
import application.domain.exceptions.InvalidQuantityException;
import application.domain.exceptions.NegativeInventoryException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Distributed stock of a physical product in one specific warehouse.
 * Negative quantities are never permitted under any circumstance.
 */
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

    /**
     * Opens a stock record. Inventory is always bound to one product and one warehouse.
     */
    public void open(String identifier, PhysicalProduct product, int initialQuantity) {
        if (identifier == null || identifier.isBlank()) {
            throw new InvalidInventoryException("Inventory identifier must be provided.");
        }
        if (product == null) {
            throw new InvalidInventoryException("Inventory must be linked to a physical product.");
        }
        requireNotNegative(initialQuantity);
        this.identifier = identifier;
        this.product = product;
        this.availableQty = initialQuantity;
        this.reservedQty = 0;
        this.damagedQty = 0;
        product.registerStock(this);
    }

    void assignWarehouse(Warehouse warehouse) {
        if (warehouse == null) {
            throw new InvalidInventoryException("Inventory must be linked to a warehouse.");
        }
        this.warehouse = warehouse;
    }

    /**
     * Registers new stock arriving at the warehouse.
     */
    public void registerInflow(int quantity) {
        requirePositive(quantity);
        this.availableQty = available() + quantity;
    }

    /**
     * Holds stock for an active cart or a pending order.
     * Damaged or non-existent stock can never be reserved.
     */
    public void reserve(int quantity) {
        requirePositive(quantity);
        if (available() == 0) {
            throw new DamagedInventoryReservationException();
        }
        if (quantity > available()) {
            throw new InsufficientInventoryException(quantity, available());
        }
        this.availableQty = available() - quantity;
        this.reservedQty = reserved() + quantity;
    }

    /**
     * Releases a previous reservation, returning the stock to the available pool.
     */
    public void releaseReservation(int quantity) {
        requirePositive(quantity);
        if (quantity > reserved()) {
            throw new InsufficientInventoryException(quantity, reserved());
        }
        this.reservedQty = reserved() - quantity;
        this.availableQty = available() + quantity;
    }

    /**
     * Confirms the sale of previously reserved stock, removing it from the warehouse.
     */
    public void confirmSaleOutflow(int quantity) {
        requirePositive(quantity);
        if (quantity > reserved()) {
            throw new InsufficientInventoryException(quantity, reserved());
        }
        this.reservedQty = reserved() - quantity;
    }

    /**
     * Manually corrects the available quantity, never below zero.
     */
    public void adjust(int newQuantity) {
        requireNotNegative(newQuantity);
        this.availableQty = newQuantity;
    }

    /**
     * Reincorporates stock coming back from an approved return.
     */
    public void registerReturn(int quantity) {
        requirePositive(quantity);
        this.availableQty = available() + quantity;
    }

    /**
     * Moves available stock into the damaged pool, excluding it from any reservation.
     */
    public void markAsDamaged(int quantity) {
        requirePositive(quantity);
        if (quantity > available()) {
            throw new InsufficientInventoryException(quantity, available());
        }
        this.availableQty = available() - quantity;
        this.damagedQty = damaged() + quantity;
    }

    public boolean canReserve(int quantity) {
        return quantity > 0 && quantity <= available();
    }

    public int totalQuantity() {
        return available() + reserved() + damaged();
    }

    private int available() {
        return availableQty == null ? 0 : availableQty;
    }

    private int reserved() {
        return reservedQty == null ? 0 : reservedQty;
    }

    private int damaged() {
        return damagedQty == null ? 0 : damagedQty;
    }

    private void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than zero.");
        }
    }

    private void requireNotNegative(int quantity) {
        if (quantity < 0) {
            throw new NegativeInventoryException();
        }
    }
}
