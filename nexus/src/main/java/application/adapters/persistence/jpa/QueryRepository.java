package application.adapters.persistence.jpa;

import application.domain.models.Buyer;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.Refund;
import application.domain.models.Return;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Read-only queries used by the GET endpoints (listings). They do not change anything.
 */
@Component
public class QueryRepository {

    private final JpaSupport support;

    public QueryRepository(JpaSupport support) {
        this.support = support;
    }

    public List<User> users() {
        return support.findAll("select u from UserAccount u order by u.identifier", User.class, Map.of());
    }

    public List<Warehouse> warehouses() {
        return support.findAll("select w from Warehouse w order by w.identifier", Warehouse.class, Map.of());
    }

    public List<Warehouse> warehousesOf(Seller seller) {
        return support.findAll("select w from SellerWarehouse w where w.owner = :s order by w.identifier",
                Warehouse.class, Map.of("s", seller));
    }

    public List<Product> products() {
        return support.findAll("select p from Product p order by p.identifier", Product.class, Map.of());
    }

    public List<Product> productsOf(Seller seller) {
        return support.findAll("select p from Product p where p.seller = :s order by p.identifier",
                Product.class, Map.of("s", seller));
    }

    public List<Inventory> inventories() {
        return support.findAll("select i from Inventory i order by i.identifier", Inventory.class, Map.of());
    }

    public List<Inventory> inventoriesOf(Seller seller) {
        return support.findAll("select i from Inventory i where i.product.seller = :s order by i.identifier",
                Inventory.class, Map.of("s", seller));
    }

    // ---- orders ----

    public List<Order> orders() {
        return support.findAll("select o from PurchaseOrder o order by o.identifier", Order.class, Map.of());
    }

    public List<Order> ordersOf(Buyer buyer) {
        return support.findAll("select o from PurchaseOrder o where o.buyer = :b order by o.identifier",
                Order.class, Map.of("b", buyer));
    }

    public List<Order> ordersWithProductsOf(Seller seller) {
        return support.findAll("select distinct o from PurchaseOrder o join o.items i "
                + "where i.product.seller = :s order by o.identifier", Order.class, Map.of("s", seller));
    }

    // ---- returns ----

    public List<Return> returns() {
        return support.findAll("select r from ReturnRequest r order by r.identifier", Return.class, Map.of());
    }

    public List<Return> returnsOf(Buyer buyer) {
        return support.findAll("select r from ReturnRequest r where r.requestedBy = :b order by r.identifier",
                Return.class, Map.of("b", buyer));
    }

    public List<Return> returnsWithProductsOf(Seller seller) {
        return support.findAll("select distinct r from ReturnRequest r join r.items i "
                + "where i.product.seller = :s order by r.identifier", Return.class, Map.of("s", seller));
    }

    // ---- refunds ----

    public List<Refund> refunds() {
        return support.findAll("select r from Refund r order by r.identifier", Refund.class, Map.of());
    }

    public List<Refund> refundsOfBuyer(Buyer buyer) {
        return support.findAll("select r from Refund r where r.relatedReturn.requestedBy = :b "
                + "order by r.identifier", Refund.class, Map.of("b", buyer));
    }

    public List<Refund> refundsInitiatedBy(Seller seller) {
        return support.findAll("select r from Refund r where r.initiatedBy = :s order by r.identifier",
                Refund.class, Map.of("s", seller));
    }

    // ---- stock movements ----

    public List<InventoryMovement> movementsOf(Seller seller) {
        return support.findAll("select m from InventoryMovement m where m.affectedInventory.product.seller = :s "
                + "order by m.executionDate, m.movementId", InventoryMovement.class, Map.of("s", seller));
    }
}
