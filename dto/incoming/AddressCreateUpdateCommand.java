package hu.progmasters.webshop.dto.incoming;

import hu.progmasters.webshop.domain.enumeration.AddressType;
import hu.progmasters.webshop.domain.enumeration.LotrCity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressCreateUpdateCommand {

    @NotNull
    private AddressType addressType;

    @NotNull
    @Pattern(regexp = "\\d{4,5}", message = "ZIP must have minimum 4 and maximum 5 digits.")
    private String zip;

    @NotNull
    private LotrCity city;

    @NotNull
    private String street;

    @NotNull
    @Pattern(regexp = "\\d{1,4}[a-zA-Z]{1,3}",
             message = "House number must be between 1 and 4 digits and can contain 1-3 letters. No special characters allowed.")
    private String houseNumber;
}
