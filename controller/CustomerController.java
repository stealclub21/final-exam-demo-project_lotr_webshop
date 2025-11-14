package hu.progmasters.webshop.controller;

import hu.progmasters.webshop.dto.incoming.CustomerCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.CustomerInfoWithPreviousOrders;
import hu.progmasters.webshop.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("lotr-webshop/customers")
@Slf4j
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/{customerId}")
    @ResponseStatus(OK)
    public CustomerInfoWithPreviousOrders getCustomer(@PathVariable Long customerId) {
        log.info("Http request, GET / lotr-webshop / customers / customerId ");
        return customerService.getCustomerById(customerId);
    }

    @GetMapping("/me")
    @ResponseStatus(OK)
    public CustomerInfoWithPreviousOrders getLoggedInCustomer() {
        log.info("Http request, GET / lotr-webshop / customers / me  ");
        return customerService.getCustomer();
    }

    @PutMapping("/update/{customerId}")
    @ResponseStatus(OK)
    public CustomerInfoWithPreviousOrders updateCustomer(
            @Valid @RequestBody CustomerCreateUpdateCommand command, @PathVariable Long customerId) {
        log.info("Http request, PUT / lotr-webshop / customers, body: {}", command.toString());
        return customerService.updateCustomer(command, customerId);
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(NO_CONTENT)
    public String deleteCustomer(@PathVariable Long customerId) {
        log.info("Http request, DELETE / lotr-webshop / customers / customerId ");
        return customerService.deleteCustomerSoft(customerId);
    }

    @PutMapping("/reactivate")
    @ResponseStatus(OK)
    public CustomerInfoWithPreviousOrders reactivateCustomer() {
        log.info("Http request, PUT / lotr-webshop / customers / reactivate " );
        return customerService.reactivateCustomer();
    }

    @PutMapping("/subscribe/{customerId}")
    @ResponseStatus(OK)
    public CustomerInfoWithPreviousOrders subscribeToPromotions(@PathVariable Long customerId) {
        log.info("Http request, PUT / lotr-webshop / customers / subscribe / customerId ");
        return customerService.subscribeToPromotions(customerId);
    }

    @PutMapping("/unsubscribe/{customerId}")
    @ResponseStatus(OK)
    public CustomerInfoWithPreviousOrders unsubscribeFromPromotions(@PathVariable Long customerId) {
        log.info("Http request, PUT / lotr-webshop / customers / unsubscribe / customerId ");
        return customerService.unsubscribeFromPromotions(customerId);
    }
}
