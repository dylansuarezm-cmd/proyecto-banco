package application.adapters.persistence.jpa.converters;

import application.domain.valueobjects.InventoryMovementType;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InventoryMovementTypeConverter extends DomainCatalogConverter<InventoryMovementType> {

    public InventoryMovementTypeConverter() {
        super(InventoryMovementType.class);
    }
}
