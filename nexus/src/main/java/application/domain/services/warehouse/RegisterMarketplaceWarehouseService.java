package application.domain.services.warehouse;

import application.domain.exceptions.DuplicateEntityException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Administrator;
import application.domain.models.MarketplaceWarehouse;
import application.domain.ports.in.RegisterMarketplaceWarehouseUseCase;
import application.domain.ports.out.AdministratorRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link RegisterMarketplaceWarehouseUseCase}.
 *
 * <p>Input: the registering administrator's identifier and the new warehouse's identity data.
 * <p>Output: the persisted {@link MarketplaceWarehouse}, linked to the administrator that registered it.
 * <p>Exceptions: {@link EntityNotFoundException} when the administrator does not exist.
 * <p>Uniqueness: {@link application.domain.exceptions.DuplicateEntityException} when the warehouse identifier is already in use.
 */
@Service
@RequiredArgsConstructor
public class RegisterMarketplaceWarehouseService implements RegisterMarketplaceWarehouseUseCase {

    private final AdministratorRepositoryPort administratorRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;

    @Override
    public MarketplaceWarehouse register(String administratorIdentifier, String identifier, String name,
                                         String location) {
        Administrator administrator = administratorRepositoryPort.findByIdentifier(administratorIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Administrator"));
        administrator.requireActive();

        requireUnused(identifier);

        MarketplaceWarehouse warehouse = new MarketplaceWarehouse();
        warehouse.register(identifier, name, location);
        administrator.registerWarehouse(warehouse);
        warehouseRepositoryPort.save(warehouse);
        return warehouse;
    }

    private void requireUnused(String value) {
        if (warehouseRepositoryPort.existsByIdentifier(value)) {
            throw new DuplicateEntityException("Warehouse " + value + " is already registered.");
        }
    }
}
