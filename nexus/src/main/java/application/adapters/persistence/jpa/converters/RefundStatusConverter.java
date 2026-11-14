package application.adapters.persistence.jpa.converters;

import application.domain.valueobjects.RefundStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RefundStatusConverter extends DomainCatalogConverter<RefundStatus> {

    public RefundStatusConverter() {
        super(RefundStatus.class);
    }
}
