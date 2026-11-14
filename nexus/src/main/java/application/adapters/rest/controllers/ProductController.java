package application.adapters.rest.controllers;

import application.adapters.persistence.jpa.QueryRepository;
import application.adapters.rest.dtos.ApiRequests;
import application.adapters.rest.dtos.ApiResponses.ProductResponse;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Product;
import application.domain.ports.in.ManageProductLifecycleUseCase;
import application.domain.ports.in.PublishDigitalProductUseCase;
import application.domain.ports.in.PublishPhysicalProductUseCase;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.valueobjects.ProductStatus;
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
 * Catalog. Only sellers publish products (always their own); buyers only see published products.
 */
@RestController
@RequestMapping("/api/products")
@Transactional
public class ProductController {

    private final PublishPhysicalProductUseCase publishPhysical;
    private final PublishDigitalProductUseCase publishDigital;
    private final ManageProductLifecycleUseCase manageLifecycle;
    private final ProductRepositoryPort productRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final QueryRepository queries;

    public ProductController(PublishPhysicalProductUseCase publishPhysical,
                             PublishDigitalProductUseCase publishDigital,
                             ManageProductLifecycleUseCase manageLifecycle,
                             ProductRepositoryPort productRepositoryPort,
                             SellerRepositoryPort sellerRepositoryPort, QueryRepository queries) {
        this.publishPhysical = publishPhysical;
        this.publishDigital = publishDigital;
        this.manageLifecycle = manageLifecycle;
        this.productRepositoryPort = productRepositoryPort;
        this.sellerRepositoryPort = sellerRepositoryPort;
        this.queries = queries;
    }

    @PostMapping("/physical")
    @PreAuthorize("hasRole('SELLER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse publishPhysical(Authentication authentication,
                                           @RequestBody ApiRequests.PublishPhysicalProduct request) {
        return ProductResponse.from(publishPhysical.publish(authentication.getName(), request.identifier(),
                request.name(), request.description(), request.price(), request.warehouseIdentifier(),
                request.initialQuantity()));
    }

    @PostMapping("/digital")
    @PreAuthorize("hasRole('SELLER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse publishDigital(Authentication authentication,
                                          @RequestBody ApiRequests.PublishDigitalProduct request) {
        return ProductResponse.from(publishDigital.publish(authentication.getName(), request.identifier(),
                request.name(), request.description(), request.price(), request.deliveryAsset()));
    }

    @PatchMapping("/{identifier}/lifecycle")
    @PreAuthorize("hasRole('SELLER')")
    public ProductResponse changeLifecycle(Authentication authentication, @PathVariable String identifier,
                                           @RequestBody ApiRequests.ChangeLifecycle request) {
        ManageProductLifecycleUseCase.Action action;
        try {
            action = ManageProductLifecycleUseCase.Action.valueOf(request.action().trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Action must be SUSPEND or DISCONTINUE.");
        }
        return ProductResponse.from(manageLifecycle.changeLifecycle(authentication.getName(), identifier, action));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ProductResponse> products(Authentication authentication) {
        List<Product> result;
        if (hasRole(authentication, "BUYER")) {
            result = queries.products().stream()
                    .filter(product -> ProductStatus.PUBLISHED.equals(product.getStatus())).toList();
        } else if (hasRole(authentication, "SELLER")) {
            result = queries.productsOf(sellerRepositoryPort.findByIdentifier(authentication.getName())
                    .orElseThrow(() -> new EntityNotFoundException("Seller")));
        } else {
            result = queries.products();
        }
        return result.stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/{identifier}")
    @Transactional(readOnly = true)
    public ProductResponse product(Authentication authentication, @PathVariable String identifier) {
        Product product = productRepositoryPort.findByIdentifier(identifier)
                .orElseThrow(() -> new EntityNotFoundException("Product"));
        if (hasRole(authentication, "BUYER") && !ProductStatus.PUBLISHED.equals(product.getStatus())) {
            throw new EntityNotFoundException("Product");
        }
        return ProductResponse.from(product);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
