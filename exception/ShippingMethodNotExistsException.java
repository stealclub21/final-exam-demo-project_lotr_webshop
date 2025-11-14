package hu.progmasters.webshop.exception;

import hu.progmasters.webshop.domain.enumeration.ShippingMethod;

public class ShippingMethodNotExistsException extends RuntimeException {

    public ShippingMethodNotExistsException(ShippingMethod method) {
        super("Shipping method not exists: " + method);
    }
}
