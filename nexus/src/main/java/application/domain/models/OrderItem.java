package application.domain.models;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderItem {
    private Product product;
    private Integer quantity;
    private BigDecimal unitPrice;
}
