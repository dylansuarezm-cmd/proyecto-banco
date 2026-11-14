package application.adapters.rest.controllers;

import application.adapters.persistence.jpa.QueryRepository;
import application.adapters.rest.dtos.ApiRequests;
import application.adapters.rest.dtos.ApiResponses.InventoryResponse;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Inventory;
import application.domain.ports.in.AdjustInventoryUseCase;
import application.domain.ports.in.MarkInventoryAsDamagedUseCase;
import application.domain.ports.in.RegisterInventoryInflowUseCase;
import application.domain.ports.out.SellerRepositoryPort;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inventory is administered only by the owning Seller or a Logistics Operator; the domain verifies ownership.
 */
@RestController
@RequestMapping("/api/inventory")
@Transactional
public class InventoryController {

    private final RegisterInventoryInflowUseCase registerInflow;
    private final AdjustInventoryUseCase adjustInventory;
    private final MarkInventoryAsDamagedUseCase markAsDamaged;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final QueryRepository queries;

    public InventoryController(RegisterInventoryInflowUseCase registerInflow, AdjustInventoryUseCase adjustInventory,
                               MarkInventoryAsDamagedUseCase markAsDamaged,
                               SellerRepositoryPort sellerRepositoryPort, QueryRepository queries) {
        this.registerInflow = registerInflow;
        this.adjustInventory = adjustInventory;
        this.markAsDamaged = markAsDamaged;
        this.sellerRepositoryPort = sellerRepositoryPort;
        this.queries = queries;
    }

    @PostMapping("/inflow")
    @PreAuthorize("hasAnyRole('SELLER','LOGISTICS_OPERATOR')")
    public InventoryResponse inflow(Authentication authentication, @RequestBody ApiRequests.InventoryMovement request) {
        return InventoryResponse.from(registerInflow.registerInflow(authentication.getName(),
                request.productIdentifier(), request.warehouseIdentifier(), request.quantity()));
    }

    /** Sets the available quantity to the given value (stock count correction). */
    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('SELLER','LOGISTICS_OPERATOR')")
    public InventoryResponse adjust(Authentication authentication, @RequestBody ApiRequests.InventoryMovement request) {
        return InventoryResponse.from(adjustInventory.adjust(authentication.getName(),
                request.productIdentifier(), request.warehouseIdentifier(), request.quantity()));
    }

    @PostMapping("/damaged")
    @PreAuthorize("hasAnyRole('SELLER','LOGISTICS_OPERATOR')")
    public InventoryResponse damaged(Authentication authentication, @RequestBody ApiRequests.InventoryMovement request) {
        return InventoryResponse.from(markAsDamaged.markAsDamaged(authentication.getName(),
                request.productIdentifier(), request.warehouseIdentifier(), request.quantity()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SELLER','ADMINISTRATOR','SUPERVISOR','LOGISTICS_OPERATOR')")
    @Transactional(readOnly = true)
    public List<InventoryResponse> inventories(Authentication authentication) {
        boolean seller = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_SELLER"));
        List<Inventory> result = seller
                ? queries.inventoriesOf(sellerRepositoryPort.findByIdentifier(authentication.getName())
                        .orElseThrow(() -> new EntityNotFoundException("Seller")))
                : queries.inventories();
        return result.stream().map(InventoryResponse::from).toList();
    }
}
