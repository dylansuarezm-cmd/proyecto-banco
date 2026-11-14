package application.adapters.rest.controllers;

import application.adapters.persistence.jpa.QueryRepository;
import application.adapters.rest.dtos.ApiRequests;
import application.adapters.rest.dtos.ApiResponses.UserResponse;
import application.adapters.security.CredentialService;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.in.ChangeUserStatusUseCase;
import application.domain.ports.in.RegisterBuyerUseCase;
import application.domain.ports.in.RegisterSellerUseCase;
import application.domain.ports.in.RegisterStaffUserUseCase;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.SystemRole;
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
 * Users: buyers register themselves; sellers and staff are registered by an Administrator (RG-02).
 */
@RestController
@RequestMapping("/api")
@Transactional
public class UserController {

    private final RegisterBuyerUseCase registerBuyer;
    private final RegisterSellerUseCase registerSeller;
    private final RegisterStaffUserUseCase registerStaff;
    private final ChangeUserStatusUseCase changeUserStatus;
    private final CredentialService credentialService;
    private final UserRepositoryPort userRepositoryPort;
    private final QueryRepository queries;

    public UserController(RegisterBuyerUseCase registerBuyer, RegisterSellerUseCase registerSeller,
                          RegisterStaffUserUseCase registerStaff, ChangeUserStatusUseCase changeUserStatus,
                          CredentialService credentialService, UserRepositoryPort userRepositoryPort,
                          QueryRepository queries) {
        this.registerBuyer = registerBuyer;
        this.registerSeller = registerSeller;
        this.registerStaff = registerStaff;
        this.changeUserStatus = changeUserStatus;
        this.credentialService = credentialService;
        this.userRepositoryPort = userRepositoryPort;
        this.queries = queries;
    }

    /** Public: any person can create a buyer account. */
    @PostMapping("/buyers")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerBuyer(@RequestBody ApiRequests.RegisterBuyer request) {
        CredentialService.requireValid(request.password());
        UserResponse response = UserResponse.from(registerBuyer.register(request.identifier(),
                request.fullName(), request.email(), request.primaryAddress()));
        credentialService.create(response.identifier(), request.password());
        return response;
    }

    @PostMapping("/sellers")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerSeller(Authentication authentication,
                                       @RequestBody ApiRequests.RegisterSeller request) {
        CredentialService.requireValid(request.password());
        UserResponse response = UserResponse.from(registerSeller.register(authentication.getName(),
                request.identifier(), request.fullName(), request.email()));
        credentialService.create(response.identifier(), request.password());
        return response;
    }

    @PostMapping("/staff")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerStaff(@RequestBody ApiRequests.RegisterStaff request) {
        CredentialService.requireValid(request.password());
        UserResponse response = UserResponse.from(registerStaff.register(request.identifier(),
                request.fullName(), request.email(), staffRole(request.role())));
        credentialService.create(response.identifier(), request.password());
        return response;
    }

    @PatchMapping("/users/{identifier}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public UserResponse changeStatus(@PathVariable String identifier,
                                     @RequestBody ApiRequests.ChangeStatus request) {
        ChangeUserStatusUseCase.Action action;
        try {
            action = ChangeUserStatusUseCase.Action.valueOf(request.action().trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Action must be ACTIVATE, BLOCK or DEACTIVATE.");
        }
        return UserResponse.from(changeUserStatus.changeStatus(identifier, action));
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public UserResponse me(Authentication authentication) {
        return find(authentication.getName());
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<UserResponse> users() {
        return queries.users().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/users/{identifier}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUPERVISOR')")
    @Transactional(readOnly = true)
    public UserResponse user(@PathVariable String identifier) {
        return find(identifier);
    }

    private UserResponse find(String identifier) {
        return userRepositoryPort.findByIdentifier(identifier).map(UserResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("User"));
    }

    private SystemRole staffRole(String role) {
        if (role == null) {
            throw new IllegalArgumentException("The role is required.");
        }
        return switch (role.trim().toUpperCase()) {
            case "ADMINISTRATOR" -> SystemRole.ADMINISTRATOR;
            case "LOGISTICS_OPERATOR" -> SystemRole.LOGISTICS_OPERATOR;
            case "SUPERVISOR" -> SystemRole.SUPERVISOR;
            default -> throw new IllegalArgumentException(
                    "Role must be ADMINISTRATOR, LOGISTICS_OPERATOR or SUPERVISOR.");
        };
    }
}
