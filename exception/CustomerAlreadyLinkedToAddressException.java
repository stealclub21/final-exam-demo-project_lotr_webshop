package hu.progmasters.webshop.exception;

public class CustomerAlreadyLinkedToAddressException extends RuntimeException{

    public CustomerAlreadyLinkedToAddressException(Long customerId) {
        super("Customer with id: " + customerId + " is already linked to this address!");
    }
}
