package application.adapters.persistence.jpa.converters;

import application.domain.valueobjects.Currency;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CurrencyConverter extends DomainCatalogConverter<Currency> {

    public CurrencyConverter() {
        super(Currency.class);
    }
}
