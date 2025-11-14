package hu.progmasters.webshop.exception;

public class NoSubscribedCustomersException extends RuntimeException {

    public NoSubscribedCustomersException() {
        super("No subscribed customers found.");
    }
}
