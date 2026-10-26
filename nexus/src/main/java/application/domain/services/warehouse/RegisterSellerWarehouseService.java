package application.domain.services.warehouse;

import application.domain.exceptions.DuplicateEntityException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Seller;
import application.domain.models.SellerWarehouse;
import application.domain.ports.in.RegisterSellerWarehouseUseCase;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link RegisterSellerWarehouseUseCase}.
 *
 * <p>Input: the owning seller's identifier and the new warehouse's identity data.
 * <p>Output: the persisted {@link SellerWarehouse}, owned by the given seller.
 * <p>Exceptions: {@link EntityNotFoundException} when the seller does not exist.
 * <p>Uniqueness: {@link application.domain.exceptions.DuplicateEntityException} when the warehouse identifier is already in use.
 */
@Service
@RequiredArgsConstructor
public class RegisterSellerWarehouseService implements RegisterSellerWarehouseUseCase {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;

    @Override
    public SellerWarehouse register(String sellerIdentifier, String identifier, String name, String location) {
        Seller seller = sellerRepositoryPort.findByIdentifier(sellerIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
        seller.requireActive();

        requireUnused(identifier);

        SellerWarehouse warehouse = new SellerWarehouse();
        warehouse.register(identifier, name, location);
        seller.addWarehouse(warehouse);
        warehouseRepositoryPort.save(warehouse);
        return warehouse;
    }

    private void requireUnused(String value) {
        if (warehouseRepositoryPort.existsByIdentifier(value)) {
            throw new DuplicateEntityException("Warehouse " + value + " is already registered.");
        }
    }
}
