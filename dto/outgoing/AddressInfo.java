package hu.progmasters.webshop.dto.outgoing;

import hu.progmasters.webshop.domain.enumeration.AddressType;
import hu.progmasters.webshop.domain.enumeration.LotrCity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressInfo {

    @Enumerated(EnumType.STRING)
    private AddressType addressType;

    private String zip;

    @Enumerated(EnumType.STRING)
    private LotrCity city;

    private String street;

    private String houseNumber;
}
