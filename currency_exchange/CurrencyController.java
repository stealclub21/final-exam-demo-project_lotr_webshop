package hu.progmasters.webshop.currency_exchange;

import hu.progmasters.webshop.domain.Order;
import hu.progmasters.webshop.service.CustomerService;
import hu.progmasters.webshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyExchangeService currencyExchangeService;
    private final OrderService orderService;
    private final CustomerService customerService;

    @GetMapping("/currency/convert")
    public double convertCurrency(@RequestParam("toCurrency") String toCurrency) {
        Order order = orderService.findPendingOrderByCustomerId(
                customerService.getLoggedInCustomer().getId());
        double totalPrice = order.getTotalPriceOfOrder();
        return currencyExchangeService.convertCurrency("HUF", toCurrency, totalPrice);
    }
}
