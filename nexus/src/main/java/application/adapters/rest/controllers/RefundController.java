package application.adapters.rest.controllers;

import application.adapters.persistence.jpa.QueryRepository;
import application.adapters.rest.dtos.ApiRequests;
import application.adapters.rest.dtos.ApiResponses.RefundResponse;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Refund;
import application.domain.models.Seller;
import application.domain.ports.in.DecideRefundUseCase;
import application.domain.ports.in.InitiateRefundUseCase;
import application.domain.ports.in.ProcessRefundUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Refunds: the seller of a returned product initiates it, an Administrator authorizes it (never the one who
 * initiated it) and then it is processed.
 */
@RestController
@RequestMapping("/api/refunds")
@Transactional
public class RefundController {

    private final InitiateRefundUseCase initiateRefund;
    private final DecideRefundUseCase decideRefund;
    private final ProcessRefundUseCase processRefund;
    private final RefundRepositoryPort refundRepositoryPort;
    private final BuyerRepositoryPort buyerRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final QueryRepository queries;

    public RefundController(InitiateRefundUseCase initiateRefund, DecideRefundUseCase decideRefund,
                            ProcessRefundUseCase processRefund, RefundRepositoryPort refundRepositoryPort,
                            BuyerRepositoryPort buyerRepositoryPort, SellerRepositoryPort sellerRepositoryPort,
                            QueryRepository queries) {
        this.initiateRefund = initiateRefund;
        this.decideRefund = decideRefund;
        this.processRefund = processRefund;
        this.refundRepositoryPort = refundRepositoryPort;
        this.buyerRepositoryPort = buyerRepositoryPort;
        this.sellerRepositoryPort = sellerRepositoryPort;
        this.queries = queries;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SELLER')")
    public RefundResponse initiate(Authentication authentication, @RequestBody ApiRequests.InitiateRefund request) {
        return RefundResponse.from(initiateRefund.initiate(authentication.getName(), request.returnIdentifier(),
                request.refundIdentifier(), request.amount()));
    }

    /** decision = APPROVE or REJECT. */
    @PatchMapping("/{refundIdentifier}/decision")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public RefundResponse decide(Authentication authentication, @PathVariable String refundIdentifier,
                                 @RequestBody ApiRequests.Decision request) {
        return RefundResponse.from(decideRefund.decide(authentication.getName(), refundIdentifier,
                Roles.parse(DecideRefundUseCase.Decision.class, request.decision())));
    }

    @PostMapping("/{refundIdentifier}/process")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public RefundResponse process(@PathVariable String refundIdentifier) {
        return RefundResponse.from(processRefund.process(refundIdentifier));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BUYER','SELLER','ADMINISTRATOR','SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<RefundResponse> refunds(Authentication authentication) {
        if (Roles.is(authentication, "BUYER")) {
            return queries.refundsOfBuyer(buyerRepositoryPort.findByIdentifier(authentication.getName())
                    .orElseThrow(() -> new EntityNotFoundException("Buyer"))).stream()
                    .map(RefundResponse::from).toList();
        }
        if (Roles.is(authentication, "SELLER")) {
            return queries.refundsInitiatedBy(seller(authentication)).stream().map(RefundResponse::from).toList();
        }
        return queries.refunds().stream().map(RefundResponse::from).toList();
    }

    @GetMapping("/{refundIdentifier}")
    @PreAuthorize("hasAnyRole('BUYER','SELLER','ADMINISTRATOR','SUPERVISOR')")
    @Transactional(readOnly = true)
    public RefundResponse refund(Authentication authentication, @PathVariable String refundIdentifier) {
        Refund found = refundRepositoryPort.findByIdentifier(refundIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Refund"));
        if (Roles.is(authentication, "BUYER")
                && !found.getRelatedReturn().getRequestedBy().getIdentifier().equals(authentication.getName())) {
            throw Roles.forbidden("Refund " + refundIdentifier + " belongs to another buyer.");
        }
        if (Roles.is(authentication, "SELLER")
                && !found.getInitiatedBy().getIdentifier().equals(authentication.getName())) {
            throw Roles.forbidden("Refund " + refundIdentifier + " was initiated by another seller.");
        }
        return RefundResponse.from(found);
    }

    private Seller seller(Authentication authentication) {
        return sellerRepositoryPort.findByIdentifier(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
    }
}
