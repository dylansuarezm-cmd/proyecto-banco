package application.domain.models;

import application.domain.valueobjects.Currency;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Invoice {
    private String identifier;
    private Order order;
    private BigDecimal totalAmount;
    private Currency currency;
    private LocalDateTime issueDate;
}
