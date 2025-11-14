package hu.progmasters.webshop.exception;

public class CustomerHasNoAddressException extends RuntimeException {

    public CustomerHasNoAddressException(Long id) {
        super("Customer has no address with id: " + id);
    }
}
