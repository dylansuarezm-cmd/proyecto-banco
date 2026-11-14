package application.adapters.persistence.jpa.converters;

import application.domain.valueobjects.ReturnStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ReturnStatusConverter extends DomainCatalogConverter<ReturnStatus> {

    public ReturnStatusConverter() {
        super(ReturnStatus.class);
    }
}
