package application.adapters.persistence.jpa.converters;

import application.domain.valueobjects.UserStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter extends DomainCatalogConverter<UserStatus> {

    public UserStatusConverter() {
        super(UserStatus.class);
    }
}
