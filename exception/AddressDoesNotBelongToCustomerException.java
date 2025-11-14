package hu.progmasters.webshop.exception;

public class AddressDoesNotBelongToCustomerException extends RuntimeException{

    public AddressDoesNotBelongToCustomerException(Long addressId, Long customerId) {
        super("Address with id: " + addressId + " does not belong to customer with id: " + customerId + "!");
    }
}
