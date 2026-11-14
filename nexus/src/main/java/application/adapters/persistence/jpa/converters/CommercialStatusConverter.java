package application.adapters.persistence.jpa.converters;

import application.domain.valueobjects.CommercialStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CommercialStatusConverter extends DomainCatalogConverter<CommercialStatus> {

    public CommercialStatusConverter() {
        super(CommercialStatus.class);
    }
}
