package hu.progmasters.webshop.exception;

public class CustomerCannotAccessPremiumServicesException extends RuntimeException {

    public CustomerCannotAccessPremiumServicesException(Long id) {
        super("Customer is not premium with id: " + id);
    }
}
