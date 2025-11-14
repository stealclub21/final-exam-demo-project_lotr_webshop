package hu.progmasters.webshop.exception;

public class MissingCustomerTypeException extends RuntimeException{

    public MissingCustomerTypeException() {
        super("Customer Type cannot be null or empty or WRONG Customer Type!");
    }
}
