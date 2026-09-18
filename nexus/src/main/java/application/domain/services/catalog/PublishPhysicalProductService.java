package application.domain.services.catalog;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.PhysicalProduct;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.ports.in.PublishPhysicalProductUseCase;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.models.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link PublishPhysicalProductUseCase}.
 *
 * <p>Input: the seller's identifier, the product's identity data, the warehouse where its initial stock will
 * be held, and the initial quantity.
 * <p>Output: the persisted, published {@link PhysicalProduct} with its {@link Inventory} record already open.
 * <p>Exceptions: {@link EntityNotFoundException} when the seller or warehouse do not exist;
 * {@link application.domain.exceptions.NegativeInventoryException} when the initial quantity is negative,
 * enforced by {@link Inventory#open}.
 */
@Service
@RequiredArgsConstructor
public class PublishPhysicalProductService implements PublishPhysicalProductUseCase {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    @Override
    public PhysicalProduct publish(String sellerIdentifier, String productIdentifier, String name,
                                   String description, String warehouseIdentifier, int initialQuantity) {
        Seller seller = sellerRepositoryPort.findByIdentifier(sellerIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
        seller.requireActive();

        Warehouse warehouse = warehouseRepositoryPort.findByIdentifier(warehouseIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse"));

        PhysicalProduct product = new PhysicalProduct();
        product.register(productIdentifier, name, description);
        seller.publish(product);
        productRepositoryPort.save(product);

        Inventory inventory = new Inventory();
        inventory.open(productIdentifier + "-" + warehouseIdentifier, product, initialQuantity);
        warehouse.addInventory(inventory);
        inventoryRepositoryPort.save(inventory);

        return product;
    }
}
