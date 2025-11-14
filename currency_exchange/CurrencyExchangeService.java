package hu.progmasters.webshop.currency_exchange;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CurrencyExchangeService {

    private static final String EXCHANGE_API_URL = "https://api.exchangerate-api.com/v4/latest/";

    public double convertCurrency(String fromCurrency, String toCurrency, double amount) {
        RestTemplate restTemplate = new RestTemplate();
        String url = EXCHANGE_API_URL + fromCurrency;
        CurrencyResponse response = restTemplate.getForObject(url, CurrencyResponse.class);

        if (response != null && response.getRates().containsKey(toCurrency)) {
            double exchangeRate = response.getRates().get(toCurrency);
            return amount * exchangeRate;
        }
        throw new IllegalArgumentException("Invalid currency code or API error.");
    }
}

