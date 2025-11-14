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
public class BasicOrderCreateUpdateCommand {

    @NotNull(message = "Must not be null!")
    private Long customerId;

    @NotNull(message = "Must not be null!")
    private Long productId;

    @NotNull(message = "Must not be null!")
    private int amount;

}
