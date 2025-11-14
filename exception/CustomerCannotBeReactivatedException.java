package hu.progmasters.webshop.exception;

public class CustomerCannotBeReactivatedException extends RuntimeException{

    public CustomerCannotBeReactivatedException(String email) {
        super("Customer cannot be reactivated with email: " + email);
    }
}
