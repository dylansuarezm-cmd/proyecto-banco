package application.adapters.rest.controllers;

import application.adapters.persistence.jpa.QueryRepository;
import application.adapters.rest.dtos.ApiRequests;
import application.adapters.rest.dtos.ApiResponses.ReturnResponse;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Return;
import application.domain.models.Seller;
import application.domain.ports.in.RequestReturnUseCase;
import application.domain.ports.in.ReviewReturnUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.ReturnRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.valueobjects.ReturnLine;
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
 * Returns: the buyer asks for them (stating which products and how many units); an Administrator or the seller
 * of a returned product reviews them. Approved physical units go back to the seller's stock.
 */
@RestController
@RequestMapping("/api/returns")
@Transactional
public class ReturnController {

    private final RequestReturnUseCase requestReturn;
    private final ReviewReturnUseCase reviewReturn;
    private final ReturnRepositoryPort returnRepositoryPort;
    private final BuyerRepositoryPort buyerRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final QueryRepository queries;

    public ReturnController(RequestReturnUseCase requestReturn, ReviewReturnUseCase reviewReturn,
                            ReturnRepositoryPort returnRepositoryPort, BuyerRepositoryPort buyerRepositoryPort,
                            SellerRepositoryPort sellerRepositoryPort, QueryRepository queries) {
        this.requestReturn = requestReturn;
        this.reviewReturn = reviewReturn;
        this.returnRepositoryPort = returnRepositoryPort;
        this.buyerRepositoryPort = buyerRepositoryPort;
        this.sellerRepositoryPort = sellerRepositoryPort;
        this.queries = queries;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BUYER')")
    public ReturnResponse request(Authentication authentication, @RequestBody ApiRequests.RequestReturn request) {
        List<ReturnLine> lines = request.lines() == null ? List.of() : request.lines().stream()
                .map(line -> new ReturnLine(line.productIdentifier(), line.quantity())).toList();
        return ReturnResponse.from(requestReturn.request(authentication.getName(), request.orderIdentifier(),
                request.returnIdentifier(), request.reason(), lines));
    }

    /** decision = APPROVE or REJECT. */
    @PatchMapping("/{returnIdentifier}/review")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SELLER')")
    public ReturnResponse review(Authentication authentication, @PathVariable String returnIdentifier,
                                 @RequestBody ApiRequests.Decision request) {
        return ReturnResponse.from(reviewReturn.review(authentication.getName(), returnIdentifier,
                Roles.parse(ReviewReturnUseCase.Decision.class, request.decision())));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BUYER','SELLER','ADMINISTRATOR','SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<ReturnResponse> returns(Authentication authentication) {
        if (Roles.is(authentication, "BUYER")) {
            return queries.returnsOf(buyerRepositoryPort.findByIdentifier(authentication.getName())
                    .orElseThrow(() -> new EntityNotFoundException("Buyer"))).stream()
                    .map(ReturnResponse::from).toList();
        }
        if (Roles.is(authentication, "SELLER")) {
            return queries.returnsWithProductsOf(seller(authentication)).stream().map(ReturnResponse::from).toList();
        }
        return queries.returns().stream().map(ReturnResponse::from).toList();
    }

    @GetMapping("/{returnIdentifier}")
    @PreAuthorize("hasAnyRole('BUYER','SELLER','ADMINISTRATOR','SUPERVISOR')")
    @Transactional(readOnly = true)
    public ReturnResponse returnRequest(Authentication authentication, @PathVariable String returnIdentifier) {
        Return found = returnRepositoryPort.findByIdentifier(returnIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Return"));
        if (Roles.is(authentication, "BUYER")
                && !found.getRequestedBy().getIdentifier().equals(authentication.getName())) {
            throw Roles.forbidden("Return " + returnIdentifier + " belongs to another buyer.");
        }
        if (Roles.is(authentication, "SELLER") && !found.includesProductOf(seller(authentication))) {
            throw Roles.forbidden("Return " + returnIdentifier + " has no products of this seller.");
        }
        return ReturnResponse.from(found);
    }

    private Seller seller(Authentication authentication) {
        return sellerRepositoryPort.findByIdentifier(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
    }
}
