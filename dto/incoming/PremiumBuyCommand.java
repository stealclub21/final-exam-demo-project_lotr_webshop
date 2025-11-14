package hu.progmasters.webshop.dto.incoming;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PremiumBuyCommand {

    private Long customerId;

    private Long emporiumProductId;

    private Integer amount;
}

