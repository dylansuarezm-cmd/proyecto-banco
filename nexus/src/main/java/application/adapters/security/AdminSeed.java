package application.adapters.security;

import application.domain.ports.in.RegisterStaffUserUseCase;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.SystemRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Creates the first Administrator when the database is empty, so someone can log in and register sellers,
 * warehouses and staff. Every other piece of data is created through the API.
 */
@Component
public class AdminSeed implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdminSeed.class);

    private final RegisterStaffUserUseCase registerStaffUserUseCase;
    private final UserRepositoryPort userRepositoryPort;
    private final CredentialService credentialService;
    private final PlatformTransactionManager transactionManager;
    private final String identifier;
    private final String password;

    public AdminSeed(RegisterStaffUserUseCase registerStaffUserUseCase, UserRepositoryPort userRepositoryPort,
                     CredentialService credentialService, PlatformTransactionManager transactionManager,
                     @Value("${nexus.seed.admin.identifier:ADMIN-001}") String identifier,
                     @Value("${nexus.seed.admin.password:Admin123*}") String password) {
        this.registerStaffUserUseCase = registerStaffUserUseCase;
        this.userRepositoryPort = userRepositoryPort;
        this.credentialService = credentialService;
        this.transactionManager = transactionManager;
        this.identifier = identifier;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            if (userRepositoryPort.existsByIdentifier(identifier)) {
                return;
            }
            registerStaffUserUseCase.register(identifier, "System Administrator",
                    "admin@nexusmarket.com", SystemRole.ADMINISTRATOR);
            credentialService.create(identifier, password);
            LOG.info("Initial administrator created: {}", identifier);
        });
    }
}
