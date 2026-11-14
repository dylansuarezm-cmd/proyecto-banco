package application.adapters.persistence.jpa.converters;

import application.domain.valueobjects.DomainCatalog;
import jakarta.persistence.AttributeConverter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Stores a domain catalog value object (role, status, currency...) as its code and rebuilds it from the
 * constants declared in its class. The domain stays free of persistence concerns.
 */
public abstract class DomainCatalogConverter<T extends DomainCatalog> implements AttributeConverter<T, String> {

    private final Class<T> type;

    protected DomainCatalogConverter(Class<T> type) {
        this.type = type;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public T convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && type.isAssignableFrom(field.getType())) {
                try {
                    T constant = type.cast(field.get(null));
                    if (constant != null && code.equals(constant.getCode())) {
                        return constant;
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Cannot read catalog constant " + field.getName(), e);
                }
            }
        }
        throw new IllegalStateException("Unknown " + type.getSimpleName() + " code: " + code);
    }
}
