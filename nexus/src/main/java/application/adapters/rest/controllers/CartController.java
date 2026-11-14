package application.adapters.rest.controllers;

import application.adapters.rest.dtos.ApiRequests;
import application.adapters.rest.dtos.ApiResponses.CartResponse;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.in.ManageCartUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The shopping cart. Each buyer works only with their own cart: the owner is always the authenticated user.
 */
@RestController
@RequestMapping("/api/cart")
@Transactional
@PreAuthorize("hasRole('BUYER')")
public class CartController {

    private final ManageCartUseCase manageCart;
    private final BuyerRepositoryPort buyerRepositoryPort;

    public CartController(ManageCartUseCase manageCart, BuyerRepositoryPort buyerRepositoryPort) {
        this.manageCart = manageCart;
        this.buyerRepositoryPort = buyerRepositoryPort;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public CartResponse cart(Authentication authentication) {
        return CartResponse.from(buyerRepositoryPort.findByIdentifier(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Buyer")).getCart());
    }

    @PostMapping("/items")
    public CartResponse add(Authentication authentication, @RequestBody ApiRequests.CartItemRequest request) {
        return CartResponse.from(manageCart.addItem(authentication.getName(), request.productIdentifier(),
                request.quantity()));
    }

    /** Sets the quantity of a product that is already in the cart. */
    @PutMapping("/items")
    public CartResponse update(Authentication authentication, @RequestBody ApiRequests.CartItemRequest request) {
        return CartResponse.from(manageCart.updateItem(authentication.getName(), request.productIdentifier(),
                request.quantity()));
    }

    @DeleteMapping("/items/{productIdentifier}")
    public CartResponse remove(Authentication authentication, @PathVariable String productIdentifier) {
        return CartResponse.from(manageCart.removeItem(authentication.getName(), productIdentifier));
    }
}
