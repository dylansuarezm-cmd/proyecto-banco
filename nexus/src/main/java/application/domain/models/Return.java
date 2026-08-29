package application.domain.models;

import application.domain.valueobjects.ReturnStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Return {
    private String identifier;
    private Order order;
    private Buyer requestedBy;
    private String reason;
    private ReturnStatus returnStatus;
    private LocalDateTime requestDate;
}
