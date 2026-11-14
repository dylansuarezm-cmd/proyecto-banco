package application.adapters.rest.controllers;

import application.adapters.persistence.jpa.QueryRepository;
import application.adapters.rest.dtos.ApiResponses.InventoryReport;
import application.adapters.rest.dtos.ApiResponses.MovementResponse;
import application.adapters.rest.dtos.ApiResponses.MovementsReport;
import application.adapters.rest.dtos.ApiResponses.RefundResponse;
import application.adapters.rest.dtos.ApiResponses.SaleLine;
import application.adapters.rest.dtos.ApiResponses.SalesReport;
import application.adapters.rest.dtos.ApiResponses.SellerReport;
import application.adapters.rest.dtos.ApiResponses.StockLine;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Refund;
import application.domain.models.Seller;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.RefundStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Seller reports: sales, available inventory and the stock changes caused by purchases, returns and refunds.
 * A seller can only read their own reports; Administrators and Supervisors can read any seller's. Everything is
 * calculated from the database, so after restarting the application the reports are exactly the same.
 */
@RestController
@RequestMapping("/api/reports/sellers/{sellerIdentifier}")
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('SELLER','ADMINISTRATOR','SUPERVISOR')")
public class ReportController {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final QueryRepository queries;

    public ReportController(SellerRepositoryPort sellerRepositoryPort, QueryRepository queries) {
        this.sellerRepositoryPort = sellerRepositoryPort;
        this.queries = queries;
    }

    @GetMapping
    public SellerReport all(Authentication authentication, @PathVariable String sellerIdentifier) {
        Seller seller = authorized(authentication, sellerIdentifier);
        return new SellerReport(seller.getIdentifier(), sales(seller), inventory(seller), movements(seller));
    }

    @GetMapping("/sales")
    public SalesReport sales(Authentication authentication, @PathVariable String sellerIdentifier) {
        return sales(authorized(authentication, sellerIdentifier));
    }

    @GetMapping("/inventory")
    public InventoryReport inventory(Authentication authentication, @PathVariable String sellerIdentifier) {
        return inventory(authorized(authentication, sellerIdentifier));
    }

    @GetMapping("/movements")
    public MovementsReport movements(Authentication authentication, @PathVariable String sellerIdentifier) {
        return movements(authorized(authentication, sellerIdentifier));
    }

    private Seller authorized(Authentication authentication, String sellerIdentifier) {
        if (Roles.is(authentication, "SELLER") && !authentication.getName().equals(sellerIdentifier)) {
            throw Roles.forbidden("A seller can only consult their own reports.");
        }
        return sellerRepositoryPort.findByIdentifier(sellerIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
    }

    private SalesReport sales(Seller seller) {
        List<SaleLine> lines = new ArrayList<>();
        for (Order order : queries.ordersWithProductsOf(seller)) {
            if (OrderStatus.CART.equals(order.getOrderStatus())
                    || OrderStatus.PENDING_PAYMENT.equals(order.getOrderStatus())) {
                continue;
            }
            for (OrderItem item : order.getItems()) {
                if (!seller.owns(item.getProduct())) {
                    continue;
                }
                lines.add(new SaleLine(order.getIdentifier(), order.getOrderStatus().getCode(),
                        order.getBuyer().getIdentifier(), item.getProduct().getIdentifier(),
                        item.getProduct().getName(), item.getProduct().type().getCode(), item.getQuantity(),
                        item.getUnitPrice(), item.subtotal()));
            }
        }
        int units = lines.stream().mapToInt(SaleLine::quantity).sum();
        BigDecimal gross = lines.stream().map(SaleLine::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Refund> refunds = queries.refundsInitiatedBy(seller);
        BigDecimal refunded = refunds.stream().filter(r -> RefundStatus.PROCESSED.equals(r.getRefundStatus()))
                .map(Refund::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SalesReport(seller.getIdentifier(), lines, units, gross, refunded, gross.subtract(refunded),
                refunds.stream().map(RefundResponse::from).toList());
    }

    private InventoryReport inventory(Seller seller) {
        List<StockLine> stock = new ArrayList<>();
        int available = 0;
        int reserved = 0;
        int damaged = 0;
        for (Inventory inventory : queries.inventoriesOf(seller)) {
            int a = inventory.getAvailableQty() == null ? 0 : inventory.getAvailableQty();
            int r = inventory.getReservedQty() == null ? 0 : inventory.getReservedQty();
            int d = inventory.getDamagedQty() == null ? 0 : inventory.getDamagedQty();
            available += a;
            reserved += r;
            damaged += d;
            stock.add(new StockLine(inventory.getProduct().getIdentifier(), inventory.getProduct().getName(),
                    inventory.getWarehouse() == null ? null : inventory.getWarehouse().getIdentifier(), a, r, d));
        }
        return new InventoryReport(seller.getIdentifier(), stock, available, reserved, damaged);
    }

    private MovementsReport movements(Seller seller) {
        List<InventoryMovement> movements = queries.movementsOf(seller);
        Map<String, Integer> byType = new LinkedHashMap<>();
        movements.forEach(m -> byType.merge(m.getMovementType().getCode(), m.getQuantity(), Integer::sum));
        return new MovementsReport(seller.getIdentifier(),
                movements.stream().map(MovementResponse::from).toList(), byType);
    }
}
