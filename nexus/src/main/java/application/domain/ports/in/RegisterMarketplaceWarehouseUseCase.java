package application.domain.ports.in;

import application.domain.models.MarketplaceWarehouse;

/** Input Port: Register Marketplace Warehouse use case. */
public interface RegisterMarketplaceWarehouseUseCase {

    MarketplaceWarehouse register(String administratorIdentifier, String identifier, String name, String location);
}
