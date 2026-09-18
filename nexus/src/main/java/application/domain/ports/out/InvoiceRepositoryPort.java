package application.domain.ports.out;

import application.domain.models.Invoice;

public interface InvoiceRepositoryPort {

    Invoice save(Invoice invoice);
}
