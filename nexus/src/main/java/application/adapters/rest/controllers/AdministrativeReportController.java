package application.adapters.rest.controllers;

import application.adapters.persistence.jpa.QueryRepository;
import application.adapters.rest.dtos.ApiResponses.AdministrativeSummary;
import application.adapters.rest.dtos.ApiResponses.SellerTotals;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Refund;
import application.domain.models.Return;
import application.domain.models.User;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.RefundStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OBJ-12: consolidated administrative information for consultation. It only reads data, so it does not change
 * anything and it is calculated from the database on every request.
 */
@RestController
@RequestMapping("/api/reports/summary")
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('ADMINISTRATOR','SUPERVISOR')")
public class AdministrativeReportController {

    private final QueryRepository queries;

    public AdministrativeReportController(QueryRepository queries) {
        this.queries = queries;
    }

    @GetMapping
    public AdministrativeSummary summary() {
        List<Order> paidOrders = queries.orders().stream()
                .filter(o -> !OrderStatus.CART.equals(o.getOrderStatus())
                        && !OrderStatus.PENDING_PAYMENT.equals(o.getOrderStatus()))
                .toList();
        List<Refund> refunds = queries.refunds();

        BigDecimal gross = BigDecimal.ZERO;
        Map<String, int[]> units = new TreeMap<>();
        Map<String, BigDecimal> grossBySeller = new TreeMap<>();
        for (Order order : paidOrders) {
            for (OrderItem item : order.getItems()) {
                String seller = item.getProduct().getSeller().getIdentifier();
                gross = gross.add(item.subtotal());
                units.computeIfAbsent(seller, s -> new int[1])[0] += item.getQuantity();
                grossBySeller.merge(seller, item.subtotal(), BigDecimal::add);
            }
        }

        Map<String, BigDecimal> refundedBySeller = new TreeMap<>();
        BigDecimal refunded = BigDecimal.ZERO;
        for (Refund refund : refunds) {
            if (!RefundStatus.PROCESSED.equals(refund.getRefundStatus())) {
                continue;
            }
            refunded = refunded.add(refund.getAmount());
            Return returned = refund.getRelatedReturn();
            returned.involvedSellers().forEach(seller -> refundedBySeller.merge(seller.getIdentifier(),
                    refund.getAmount().min(returned.refundableAmountFor(seller)), BigDecimal::add));
        }

        List<SellerTotals> bySeller = grossBySeller.keySet().stream().map(seller -> {
            BigDecimal sellerRefunded = refundedBySeller.getOrDefault(seller, BigDecimal.ZERO);
            return new SellerTotals(seller, units.get(seller)[0], grossBySeller.get(seller), sellerRefunded,
                    grossBySeller.get(seller).subtract(sellerRefunded));
        }).toList();

        List<Inventory> inventories = queries.inventories();
        return new AdministrativeSummary(
                count(queries.users(), u -> u.getRole().getCode()),
                count(queries.products(), p -> p.getStatus().getCode()),
                count(queries.orders(), o -> o.getOrderStatus().getCode()),
                count(queries.returns(), r -> r.getReturnStatus().getCode()),
                count(refunds, r -> r.getRefundStatus().getCode()),
                inventories.stream().mapToInt(i -> value(i.getAvailableQty())).sum(),
                inventories.stream().mapToInt(i -> value(i.getReservedQty())).sum(),
                inventories.stream().mapToInt(i -> value(i.getDamagedQty())).sum(),
                gross, refunded, gross.subtract(refunded), bySeller);
    }

    private static int value(Integer number) {
        return number == null ? 0 : number;
    }

    private static <T> Map<String, Long> count(List<T> items, Function<T, String> key) {
        return items.stream().collect(Collectors.groupingBy(key, TreeMap::new, Collectors.counting()));
    }
}
