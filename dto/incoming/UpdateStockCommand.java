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
public class UpdateStockCommand {

    @NotNull(message = "Id must not be null!")
    private Long productId;

    @NotNull(message = "Amount must not be null!")
    private Integer amount;
}
