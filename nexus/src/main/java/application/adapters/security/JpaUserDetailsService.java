package application.adapters.security;

import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads the login data of a user: password from the credentials table, role and active status from the
 * domain user. A blocked or deactivated user (RG-01) cannot authenticate.
 */
@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepositoryPort userRepositoryPort;
    private final CredentialService credentialService;

    public JpaUserDetailsService(UserRepositoryPort userRepositoryPort, CredentialService credentialService) {
        this.userRepositoryPort = userRepositoryPort;
        this.credentialService = credentialService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepositoryPort.findByIdentifier(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user " + username));
        UserCredential credential = credentialService.find(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " has no credentials"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getIdentifier())
                .password(credential.getPasswordHash())
                .roles(user.getRole().getCode())
                .disabled(!user.isActive())
                .build();
    }
}
