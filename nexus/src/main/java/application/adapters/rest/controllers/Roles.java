package application.adapters.rest.controllers;

import application.domain.exceptions.UnauthorizedDomainAccessException;
import org.springframework.security.core.Authentication;

/** Small helpers to read the role of the authenticated user. */
final class Roles {

    private Roles() {
    }

    static boolean is(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    static <E extends Enum<E>> E parse(Class<E> type, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The value must be provided. Allowed: "
                    + java.util.Arrays.toString(type.getEnumConstants()));
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value '" + value + "'. Allowed: "
                    + java.util.Arrays.toString(type.getEnumConstants()));
        }
    }

    static UnauthorizedDomainAccessException forbidden(String message) {
        return new UnauthorizedDomainAccessException(message);
    }
}
