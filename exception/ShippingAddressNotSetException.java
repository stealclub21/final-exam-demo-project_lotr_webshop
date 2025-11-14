package hu.progmasters.webshop.exception;

public class ShippingAddressNotSetException extends RuntimeException {

    public ShippingAddressNotSetException(Long id) {
        super("Shipping address is not set for customer with id: " + id);
    }
}
