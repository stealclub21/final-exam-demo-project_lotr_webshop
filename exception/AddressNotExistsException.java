package hu.progmasters.webshop.exception;

public class AddressNotExistsException extends RuntimeException{

    public AddressNotExistsException(Long addressId) {
        super("Address with id: " + addressId + " does not exist!");
    }
}
