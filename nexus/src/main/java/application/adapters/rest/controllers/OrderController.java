package application.adapters.rest.controllers;

import application.adapters.persistence.jpa.QueryRepository;
import application.adapters.rest.dtos.ApiRequests;
import application.adapters.rest.dtos.ApiResponses.OrderResponse;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Seller;
import application.domain.ports.in.CheckoutOrderUseCase;
import application.domain.ports.in.ConfirmDeliveryUseCase;
import application.domain.ports.in.DispatchOrderUseCase;
import application.domain.ports.in.ProcessPaymentUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.valueobjects.Currency;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Purchase process: checkout and payment (buyer), dispatch and delivery (logistics), and consultation.
 * A buyer only sees and pays their own orders; a seller only sees the lines of their own products.
 */
@RestController
@RequestMapping("/api/orders")
@Transactional
public class OrderController {

    private final CheckoutOrderUseCase checkoutOrder;
    private final ProcessPaymentUseCase processPayment;
    private final DispatchOrderUseCase dispatchOrder;
    private final ConfirmDeliveryUseCase confirmDelivery;
    private final OrderRepositoryPort orderRepositoryPort;
    private final BuyerRepositoryPort buyerRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final QueryRepository queries;

    public OrderController(CheckoutOrderUseCase checkoutOrder, ProcessPaymentUseCase processPayment,
                           DispatchOrderUseCase dispatchOrder, ConfirmDeliveryUseCase confirmDelivery,
                           OrderRepositoryPort orderRepositoryPort, BuyerRepositoryPort buyerRepositoryPort,
                           SellerRepositoryPort sellerRepositoryPort, QueryRepository queries) {
        this.checkoutOrder = checkoutOrder;
        this.processPayment = processPayment;
        this.dispatchOrder = dispatchOrder;
        this.confirmDelivery = confirmDelivery;
        this.orderRepositoryPort = orderRepositoryPort;
        this.buyerRepositoryPort = buyerRepositoryPort;
        this.sellerRepositoryPort = sellerRepositoryPort;
        this.queries = queries;
    }

    /** Turns the whole cart into an order and reserves the stock of the physical products. */
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BUYER')")
    public OrderResponse checkout(Authentication authentication, @RequestBody ApiRequests.Checkout request) {
        return OrderResponse.from(checkoutOrder.checkout(authentication.getName(), request.orderIdentifier()), null);
    }

    /** Pays the order and issues its invoice. Only the buyer who placed the order can pay it. */
    @PostMapping("/{orderIdentifier}/payment")
    @PreAuthorize("hasRole('BUYER')")
    public OrderResponse pay(Authentication authentication, @PathVariable String orderIdentifier,
                             @RequestBody ApiRequests.Payment request) {
        Order order = find(orderIdentifier);
        if (!order.getBuyer().getIdentifier().equals(authentication.getName())) {
            throw Roles.forbidden("Order " + orderIdentifier + " belongs to another buyer.");
        }
        return OrderResponse.from(processPayment.processPayment(orderIdentifier, request.invoiceIdentifier(),
                currency(request.currency())), null);
    }

    /** The logistics operator sends the paid order from a warehouse. */
    @PostMapping("/{orderIdentifier}/dispatch")
    @PreAuthorize("hasRole('LOGISTICS_OPERATOR')")
    public OrderResponse dispatch(Authentication authentication, @PathVariable String orderIdentifier,
                                  @RequestBody ApiRequests.Dispatch request) {
        return OrderResponse.from(dispatchOrder.dispatch(authentication.getName(), orderIdentifier,
                request.warehouseIdentifier(), request.shipmentIdentifier()), null);
    }

    /** Confirms the delivery and closes the order. */
    @PostMapping("/{orderIdentifier}/delivery")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR','ADMINISTRATOR')")
    public OrderResponse deliver(@PathVariable String orderIdentifier) {
        return OrderResponse.from(confirmDelivery.confirmDelivery(orderIdentifier), null);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BUYER','SELLER','ADMINISTRATOR','SUPERVISOR','LOGISTICS_OPERATOR')")
    @Transactional(readOnly = true)
    public List<OrderResponse> orders(Authentication authentication) {
        if (Roles.is(authentication, "BUYER")) {
            return queries.ordersOf(buyer(authentication)).stream().map(o -> OrderResponse.from(o, null)).toList();
        }
        if (Roles.is(authentication, "SELLER")) {
            Seller seller = seller(authentication);
            return queries.ordersWithProductsOf(seller).stream().map(o -> OrderResponse.from(o, seller)).toList();
        }
        return queries.orders().stream().map(o -> OrderResponse.from(o, null)).toList();
    }

    @GetMapping("/{orderIdentifier}")
    @PreAuthorize("hasAnyRole('BUYER','SELLER','ADMINISTRATOR','SUPERVISOR','LOGISTICS_OPERATOR')")
    @Transactional(readOnly = true)
    public OrderResponse order(Authentication authentication, @PathVariable String orderIdentifier) {
        Order order = find(orderIdentifier);
        if (Roles.is(authentication, "BUYER")) {
            if (!order.getBuyer().getIdentifier().equals(authentication.getName())) {
                throw Roles.forbidden("Order " + orderIdentifier + " belongs to another buyer.");
            }
            return OrderResponse.from(order, null);
        }
        if (Roles.is(authentication, "SELLER")) {
            Seller seller = seller(authentication);
            if (!order.includesProductOf(seller)) {
                throw Roles.forbidden("Order " + orderIdentifier + " has no products of seller "
                        + seller.getIdentifier() + ".");
            }
            return OrderResponse.from(order, seller);
        }
        return OrderResponse.from(order, null);
    }

    private Order find(String identifier) {
        return orderRepositoryPort.findByIdentifier(identifier)
                .orElseThrow(() -> new EntityNotFoundException("Order"));
    }

    private Buyer buyer(Authentication authentication) {
        return buyerRepositoryPort.findByIdentifier(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Buyer"));
    }

    private Seller seller(Authentication authentication) {
        return sellerRepositoryPort.findByIdentifier(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
    }

    private Currency currency(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Currency must be provided. Allowed: COP, USD, EUR.");
        }
        return switch (code.trim().toUpperCase()) {
            case "COP" -> Currency.COP;
            case "USD" -> Currency.USD;
            case "EUR" -> Currency.EUR;
            default -> throw new IllegalArgumentException("Invalid currency '" + code + "'. Allowed: COP, USD, EUR.");
        };
    }
}
