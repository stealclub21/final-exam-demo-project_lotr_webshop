package hu.progmasters.webshop.dto.incoming;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemCreateUpdateCommand {

    @NotNull(message = "Must not be null!")
    private Long productId;

    @Positive(message = "Quantity must be positive!")
    private int piecesOrdered;

}
