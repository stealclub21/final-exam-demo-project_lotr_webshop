package hu.progmasters.webshop.currency_exchange;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class CurrencyResponse {
    private String base;
    private Map<String, Double> rates;
}
