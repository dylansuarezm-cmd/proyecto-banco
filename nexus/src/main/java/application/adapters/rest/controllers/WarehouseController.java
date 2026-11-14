package application.adapters.rest.controllers;

import application.adapters.persistence.jpa.QueryRepository;
import application.adapters.rest.dtos.ApiRequests;
import application.adapters.rest.dtos.ApiResponses.WarehouseResponse;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedDomainAccessException;
import application.domain.models.SellerWarehouse;
import application.domain.models.Warehouse;
import application.domain.ports.in.RegisterMarketplaceWarehouseUseCase;
import application.domain.ports.in.RegisterSellerWarehouseUseCase;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
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

@RestController
@RequestMapping("/api/warehouses")
@Transactional
public class WarehouseController {

    private final RegisterMarketplaceWarehouseUseCase registerMarketplaceWarehouse;
    private final RegisterSellerWarehouseUseCase registerSellerWarehouse;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final QueryRepository queries;

    public WarehouseController(RegisterMarketplaceWarehouseUseCase registerMarketplaceWarehouse,
                               RegisterSellerWarehouseUseCase registerSellerWarehouse,
                               WarehouseRepositoryPort warehouseRepositoryPort,
                               SellerRepositoryPort sellerRepositoryPort, QueryRepository queries) {
        this.registerMarketplaceWarehouse = registerMarketplaceWarehouse;
        this.registerSellerWarehouse = registerSellerWarehouse;
        this.warehouseRepositoryPort = warehouseRepositoryPort;
        this.sellerRepositoryPort = sellerRepositoryPort;
        this.queries = queries;
    }

    @PostMapping("/marketplace")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponse registerMarketplace(Authentication authentication,
                                                 @RequestBody ApiRequests.RegisterWarehouse request) {
        return WarehouseResponse.from(registerMarketplaceWarehouse.register(authentication.getName(),
                request.identifier(), request.name(), request.location()));
    }

    @PostMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponse registerSeller(Authentication authentication,
                                            @RequestBody ApiRequests.RegisterWarehouse request) {
        return WarehouseResponse.from(registerSellerWarehouse.register(authentication.getName(),
                request.identifier(), request.name(), request.location()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUPERVISOR','LOGISTICS_OPERATOR','SELLER')")
    @Transactional(readOnly = true)
    public List<WarehouseResponse> warehouses(Authentication authentication) {
        List<Warehouse> result = isSeller(authentication)
                ? queries.warehousesOf(sellerRepositoryPort.findByIdentifier(authentication.getName())
                        .orElseThrow(() -> new EntityNotFoundException("Seller")))
                : queries.warehouses();
        return result.stream().map(WarehouseResponse::from).toList();
    }

    @GetMapping("/{identifier}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUPERVISOR','LOGISTICS_OPERATOR','SELLER')")
    @Transactional(readOnly = true)
    public WarehouseResponse warehouse(Authentication authentication, @PathVariable String identifier) {
        Warehouse warehouse = warehouseRepositoryPort.findByIdentifier(identifier)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse"));
        if (isSeller(authentication) && !(warehouse instanceof SellerWarehouse sellerWarehouse
                && sellerWarehouse.getOwner().getIdentifier().equals(authentication.getName()))) {
            throw new UnauthorizedDomainAccessException("A seller can only see its own warehouses.");
        }
        return WarehouseResponse.from(warehouse);
    }

    private boolean isSeller(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_SELLER"));
    }
}
