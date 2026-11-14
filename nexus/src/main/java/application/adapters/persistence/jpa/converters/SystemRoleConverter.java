package application.adapters.persistence.jpa.converters;

import application.domain.valueobjects.SystemRole;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SystemRoleConverter extends DomainCatalogConverter<SystemRole> {

    public SystemRoleConverter() {
        super(SystemRole.class);
    }
}
