package hu.progmasters.webshop.exception;

public class CustomerNotFoundException extends RuntimeException{

    public CustomerNotFoundException(String emailOrId) {
        super("Customer not found with id / email: " + emailOrId);
    }
}
