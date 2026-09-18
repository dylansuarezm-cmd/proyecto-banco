package application.domain.ports.in;

import application.domain.models.Product;

/** Input Port: Manage Product Lifecycle use case (suspend or discontinue a product). */
public interface ManageProductLifecycleUseCase {

    enum Action { SUSPEND, DISCONTINUE }

    Product changeLifecycle(String sellerIdentifier, String productIdentifier, Action action);
}
