package hu.progmasters.webshop.dto.incoming;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderItemsInCartCommand {

    @NotNull(message = "customerId must not be null")
    private Long customerId;

    @NotNull(message = "emporiumProductId must not be null")
    private Long emporiumProductId;

    @NotNull(message = "amount must not be null")
    private Integer amount;
}
