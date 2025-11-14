package hu.progmasters.webshop.dto.incoming;

import hu.progmasters.webshop.domain.enumeration.CustomerType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddCustomerTypeToProductCommand {

    @NotNull(message = "Product id must be provided.")
    private Long productId;

    @Enumerated
    @NotNull(message = "Customer type must be provided.")
    private CustomerType customerType;
}
