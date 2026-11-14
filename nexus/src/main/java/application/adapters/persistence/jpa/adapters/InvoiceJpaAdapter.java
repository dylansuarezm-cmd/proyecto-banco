package application.adapters.persistence.jpa.adapters;

import application.adapters.persistence.jpa.JpaSupport;
import application.domain.models.Invoice;
import application.domain.ports.out.InvoiceRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class InvoiceJpaAdapter implements InvoiceRepositoryPort {

    private final JpaSupport support;

    public InvoiceJpaAdapter(JpaSupport support) {
        this.support = support;
    }

    @Override
    public Invoice save(Invoice invoice) {
        return support.save(invoice);
    }

    @Override
    public boolean existsByIdentifier(String identifier) {
        return support.existsByIdentifier("Invoice", identifier);
    }
}
