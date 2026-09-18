package application.domain.ports.in;

import application.domain.models.SellerWarehouse;

/** Input Port: Register Seller Warehouse use case. */
public interface RegisterSellerWarehouseUseCase {

    SellerWarehouse register(String sellerIdentifier, String identifier, String name, String location);
}
