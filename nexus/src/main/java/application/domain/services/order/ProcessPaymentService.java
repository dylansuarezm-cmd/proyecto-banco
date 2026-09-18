package application.domain.services.order;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.PhysicalProduct;
import application.domain.ports.in.ProcessPaymentUseCase;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.valueobjects.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link ProcessPaymentUseCase}.
 *
 * <p>Input: the order's identifier, the identifier to assign to its invoice, and the invoicing currency.
 * <p>Output: the persisted {@link Order}, now {@code PAID}, with its {@link Invoice} issued and the previously
 * reserved {@link Inventory} confirmed as sold.
 * <p>Exceptions: {@link EntityNotFoundException} when the order does not exist or a physical item has no
 * inventory record; {@link application.domain.exceptions.InvalidStatusTransitionException} when the order is
 * not {@code PENDING_PAYMENT}, enforced by {@link Order#markAsPaid}.
 */
@Service
@RequiredArgsConstructor
public class ProcessPaymentService implements ProcessPaymentUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    @Override
    public Order processPayment(String orderIdentifier, String invoiceIdentifier, Currency currency) {
        Order order = orderRepositoryPort.findByIdentifier(orderIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Order"));

        Invoice invoice = new Invoice();
        invoice.issueFor(invoiceIdentifier, order, currency);
        order.markAsPaid(invoice);
        invoiceRepositoryPort.save(invoice);

        confirmReservedInventory(order);
        return orderRepositoryPort.save(order);
    }

    private void confirmReservedInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            if (!(item.getProduct() instanceof PhysicalProduct physicalProduct)) {
                continue;
            }
            Inventory inventory = physicalProduct.getInventory().stream()
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Inventory"));
            inventory.confirmSaleOutflow(item.getQuantity());
            inventoryRepositoryPort.save(inventory);
        }
    }
}
