package hu.progmasters.webshop.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException(Long customerId) {
        super("Cart is empty for customer with id: " + customerId);
    }
}
