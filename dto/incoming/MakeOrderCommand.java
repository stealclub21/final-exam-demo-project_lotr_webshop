package hu.progmasters.webshop.dto.incoming;

import hu.progmasters.webshop.domain.enumeration.ShippingMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MakeOrderCommand {

    @NotNull(message = "addressId must not be null")
    private Long addressId;

    @NotNull(message = "customerId must not be null")
    private Long customerId;

    @NotNull(message = "shippingMethod must not be null")
    private ShippingMethod shippingMethod;
}
