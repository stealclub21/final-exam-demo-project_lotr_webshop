package hu.progmasters.webshop.exception;

public class CustomerAlreadyExistsException extends RuntimeException{

    public CustomerAlreadyExistsException(String email) {
        super("Customer with e-mail: " + email + " already exists!");
    }
}
