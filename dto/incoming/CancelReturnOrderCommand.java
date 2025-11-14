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
public class CancelReturnOrderCommand {
    @NotNull(message = "Order id must be provided")
    private Long orderId;

    @NotNull(message = "Customer id must be provided")
    private Long customerId;
}
